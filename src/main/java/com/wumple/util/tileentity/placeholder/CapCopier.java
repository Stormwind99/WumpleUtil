package com.wumple.util.tileentity.placeholder;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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
    
    default TileEntity getNewTE()
    {
        return new TileEntityPlaceholder();
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
            destCap.copyToFrom(others);
        }
    }
  
    default <Z extends ICapabilityProvider> void copyToFrom(List<ItemStack> drops, ICapabilityProvider src, World world)
    {
        T srcCap = getCap(src);
        
        if (srcCap != null)
        {
            for (int i = 0; i < drops.size(); ++i)
            {
                ItemStack before = drops.get(i);
                
                // only check before if world is available, since init depends on world
                ItemStack after = (world != null) ? check(world, before) : before;
                
                // in case check changes the stack, replace old in drops with new
                if (before != after)
                {
                    drops.set(i, after);
                }
                
                T destCap = getCap(after);
                
                if (destCap != null)
                {
                    destCap.copyFrom(srcCap);
                }
            }
        }
    }
    
    default void copyToFrom(BlockPos pos, ICapabilityProvider stack, World world)
    {
        T srcCap = getCap(stack);
        
        if (srcCap != null)
        {
            TileEntity tileentity = world.getTileEntity(pos);
            
            if (tileentity == null)
            {
                tileentity = getNewTE();
                if (tileentity != null)
                {
                    Chunk chunk = world.getChunk(pos);
                    // Obvious method doesn't work: world.setTileEntity(pos, tileentity);
                    // Block.hasTileEntity() false would cause Chunk.addTileEntity() to reject
                    tileentity.setWorld(world);
                    tileentity.setPos(pos);
                    tileentity.validate();
                    chunk.getTileEntityMap().put(pos, tileentity);
                    chunk.markDirty();
                    world.addTileEntity(tileentity);
                    // TODO: tileentity will not persist - loading/saving will strip it out since Block.hasTileEntity() false
                }
            }
            
            T destCap = getCap(tileentity);
            
            if (destCap != null)
            {
                destCap.copyFrom(srcCap);
            }
        }

    }
    
}
