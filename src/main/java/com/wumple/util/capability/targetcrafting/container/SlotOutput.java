package com.wumple.util.capability.targetcrafting.container;

import com.wumple.util.crafting.SlotBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotOutput extends SlotBase<ContainerCrafting>
{
    public SlotOutput(ContainerCrafting containerIn, EntityPlayer playerIn, IInventory inventory, int slotIndex, int xDisplayPosition, int yDisplayPosition)
    {
        super(containerIn, playerIn, inventory, slotIndex, xDisplayPosition, yDisplayPosition);
        container = containerIn;
    }
    
    @Override
    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack)
    {
        onCrafting(stack);
      
        container.onSlotChanged(this);

        return super.onTake(thePlayer, stack);
    }
}