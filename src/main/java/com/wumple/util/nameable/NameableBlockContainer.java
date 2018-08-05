package com.wumple.util.nameable;

import com.wumple.util.base.misc.Util;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

abstract public class NameableBlockContainer extends BlockContainer
{
    protected NameableBlockContainer(Material materialIn)
    {
        super(materialIn);
    }
    
    protected NameableBlockContainer(Material materialIn, MapColor color)
    {
        super(materialIn, color);
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (stack.hasDisplayName())
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            INameable nameable = Util.as(tileentity, INameable.class);
            if (nameable != null) { nameable.setCustomName(stack.getDisplayName()); }
        }
    }
}
