package com.wumple.util.capability.thing;


import javax.annotation.Nullable;

import com.wumple.util.adapter.IThing;
import com.wumple.util.container.capabilitylistener.SimpleCapabilityProvider;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class ThingCapProvider<T extends IThing, W extends IThingCap<T> > extends SimpleCapabilityProvider<W>
{
    protected T owner = null;

    public ThingCapProvider(Capability<W> capability, @Nullable EnumFacing facing, T ownerIn)
    {
        super(capability, facing, (capability != null) ? capability.getDefaultInstance() : null);
        owner = ownerIn;
    }

    public ThingCapProvider(Capability<W> capability, @Nullable EnumFacing facing, W instance, T ownerIn)
    {
        super(capability, facing, instance);
        owner = ownerIn;
    }

    public W getInstance()
    {
        W cap = super.getInstance();
        cap.checkInit(owner);
        return cap;
    }
}