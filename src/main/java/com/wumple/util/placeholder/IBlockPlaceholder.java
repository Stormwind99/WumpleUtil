package com.wumple.util.placeholder;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface IBlockPlaceholder extends ITileEntityProvider
{
    default public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityPlaceholder();
    }
}
