package com.wumple.util.capability.eventtimed;

import java.util.List;

import com.wumple.util.capability.itemstack.IItemStackCap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public interface IEventTimedItemStackCap<T extends Expiration> extends IItemStackCap
{
    
    public T newT();
    public IEventTimedItemStackCap<T> getCap(ItemStack stack);

    long getDate();

    long getTime();

    void setDate(long dateIn);

    void setTime(long timeIn);

    void setExpiration(long dateIn, long timeIn);

    void reschedule(long timeIn);

    T setInfo(T infoIn);

    T getInfo();

    boolean checkInitialized(World world);

    ItemStack expired(World world, ItemStack stack);

    void evaluate(World world, Integer index, IItemHandler itemhandler, ItemStack stack);
    
    /*
     * Evaluate this timer, which belongs to stack
     */
    ItemStack evaluate(World world, ItemStack stack);

    boolean isEnabled();

    boolean isDebugging();

    /*
     * Build tooltip info based on this timer
     */
    void doTooltip(ItemStack stack, EntityPlayer entity, boolean advanced, List<String> tips);

    void ratioShift(int fromRatio, int toRatio);
    
    void handleCraftedTimers(World world, IInventory craftMatrix, ItemStack crafting);

}