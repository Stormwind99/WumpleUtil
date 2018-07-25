package com.wumple.util.capability.base;

import java.util.List;

import com.wumple.util.misc.CraftingUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

abstract public class EventTimedItemStackCap<T extends Expiration> extends ItemStackCap implements IEventTimedItemStackCap<T>
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

    public EventTimedItemStackCap()
    {
        super();
        info = newT();
    }

    public EventTimedItemStackCap(EventTimedItemStackCap<T> other)
    {
        super(other);
        info = other.info;
    }
    
    abstract protected T newT();

    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#getDate()
     */
    @Override
    public long getDate()
    {
        return info.getDate();
    }

    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#getTime()
     */
    @Override
    public long getTime()
    {
        return info.getTime();
    }
    
    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#setDate(long)
     */
    @Override
    public void setDate(long dateIn)
    {
        info.setDate(dateIn);
    }

    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#setTime(long)
     */
    @Override
    public void setTime(long timeIn)
    {
        info.setTime(timeIn);
    }
    
    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#setExpiration(long, long)
     */
    @Override
    public void setExpiration(long dateIn, long timeIn)
    {
        info.set(dateIn, timeIn);
    }

    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#reschedule(long)
     */
    @Override
    public void reschedule(long timeIn)
    {
        info.reschedule(timeIn);
        forceUpdate();
    }

    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#setInfo(T)
     */
    @Override
    public T setInfo(T infoIn)
    {
        info = infoIn;
        return info;
    }

    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#getInfo()
     */
    @Override
    public T getInfo()
    {
        return info;
    }

    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#checkInitialized(net.minecraft.world.World)
     */
    @Override
    public boolean checkInitialized(World world)
    {
        return info.checkInitialized(world, owner);
    }

    // ----------------------------------------------------------------------
    // Functionality
    
    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#expired(net.minecraft.world.World, net.minecraft.item.ItemStack)
     */
    @Override
    abstract public ItemStack expired(World world, ItemStack stack);
    
    /*
    public ItemStack expired(World world, ItemStack stack)
    {
        RotProperty rotProps = ConfigHandler.rotting.getRotProperty(stack);
        // forget owner to eliminate dependency
        owner = null;
        return (rotProps != null) ? rotProps.forceRot(stack) : null;
    }
    */

    /*
     * Evaluate this timer, which belongs to stack
     */
    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#evaluate(net.minecraft.world.World, net.minecraft.item.ItemStack)
     */
    @Override
    public ItemStack evaluate(World world, ItemStack stack)
    {
        if (!info.checkInitialized(world, stack))
        {
            forceUpdate();
        }

        if (!info.isNonExpiring())
        {
            if (info.hasExpired())
            {
                return expired(world, stack);
            }
        }

        return stack;
    }
    
    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#isEnabled()
     */
    @Override
    abstract public boolean isEnabled();
    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#isDebugging()
     */
    @Override
    abstract public boolean isDebugging();
    
    /*
    public boolean isEnabled()
    {
        return ConfigContainer.enabled;
    }
    
    public boolean isDebugging()
    {
        return ConfigContainer.zdebugging.debug;
    }
     */

    /*
     * Build tooltip info based on this timer
     */
    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#doTooltip(net.minecraft.item.ItemStack, net.minecraft.entity.player.EntityPlayer, boolean, java.util.List)
     */
    @Override
    public void doTooltip(ItemStack stack, EntityPlayer entity, boolean advanced, List<String> tips)
    {
        if (isEnabled() && (stack != null) && !stack.isEmpty() && (entity != null))
        {
            if (info != null)
            {
                World world = entity.getEntityWorld();
                        
                // if not initialized, set with reasonable guess to be overwritten by server update
                checkInitialized(world);
                
                // timer state
                boolean beingCrafted = CraftingUtil.isItemBeingCraftedBy(stack, entity);
                String key = getStateTooltipKey(info, beingCrafted);
                
                if (key != null)
                {
                    tips.add(new TextComponentTranslation(key, info.getPercent() + "%", info.getDaysLeft(),
                            info.getDaysTotal()).getUnformattedText());
                }

                // advanced tooltip debug info
                if (advanced && isDebugging())
                {
                    tips.add(new TextComponentTranslation("misc.foodfunk.tooltip.advanced.datetime", info.getDate(),
                            info.getTime()).getUnformattedText());
                    tips.add(new TextComponentTranslation("misc.foodfunk.tooltip.advanced.expire", info.getCurTime(),
                            info.getExpirationTimestamp()).getUnformattedText());

                    int dimension = world.provider.getDimension();
                    int dimensionRatio = info.getDimensionRatio(world);
                    tips.add(new TextComponentTranslation("misc.foodfunk.tooltip.advanced.dimratio", dimensionRatio, dimension).getUnformattedText());
                }
            }
        }
    }

    protected String getStateTooltipKey(T local, boolean beingCrafted)
    {
        String key = null;

        if (local.isNonExpiring())
        {
            key = "misc.wumple.util.tooltip.timer.nonexpiring";
        }
        else if (local.isSet() && !beingCrafted)
        {
            if (local.getPercent() >= 100)
            {
                key = "misc.wumple.util.tooltip.timer.expired";
            }
            else
            {
                key = "misc.wumple.util.tooltip.timer.timer";
            }
        }        
        else if (local.time > 0)
        {
            key = "misc.wumple.util.tooltip.timer.crafting";
        }

        return key;
    }
    
    /* (non-Javadoc)
     * @see com.wumple.util.capability.base.IEventTimedItemStackCap#ratioShift(int, int)
     */
    @Override
    public void ratioShift(int fromRatio, int toRatio)
    {
        info.ratioShift(fromRatio, toRatio, owner);
        forceUpdate();
    }
}
