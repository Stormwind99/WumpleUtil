package com.wumple.util.xchest2;

import net.minecraft.tileentity.TileEntityType;

public class XChestType
{
	public TileEntityType<XChestTileEntity> chestTileType;

	public String name() { return "basechest"; }
	public TileEntityType<XChestTileEntity> tileType() { return chestTileType; }
}
