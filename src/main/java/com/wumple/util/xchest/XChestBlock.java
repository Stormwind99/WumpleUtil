package com.wumple.util.xchest;

import com.wumple.util.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class XChestBlock extends ChestBlock
{
	//private final BaseChestType chestType;

	public XChestBlock(Block.Properties properties) {
		super(properties);
	}


	/*
	public BaseChestBlock(BaseChestType chestType)
	{
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2.5F).sound(SoundType.STONE).harvestTool(ToolType.PICKAXE));
        this.chestType = chestType;
        setRegistryName(Reference.MOD_ID, "chest_" + chestType.name().toLowerCase());
	}
	*/

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn)
	{
		//return new BaseChestTileEntity(chestType);
		return new XChestTileEntity();
	}

	/*
	public BaseChestType getChestType()
	{
		return chestType;
	}
	*/
}
