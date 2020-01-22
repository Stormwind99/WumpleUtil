package com.wumple.util.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GuiUtil
{
    /*
     * Is the gui slot under the mouse holding the given itemstack?
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean isOpenContainerSlotUnderMouse(ItemStack stack)
    {
        Screen guiscreen = Minecraft.getInstance().currentScreen;
        if (guiscreen instanceof ContainerScreen<?>)
        {
            ContainerScreen<?> guichest = (ContainerScreen<?>)guiscreen;
            Slot slot = (guichest != null) ? guichest.getSlotUnderMouse() : null;
            // does the slot hold the hinted stack?
            // and is the slot not a player inventory slot (if inventory not open)?
            // if inventory open, then:
            //    guiscreen.inventorySlots instanceof ContainerPlayer
        	//    slot.inventory instanceof InventoryPlayer
            if ((slot != null) && (slot.getStack() == stack) && 
            		(
            				!(slot.inventory instanceof PlayerInventory) // ||
            				// ((guichest.inventorySlots instanceof ContainerPlayer) && (slot.inventory instanceof InventoryPlayer))
            		) )
            {
                return true;
            }
        }
        
        return false;        
    }
}