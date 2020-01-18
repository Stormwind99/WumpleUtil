package com.wumple.util.blockrepair;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.annotation.Nullable;

import com.wumple.util.misc.LeafUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/*
 * Originally based on CoroUtil's BlockRepairingBlock
 * from https://github.com/Corosauce/CoroUtil
 */
public class TileEntityRepairingBlock extends TileEntity implements ITickableTileEntity, IRepairing
{
	protected RepairingState repairState = new RepairingState();

	public TileEntityRepairingBlock()
	{
		super(MyObjectHolder.RepairingBlock_Tile);
	}

	public TileEntityRepairingBlock(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	public void setTicksToRepair(IWorld world, int ticksToRepair)
	{
		repairState.setTicksToRepair(world.getWorld().getGameTime(), ticksToRepair);
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
		return repairState.timeToRepairAt;
	}

	@Override
	public long getTimeToGiveUpAt()
	{
		return repairState.getTimeToGiveUpAt(getExpirationTimeLength());
	}

	protected boolean isTimeToRepair()
	{
		return repairState.isTimeToRepair(getWorld().getGameTime());
	}

	public boolean isTimeToGiveUp()
	{
		return repairState.isTimeToGiveUp(getExpirationTimeLength(), getWorld().getGameTime());
	}

	protected boolean isDataInvalid()
	{
		return repairState.isDataInvalid(this.getBlockState());
	}

	// override to change behavior when determining if block is repairable now
	protected boolean canRepairBlock()
	{
		// don't repair now if any Entity's are in our bounds
		AxisAlignedBB aabb = repairState.orig_blockState.getCollisionShape(this.getWorld(), this.getPos())
				.getBoundingBox();
		aabb = aabb.offset(this.getPos());
		List<LivingEntity> listTest = this.getWorld().getEntitiesWithinAABB(LivingEntity.class, aabb);
		return (listTest.size() == 0);
	}

	// override to change behavior when a block can't be repaired now (aka
	// canRepairBlock() returned false)
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
		getWorld().setBlockState(this.getPos(), repairState.orig_blockState);
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
							BlockState state = world.getBlockState(posFix);
							// modify just the DISTANCE property, leaving the rest as-is
							world.setBlockState(posFix, state.with(LeavesBlock.DISTANCE, Integer.valueOf(1)), 4);
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

	// override to change behavior to restore a block (after canRestoreBlock()
	// returns true)
	protected void restoreBlock()
	{
		RepairManager.log("restoring block to state: " + repairState.orig_blockState + " at " + this.getPos());
		preRestoreBlock();
		coreRestoreBlock();
		postRestoreBlock();
	}

	protected void giveUp()
	{
		RepairManager.log("giving up on state: " + repairState.orig_blockState + " at " + this.getPos());
		getWorld().setBlockState(this.getPos(), Blocks.AIR.getDefaultState());
		markDirty();
		this.markDirty();
	}

	// implements ITickableTileEntity
	@Override
	public void tick()
	{
		if (!getWorld().isRemote)
		{
			if (isTimeToRepair())
			{
				// if for some reason data is invalid, remove block
				if (isDataInvalid())
				{
					RepairManager.log("invalid state for repairing block, removing, orig_blockState: "
							+ repairState.orig_blockState + " vs " + this.getBlockState());
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
	public CompoundNBT write(CompoundNBT var1)
	{
		repairState.write(var1);
		return super.write(var1);
	}

	@Override
	public void read(CompoundNBT var1)
	{
		super.read(var1);
		repairState.read(var1);
	}

	/*
	 * Original block (to repair) data accessors
	 */

	public void init(IWorld world, int ticksToRepair, BlockState blockstate, float hardness, float explosionResistance)
	{
		repairState.init(world.getWorld().getGameTime(), ticksToRepair, blockstate, hardness, explosionResistance);
		markDirty();
	}
	
	public void setRepairingState(RepairingState newState)
	{
		repairState = newState;
		markDirty();
	}

	// ----------------------------------------------------------------------
	// Update custom data to client
	// from
	// https://github.com/Chisel-Team/Chisel/blob/1.10/dev/src/main/java/team/chisel/common/block/TileAutoChisel.java

	@Override
	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag()
	{
		CompoundNBT ret = super.getUpdateTag();
		repairState.getUpdateTag(ret);
		return ret;
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		handleUpdateTag(pkt.getNbtCompound());
		super.onDataPacket(net, pkt);
	}

	@Override
	public void handleUpdateTag(CompoundNBT tag)
	{
		repairState.handleUpdateTag(tag);
		super.handleUpdateTag(tag);
	}
}