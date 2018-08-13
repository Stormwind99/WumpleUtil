package com.wumple.util.capability.eventtimed;

import java.util.List;

import com.wumple.util.ModConfig;
import com.wumple.util.WumpleUtil;
import com.wumple.util.adapter.IThing;
import com.wumple.util.adapter.TUtil;
import com.wumple.util.capability.copier.ICopyableCap;
import com.wumple.util.capability.thing.IThingCap;
import com.wumple.util.container.misc.ContainerUtil;
import com.wumple.util.misc.CraftingUtil;
import com.wumple.util.tooltip.ITooltipProvider;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;

public interface IEventTimedThingCap<W extends IThing, T extends Expiration> extends IThingCap<W>, ICopyableCap< IEventTimedThingCap<W,T> >, ITooltipProvider
{
    public T newT();
    public IEventTimedThingCap<W,T> getCap(ICapabilityProvider stack);

    T setInfo(T infoIn);

    T getInfo();

    W expired(World world, W stack);

    boolean isEnabled();

    boolean isDebugging();

    default long getDate()
    {
        return getInfo().getDate();
    }

    default long getTime()
    {
        return getInfo().getTime();
    }
    
    default void setDate(long dateIn)
    {
        getInfo().setDate(dateIn);
    }

    default void setTime(long timeIn)
    {
        getInfo().setTime(timeIn);
    }
    
    default void setExpiration(long dateIn, long timeIn)
    {
        getInfo().set(dateIn, timeIn);
    }
    
    default long getExpirationTimestamp()
    {
        return getInfo().getExpirationTimestamp();
    }

    default boolean isExpirationTimestampSet()
    {
        return getInfo().isSet();
    }
    
    default boolean isNonExpiring()
    {
        return getInfo().isNonExpiring();
    }

    default void reschedule(long timeIn)
    {
        getInfo().reschedule(timeIn);
        forceUpdate();
    }

    default boolean checkInitialized(World world)
    {
        boolean initedAlready = getInfo().checkInitialized(world, getOwner());
        if (!initedAlready) { forceUpdate(); }
        return initedAlready;
    }

    // ----------------------------------------------------------------------
    // Functionality
    
    /*
    @Override
    default ItemStack check(World world, ItemStack stack)
    {
        T info = getInfo();
        info.checkInitialized(world, TUtil.to(stack));
        return stack;
    }

    @Override
    default <I extends IThing> I check(World world, I before)
    {
        return before;
    }
    
    @Override
    default IThing check(World world, ICapabilityProvider stack)
    {
        IEventTimedThingCap<W,T> cap = getCap(stack);
        cap.checkInitialized(world);
        return cap.getOwner();
    }
    
    @Override
    default IThing check(World world, IThing stack)
    {
        IEventTimedThingCap<W,T> cap = getCap(stack);
        cap.checkInitialized(world);
        return cap.getOwner();
    }
    */

    @Override
    default ItemStack check(World world, ItemStack stack)
    {
        IEventTimedThingCap<W,T> cap = getCap(stack);
        cap.checkInitialized(world);
        return cap.getOwner().as(ItemStack.class);
    }
    
    /*
     * Evaluate this timer, which belongs to stack
     */
    default W evaluate(World world, W stack)
    {
        T info = getInfo();
        
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
    
    default void ratioShift(int fromRatio, int toRatio)
    {
        getInfo().ratioShift(fromRatio, toRatio, getOwner());
        forceUpdate();
    }
        
    @Override
    default void copyFrom(IEventTimedThingCap<W, T> other)
    {
        // Avoid cheating from crafting or break rotting items
        
        // For example: 
        // Melon 0/14 days -> Slices 0/7 days -> Melon 0/7 days or 7/14 days
        // Melon 8/14 days -> Slices 6/7 days -> Melon 6/7 days or 13/14 days
        // Crafting: Ingredients 1/7, 2/7, 3/7 days -> Results 3/7 days
        // Crafting: Ingredients 1/7, 2/7, 3/7 days -> Results 10/14 days
        // Crafting: Ingredients 5/14, 6/14 -> Results 0/7 days
        // Crafting: Ingredients 6/14, 9/14 -> Results 2/7 days
        
        if (ModConfig.zdebugging.debug) { WumpleUtil.logger.info("copyFrom: other " + other + " this " + this); }
        
        if (!this.isExpirationTimestampSet())
        {
            if (other.isExpirationTimestampSet())
            {
                if (ModConfig.zdebugging.debug) { WumpleUtil.logger.info("copyFrom: uninit this, copying " + other.getDate() + " " + other.getTime()); }
                setExpiration(other.getDate(), other.getTime());
                forceUpdate();               
            }
            else
            {
                // handle uninitialized src or dest
                // should never happen - but for now just skip this operation
                if (ModConfig.zdebugging.debug) { WumpleUtil.logger.info("copyFrom: skipping uninit other " + other.isExpirationTimestampSet() + " this " + this.isExpirationTimestampSet()); }
                return;  
            }
        }
        
        // handle dimension-related state:
        // if other.nonExpiring && info.nonExpiring, do nothing
        // if other.nonExpiring && !info.nonExpiring, do nothing
        // if !other.nonExp && info.nonExpiring, do nothing
        // if !other.nonExp && !info.nonExpiring, do below
        
        if (!this.isNonExpiring())
        {
            long d_o = other.getDate();
            long t_o = other.getTime();
            long e_o = d_o + t_o; // aka other.getExpirationTimestamp();
            long d_i = this.getDate();
            long t_i = this.getTime();
            long e_i = d_i + t_i; // aka info.getExpirationTimestamp();

            long new_d_i = d_i;
            if (e_i > e_o)
            {
                // clamp dest expiration timestamp at src expiration timestamp by moving destination date backwards
                new_d_i = e_o - t_i;
            }
            
            if (ModConfig.zdebugging.debug) { WumpleUtil.logger.info("copyFrom: setting"
                    + " new_d_i " + new_d_i
                    + " d_o " + d_o
                    + " t_o " + t_o
                    + " e_o " + e_o
                    + " d_i " + d_i
                    + " t_i " + t_i
                    + " e_i " + e_i
                    ); }
            
            setExpiration(new_d_i, t_i);
            forceUpdate();
        }
        else
        {
            if (ModConfig.zdebugging.debug) { WumpleUtil.logger.info("copyFrom: skipping this isNotExpiring"); }
        }
    }
    
    default void evaluate(World world, Integer index, IItemHandler itemhandler, W stack)
    {
        int count = stack.getCount();
        W newStack = evaluate(world, stack);
        ContainerUtil.checkUpdateSlot(index, itemhandler, stack.as(ItemStack.class), count, newStack.as(ItemStack.class));
    }

    /*
     * Set timer on crafted items dependent on the ingredients
     */
    default void handleCraftedTimers(World world, IInventory craftMatrix, ItemStack crafting)
    {
        checkInitialized(world);
        
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
    
    /*
     * Build tooltip info based on this timer
     */
    default void doTooltip(ItemStack stack, EntityPlayer entity, boolean advanced, List<String> tips)
    {
        if (isEnabled() && (entity != null))
        {
            T myinfo = getInfo();
            
            if (myinfo != null)
            {
                World world = entity.getEntityWorld();
                        
                // if not initialized, set with reasonable guess to be overwritten by server update
                checkInitialized(world);
                
                // Rot state
                boolean beingCrafted = (stack != null) ? CraftingUtil.isItemBeingCraftedBy(stack, entity) : false;
                String key = getStateTooltipKey(myinfo, beingCrafted);

                if (key != null)
                {
                    tips.add(new TextComponentTranslation(key, myinfo.getPercent() + "%", myinfo.getDaysLeft(),
                            myinfo.getDaysTotal()).getUnformattedText());
                }

                // advanced tooltip debug info
                if (advanced && isDebugging())
                {
                    tips.add(new TextComponentTranslation("misc.wumpleutil.tooltip.advanced.datetime", myinfo.getDate(),
                            myinfo.getTime()).getUnformattedText());
                    tips.add(new TextComponentTranslation("misc.wumpleutil.tooltip.advanced.expire", myinfo.getCurTime(),
                            myinfo.getExpirationTimestamp()).getUnformattedText());

                    int dimension = world.provider.getDimension();
                    int dimensionRatio = myinfo.getDimensionRatio(world);
                    tips.add(new TextComponentTranslation("misc.wumpleutil.tooltip.advanced.dimratio", dimensionRatio, dimension).getUnformattedText());
                }
            }
        }
    }
    
    default String getStateTooltipKey(T local, boolean beingCrafted)
    {
        String key = null;

        if (local.isNonExpiring())
        {
            key = "misc.wumpleutil.tooltip.timer.nonexpiring";
        }
        else if (local.isSet() && !beingCrafted)
        {
            if (local.getPercent() >= 100)
            {
                key = "misc.wumpleutil.tooltip.timer.expired";
            }
            else
            {
                key = "misc.wumpleutil.tooltip.timer.timer";
            }
        }        
        else if (local.time > 0)
        {
            key = "misc.wumpleutil.tooltip.timer.crafting";
        }

        return key;
    }
}