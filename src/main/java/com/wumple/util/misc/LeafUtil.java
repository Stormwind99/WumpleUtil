package com.wumple.util.misc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class LeafUtil
{
    public static boolean isLeaves(Block block, BlockState blockstate, IWorld world, BlockPos pos)
    {
    	return BlockTags.LEAVES.contains(block) || (block instanceof LeavesBlock);
    }
    
    public static boolean canSustainLeaves(Block block, BlockState blockstate, IWorld world, BlockPos pos)
    {
    	return BlockTags.LOGS.contains(block) || isLeaves(block, blockstate, world, pos);
    }

    public static boolean isLeaves(IWorld world, BlockPos pos)
    {
        BlockState blockstate = world.getBlockState(pos);
        Block block = blockstate.getBlock();
            
        return isLeaves(block, blockstate, world, pos);
    }
    
    public static boolean canLeavesGrowAtLocation(IWorld worldIn, BlockPos pos)
    {       
        boolean leavesCanGrow = false; 
        
        BlockPos[] positions = new BlockPos[] { pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south() };

        for (BlockPos blockpos : positions)
        {
            final BlockState blockState = worldIn.getBlockState(blockpos);
            final Block block = blockState.getBlock();

            if (canSustainLeaves(block, blockState, worldIn, blockpos))
            {
                leavesCanGrow = true;
                break;
            }
        }

        return leavesCanGrow;
    }
}
