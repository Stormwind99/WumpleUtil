package com.wumple.util.capability.eventtimed;

import java.util.List;

import com.wumple.util.adapter.IThing;
import com.wumple.util.adapter.TUtil;
import com.wumple.util.capability.thing.ThingCap;
import com.wumple.util.container.ContainerUtil;
import com.wumple.util.misc.CraftingUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;

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
    
    @Override
    abstract public T newT();
    @Override
    abstract public IEventTimedThingCap<W,T> getCap(ICapabilityProvider stack);
    @Override
    abstract public W expired(World world, W stack);
    @Override
    abstract public boolean isEnabled();
    @Override
    abstract public boolean isDebugging();

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
    public long getDate()
    {
        return info.getDate();
    }

    @Override
    public long getTime()
    {
        return info.getTime();
    }
    
    @Override
    public void setDate(long dateIn)
    {
        info.setDate(dateIn);
    }

    @Override
    public void setTime(long timeIn)
    {
        info.setTime(timeIn);
    }
    
    @Override
    public void setExpiration(long dateIn, long timeIn)
    {
        info.set(dateIn, timeIn);
    }

    @Override
    public void reschedule(long timeIn)
    {
        info.reschedule(timeIn);
        forceUpdate();
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

    @Override
    public boolean checkInitialized(World world)
    {
        return info.checkInitialized(world, owner);
    }

    // ----------------------------------------------------------------------
    // Functionality
    
    public void evaluate(World world, Integer index, IItemHandler itemhandler, W stack)
    {
        int count = stack.getCount();
        W newStack = evaluate(world, stack);
        ContainerUtil.checkUpdateSlot(index, itemhandler, stack.as(ItemStack.class), count, newStack.as(ItemStack.class));
    }

    /*
     * Evaluate this timer, which belongs to stack
     */
    @Override
    public W evaluate(World world, W stack)
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
    
    /*
     * Build tooltip info based on this timer
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
    
    @Override
    public void ratioShift(int fromRatio, int toRatio)
    {
        info.ratioShift(fromRatio, toRatio, owner);
        forceUpdate();
    }
    
    /*
     * Set timer on crafted items dependent on the ingredients
     */
    public void handleCraftedTimers(World world, IInventory craftMatrix, ItemStack crafting)
    {
        int slots = craftMatrix.getSizeInventory();
        for (int i = 0; i < slots; i++)
        {
            ItemStack stack = craftMatrix.getStackInSlot(i);

            if (stack == null || stack.isEmpty() || stack.getItem() == null)
            {
                continue;
            }

            @SuppressWarnings("unchecked")
            W thing = (W)TUtil.to(stack);
            IEventTimedThingCap<W,T> cap = getCap(thing);

            if (cap != null)
            {
                copyFrom(cap);
            }
        }
    }
}
