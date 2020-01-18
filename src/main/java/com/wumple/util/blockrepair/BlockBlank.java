package com.wumple.util.blockrepair;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockBlank extends AirBlock
{
	public BlockBlank()
	{
		this(Block.Properties.create(Material.AIR));
	}

	public BlockBlank(Block.Properties properties)
	{
		super(properties);
	}
}