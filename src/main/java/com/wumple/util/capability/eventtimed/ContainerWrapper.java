package com.wumple.util.capability.eventtimed;

import net.minecraft.inventory.container.Container;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemStackHandler;

public class ContainerWrapper extends ItemStackHandler
{
    protected Container container;
    
    public ContainerWrapper(Container containerIn)
    {
    	super( ObfuscationReflectionHelper.getPrivateValue(Container.class, containerIn, "field_75153_a") ) ; // "inventoryItemStacks"
        container = containerIn;
    }

    protected void detectAndSendChanges()
    {
        container.detectAndSendChanges();
    }    
}
