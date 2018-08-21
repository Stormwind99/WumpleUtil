package com.wumple.util.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Redirectors for backwards compatibility
 */
public class CraftingUtil
{
    public static boolean isItemBeingCraftedBy(ItemStack stack, Entity entity)
    {
        return com.wumple.util.crafting.CraftingUtil.isItemBeingCraftedBy(stack, entity);
    }
    
    public static EntityPlayer findPlayer(InventoryCrafting inv)
    {
        return com.wumple.util.crafting.CraftingUtil.findPlayer(inv);
    }
    
    public static World findWorld(InventoryCrafting inv)
    {
        return com.wumple.util.crafting.CraftingUtil.findWorld(inv);
    }
}
