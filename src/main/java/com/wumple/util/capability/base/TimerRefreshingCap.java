package com.wumple.util.capability.base;

import com.wumple.util.adapter.IThing;
import com.wumple.util.container.Walker;
import com.wumple.util.misc.SUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber
abstract public class TimerRefreshingCap<T extends IThing, W extends Expiration> extends TickingThingCap<T>
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

    // transient data
    // ticks since last refresh of contents - special value 0 means need to cache preserving settings
    protected int refreshingRatio = 0;

    // ----------------------------------------------------------------------
    // Init

    TimerRefreshingCap()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    TimerRefreshingCap(T ownerIn)
    {
        this();
        owner = ownerIn;
    }

    public int getRatio()
    {
        return refreshingRatio;
    }

    /*
    @Override
    protected void cache()
    {
        Integer ratio = owner.getPreservingProperty();
        // at this point ratio should not be null - probably a bug, maybe throw exception
        preservingRatio = (ratio != null) ? ratio.intValue() : ConfigHandler.NO_PRESERVING;
    }
     */

    protected void doIt(long timeSinceLast)
    {
        // adjust for preserving ratio
        long expTime = getExpirationTime(timeSinceLast);

        IItemHandler capability = owner.fetchCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        freshenTheseContents(capability, expTime);
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
    
    abstract protected IEventTimedItemStackCap<W> getCap(ItemStack stack);
    /*
     * return RotCapHelper.getRot(stack)
     */

    protected boolean freshenStack(int index, IItemHandler itemhandler, ItemStack stack, long time)
    {
        IEventTimedItemStackCap<W> cap = (!SUtil.isEmpty(stack)) ? getCap(stack) : null;

        return (cap != null) ? rescheduleAndCheck(cap, index, itemhandler, stack, time) : false;
    }
    
    protected boolean rescheduleAndCheck(IEventTimedItemStackCap<W> cap, int index, IItemHandler itemhandler, ItemStack stack, long time)
    {
        cap.reschedule(time);
        
        // we're here, might as well see if reschedule caused expirationt
        // TODO could check timer
        //    RotHandler.evaluateRot(owner.getWorld(), cap, index, itemhandler, stack);
        
        return true;
    }

    /**
     * Automatically adjust the expiration start date on items stored within the chest so don't expire
     */
    protected long getExpirationTime(long time)
    {
        return (time * refreshingRatio) / 100;
    }

    protected void handleOnTick(World world)
    {
        if (owner != null)
        {
            if (owner.isInvalid())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                owner.invalidate();
                owner = null;
            }
            else
            {
                boolean freshen = updateAndCache();
                if (freshen)
                {
                    evaluate();
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // Event Handlers

    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent event)
    {
        handleOnTick(event.world);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        World world = Minecraft.getMinecraft().world;
        if ((world != null) && (world.isRemote == true))
        {
            handleOnTick(world);
        }
    }
}