package com.wumple.util.xchest;

import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntityType;

public class XChestTileEntity extends ChestTileEntity
{
	//private BaseChestType chestType;

	public XChestTileEntity()
	{
		//this(new BaseChestType());
	}

	protected XChestTileEntity(TileEntityType<?> typeIn)
	{
		super(typeIn);
	}

	/*
	public BaseChestTileEntity(BaseChestType chestType)
	{
		super(chestType.tileType());
		this.chestType = chestType;
	}
	
	public BaseChestType getChestType()
	{
		return chestType;
	}
	*/
}
