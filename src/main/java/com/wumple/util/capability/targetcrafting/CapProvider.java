package com.wumple.util.capability.targetcrafting;

import javax.annotation.Nullable;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.thing.ThingCapProvider;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class CapProvider<U extends IContainerCraftingOwner> extends ThingCapProvider<IThing, U>
{
    public CapProvider(Capability<U> capability, @Nullable EnumFacing facing, IThing ownerIn)
    {
        super(capability, facing, (capability != null) ? capability.getDefaultInstance() : null, ownerIn);
    }

    public CapProvider(Capability<U> capability, @Nullable EnumFacing facing, U instance,
            IThing ownerIn)
    {
        super(capability, facing, instance, ownerIn);
    }

    /*
    public static CapProvider<T> createProvider(IThing ownerIn)
    {
        return new CapProvider(IPantographCap.CAPABILITY, IPantographCap.DEFAULT_FACING, ownerIn);
    }
    */
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return ( (capability != null) && (
                (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) ||
                super.hasCapability(capability, facing) )
                ) ;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return (T)getInstance().handler();
        }
        if (hasCapability(capability, facing))
        {
            return (T)getCapability().cast(getInstance());
        }

        return null;
    }
}
