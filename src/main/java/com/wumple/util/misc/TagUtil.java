package com.wumple.util.misc;

import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class TagUtil
{
    public static boolean hasTag(ItemStack stack, String tag)
    {
        if (stack.isEmpty() || (stack.getItem() == null)) 
        {
            return false;
        }
        
        ResourceLocation loc = new ResourceLocation(tag);
        return ItemTags.getCollection().getOrCreate(loc).contains(stack.getItem());
    }

    // Originally from https://github.com/MinecraftModDevelopment/Modding-Resources/blob/master/dev_pins.md
    public static boolean tagsMatches(ItemStack stack1, ItemStack stack2)
    {
    	Collection<ResourceLocation> locs1 = ItemTags.getCollection().getOwningTags(stack1.getItem());
    	Collection<ResourceLocation> locs2 = ItemTags.getCollection().getOwningTags(stack2.getItem());
    	
    	return locs1.equals(locs2);
    }
}
