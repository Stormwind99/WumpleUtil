package com.wumple.util.xmap;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public interface IXFilledMapItem
{
	boolean isAMapScaleValid(byte scale);

	ItemStack setupANewMap(World worldIn, int worldX, int worldZ, byte scale, boolean trackingPosition,
			boolean unlimitedTracking);
	
	default ItemStack setupANewMap(World worldIn, int worldX, int worldZ, byte scale)
		{ return setupANewMap(worldIn, worldX, worldZ, scale, true, false); }

	String getID();

	boolean fillMapData(World worldIn, Entity viewer, ItemStack itemstack);

	// -------------------------------------------------------------------------------------
	// MapData-using API's for backwards compatibility, etc - try not to use these

	void fillMapData(World worldIn, Entity viewer, MapData data);

	void updateMapData(World worldIn, Entity viewer, MapData data);

	void updateMapDataArea(World worldIn, Entity viewer, MapData data, int startPixelX, int startPixelZ,
			int endPixelX, int endPixelZ, BiFunction<Integer, Integer, Boolean> usePixel);

	@Nullable
	public MapData getMyMapData(ItemStack stack, World worldIn);
}