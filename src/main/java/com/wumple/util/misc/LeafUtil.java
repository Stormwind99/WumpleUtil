package com.wumple.util.misc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LeafUtil
{
    public static boolean isLeaves(Block block, IBlockState blockstate, World world, BlockPos pos)
    {
        return (block.isLeaves(blockstate, world, pos) || (block instanceof BlockLeaves));
    }

    public static boolean isLeaves(World world, BlockPos pos)
    {
        IBlockState blockstate = world.getBlockState(pos);
        Block block = blockstate.getBlock();
            
        return isLeaves(block, blockstate, world, pos);
    }
    
    public static boolean canLeavesGrowAtLocation(World worldIn, BlockPos pos)
    {       
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
