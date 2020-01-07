package com.wumple.util.xmap;

import com.wumple.util.map.MapUtil;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public interface IXMapAPI
{
    default Item getEmptyMapItem() { return Items.MAP; }
	default Item getFilledMapItem() { return Items.FILLED_MAP; }

	boolean isEmptyMap(ItemStack itemstack1);
    boolean isFilledMap(ItemStack itemstack1);

    default byte getMaxScale() { return 4; }
    default byte getDefaultScale() { return 0; }

    default boolean isMapScaleValid(byte scale) { return (scale >= 0) && (scale <= getMaxScale()); } 
    boolean isMapScaleValid(ItemStack itemstack, byte scale);
	
    ItemStack setupNewMap(World worldIn, int worldX, int worldZ, byte scale, boolean trackingPosition, boolean unlimitedTracking);
    default ItemStack setupNewMap(World worldIn, int worldX, int worldZ, byte scale)
    	{ return setupNewMap(worldIn, worldX, worldZ, scale, true, false); }
    default ItemStack setupNewMap(World worldIn, int worldX, int worldZ)
    	{ return setupNewMap(worldIn, worldX, worldZ, getDefaultScale(), true, false); }
    

    ItemStack copyMap(ItemStack itemstack, int i);
    default ItemStack copyMap(ItemStack itemstack) { return copyMap(itemstack, 1); }

    boolean isExplorationMap(MapData mapData);
    boolean isExplorationMap(ItemStack itemstack, World worldIn);

    MapData getMapData(ItemStack itemstack, World worldIn);
    
	
	MapData createMapData(String mapName);
	
	default void mapScaleDirection(ItemStack stack, int direction) { MapUtil.mapScaleDirection(stack, direction); }
	default void increaseMapScale(ItemStack stack) { mapScaleDirection(stack, 1); }
	default void decreaseMapScale(ItemStack stack) { mapScaleDirection(stack, -1); }
}