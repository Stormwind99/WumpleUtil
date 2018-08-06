package com.wumple.util.placeholder;

import com.wumple.util.misc.RegistrationHelpers;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraftforge.registries.IForgeRegistry;

public class BlockPlaceholder extends Block implements IBlockPlaceholder
{
    public static void register(final IForgeRegistry<Block> registry)
    {
        RegistrationHelpers.regHelper(registry, new BlockPlaceholder());
    }
    
    public BlockPlaceholder()
    { super(Material.AIR); }
    
    public BlockPlaceholder(Material blockMaterialIn, MapColor blockMapColorIn)
    {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockPlaceholder(Material materialIn)
    {
        super(materialIn);
    }
}
