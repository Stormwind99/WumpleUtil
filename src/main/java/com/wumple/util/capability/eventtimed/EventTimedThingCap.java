package com.wumple.util.capability.eventtimed;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.thing.ThingCap;

abstract public class EventTimedThingCap<W extends IThing, T extends Expiration> extends ThingCap<W> implements IEventTimedThingCap<W,T>
{
    /*
    // in derived class, do:
    
    // The {@link Capability} instance
    @CapabilityInject(IRot.class)
    public static final Capability<IRot> CAPABILITY = null;
    public static final EnumFacing DEFAULT_FACING = null;

    // IDs of the capability
    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "rot");
    
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IRot.class, new RotStorage(), () -> new Rot());

        CapabilityContainerListenerManager.registerListenerFactory(ContainerListenerRot::new);
    }
    */
    
    // info holds the expiration data (composition due to cap network serialization classes)
    protected T info;

    public EventTimedThingCap()
    {
        super();
        info = newT();
    }

    public EventTimedThingCap(EventTimedThingCap<W,T> other)
    {
        super();
        info = other.info;
    }
    
    @Override
    public T setInfo(T infoIn)
    {
        info = infoIn;
        return info;
    }

    @Override
    public T getInfo()
    {
        return info;
    }
}
