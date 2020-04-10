package com.wumple.util.capability.timerrefreshing;

import com.wumple.util.adapter.IThing;
import com.wumple.util.adapter.TUtil;
import com.wumple.util.capability.eventtimed.Expiration;
import com.wumple.util.capability.eventtimed.IEventTimedThingCap;
import com.wumple.util.capability.tickingthing.TickingThingCap;
import com.wumple.util.container.Walker;
import com.wumple.util.misc.ItemStackUtil;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

abstract public class TimerRefreshingCap<T extends IThing, W extends Expiration, X extends IThing> extends TickingThingCap<T> implements ITimerRefreshingCap<T, W>
{
    /*
    // The {@link Capability} instance
    @CapabilityInject(IPreserving.class)
    public static final Capability<IPreserving> CAPABILITY = null;
    public static final EnumFacing DEFAULT_FACING = null;

    // IDs of the capability
    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "preserving");
    
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IPreserving.class, new PreservingStorage(), () -> new Preserving());
    }
    */
    
    abstract protected LazyOptional< ? extends IEventTimedThingCap<X,W> > getTimedCap(ICapabilityProvider stack);
    protected LazyOptional< ? extends IEventTimedThingCap<X,W> > getTimedCap(ItemStack stack)
    {
        return getTimedCap(TUtil.to(stack));
    }

    // transient data
    // ticks since last refresh of contents - special value 0 means need to cache preserving settings
    protected int refreshingRatio = 0;

    // ----------------------------------------------------------------------
    // Init

    public TimerRefreshingCap()
    {
        super();
        // super does MinecraftForge.EVENT_BUS.register(this);
    }

    public TimerRefreshingCap(T ownerIn)
    {
        super(ownerIn);
        // super does MinecraftForge.EVENT_BUS.register(this);
    }
    
    // ----------------------------------------------------------------------
    // Main

    @Override
    public int getRatio()
    {
        return refreshingRatio;
    }

    @Override
    protected void doIt(long timeSinceLast)
    {
        // adjust for preserving ratio
        long expTime = getExpirationTime(timeSinceLast);

        LazyOptional<IItemHandler> capability = owner.fetchCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        capability.ifPresent( (cap)->{ freshenTheseContents(cap, expTime); } );
    }
    
    // ----------------------------------------------------------------------
    // Internal

    protected void freshenTheseContents(IItemHandler inventory, long time)
    {
        Walker.walkContainer(inventory, (index, itemhandler, stack) -> {
            freshenStack(index, itemhandler, stack, time);
        });

        owner.markDirty();
    }

    protected boolean freshenStack(int index, IItemHandler itemhandler, ItemStack stack, long time)
    {
        LazyOptional< ? extends IEventTimedThingCap<X,W> > lcap = (!ItemStackUtil.isEmpty(stack)) ? getTimedCap(stack) : null;

        return lcap.map(cap->{ return rescheduleAndCheck(cap, index, itemhandler, stack, time); }).orElse(false);
    }
    
    @SuppressWarnings("unchecked")
    protected boolean rescheduleAndCheck(IEventTimedThingCap<X,W> cap, int index, IItemHandler itemhandler, ItemStack stack, long time)
    {
        assert (cap != null);
        
        cap.reschedule(time);
        
        // we're here, might as well see if reschedule caused expiration
        cap.evaluate(owner.getWorld(), index, itemhandler, (X)TUtil.to(stack));
        
        return true;
    }

    /**
     * Automatically adjust the expiration start date on items stored within the chest so don't expire
     */
    protected long getExpirationTime(long time)
    {
        return (time * refreshingRatio) / 100;
    }
}
