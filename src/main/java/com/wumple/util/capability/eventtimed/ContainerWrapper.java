package com.wumple.util.capability.eventtimed;

import net.minecraft.inventory.Container;
import net.minecraftforge.items.ItemStackHandler;

public class ContainerWrapper extends ItemStackHandler
{
    protected Container container;
    
    public ContainerWrapper(Container containerIn)
    {
        super(containerIn.inventoryItemStacks);
        container = containerIn;
    }

    protected void detectAndSendChanges()
    {
        container.detectAndSendChanges();
    }    
}
