package com.wumple.util.config;

import java.util.ArrayList;

import com.wumple.util.misc.TypeIdentifier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class MatchingConfigBase
{
    // special tags for backwards compatibility
    public static final String FOOD_TAG = "minecraft:food";
    public static final String PLAYER_TAG = "entity:player";
    public static final String SPIDER_TAG = "entity:spider";
    
    // ----------------------------------------------------------------------
    // Utility
    
    /**
     * @see TypeIdentifier for opposite direction but similiar code
     * @param itemStack for which to get namekeys for lookup
     * @return namekeys to search config for, in order
     */
    static public ArrayList<String> getItemStackNameKeys(ItemStack itemStack)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        if (itemStack == null)
        {
            return nameKeys;
        }

        Item item = itemStack.getItem();
        
        addNameKeys(nameKeys, itemStack);
        addNameKeys(nameKeys, (Object)itemStack);
        addNameKeys(nameKeys, (Object)item);
        
        return nameKeys;
    }

    static public ArrayList<String> getEntityNameKeys(Entity entity)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        if (entity == null)
        {
            return nameKeys;
        }

        addNameKeys(nameKeys, entity);
        addNameKeys(nameKeys, (Object)entity);
        
        return nameKeys;
    }

    static public ArrayList<String> addNameKeys(ArrayList<String> nameKeys, Entity entity)
    {
        String name = (entity == null) ? null : EntityList.getEntityString(entity);

        if (name != null)
        {
            nameKeys.add(name);
        }
        
        return nameKeys;
    }
    
    static public ArrayList<String> addNameKeys(ArrayList<String> nameKeys, ItemStack itemStack)
    {
        Item item = itemStack.getItem();

        ResourceLocation loc = Item.REGISTRY.getNameForObject(item);
        
        if (loc != null)
        {
            String key2 = loc.toString();

            nameKeys.add(key2 + "@" + itemStack.getMetadata());
            nameKeys.add(key2);
        }

        if (!itemStack.isEmpty())
        {
            int oreIds[] = OreDictionary.getOreIDs(itemStack);
            for (int oreId : oreIds)
            {
                nameKeys.add(OreDictionary.getOreName(oreId));
            }
        }
        
        return nameKeys;
    }
    
    static public ArrayList<String> addNameKeys(ArrayList<String> nameKeys, Object object)
    {   
        // class names for dynamic matching
        Class<?> c = object.getClass();
        while (c != null)
        {
            String classname = c.getName();
            nameKeys.add(classname);
            c = c.getSuperclass();
        }
        
        // special tags for backwards compatibility 
        if (object instanceof ItemFood)
        {
            nameKeys.add(FOOD_TAG);
        }
        
        if (object instanceof EntityPlayer)
        {
            nameKeys.add(PLAYER_TAG);
        }
        
        if (object instanceof EntitySpider)
        {
            nameKeys.add(SPIDER_TAG);
        }
        
        return nameKeys;
    }
}
