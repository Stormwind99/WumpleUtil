package com.wumple.util.container;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerUtil
{
    public static void checkUpdateSlot(Integer index, IItemHandler itemhandler, ItemStack stack, int count, ItemStack newStack)
    {
        if (newStack == null || newStack.isEmpty() || (newStack != stack))
        {
            if (newStack == null)
            {
                newStack = ItemStack.EMPTY;
            }
            // Equivalent to inventory.setInventorySlotContents(i, rotItem);
            @SuppressWarnings("unused")
            ItemStack oldStack = itemhandler.extractItem(index, count, false);
            itemhandler.insertItem(index, newStack, false);
        }
    }
}
