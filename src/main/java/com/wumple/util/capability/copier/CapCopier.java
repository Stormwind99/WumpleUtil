package com.wumple.util.capability.copier;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.world.BlockEvent;

public interface CapCopier<T extends ICopyableCap<T> >
{
    /*  
    FML workaround: copy and paste the following into implementing classes
      
    @SubscribeEvent
    public void onHarvest(BlockEvent.HarvestDropsEvent event)
    { CapCopier.super.onHarvest(event); }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event)
    { CapCopier.super.onBreak(event); }
    
    @SubscribeEvent
    public void onPlace(BlockEvent.PlaceEvent event)
    { CapCopier.super.onPlace(event); }
     */
    
    TileEntity setLastTileEntity(TileEntity other);
    T getCap(ICapabilityProvider provider);
    
    default ItemStack check(World world, ItemStack stack)
    {
        return stack;
    }
    
    default void onHarvest(BlockEvent.HarvestDropsEvent event)
    {
        World world = event.getWorld();
        if (world.isRemote) { return; }
        
        List<ItemStack> drops = event.getDrops();
        TileEntity tileentity = setLastTileEntity(null);
        
        copyToFrom(drops, tileentity, world);
    }
    
    default void onBreak(BlockEvent.BreakEvent event)
    {   
        World world = event.getWorld();
        
        if (world.isRemote)
        {
            return;
        }
        
        BlockPos pos = event.getPos();
        TileEntity tileentity = world.getTileEntity(pos);
        setLastTileEntity(tileentity);
    }

    default void onPlaceBlock(BlockEvent.PlaceEvent event)
    {
        World world = event.getWorld();
        
        if (world.isRemote) { return; }
        
        ItemStack stack = event.getPlayer().getHeldItem(event.getHand());
        BlockPos pos = event.getPos();
        
        copyToFrom(pos, stack, world);
    }
    
    default <Z extends ICapabilityProvider> void copyToFrom(ICapabilityProvider dest, List<Z> others)
    {
        T destCap = getCap(dest);

        if (destCap != null)
        {
            destCap.copyFromProviders(others);
        }
    }
  
    default <Z extends ICapabilityProvider> void copyToFrom(List<ItemStack> drops, ICapabilityProvider src, World world)
    {
        T srcCap = getCap(src);
        
        if (srcCap != null)
        {
            srcCap.copyTo(drops, world);
        }
    }
   
    
    default void copyToFrom(BlockPos pos, ICapabilityProvider stack, World world)
    {
        T srcCap = getCap(stack);
        
        if (srcCap != null)
        {
            srcCap.copyTo(pos, world);
        }
    }
}
