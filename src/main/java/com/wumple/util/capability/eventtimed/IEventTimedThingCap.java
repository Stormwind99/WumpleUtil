package com.wumple.util.capability.eventtimed;

import java.util.List;

import com.wumple.util.ModConfiguration;
import com.wumple.util.adapter.IThing;
import com.wumple.util.adapter.TUtil;
import com.wumple.util.capability.copier.ICopyableCap;
import com.wumple.util.capability.thing.IThingCap;
import com.wumple.util.container.ContainerUtil;
import com.wumple.util.tooltip.ITooltipProvider;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public interface IEventTimedThingCap<W extends IThing, T extends Expiration> extends IThingCap<W>, ICopyableCap< IEventTimedThingCap<W,T> >, ITooltipProvider
{
    public T newT();
    public LazyOptional< ? extends IEventTimedThingCap<W,T> > getCap(ICapabilityProvider stack);

    T setInfo(T infoIn);

    T getInfo();
    
    default W expired(World world)
    { return expired(world, getOwner()); }

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
        forceUpdate();
    }

    default void setTime(long timeIn)
    {
        getInfo().setTime(timeIn);
        forceUpdate();
    }
    
    default void setExpiration(long dateIn, long timeIn)
    {
        getInfo().set(dateIn, timeIn);
        forceUpdate();
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
        IThing owner = getOwner();
        World worldOut = (owner != null) ? owner.getWorldBackup(world) : world;
        boolean initedAlready = getInfo().checkInitialized(worldOut, owner);
        if (!initedAlready) { forceUpdate(); }
        return initedAlready;
    }
    
    default boolean checkInitialized(World world, W stack)
    {
        IThing owner = getOwner();
        World worldOut = (owner != null) ? owner.getWorldBackup(world) : world;
        boolean initedAlready = getInfo().checkInitialized(worldOut, stack);
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
        LazyOptional<? extends IEventTimedThingCap<W,T> > lcap = getCap(stack);
        
        ItemStack ret = lcap.map(cap->{
        	cap.checkInitialized(world);
        	return cap.getOwner().as(ItemStack.class);
        }).orElse(null);
        
        return ret;
    }
    
    /*
     * Evaluate this timer, which belongs to stack
     */
    default W evaluate(World world)
    {
        return evaluate(world, getOwner());
    }
    
    /*
     * Evaluate this timer, which belongs to stack
     */
    default W evaluate(World world, W stack)
    {
        checkInitialized(world, stack);
        
        T info = getInfo();
        
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
      
    /*
    @Override
    default void copyFrom(IEventTimedThingCap<W, T> other, World world)
    {
        // Avoid cheating from crafting or break rotting items
        
        // For example: 
        // Melon 0/14 days -> Slices 0/7 days -> Melon 0/7 days or 7/14 days
        // Melon 8/14 days -> Slices 6/7 days -> Melon 6/7 days or 13/14 days
        // Crafting: Ingredients 1/7, 2/7, 3/7 days -> Results 3/7 days
        // Crafting: Ingredients 1/7, 2/7, 3/7 days -> Results 10/14 days
        // Crafting: Ingredients 5/14, 6/14 -> Results 0/7 days
        // Crafting: Ingredients 6/14, 9/14 -> Results 2/7 days
        
        if (ModConfig.Debugging.debug.get()) { log("copyFrom: other " + other + " this " + this); }
        
        if (!this.isExpirationTimestampSet())
        {
            if (other.isExpirationTimestampSet())
            {
                if (ModConfig.Debugging.debug.get()) { log("copyFrom: uninit this, copying " + other.getDate() + " " + other.getTime()); }
                setExpiration(other.getDate(), other.getTime());
            }
            else
            {
                // handle uninitialized src or dest
                // should never happen - but for now just skip this operation
                if (ModConfig.Debugging.debug.get()) { log("copyFrom: skipping uninit other " + other.isExpirationTimestampSet() + " this " + this.isExpirationTimestampSet()); }
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
            
            if (ModConfig.Debugging.debug.get()) { log("copyFrom: setting"
                    + " new_d_i " + new_d_i
                    + " d_o " + d_o
                    + " t_o " + t_o
                    + " e_o " + e_o
                    + " d_i " + d_i
                    + " t_i " + t_i
                    + " e_i " + e_i
                    ); }
            
            setExpiration(new_d_i, t_i);
        }
        else
        {
            if (ModConfig.Debugging.debug.get()) { log("copyFrom: skipping this isNotExpiring"); }
        }
    }
    */
    
    default public void log(String out)
    {
    	
    }
    
    @Override
    default void copyFrom(IEventTimedThingCap<W, T> other, World world)
    {
        // Avoid cheating from crafting or break rotting items
        
        // For example: 
        // Melon 0/14 days -> Slices 0/7 days -> Melon 0/7 days or 7/14 days
        // Melon 8/14 days -> Slices 6/7 days -> Melon 6/7 days or 13/14 days
        // Crafting: Ingredients 1/7, 2/7, 3/7 days -> Results 3/7 days
        // Crafting: Ingredients 1/7, 2/7, 3/7 days -> Results 10/14 days
        // Crafting: Ingredients 5/14, 6/14 -> Results 0/7 days
        // Crafting: Ingredients 6/14, 9/14 -> Results 2/7 days
        
        if (ModConfiguration.Debugging.debug.get()) { log("copyFrom: other " + other + " this " + this); }

        // world might be null
        this.checkInitialized(world);
        other.checkInitialized(world);
        
        if (!this.isExpirationTimestampSet())
        {
            if (other.isExpirationTimestampSet())
            {
                if (ModConfiguration.Debugging.debug.get()) { log("copyFrom: uninit this, copying " + other.getDate() + " " + other.getTime()); }
                setExpiration(other.getDate(), other.getTime());
            }
        }
        
        if (!other.isExpirationTimestampSet())
        {
            // handle uninitialized src
            // should never happen - but for now just skip this operation
            if (ModConfiguration.Debugging.debug.get()) { log("copyFrom: skipping uninit other " + other.isExpirationTimestampSet() + " this " + this.isExpirationTimestampSet()); }
            assert(other.isExpirationTimestampSet());
            return;              
        }         
        
        // handle dimension-related state:
        // if other.nonExpiring && info.nonExpiring, keep oldest non-expiring (was do nothing)
        // if other.nonExpiring && !info.nonExpiring, keep info (was do nothing)
        // if !other.nonExp && info.nonExpiring, keep other (was do nothing)
        // if !other.nonExp && !info.nonExpiring, do merge (see below)
        
        if (this.isNonExpiring() && other.isNonExpiring())
        {
            long d_o = other.getDate();
            long d_i = this.getDate();
            long new_d_i = Math.min(d_o, d_i);
            long t_i = ExpirationBase.NO_EXPIRATION;
            
            if (ModConfiguration.Debugging.debug.get())
            { 
                long t_o = other.getTime();
                
                log("copyFrom: non-exp setting"
                    + " new_d_i " + new_d_i
                    + " d_o " + d_o
                    + " t_o " + t_o
                    + " d_i " + d_i
                    + " t_i " + t_i
                    );
            }
            
            setExpiration(new_d_i, t_i);
        }
        else if ((!this.isNonExpiring()) && (!other.isNonExpiring()))
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
            
            if (ModConfiguration.Debugging.debug.get()) { log("copyFrom: setting"
                    + " new_d_i " + new_d_i
                    + " d_o " + d_o
                    + " t_o " + t_o
                    + " e_o " + e_o
                    + " d_i " + d_i
                    + " t_i " + t_i
                    + " e_i " + e_i
                    ); }
            
            setExpiration(new_d_i, t_i);
        }
        else
        {
            if (ModConfiguration.Debugging.debug.get()) { log("copyFrom: skipping isNotExpiring this " + this.isNonExpiring() + " other " + other.isNonExpiring()); }
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
            LazyOptional<? extends IEventTimedThingCap<W,T> > cap = getCap(thing);

            cap.ifPresent((c) -> 
            {
                copyFrom(c, world);
            }
            );
        }
    }
    
    /*
     * Build tooltip info based on this timer
     */
    default void doTooltip(ItemStack stack, PlayerEntity entity, boolean advanced, List<ITextComponent> tips)
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
                boolean beingCrafted = false; // PORT (stack != null) ? CraftingUtil.isItemBeingCraftedBy(stack, entity) : false;
                String key = getStateTooltipKey(myinfo, beingCrafted);

                if (key != null)
                {
                    tips.add(new TranslationTextComponent(key, myinfo.getPercent() + "%", myinfo.getDaysLeft(),
                            myinfo.getDaysTotal()));
                }

                // advanced tooltip debug info
                if (advanced && isDebugging())
                {
                    tips.add(new TranslationTextComponent("misc.wumpleutil.tooltip.advanced.datetime", myinfo.getDate(),
                            myinfo.getTime()));
                    tips.add(new TranslationTextComponent("misc.wumpleutil.tooltip.advanced.expire", myinfo.getCurTime(),
                            myinfo.getExpirationTimestamp()));

                    int dimension = world.getDimension().getType().getId();
                    int dimensionRatio = myinfo.getDimensionRatio(world);
                    tips.add(new TranslationTextComponent("misc.wumpleutil.tooltip.advanced.dimratio", dimensionRatio, dimension));
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