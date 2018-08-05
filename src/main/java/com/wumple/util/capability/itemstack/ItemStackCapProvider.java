package com.wumple.util.capability.itemstack;

import javax.annotation.Nullable;

import com.wumple.util.container.capabilitylistener.SimpleCapabilityProvider;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class ItemStackCapProvider<T extends IItemStackCap> extends SimpleCapabilityProvider<T>
{
    ItemStack owner = null;

    public ItemStackCapProvider(Capability<T> capability, @Nullable EnumFacing facing, ItemStack stack)
    {
        super(capability, facing, (capability != null) ? capability.getDefaultInstance() : null);
        owner = stack;
    }

    public ItemStackCapProvider(Capability<T> capability, @Nullable EnumFacing facing, T instance, ItemStack stack)
    {
        super(capability, facing, instance);
        owner = stack;
    }

    @Override
    public T getInstance()
    {
        T cap = super.getInstance();
        cap.setOwner(owner);
        return cap;
    }
}