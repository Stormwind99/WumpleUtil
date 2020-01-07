package com.wumple.util.crafting;

import java.lang.reflect.Field;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

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
