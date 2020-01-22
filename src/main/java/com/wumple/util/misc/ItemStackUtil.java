package com.wumple.util.misc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackUtil
{
    static public boolean isEmpty(ItemStack stack)
    {
        return ((stack == null) || (stack.isEmpty()));
    }
    
	/*
	 * Copy the ItemStack but change its Item to srcItem
	 */
	public static ItemStack pseudoClone(ItemStack itemstack, Item srcItem)
	{
		// from ItemStack.copy
		ItemStack itemstack2 = new ItemStack(srcItem);
		itemstack2.setAnimationsToGo(itemstack.getAnimationsToGo());
		if (itemstack.hasTag()) itemstack2.setTag(itemstack.getTag());		
		return itemstack2;
	}
}
