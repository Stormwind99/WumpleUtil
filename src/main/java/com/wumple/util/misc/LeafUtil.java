package com.wumple.util.misc;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LeafUtil
{
    public static boolean canLeavesGrowAtLocation(World worldIn, BlockPos pos)
    {       
        /*
        if (!ConfigurationHandler.generalSettings.growOutward)
        {
            return true;
        }
        */

        boolean leavesCanGrow = false; 

        BlockPos[] positions = new BlockPos[] { pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south() };

        for (BlockPos blockpos : positions)
        {
            final IBlockState blockState = worldIn.getBlockState(blockpos);
            final Block block = blockState.getBlock();

            if (block.canSustainLeaves(blockState, worldIn, blockpos) || block.isLeaves(blockState, worldIn, blockpos))
            {
                leavesCanGrow = true;
                break;
            }
        }

        return leavesCanGrow;
    }
}
