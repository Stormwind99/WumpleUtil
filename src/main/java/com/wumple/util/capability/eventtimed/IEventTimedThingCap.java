package com.wumple.util.capability.eventtimed;

import java.util.List;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.thing.IThingCap;
import com.wumple.util.tileentity.placeholder.ICopyableCap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;

public interface IEventTimedThingCap<W extends IThing, T extends Expiration> extends IThingCap<W>, ICopyableCap< IEventTimedThingCap<W,T> >
{
    public T newT();
    public IEventTimedThingCap<W,T> getCap(ICapabilityProvider stack);

    long getDate();

    long getTime();

    void setDate(long dateIn);

    void setTime(long timeIn);

    void setExpiration(long dateIn, long timeIn);

    void reschedule(long timeIn);

    T setInfo(T infoIn);

    T getInfo();

    boolean checkInitialized(World world);

    W expired(World world, W stack);

    void evaluate(World world, Integer index, IItemHandler itemhandler, W stack);
    
    /*
     * Evaluate this timer, which belongs to stack
     */
    W evaluate(World world, W stack);

    boolean isEnabled();

    boolean isDebugging();

    /*
     * Build tooltip info based on this timer
     */
    void doTooltip(ItemStack stack, EntityPlayer entity, boolean advanced, List<String> tips);

    void ratioShift(int fromRatio, int toRatio);
    
    void handleCraftedTimers(World world, IInventory craftMatrix, ItemStack crafting);
}