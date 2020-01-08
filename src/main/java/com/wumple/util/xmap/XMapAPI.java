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
	
	private static ResourceLocation emptyMapTag = new ResourceLocation("forge", "empty_maps");
	private static ResourceLocation filledMapTag = new ResourceLocation("forge", "filled_maps");
	
	public ResourceLocation getEmptyMapTag() { return emptyMapTag; }
	public ResourceLocation getFilledMapTag() { return filledMapTag; }
	
	@Override
	public boolean isEmptyMap(ItemStack itemstack1)
	{
		return ItemTags.getCollection().getOrCreate(getEmptyMapTag()).contains(itemstack1.getItem());
	}

	@Override
	public boolean isFilledMap(ItemStack itemstack1)
	{
		return ItemTags.getCollection().getOrCreate(getFilledMapTag()).contains(itemstack1.getItem());
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
	public ItemStack copyMapShallow(ItemStack itemstack)
	{
		if (itemstack.getItem() instanceof XFilledMapItem)
		{
			ItemStack newstack = ((XFilledMapItem)(itemstack.getItem())).copyMapShallow(itemstack);
			return newstack;
		}
		else
		{
			return itemstack.copy();
		}
	}
		
	@Override
	public ItemStack copyMapDeep(ItemStack itemstack, World worldIn)
	{
		ItemStack newstack = null;
		
		if (itemstack.getItem() instanceof IXFilledMapItem)
		{
			newstack = ((IXFilledMapItem)(itemstack.getItem())).copyMapDeep(itemstack, worldIn);
		}
		else
		{
			newstack = itemstack.copy();
			cloneMapData(itemstack, newstack, worldIn);
		}
		
		return newstack;
	}
	
	@Override
	public ItemStack copyMapDeepLocked(ItemStack itemstack, World worldIn)
	{
		return FilledMapItem.func_219992_b(worldIn, itemstack);
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
	public MapData getMapDataIfExists(ItemStack itemstack, World worldIn)
	{
		MapData mapdata;
		
		// use XFilledMapItem if possible to get XFilledMapData instead of just MapData
		if (itemstack.getItem() instanceof IXFilledMapItem)
		{
			IXFilledMapItem item = Util.as(itemstack.getItem(), IXFilledMapItem.class);
			mapdata = item.getMyData(itemstack, worldIn);
		}
		// otherwise if normal map, fallback
		else
		{
			mapdata = FilledMapItem.getData(itemstack, worldIn);
		}
		
		return mapdata;
	}
	
	@Override
	public MapData createMapData(String mapName)
	{
		return new MapData(mapName);
	}
}