package com.wumple.util.capability.copier;

import java.util.List;

import org.apache.logging.log4j.LogManager;

import com.wumple.util.Reference;
import com.wumple.util.base.misc.Util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
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
    LazyOptional<? extends T> getCap(ICapabilityProvider provider);
    
    default ItemStack check(World world, ItemStack stack)
    {
        LazyOptional<? extends T> lcap = getCap(stack);
        ItemStack ret = lcap.map( cap->{return cap.check(world, stack);} ).orElse(null);
        return ret;
    }
    
    default void onHarvest(BlockEvent.HarvestDropsEvent event)
    {
        IWorld world = event.getWorld();
        if (world.isRemote()) { return; }
        
        List<ItemStack> drops = event.getDrops();
        TileEntity tileentity = setLastTileEntity(null);
        
        copyToFrom(drops, tileentity, world.getWorld());
    }
    
    default void onBreak(BlockEvent.BreakEvent event)
    {   
        IWorld world = event.getWorld();
        
        if (world.isRemote())
        {
            return;
        }
        
        BlockPos pos = event.getPos();
        TileEntity tileentity = world.getTileEntity(pos);
        
        setLastTileEntity(tileentity);
    }

    default void onPlaceBlock(BlockEvent.EntityPlaceEvent event)
    {
        IWorld world = event.getWorld();
        
        if (world.isRemote()) { return; }
        
        PlayerEntity pentity = Util.as(event.getEntity(), PlayerEntity.class);
        ItemStack stack = (pentity != null) ? pentity.getHeldItemMainhand() : null;
        BlockPos pos = event.getPos();
                
        // this can fail if pos (aka Block at pos) has no TileEntity!
        copyToFrom(pos, stack, world.getWorld());
    }
    
    default <Z extends ICapabilityProvider> void copyToFrom(ICapabilityProvider dest, List<Z> others, World world)
    {
        LazyOptional<? extends T> destCap = getCap(dest);

        destCap.ifPresent((d)->
        {
            d.copyFromProviders(others, world);
        });
    }
  
    default <Z extends ICapabilityProvider> void copyToFrom(List<ItemStack> drops, ICapabilityProvider src, World world)
    {
        LazyOptional<? extends T> srcCap = getCap(src);
        
        srcCap.ifPresent(s->
        {
            s.copyTo(drops, world);
        });
    }
   
    default void copyToFrom(BlockPos pos, ICapabilityProvider stack, World world)
    {
        LazyOptional<? extends T> srcCap = getCap(stack);
        
        srcCap.ifPresent(s->
        {
            s.copyTo(pos, world);
        });
    }
}
