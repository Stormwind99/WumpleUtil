package com.wumple.util.capability.base;

import net.minecraft.item.ItemStack;

public interface IItemStackCap
{

    byte getForceId();

    void setForceId(byte newid);

    void forceUpdate();

    /*
     * Set the owner of this capability, and init based on that owner
     */
    void setOwner(ItemStack ownerIn);

    ItemStack getOwner();

}