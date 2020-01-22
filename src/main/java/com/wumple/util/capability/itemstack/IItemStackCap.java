package com.wumple.util.capability.itemstack;

import net.minecraft.item.ItemStack;

public interface IItemStackCap
{
    void forceUpdate();

    /*
     * Set the owner of this capability, and init based on that owner
     */
    void setOwner(ItemStack ownerIn);

    ItemStack getOwner();
}