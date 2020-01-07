package com.wumple.util.crafting;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * Removes recipes.
 * 
 * From (with additions):
 * https://github.com/Choonster-Minecraft-Mods/TestMod3/blob/1.14.4/src/main/java/choonster/testmod3/init/ModCrafting.java
 */
public class RecipeRemover
{
	private static final Logger LOGGER = LogManager.getLogger();

	private static final Field RECIPES = ObfuscationReflectionHelper.findField(RecipeManager.class,
			"field_199522_d" /* recipes */);

	/*
	// Example: Removes recipes from the server's recipe manager when it starts up.
	@SubscribeEvent
	public void removeRecipes(FMLServerStartedEvent event) {
		final RecipeManager recipeManager = event.getServer().getRecipeManager();
		removeRecipes(recipeManager, FireworkRocketRecipe.class);
		removeRecipes(recipeManager, FireworkStarRecipe.class);
		removeRecipes(recipeManager, FireworkStarFadeRecipe.class);
		removeRecipes(recipeManager, ModTags.Items.VANILLA_DYES);
		removeRecipes(recipeManager, ModTags.Items.VANILLA_TERRACOTTA);
	}
	
	/**
	 * Removes all crafting recipes with an output item contained in the specified tag.
	 *
	 * @param recipeManager The recipe manager
	 * @param tag           The tag
	 */
	public static void removeRecipes(final RecipeManager recipeManager, final Tag<Item> tag)
	{
		final int recipesRemoved = removeRecipes(recipeManager, recipe -> {
			final ItemStack recipeOutput = recipe.getRecipeOutput();
			return !recipeOutput.isEmpty() && recipeOutput.getItem().isIn(tag);
		});

		LOGGER.info("Removed {} recipe(s) for tag {}", recipesRemoved, tag.getId());
	}

	/**
	 * Removes all crafting recipes with an output item of the specified resource location.
	 *
	 * @param recipeManager The recipe manager
	 * @param loc           The resource location
	 */
	public static void removeRecipes(final RecipeManager recipeManager, final ResourceLocation loc)
	{
		final int recipesRemoved = removeRecipes(recipeManager, recipe -> {
			final ItemStack recipeOutput = recipe.getRecipeOutput();
			return !recipeOutput.isEmpty() && (recipeOutput.getItem().getRegistryName().compareTo(loc) == 0);		
		});

		LOGGER.info("Removed {} recipe(s) for resid {}", recipesRemoved, loc.toString());
	}
	
	/**
	 * Remove all crafting recipes that are instances of the specified class.
	 * <p>
	 * Test for this thread:
	 * https://www.minecraftforge.net/forum/topic/33420-removing-vanilla-recipes/
	 *
	 * @param recipeManager The recipe manager
	 * @param recipeClass   The recipe class
	 */
	public static void removeRecipes(final RecipeManager recipeManager, final Class<? extends IRecipe<?>> recipeClass)
	{
		final int recipesRemoved = removeRecipes(recipeManager, recipeClass::isInstance);

		LOGGER.info("Removed {} recipe(s) for class {}", recipesRemoved, recipeClass);
	}

	/**
	 * Remove all crafting recipes that match the specified predicate.
	 *
	 * @param recipeManager The recipe manager
	 * @param predicate     The predicate
	 * @return The number of recipes removed
	 */
	public static int removeRecipes(final RecipeManager recipeManager, final Predicate<IRecipe<?>> predicate)
	{
		final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> existingRecipes;
		try
		{
			@SuppressWarnings("unchecked")
			final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipesMap = (Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>>) RECIPES
					.get(recipeManager);
			existingRecipes = recipesMap;
		} catch (final IllegalAccessException e)
		{
			throw new RuntimeException("Couldn't get recipes map while removing recipes", e);
		}

		final Object2IntMap<IRecipeType<?>> removedCounts = new Object2IntOpenHashMap<>();
		final ImmutableMap.Builder<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> newRecipes = ImmutableMap
				.builder();

		// For each recipe type, create a new map that doesn't contain the recipes to be removed
		existingRecipes.forEach((recipeType, existingRecipesForType) -> {
			//noinspection UnstableApiUsage
			final ImmutableMap<ResourceLocation, IRecipe<?>> newRecipesForType = existingRecipesForType.entrySet()
					.stream().filter(entry -> !predicate.test(entry.getValue()))
					.collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

			removedCounts.put(recipeType, existingRecipesForType.size() - newRecipesForType.size());
			newRecipes.put(recipeType, newRecipesForType);
		});

		final int removedCount = removedCounts.values().stream().reduce(0, Integer::sum);

		try
		{
			RECIPES.set(recipeManager, newRecipes.build());
		}
		catch (final IllegalAccessException e)
		{
			throw new RuntimeException("Couldn't replace recipes map while removing recipes", e);
		}

		return removedCount;
	}
}