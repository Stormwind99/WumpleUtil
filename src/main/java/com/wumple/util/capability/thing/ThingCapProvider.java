package com.wumple.util.capability.thing;


import javax.annotation.Nullable;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.listener.SimpleCapabilityProvider;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class ThingCapProvider<T extends IThing, W extends IThingCap<T> > extends SimpleCapabilityProvider<W>
{
    protected T owner = null;

    public ThingCapProvider(Capability<W> capability, @Nullable Direction facing, T ownerIn)
    {
        super(capability, facing, (capability != null) ? capability.getDefaultInstance() : null);
        owner = ownerIn;
    }

    public ThingCapProvider(Capability<W> capability, @Nullable Direction facing, W instance, T ownerIn)
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