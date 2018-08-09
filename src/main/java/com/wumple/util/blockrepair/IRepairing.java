package com.wumple.util.blockrepair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public interface IRepairing extends IRepairingTimes
{
    void init(World world, int ticksToRepair, IBlockState state, float hardness, float explosionResistance);
}
