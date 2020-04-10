package com.wumple.util.capability.tickingthing;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.thing.ThingCap;
import com.wumple.util.misc.TimeStampUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

abstract public class TickingThingCap<T extends IThing> extends ThingCap<T> implements ITickingThingCap<T>
{
    /*
    // In derived class, do:
     
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
    
    abstract protected void cache(); 
    abstract protected long getEvaluationInterval();
    abstract protected void doIt(long timeSinceLast);
    
    // transient data
    // ticks since last evaluation - special value 0 means need to cache any settings
    protected int tick = 0;
    
    // persisted data
    protected long lastCheckTime = TIME_NOT_SET;

    // ----------------------------------------------------------------------
    // Init
    
    public TickingThingCap()
    {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public TickingThingCap(T ownerIn)
    {
        super(ownerIn);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public long getLastCheckTime()
    {
        return lastCheckTime;
    }

    @Override
    public void setLastCheckTime(long time)
    {
        lastCheckTime = time;
    }

    /*
     * Set the owner of this capability, and init based on that owner
     */
    @Override
    public void checkInit(T ownerIn)
    {
        if (!ownerIn.sameAs(owner))
        {
            owner = ownerIn;
            lastCheckTime = TimeStampUtil.getLastWorldTimestamp();
            initialize();
        }
    }
        
    protected boolean isTimeToEvaluate()
    {
        return (tick >= getEvaluationInterval());
    }

    /**
     * Tick counters, cache data, etc
     * 
     * @return boolean should we evaluate this tick?
     */
    protected boolean updateAndCache()
    {
        // tick of 0 represents "cache any transient data" like preserving ratio
        if (tick == 0)
        {
            cache();
        }

        if (!isTimeToEvaluate())
        {
            tick++;
            return false;
        }

        // reset to 1 since 0 is special "cache any transient data" state
        tick = 1;
        return true;
    }
    
    @Override
    public void always()
    {
    }

    @Override
    public void evaluate()
    {
        // only freshen on server, and rely on cap data being sent to clients
        if ((owner.getWorld() == null) || owner.getWorld().isRemote)
        {
            return;
        }

        long worldTime = TimeStampUtil.getLastWorldTimestamp();

        if (lastCheckTime <= TIME_NOT_SET)
        {
            lastCheckTime = worldTime;
        }

        long timeSinceLast = worldTime - lastCheckTime;
        lastCheckTime = worldTime;

        doIt(timeSinceLast);
    }
    
    public void invalidate()
    {
        MinecraftForge.EVENT_BUS.unregister(this);
        if (owner != null)
        {
            owner.invalidate();
            owner = null;
        }        
    }
    
    public void invalidateCap()
    {
        MinecraftForge.EVENT_BUS.unregister(this);
        if (owner != null)
        {
        	owner = null;
        }
    }
    
    /**
     * Automatically adjust the use-by date on food items stored within the chest so don't rot
     */
    protected void handleOnTick(World world)
    {
        if (owner != null)
        {
            if (owner.isInvalid())
            {
                invalidate();
            }
            else 
            {
                boolean eval = updateAndCache();
                always();
                if (eval)
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
    @OnlyIn(Dist.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        World world = Minecraft.getInstance().world;
        if ((world != null) && (world.isRemote == true))
        {
            handleOnTick(world);
        }
    }
}
