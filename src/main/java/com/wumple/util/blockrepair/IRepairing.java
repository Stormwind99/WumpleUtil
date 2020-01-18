package com.wumple.util.blockrepair;

import net.minecraft.block.BlockState;
import net.minecraft.world.IWorld;

public interface IRepairing extends IRepairingTimes
{
	void init(IWorld world, int ticksToRepair, BlockState state, float hardness, float explosionResistance);
	void setRepairingState(RepairingState newState);
}
