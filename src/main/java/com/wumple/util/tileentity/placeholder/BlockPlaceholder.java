package com.wumple.util.tileentity.placeholder;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class BlockPlaceholder extends Block implements IBlockPlaceholder
{
    public BlockPlaceholder(Material blockMaterialIn, MapColor blockMapColorIn)
    {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockPlaceholder(Material materialIn)
    {
        super(materialIn);
    }
}
