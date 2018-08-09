package com.wumple.util.blockrepair;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockBlank extends Block
{
    public BlockBlank()
    {
        this(Material.AIR);
        setCreativeTab(CreativeTabs.MISC);
    }
    
    public BlockBlank(Material materialIn)
    {
        super(materialIn);
    }
}