package com.wumple.util.capability.copier;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.wumple.util.Reference;
import com.wumple.util.misc.CraftingUtil;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * RecipeFactory to merge different itemstacks into one itemstack for a cap T
 */
public abstract class CapMergeRecipeFactory<T extends ICopyableCap<?> > implements IRecipeFactory
{   
    abstract public CapMergeRecipe newCapMergeRecipe(ResourceLocation group, @Nonnull ItemStack result, Object... recipe);
    public ResourceLocation getResourceLocation() { return new ResourceLocation(Reference.MOD_ID, "cap_merge_crafting"); }
    
    /**
     * hook for JSON to be able to use this recipe
     * 
     * @see _factories.json
     * @see filled_map_transcribe.json
     */
    @Override
    public IRecipe parse(JsonContext context, JsonObject json)
    {
        // TODO get rid of using ShapelessOreRecipe.factory to parse JSON
        ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);

        return newCapMergeRecipe(getResourceLocation(), recipe.getRecipeOutput());
    }

    /**
     * The actual recipe to merge different itemstacks into one itemstack for a cap T
     */
    public abstract class CapMergeRecipe extends ShapelessOreRecipe
    {
        abstract protected T getCap(ICapabilityProvider provider);

        public CapMergeRecipe(ResourceLocation group, @Nonnull ItemStack result, Object... recipe)
        {
            super(group, result, recipe);
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
                
                ItemStack newStack = new ItemStack(exampleItem, newCount, exampleStack.getMetadata());

                T cap = getCap(newStack);
                if (cap != null)
                {
                    cap.copyFromProviders(stacks, world);
                }
                                
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
        }

        /**
         * return the items involved in this recipe, or null if not present
         * 
         * @param inv
         *            inventory to check for tems
         * @return dest and src items found, or null if not found
         */
        protected CraftingSearchResults getStuff(InventoryCrafting inv)
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
                    T cap = getCap(itemstack1);
                    
                    if (cap != null)
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
                        if (cap != null)
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
        public boolean matches(InventoryCrafting inv, World worldIn)
        {
            log("CapMergeRecipe recipeMatches begin");
            final CraftingSearchResults results = this.getStuff(inv);

            boolean doesMatch = (results != null);

            log("CapMergeRecipe doesMatch " + doesMatch);
            log("CapMergeRecipe recipeMatches end");

            return doesMatch;
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inv)
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
}