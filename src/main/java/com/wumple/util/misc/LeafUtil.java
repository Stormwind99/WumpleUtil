package com.wumple.util.misc;

import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LeafUtil
{
	public static boolean isLeaves(BlockState state)
	{
		return state.isIn(BlockTags.LEAVES);
	}
	
	public static boolean canSustainLeaves(BlockState state)
	{
		return state.isIn(BlockTags.LOGS);
	}
	
    public static boolean canLeavesGrowAtLocation(World worldIn, BlockPos pos)
    {       
        boolean leavesCanGrow = false; 

        BlockPos[] positions = new BlockPos[] { pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south() };

        for (BlockPos blockpos : positions)
        {
            final BlockState blockState = worldIn.getBlockState(blockpos);

            if (canSustainLeaves(blockState) || isLeaves(blockState))
            {
                leavesCanGrow = true;
                break;
            }
        }

        return leavesCanGrow;
    }
}
