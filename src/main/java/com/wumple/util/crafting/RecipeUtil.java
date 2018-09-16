package com.wumple.util.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

/*
 * from:
 *  https://github.com/Choonster-Minecraft-Mods/TestMod3/blob/1.12.2/src/main/java/choonster/testmod3/init/ModRecipes.java
 *  https://github.com/Choonster-Minecraft-Mods/TestMod3/blob/1.12.2/src/main/java/choonster/testmod3/crafting/recipe/RecipeUtil.java
 */

/**
 * Utility methods for {@link IRecipe}s.
 *
 * @author Choonster
 */
public class RecipeUtil
{

    protected static void log(String msg)
    {
    }

    /**
     * Remove all crafting recipes with the specified {@link Block} as their output.
     *
     * @param output
     *            The output Block
     */
    public static void removeRecipes(final Block output)
    {
        removeRecipes(Item.getItemFromBlock(output));
    }

    /**
     * Remove all crafting recipes with the specified {@link Item} as their output.
     *
     * @param output
     *            The output Item
     */
    public static void removeRecipes(final Item output)
    {
        final int recipesRemoved = removeRecipes(recipe -> {
            final ItemStack recipeOutput = recipe.getRecipeOutput();
            return !recipeOutput.isEmpty() && recipeOutput.getItem() == output;
        });

        log("Removed " + recipesRemoved + " recipe(s) for " + output.getRegistryName());
    }

    /**
     * Remove all crafting recipes that are instances of the specified class.
     * <p>
     * Test for this thread: http://www.minecraftforge.net/forum/index.php/topic,33631.0.html
     *
     * @param recipeClass
     *            The recipe class
     */
    public static void removeRecipes(final Class<? extends IRecipe> recipeClass)
    {
        final int recipesRemoved = removeRecipes(recipeClass::isInstance);

        log("Removed " + recipesRemoved + " recipe(s) for " + recipeClass);
    }

    /**
     * Remove all crafting recipes that match the specified predicate.
     *
     * @param predicate
     *            The predicate
     * @return The number of recipes removed
     */
    public static int removeRecipes(final Predicate<IRecipe> predicate)
    {
        int recipesRemoved = 0;

        final IForgeRegistry<IRecipe> registry = ForgeRegistries.RECIPES;
        final List<IRecipe> toRemove = new ArrayList<>();

        for (final IRecipe recipe : registry)
        {
            if (predicate.test(recipe))
            {
                toRemove.add(recipe);
                recipesRemoved++;
            }
        }

        log("Overriding recipes with dummy recipes, please ignore any following \"Dangerous alternative prefix\" warnings.");
        toRemove.forEach(recipe -> {
            final ResourceLocation registryName = Objects.requireNonNull(recipe.getRegistryName());
            final IRecipe replacement = new DummyRecipe().setRegistryName(registryName);
            registry.register(replacement);
        });

        return recipesRemoved;
    }

    /**
     * Parse the input of a shaped recipe.
     * <p>
     * Adapted from {@link ShapedOreRecipe#factory}.
     *
     * @param context
     *            The parsing context
     * @param json
     *            The recipe's JSON object
     * @return A ShapedPrimer containing the input specified in the JSON object
     */
    public static CraftingHelper.ShapedPrimer parseShaped(final JsonContext context, final JsonObject json)
    {
        final Map<Character, Ingredient> ingredientMap = Maps.newHashMap();
        for (final Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(json, "key").entrySet())
        {
            if (entry.getKey().length() != 1)
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            if (" ".equals(entry.getKey()))
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

            ingredientMap.put(entry.getKey().toCharArray()[0], CraftingHelper.getIngredient(entry.getValue(), context));
        }

        ingredientMap.put(' ', Ingredient.EMPTY);

        final JsonArray patternJ = JsonUtils.getJsonArray(json, "pattern");

        if (patternJ.size() == 0)
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");

        final String[] pattern = new String[patternJ.size()];
        for (int x = 0; x < pattern.length; ++x)
        {
            final String line = JsonUtils.getString(patternJ.get(x), "pattern[" + x + "]");
            if (x > 0 && pattern[0].length() != line.length())
                throw new JsonSyntaxException("Invalid pattern: each row must  be the same width");
            pattern[x] = line;
        }

        final CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
        primer.width = pattern[0].length();
        primer.height = pattern.length;
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = NonNullList.withSize(primer.width * primer.height, Ingredient.EMPTY);

        final Set<Character> keys = Sets.newHashSet(ingredientMap.keySet());
        keys.remove(' ');

        int index = 0;
        for (final String line : pattern)
        {
            for (final char chr : line.toCharArray())
            {
                final Ingredient ing = ingredientMap.get(chr);
                if (ing == null)
                    throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
                primer.input.set(index++, ing);
                keys.remove(chr);
            }
        }

        if (!keys.isEmpty())
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

        return primer;
    }

    /**
     * Parse the input of a shapeless recipe.
     * <p>
     * Adapted from {@link ShapelessOreRecipe#factory}.
     *
     * @param context
     *            The parsing context
     * @param json
     *            The recipe's JSON object
     * @return A NonNullList containing the ingredients specified in the JSON object
     */
    public static NonNullList<Ingredient> parseShapeless(final JsonContext context, final JsonObject json)
    {
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        for (final JsonElement element : JsonUtils.getJsonArray(json, "ingredients"))
            ingredients.add(CraftingHelper.getIngredient(element, context));

        if (ingredients.isEmpty())
            throw new JsonParseException("No ingredients for shapeless recipe");

        return ingredients;
    }
}
