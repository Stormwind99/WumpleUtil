package com.wumple.util.capability.thing;

import com.wumple.util.adapter.IThing;

abstract public class ThingCap<T extends IThing> implements IThingCap<T>
{
    /*
    // In derived class, do:
    
    // The {@link Capability} instance
    @CapabilityInject(IWebSlinger.class)
    public static final Capability<IWebSlinger> CAPABILITY = null;
    public static final EnumFacing DEFAULT_FACING = null;

    // IDs of the capability
    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "webslinger");
    
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IWebSlinger.class, new WebSlingerStorage(), () -> new WebSlingerCapability());
    }
    */
    
    /// Data
    protected T owner = null;
    
    protected void initialize()
    {
        // on server, setting default waits until later so a World will be present
        // on client, tooltip will init with reasonable guess until update is received from server
        
        // set to first value so stacking will work on client before first update received
        forceUpdate();
    }

    public void checkInit(T ownerIn)
    {
        if (owner != ownerIn)
        {
            owner = ownerIn;

            initialize();
        }
    }

    @Override
    public <X> X getOwnerAs(Class<X> x)
    {
        return (owner != null) ? owner.as(x) : null;
    }
    
    @Override
    public void setOwner(T ownerIn)
    {
    	owner = ownerIn;
    }

    // ----------------------------------------------------------------------
    // Init

    public ThingCap()
    {
    }

    public ThingCap(T ownerIn)
    {
        this();
        checkInit(ownerIn);
    }

    @Override
    public void forceUpdate()
    {
        if (owner != null) { owner.forceUpdate(); }
    }
    
    @Override
    public T getOwner()
    {
        return owner;
    }
}
