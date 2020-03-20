package com.wumple.util.capability.copier;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;

import com.wumple.util.Reference;
import com.wumple.util.crafting.CraftingUtil;
import com.wumple.util.crafting.XShapelessRecipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

/**
 * The actual recipe to merge different itemstacks into one itemstack for a cap
 * T
 */
public abstract class CapMergeRecipe<T extends ICopyableCap<?> > extends XShapelessRecipe
{
	abstract protected LazyOptional<? extends T> getCap(ICapabilityProvider provider);

	public CapMergeRecipe(ResourceLocation idIn, String groupIn, ItemStack recipeOutputIn,
			NonNullList<Ingredient> recipeItemsIn)
	{
		super(idIn, groupIn, recipeOutputIn, recipeItemsIn);
	}

	public class CraftingSearchResults
	{
		public ArrayList<ItemStack> stacks;
		public int count;
		public ItemStack exampleStack;
		public Item exampleItem;

		public CraftingSearchResults(ArrayList<ItemStack> stacksIn, int countIn)
		{
			stacks = stacksIn;
			count = countIn;

			exampleStack = stacks.get(0);
			exampleItem = exampleStack.getItem();
		}

		public ItemStack create(World world)
		{
			int newCount = Math.min(stacks.size(), exampleItem.getItemStackLimit(exampleStack));

			ItemStack newStack = new ItemStack(exampleItem, newCount);

			LazyOptional<? extends T> cap = getCap(newStack);
			cap.ifPresent(c->
			{
				c.copyFromProviders(stacks, world);
			});

			return newStack;
		}

		public ItemStack create()
		{
			return create(getWorld());
		}

		public World getWorld()
		{
			return null;
		}
	}

	public void log(String msg)
	{
		// LogManager.getLogger(Reference.MOD_ID).info(msg);
	}

	/**
	 * return the items involved in this recipe, or null if not present
	 * 
	 * @param inv inventory to check for tems
	 * @return dest and src items found, or null if not found
	 */
	protected CraftingSearchResults getStuff(CraftingInventory inv)
	{
		ItemStack firstStack = ItemStack.EMPTY.copy();
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		int count = 0;
		int caps = 0;

		for (int j = 0; j < inv.getSizeInventory(); ++j)
		{
			final ItemStack itemstack1 = inv.getStackInSlot(j);

			if (!itemstack1.isEmpty())
			{
				LazyOptional<? extends T> lcap = getCap(itemstack1);
				
				if (lcap.isPresent())
				{
					if (firstStack.isEmpty())
					{
						firstStack = itemstack1;
					}
				}

				if (ItemStack.areItemsEqual(firstStack, itemstack1))
				{
					stacks.add(itemstack1);
					count += itemstack1.getCount();
					if (lcap.isPresent())
					{
						caps++;
					}
				}
				else
				{
					return null;
				}
			}
		}

		// must have at least 2 items to stack
		// must have at least one cap T version of the item
		if ((stacks.size() < 2) || (caps < 1))
		{
			return null;
		}

		return new CraftingSearchResults(stacks, count);
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn)
	{
		log("CapMergeRecipe recipeMatches begin");
		final CraftingSearchResults results = this.getStuff(inv);

		boolean doesMatch = (results != null);

		log("CapMergeRecipe doesMatch " + doesMatch);
		log("CapMergeRecipe recipeMatches end");

		return doesMatch;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		final CraftingSearchResults results = this.getStuff(inv);

		if (results != null)
		{
			ItemStack itemstack2 = results.create(CraftingUtil.findWorld(inv));

			log("CapMergeRecipe getCraftingResults result " + itemstack2);

			return itemstack2;
		}
		else
		{
			log("CapMergeRecipe getCraftingResults no results");

			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isDynamic()
	{
		return true;
	}

	/**
	 * Used to determine if this recipe can fit in a grid of the given width/height
	 */
	@Override
	public boolean canFit(int width, int height)
	{
		return (width * height) >= 2;
	}
}
