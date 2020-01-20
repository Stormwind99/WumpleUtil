package com.wumple.util.crafting;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class CraftingUtil
{
	/*
	 * Grow results in craftMatrix by 1 - useful for recipes that don't consume an ingredient
	 */
	public static void growByOne(IInventory craftMatrix, ItemStack results)
	{
		for (int i = craftMatrix.getSizeInventory() - 1; i >= 0; i--)
		{
			final ItemStack slot = craftMatrix.getStackInSlot(i);

			if (slot == null)
			{
				continue;
			}
			else if (slot == results)
			{
				// increment stack size by 1 so when decreased automatically by 1 there is still 1 there
				slot.grow(1);
			}
		}
	}
}
