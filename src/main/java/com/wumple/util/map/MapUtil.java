package com.wumple.util.map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

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
}
