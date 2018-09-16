package com.wumple.util.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

// from https://github.com/Choonster-Minecraft-Mods/TestMod3/blob/5079e1a1fc15b50d34e3fa210b8830d913a73fe3/src/main/java/choonster/testmod3/crafting/recipe/DummyRecipe.java

/**
 * A no-op implementation of {@link IRecipe} designed to override vanilla recipes.
 *
 * @author Choonster
 */
public class DummyRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(final InventoryCrafting inv, final World worldIn) {
		return false;
	}

	@Override
	public ItemStack getCraftingResult(final InventoryCrafting inv) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(final int width, final int height) {
		return false;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}
}