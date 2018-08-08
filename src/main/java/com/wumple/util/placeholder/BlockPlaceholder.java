package com.wumple.util.placeholder;

import com.wumple.util.misc.RegistrationHelpers;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class BlockPlaceholder extends Block implements IBlockPlaceholder
{
    public BlockPlaceholder()
    {
        this(Material.AIR);
    }
    
    public BlockPlaceholder(Material blockMaterialIn, MapColor blockMapColorIn)
    {
        super(blockMaterialIn, blockMapColorIn);
        RegistrationHelpers.nameHelper(this, "wumpleutil:placeholder");
    }

    public BlockPlaceholder(Material materialIn)
    {
        this(materialIn, materialIn.getMaterialMapColor());
    }
}
