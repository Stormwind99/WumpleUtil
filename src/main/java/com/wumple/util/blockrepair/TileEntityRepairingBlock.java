package com.wumple.util.blockrepair;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.annotation.Nullable;

import com.wumple.util.misc.LeafUtil;

/*
 * Based on CoroUtil's TileEntityRepairingBlock
 * from https://github.com/Corosauce/CoroUtil
 */

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/*
 * Based on CoroUtil's BlockRepairingBlock
 * from https://github.com/Corosauce/CoroUtil
 */
public class TileEntityRepairingBlock extends TileEntity implements ITickable, IRepairing
{
    // block state to restore later
    protected IBlockState orig_blockState;

    // cached values of original block to use for this tile entity's block
    protected float orig_hardness = 1;
    protected float orig_explosionResistance = 1;

    // when to restore state
    protected long timeToRepairAt = 0;
    protected long creationTime = 0;

    public void setTicksToRepair(World world, int ticksToRepair)
    {
        long currentTime = world.getTotalWorldTime();
        if (creationTime == 0)
        {
            creationTime = currentTime;
        }
        timeToRepairAt = currentTime + ticksToRepair;
        markDirty();
    }

    // override to change behavior when determining if repairing block has expired
    @Override
    public long getExpirationTimeLength()
    {
        // override to change behavior
        return 0;
    }
    
    @Override
    public long getTimeToRepairAt()
    {
        return timeToRepairAt;
    }
    
    @Override
    public long getTimeToGiveUpAt()
    {
        long timeExpiration = 0;
        long exp = getExpirationTimeLength();
        if ((creationTime != 0) && (exp > 0))
        {
            timeExpiration = creationTime + exp;
        }        
        return timeExpiration;
    }
    
    protected boolean isTimeToRepair()
    {
        long currentTime = getWorld().getTotalWorldTime();
        return (currentTime >= timeToRepairAt);
    }
    
    public boolean isTimeToGiveUp()
    {
        long timeExpiration = getTimeToGiveUpAt();
        if (timeExpiration > 0)
        {
            long currentTime = getWorld().getTotalWorldTime();
            return (currentTime >= timeExpiration);
        }
        
        return false;
    }
    
    protected boolean isDataInvalid()
    {
        return orig_blockState == null || orig_blockState == this.getBlockType().getDefaultState();
    }
    
    // override to change behavior when determining if block is repairable now
    protected boolean canRepairBlock()
    {
        // don't repair now if any Entity's are in our bounds
        AxisAlignedBB aabb = this.getBlockType().getDefaultState().getBoundingBox(this.getWorld(), this.getPos());
        aabb = aabb.offset(this.getPos());
        List<EntityLivingBase> listTest = this.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        return (listTest.size() == 0);
    }

    // override to change behavior when a block can't be repaired now (aka canRepairBlock() returned false)
    protected void onCantRepairBlock()
    {
        // override to change behavior
        if (isTimeToGiveUp())
        {
            giveUp();
        }
    }

    // override to change behavior before a block is restored
    protected void preRestoreBlock()
    {
        // do nothing by default
        // override to change behavior
    }

    // override to change behavior of restoring a block
    protected void coreRestoreBlock()
    {
        getWorld().setBlockState(this.getPos(), orig_blockState);
        markDirty();
    }
    
    protected void cancelAdjacentLeafDecay()
    {
        // try to untrigger leaf decay for those large trees too far from wood source
        // also undo it for neighbors around it
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    BlockPos posFix = pos.add(x, y, z);
                    if (LeafUtil.isLeaves(world, posFix))
                    {
                        try
                        {
                            RepairManager.log("restoring leaf to non decay state at pos: " + posFix);
                            IBlockState state = world.getBlockState(posFix);
                            // modify just the CHECK_DECAY property, leaving the rest as-is
                            world.setBlockState(posFix, state.withProperty(BlockLeaves.CHECK_DECAY, false), 4);
                        }
                        catch (Exception ex)
                        {
                            // must be a modded block that doesn't use decay
                            if (RepairManager.isDebugEnabled())
                            {
                                // debug log stack trace
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                ex.printStackTrace(pw);
                                String sStackTrace = sw.toString(); // stack trace as a string

                                RepairManager.log("Assume modded block not using decay: " + sStackTrace);
                            }
                        }
                    }
                }
            }
        }

    }

    // override to change behavior after a block is restored
    protected void postRestoreBlock()
    {
        markDirty();

        cancelAdjacentLeafDecay();
    }

    // override to change behavior to restore a block (after canRestoreBlock() returns true)
    protected void restoreBlock()
    {
        RepairManager.log("restoring block to state: " + orig_blockState + " at " + this.getPos());
        preRestoreBlock();
        coreRestoreBlock();
        postRestoreBlock();
    }
    
    protected void giveUp()
    {
        RepairManager.log("giving up on state: " + orig_blockState + " at " + this.getPos());
        getWorld().setBlockState(this.getPos(), Blocks.AIR.getDefaultState());
        markDirty();
        this.invalidate();
    }
    
    // implements ITickable
    @Override
    public void update()
    {
        if (!getWorld().isRemote)
        {
            if (isTimeToRepair())
            {
                // if for some reason data is invalid, remove block
                if (isDataInvalid())
                {
                    RepairManager.log("invalid state for repairing block, removing, orig_blockState: " + orig_blockState + " vs " + this.getBlockType().getDefaultState());
                    giveUp();
                }
                else
                {
                    if (canRepairBlock())
                    {
                        restoreBlock();
                    }
                    else
                    {
                        onCantRepairBlock();
                    }
                }
            }
        }
    }

    /*
     * Read and write additional NBT data
     */

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound var1)
    {
        if (orig_blockState != null)
        {
            String str = Block.REGISTRY.getNameForObject(this.orig_blockState.getBlock()).toString();
            var1.setString("orig_blockName", str);

            NBTTagCompound stateNBT = new NBTTagCompound();
            NBTUtil.writeBlockState(stateNBT, this.orig_blockState);
            var1.setTag("orig_blockState2", stateNBT);
        }
        var1.setLong("timeToRepairAt", timeToRepairAt);
        var1.setLong("creationTime", creationTime);

        var1.setFloat("orig_hardness", orig_hardness);
        var1.setFloat("orig_explosionResistance", orig_explosionResistance);

        return super.writeToNBT(var1);
    }

    @Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        timeToRepairAt = var1.getLong("timeToRepairAt");
        creationTime = var1.getLong("creationTime");
        try
        {
            Block block = Block.getBlockFromName(var1.getString("orig_blockName"));
            if (block != null)
            {
                this.orig_blockState = NBTUtil.readBlockState(var1.getCompoundTag("orig_blockState2"));
            }
        }
        catch (Exception ex)
        {
            if (RepairManager.isDebugEnabled())
            {
                ex.printStackTrace();
            }

            this.orig_blockState = Blocks.AIR.getDefaultState();
        }

        orig_hardness = var1.getFloat("orig_hardness");
        orig_explosionResistance = var1.getFloat("orig_explosionResistance");
    }

    /*
     * Original block (to repair) data accessors
     */

    public void init(World world, int ticksToRepair, IBlockState state, float hardness, float explosionResistance)
    {
        this.orig_blockState = state;
        this.orig_hardness = hardness;
        this.orig_explosionResistance = explosionResistance;
        setTicksToRepair(world, ticksToRepair);
        markDirty();
    }

    public IBlockState getOrig_blockState()
    {
        return orig_blockState;
    }

    public float getOrig_hardness()
    {
        return orig_hardness;
    }

    public float getOrig_explosionResistance()
    {
        return orig_explosionResistance;
    }
    
    // ----------------------------------------------------------------------
    // Update custom data to client
    // from https://github.com/Chisel-Team/Chisel/blob/1.10/dev/src/main/java/team/chisel/common/block/TileAutoChisel.java

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound ret = super.getUpdateTag();
        ret.setLong("timeToRepairAt", timeToRepairAt);
        ret.setLong("creationTime", creationTime);
        return ret;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        handleUpdateTag(pkt.getNbtCompound());
        super.onDataPacket(net, pkt);
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        timeToRepairAt = tag.getLong("timeToRepairAt");
        creationTime = tag.getLong("creationTime");
        super.handleUpdateTag(tag);
    }
}