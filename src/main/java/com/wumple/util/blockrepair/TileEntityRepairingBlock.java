package com.wumple.util.blockrepair;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/*
 * Based on CoroUtil's BlockRepairingBlock
 * from https://github.com/Corosauce/CoroUtil
 */
public class TileEntityRepairingBlock extends TileEntity implements ITickable
{
    // block state to restore later
    protected IBlockState orig_blockState;

    // cached values of original block to use for this tile entity's block
    protected float orig_hardness = 1;
    protected float orig_explosionResistance = 1;

    // when to restore state
    protected long timeToRepairAt = 0;

    public void setTicksToRepair(World world, int ticksToRepair)
    {
        this.timeToRepairAt = world.getTotalWorldTime() + ticksToRepair;
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
        // do nothing by default
        // override to change behavior
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
    }

    // override to change behavior after a block is restored
    protected void postRestoreBlock()
    {
        markDirty();

        // try to untrigger leaf decay for those large trees too far from wood source
        // also undo it for neighbors around it
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    BlockPos posFix = pos.add(x, y, z);
                    IBlockState state = world.getBlockState(posFix);
                    if (state.getBlock() instanceof BlockLeaves)
                    {
                        try
                        {
                            BlockRepairManager.log("restoring leaf to non decay state at pos: " + posFix);
                            // modify just the CHECK_DECAY property, leaving the rest as-is
                            world.setBlockState(posFix, state.withProperty(BlockLeaves.CHECK_DECAY, false), 4);
                        }
                        catch (Exception ex)
                        {
                            // must be a modded block that doesn't use decay
                            if (BlockRepairManager.isDebugEnabled())
                            {
                                // debug log stack trace
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                ex.printStackTrace(pw);
                                String sStackTrace = sw.toString(); // stack trace as a string

                                BlockRepairManager.log("Assume modded block not using decay: " + sStackTrace);
                            }
                        }
                    }
                }
            }
        }
    }

    // override to change behavior to restore a block (after canRestoreBlock() returns true)
    protected void restoreBlock()
    {
        BlockRepairManager.log("restoring block to state: " + orig_blockState + " at " + this.getPos());
        preRestoreBlock();
        coreRestoreBlock();
        postRestoreBlock();
    }

    // implements ITickable
    @Override
    public void update()
    {
        if (!getWorld().isRemote)
        {

            if (getWorld().getTotalWorldTime() >= timeToRepairAt)
            {

                // if for some reason data is invalid, remove block
                if (orig_blockState == null || orig_blockState == this.getBlockType().getDefaultState())
                {
                    BlockRepairManager.log("invalid state for repairing block, removing, orig_blockState: " + orig_blockState + " vs "
                            + this.getBlockType().getDefaultState());
                    getWorld().setBlockState(this.getPos(), Blocks.AIR.getDefaultState());
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

        var1.setFloat("orig_hardness", orig_hardness);
        var1.setFloat("orig_explosionResistance", orig_explosionResistance);

        return super.writeToNBT(var1);
    }

    @Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        timeToRepairAt = var1.getLong("timeToRepairAt");
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
            ex.printStackTrace();

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
}