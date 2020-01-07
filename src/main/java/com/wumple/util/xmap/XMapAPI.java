package com.wumple.util.xmap;

import com.wumple.util.base.misc.Util;
import com.wumple.util.map.MapDataUtil;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class XMapAPI implements IXMapAPI
{
	public static IXMapAPI instance;
	
	public static IXMapAPI getInstance()
	{
		if (instance == null)
		{
			register(new XMapAPI());
		}
		return instance;
	}

	public static void register(IXMapAPI api)
	{
		instance = api;
	}

	public XMapAPI()
	{
		super();
	}

	@Override
	public ItemStack setupNewMap(World worldIn, int worldX, int worldZ, byte scale, boolean trackingPosition, boolean unlimitedTracking)
	{
		return XFilledMapItem.setupNewMap(worldIn, worldX, worldZ, scale, trackingPosition, unlimitedTracking);
	}

	@Override
	public boolean isEmptyMap(ItemStack itemstack1)
	{
		return ItemTags.getCollection().getOrCreate(new ResourceLocation("forge", "empty_maps")).contains(itemstack1.getItem());
	}

	@Override
	public boolean isFilledMap(ItemStack itemstack1)
	{
		return ItemTags.getCollection().getOrCreate(new ResourceLocation("forge", "filled_maps")).contains(itemstack1.getItem());
	}
	
	@Override
	public boolean isMapScaleValid(ItemStack itemstack, byte scale)
	{
		// byte range is -128 to 127
		if (itemstack.getItem() instanceof IXFilledMapItem)
		{
			IXFilledMapItem item = (IXFilledMapItem) (itemstack.getItem());
			return item.isAMapScaleValid(scale);
		}
		else
		{
			return isMapScaleValid(scale);
		}
	}
	
	@Override
	public ItemStack copyMap(ItemStack itemstack, int i)
	{
		// MAYBE check if itemstack is a valid map and return EMPTY if not?
		
		ItemStack itemstack2 = itemstack.copy();
		itemstack2.setCount(i);
	
		return itemstack2;
	}

	@Override
	public boolean isExplorationMap(MapData mapData)
	{
		return MapDataUtil.isExplorationMap(mapData);
	}

	@Override
	public boolean isExplorationMap(ItemStack itemstack, World worldIn)
	{
		FilledMapItem item = Util.as(itemstack.getItem(), FilledMapItem.class);
		MapData mapdata = (item != null) ? FilledMapItem.getMapData(itemstack, worldIn) : null;
	
		return isExplorationMap(mapdata);
	}

	@Override
	public MapData getMapData(ItemStack itemstack, World worldIn)
	{
		MapData mapdata;
		
		// use XFilledMapItem if possible to get XFilledMapData instead of just MapData
		if (itemstack.getItem() instanceof IXFilledMapItem)
		{
			IXFilledMapItem item = Util.as(itemstack.getItem(), IXFilledMapItem.class);
			mapdata = item.getMyMapData(itemstack, worldIn);
		}
		// otherwise if normal map, fallback
		else
		{
			mapdata = FilledMapItem.getMapData(itemstack, worldIn);
		}
		
		return mapdata;
	}

	@Override
	public MapData createMapData(String mapName)
	{
		return new MapData(mapName);
	}
}