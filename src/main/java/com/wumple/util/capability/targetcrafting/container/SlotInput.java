package com.wumple.util.capability.targetcrafting.container;

import com.wumple.util.crafting.SlotBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotInput extends SlotBase<ContainerCrafting>
{
    public SlotInput(ContainerCrafting containerIn, EntityPlayer playerIn, IInventory inventory, int slotIndex, int xDisplayPosition, int yDisplayPosition)
    {
        super(containerIn, playerIn, inventory, slotIndex, xDisplayPosition, yDisplayPosition);
        container = containerIn;
    }
    
    /**
     * Helper method to put a stack in the slot.
     */
    public void putStack(ItemStack stack)
    {
        super.putStack(stack);
        container.onSlotChanged(this);
    }
}