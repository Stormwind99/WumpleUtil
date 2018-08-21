package com.wumple.util.crafting;

import java.lang.reflect.Field;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CraftingUtil
{
    public static boolean isItemBeingCraftedBy(ItemStack stack, Entity entity)
    {
        boolean beingCrafted = false;

        EntityPlayer player = (EntityPlayer) (entity);
        if (player != null)
        {
            if (player.openContainer != null)
            {
                Slot slot = player.openContainer.getSlot(0);
                if ((slot != null) && (slot instanceof SlotCrafting) && (slot.getStack() == stack))
                {
                    beingCrafted = true;
                }
            }
        }

        return beingCrafted;
    }
    
    // adapted from http://www.minecraftforge.net/forum/topic/22927-player-based-crafting-recipes/
    protected static final Field eventHandlerField = ReflectionHelper.findField(InventoryCrafting.class, "eventHandler", "field_70465_c");
    protected static final Field containerPlayerPlayerField = ReflectionHelper.findField(ContainerPlayer.class, "player", "field_82862_h");
    protected static final Field slotCraftingPlayerField = ReflectionHelper.findField(SlotCrafting.class, "player", "field_75238_b");

    public static EntityPlayer findPlayer(InventoryCrafting inv)
    {
        try
        {
            Container container = (Container) eventHandlerField.get(inv);
            if (container instanceof ContainerPlayer)
            {
                return (EntityPlayer) containerPlayerPlayerField.get(container);
            }
            else if (container instanceof ContainerWorkbench)
            {
                return (EntityPlayer) slotCraftingPlayerField.get(container.getSlot(0));
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            //throw e;
            return null;
        }
    }
    
    public static World findWorld(InventoryCrafting inv)
    {
        EntityPlayer player = findPlayer(inv);
        return (player != null) ? player.getEntityWorld() : null;
    }
}
