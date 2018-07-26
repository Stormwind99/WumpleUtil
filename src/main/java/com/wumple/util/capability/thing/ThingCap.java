package com.wumple.util.capability.thing;

import com.wumple.util.adapter.EntityThing;
import com.wumple.util.adapter.IThing;
import com.wumple.util.misc.Util;

import net.minecraft.entity.EntityLiving;

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
    public EntityLiving getOwner()
    {
        if (owner instanceof EntityThing)
        {
            EntityThing thing = Util.as(owner, EntityThing.class);
            EntityLiving living = Util.as(thing.owner, EntityLiving.class);
            return living;
        }

        return null;
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
}
