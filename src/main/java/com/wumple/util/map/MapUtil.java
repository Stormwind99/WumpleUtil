package com.wumple.util.map;

import com.wumple.util.xmap.XMapAPI;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class MapUtil
{
	public static void mapScaleDirection(ItemStack stack, int direction)
	{
		// TODO get map_scale_direction if present and modify it
		stack.getOrCreateTag().putInt("map_scale_direction", direction);
	}
	
	public static int extractMapScaleDirection(ItemStack stack)
	{
		int value = 0;
		
		CompoundNBT compoundnbt = stack.getTag();
		if (compoundnbt != null && compoundnbt.contains("map_scale_direction", 99))
		{
			value = compoundnbt.getInt("map_scale_direction");
			compoundnbt.remove("map_scale_direction");
		}
		
		return value;
	}
	
	/*
	 * Copy MapData from dest to a new copy on newDest
	 */
	public static void forceDeepCloneMap(ItemStack newDest, ItemStack dest, World worldIn)
	{
		// get original map data
		MapData mapdata = XMapAPI.getInstance().getMapData(dest, worldIn);
		
		// force new copy to start with a new MapData
		int i = worldIn.getNextMapId();
		newDest.getOrCreateTag().putInt("map", i);
		
		// copy dest's original map data to newDest (new copy of original)
		MapData mapdata1 = XMapAPI.getInstance().getMapData(newDest, worldIn); // will create new map data due to above map id change
		mapdata1.copyFrom(mapdata);
		mapdata1.scale = mapdata.scale;
		mapdata1.trackingPosition = mapdata.trackingPosition;
		mapdata1.unlimitedTracking = mapdata.unlimitedTracking;
		mapdata1.dimension = mapdata.dimension;
		mapdata1.locked = mapdata.locked;
		
		mapdata1.markDirty();
	}

}
