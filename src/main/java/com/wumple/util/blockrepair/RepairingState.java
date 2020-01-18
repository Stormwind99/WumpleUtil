package com.wumple.util.blockrepair;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.registries.ForgeRegistries;

public class RepairingState
{
	// block state to restore later
	public BlockState orig_blockState;

	// cached values of original block to use for this tile entity's block
	public float orig_hardness = 1;
	public float orig_explosionResistance = 1;

	// when to restore state
	public long timeToRepairAt = 0;
	public long creationTime = 0;
	
	RepairingState()
	{
	}
	
	RepairingState(IWorld world, BlockPos pos, int ticksToRepair)
	{
		init(world, pos, ticksToRepair);
	}
	
	public void init(long currentTime, int ticksToRepair, BlockState state, float hardness, float explosionResistance)
	{
		orig_blockState = state;
		orig_hardness = hardness;
		orig_explosionResistance = explosionResistance;
		setTicksToRepair(currentTime, ticksToRepair);
	}
	
	public void init(IWorld world, BlockPos pos, int ticksToRepair)
	{
		BlockState oldState = world.getBlockState(pos);
		float oldHardness = oldState.getBlockHardness(world, pos);
		float oldExplosionResistance = 1;
		try
		{
			oldExplosionResistance = oldState.getBlock().getExplosionResistance(oldState, world, pos, null, null);
		}
		catch (Exception ex)
		{

		}
		
		init(world.getWorld().getGameTime(), ticksToRepair, oldState, oldHardness, oldExplosionResistance);
	}
	
	public void setTicksToRepair(long currentTime, int ticksToRepair)
	{
		if (creationTime == 0)
		{
			creationTime = currentTime;
		}
		timeToRepairAt = currentTime + ticksToRepair;
	}

	public boolean isTimeToRepair(long currentTime)
	{
		return (currentTime >= timeToRepairAt);
	}
	
	public long getTimeToGiveUpAt(long exp)
	{
		long timeExpiration = 0;
		if ((creationTime != 0) && (exp > 0))
		{
			timeExpiration = creationTime + exp;
		}
		return timeExpiration;
	}
	
	public boolean isTimeToGiveUp(long exp, long currentTime)
	{
		long timeExpiration = getTimeToGiveUpAt(exp);
		if (timeExpiration > 0)
		{
			return (currentTime >= timeExpiration);
		}

		return false;
	}
	
	public void getUpdateTag(CompoundNBT ret)
	{
		ret.putLong("timeToRepairAt", timeToRepairAt);
		ret.putLong("creationTime", creationTime);
	}
	
	public void handleUpdateTag(CompoundNBT tag)
	{
		timeToRepairAt = tag.getLong("timeToRepairAt");
		creationTime = tag.getLong("creationTime");
	}

	public void write(CompoundNBT var1)
	{
		if (orig_blockState != null)
		{
			ResourceLocation loc = ForgeRegistries.BLOCKS.getKey(orig_blockState.getBlock());
			var1.putString("orig_blockName", loc.toString());

			CompoundNBT stateNBT = NBTUtil.writeBlockState(orig_blockState);
			var1.put("orig_blockState2", stateNBT);
		}
		var1.putLong("timeToRepairAt", timeToRepairAt);
		var1.putLong("creationTime", creationTime);

		var1.putFloat("orig_hardness", orig_hardness);
		var1.putFloat("orig_explosionResistance", orig_explosionResistance);
	}
	
	public void read(CompoundNBT var1)
	{
		timeToRepairAt = var1.getLong("timeToRepairAt");
		creationTime = var1.getLong("creationTime");
		try
		{
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(var1.getString("orig_blockName")));
			if (block != null)
			{
				this.orig_blockState = NBTUtil.readBlockState(var1.getCompound("orig_blockState2"));
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
	
	public boolean isDataInvalid(BlockState currentBlockState)
	{
		return orig_blockState == null || orig_blockState == currentBlockState;
	}

}
