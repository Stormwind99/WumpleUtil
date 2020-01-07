package com.wumple.util.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wumple.util.base.function.FourFunction;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/*
 * ShapelessRecipe with a generic Serializer added that can be re-used for custom shaped recipes
 * 
 * Mostly adapted from net.minecraft.item.crafting.ShapelessRecipe with modified-for-reuse Serializer<T extends ShapedRecipe>
 * 
 * To use:
 *   Create ChildRecipe inherits ShapelessRecipe
 *   Create instance of ChildRecipe.Serializer<ChildRecipe> and register it:
 
 	void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
    	{
        	CRAFTING_CHILDRECIPE = new ChildRecipe.Serializer<ChildRecipe>(ChildRecipe::new);
    		CRAFTING_CHILDRECIPE.setRegistryName("childrecipe");
        	event.getRegistry().registerAll(CRAFTING_CHILDRECIPE);
        }

 */
public class ShapelessRecipe implements ICraftingRecipe
{

	static int MAX_WIDTH = 3;
	static int MAX_HEIGHT = 3;

	/**
	 * Expand the max width and height allowed in the deserializer. This should be
	 * called by modders who add custom crafting tables that are larger than the
	 * vanilla 3x3.
	 * 
	 * @param width  your max recipe width
	 * @param height your max recipe height
	 */
	public static void setCraftingSize(int width, int height)
	{
		if (MAX_WIDTH < width)
			MAX_WIDTH = width;
		if (MAX_HEIGHT < height)
			MAX_HEIGHT = height;
	}

	private final ResourceLocation id;
	private final String group;
	private final ItemStack recipeOutput;
	private final NonNullList<Ingredient> recipeItems;
	private final boolean isSimple;

	public ShapelessRecipe(ResourceLocation idIn, String groupIn, ItemStack recipeOutputIn,
			NonNullList<Ingredient> recipeItemsIn)
	{
		this.id = idIn;
		this.group = groupIn;
		this.recipeOutput = recipeOutputIn;
		this.recipeItems = recipeItemsIn;
		this.isSimple = recipeItemsIn.stream().allMatch(Ingredient::isSimple);
	}

	public ResourceLocation getId()
	{
		return this.id;
	}

	public IRecipeSerializer<?> getSerializer()
	{
		return IRecipeSerializer.CRAFTING_SHAPELESS;
	}

	/**
	 * Recipes with equal group are combined into one button in the recipe book
	 */
	public String getGroup()
	{
		return this.group;
	}

	/**
	 * Get the result of this recipe, usually for display purposes (e.g. recipe
	 * book). If your recipe has more than one possible result (e.g. it's dynamic
	 * and depends on its inputs), then return an empty stack.
	 */
	public ItemStack getRecipeOutput()
	{
		return this.recipeOutput;
	}

	public NonNullList<Ingredient> getIngredients()
	{
		return this.recipeItems;
	}

	public boolean matches(CraftingInventory inv, World worldIn)
	{
		RecipeItemHelper recipeitemhelper = new RecipeItemHelper();
		java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
		int i = 0;

		for (int j = 0; j < inv.getSizeInventory(); ++j)
		{
			ItemStack itemstack = inv.getStackInSlot(j);
			if (!itemstack.isEmpty())
			{
				++i;
				if (isSimple)
					recipeitemhelper.func_221264_a(itemstack, 1);
				else
					inputs.add(itemstack);
			}
		}

		return i == this.recipeItems.size() && (isSimple ? recipeitemhelper.canCraft(this, (IntList) null)
				: net.minecraftforge.common.util.RecipeMatcher.findMatches(inputs, this.recipeItems) != null);
	}

	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		return this.recipeOutput.copy();
	}

	/**
	 * Used to determine if this recipe can fit in a grid of the given width/height
	 */
	public boolean canFit(int width, int height)
	{
		return width * height >= this.recipeItems.size();
	}

	public static class Serializer<T extends ShapelessRecipe>
			extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>>
			implements IRecipeSerializer<ShapelessRecipe>
	{
		//private static final ResourceLocation NAME = new ResourceLocation("minecraft", "crafting_shapeless");

		// builder that instantiates a new T extends ShapedRecipe 
		private FourFunction<ResourceLocation, String, ItemStack, NonNullList<Ingredient>, T> function;

		public Serializer(FourFunction<ResourceLocation, String, ItemStack, NonNullList<Ingredient>, T> functionIn)
		{
			this.function = functionIn;
		}

		public ShapelessRecipe read(ResourceLocation recipeId, JsonObject json)
		{
			String s = JSONUtils.getString(json, "group", "");
			NonNullList<Ingredient> nonnulllist = readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
			if (nonnulllist.isEmpty())
			{
				throw new JsonParseException("No ingredients for shapeless recipe");
			}
			else if (nonnulllist.size() > MAX_WIDTH * MAX_HEIGHT)
			{
				throw new JsonParseException(
						"Too many ingredients for shapeless recipe the max is " + (MAX_WIDTH * MAX_HEIGHT));
			}
			else
			{
				ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
				return function.apply(recipeId, s, itemstack, nonnulllist);
			}
		}

		private static NonNullList<Ingredient> readIngredients(JsonArray p_199568_0_)
		{
			NonNullList<Ingredient> nonnulllist = NonNullList.create();

			for (int i = 0; i < p_199568_0_.size(); ++i)
			{
				Ingredient ingredient = Ingredient.deserialize(p_199568_0_.get(i));
				if (!ingredient.hasNoMatchingItems())
				{
					nonnulllist.add(ingredient);
				}
			}

			return nonnulllist;
		}

		public ShapelessRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
		{
			String s = buffer.readString(32767);
			int i = buffer.readVarInt();
			NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

			for (int j = 0; j < nonnulllist.size(); ++j)
			{
				nonnulllist.set(j, Ingredient.read(buffer));
			}

			ItemStack itemstack = buffer.readItemStack();
			return function.apply(recipeId, s, itemstack, nonnulllist);
		}

		public void write(PacketBuffer buffer, ShapelessRecipe recipe)
		{
			buffer.writeString(recipe.group);
			buffer.writeVarInt(recipe.recipeItems.size());

			for (Ingredient ingredient : recipe.recipeItems)
			{
				ingredient.write(buffer);
			}

			buffer.writeItemStack(recipe.recipeOutput);
		}
	}
}
