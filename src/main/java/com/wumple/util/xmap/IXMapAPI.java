package com.wumple.util.xmap;

import com.wumple.util.map.MapUtil;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public interface IXMapAPI
{
	// ------------------------------------------------------------------------
	// Type and checks
	
    default Item getEmptyMapItem() { return Items.MAP; }
	default Item getFilledMapItem() { return Items.FILLED_MAP; }

	boolean isEmptyMap(ItemStack itemstack1);
    boolean isFilledMap(ItemStack itemstack1);
    
    boolean isExplorationMap(MapData mapData);
    boolean isExplorationMap(ItemStack itemstack, World worldIn);

    // ------------------------------------------------------------------------
    // Creation and copying
    
    ItemStack setupNewMap(World worldIn, int worldX, int worldZ, byte scale, boolean trackingPosition, boolean unlimitedTracking);
    default ItemStack setupNewMap(World worldIn, int worldX, int worldZ, byte scale)
    	{ return setupNewMap(worldIn, worldX, worldZ, scale, true, false); }
    default ItemStack setupNewMap(World worldIn, int worldX, int worldZ)
    	{ return setupNewMap(worldIn, worldX, worldZ, getDefaultScale(), true, false); }
    
    ItemStack copyMapShallow(ItemStack itemstack);
	default ItemStack copyMapShallow(ItemStack stack, int count)
		{ ItemStack s=copyMapShallow(stack); s.setCount(count);  return s; }
	ItemStack copyMapDeep(ItemStack stack, World worldIn);
	default ItemStack copyMapDeep(ItemStack stack, World w, int count)
		{ ItemStack s=copyMapDeep(stack, w); s.setCount(count);  return s; }
	ItemStack copyMapDeepLocked(ItemStack stack, World worldIn);

	// ------------------------------------------------------------------------
	// Scale
	
    default byte getMaxScale() { return 4; }
    default byte getDefaultScale() { return 0; }

    default boolean isMapScaleValid(byte scale) { return (scale >= 0) && (scale <= getMaxScale()); } 
    boolean isMapScaleValid(ItemStack itemstack, byte scale);

	default void mapScaleDirection(ItemStack stack, int direction) { MapUtil.mapScaleDirection(stack, direction); }
	default void increaseMapScale(ItemStack stack) { mapScaleDirection(stack, 1); }
	default void decreaseMapScale(ItemStack stack) { mapScaleDirection(stack, -1); }
	
	// ------------------------------------------------------------------------
	// MapData related
	
    MapData getMapData(ItemStack itemstack, World worldIn);
    MapData getMapDataIfExists(ItemStack itemstack, World worldIn);
	MapData createMapData(String mapName);
	
	/*
	 * Deep copy map data from itemstack to newstack
	 */
	default public void cloneMapData(ItemStack itemstack, ItemStack newstack, World worldIn)
	{
		MapData mapdata = getMapData(itemstack, worldIn);
		MapData mapdata1 = FilledMapItem.getMapData(newstack, worldIn);
		mapdata1.copyFrom(mapdata);
		mapdata1.scale = mapdata.scale;
		mapdata1.trackingPosition = mapdata.trackingPosition;
		mapdata1.unlimitedTracking = mapdata.unlimitedTracking;
		mapdata1.dimension = mapdata.dimension;
		mapdata1.locked = mapdata.locked;
	}
}