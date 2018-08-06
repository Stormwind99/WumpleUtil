package com.wumple.util.blockrepair;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockBlank extends Block
{
    public BlockBlank()
    {
        this(Material.AIR);
    }
    
    public BlockBlank(Material materialIn)
    {
        super(materialIn);
    }
}