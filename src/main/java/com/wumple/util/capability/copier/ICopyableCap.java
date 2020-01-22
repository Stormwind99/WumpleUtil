package com.wumple.util.capability.copier;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public interface ICopyableCap<T extends ICopyableCap<T>>
{
    LazyOptional<? extends T> getCap(ICapabilityProvider provider);
    
    void copyFrom(T other, World world);

    @SuppressWarnings("unchecked")
    default void copyFrom2(ICopyableCap<T> other, World world)
    {
        copyFrom((T) other, world);
    }
    
    default void copyFrom(List<T> stacks, World world)
    {
        for (int i = 0; i < stacks.size(); i++)
        {
            T ccap = stacks.get(i);

            if (ccap != null)
            {
                copyFrom(ccap, world);
            }
        }
    }
    
    default <X extends ICapabilityProvider> void copyFromProviders(List<X> stacks, World world)
    {
        for (int i = 0; i < stacks.size(); i++)
        {
            X provider = stacks.get(i);
            copyFromProvider(provider, world);
        }
    }
    
    default <X extends ICapabilityProvider> void copyFromProvider(X provider, World world)
    {
        LazyOptional<? extends T> ccap = (provider != null) ? getCap(provider) : LazyOptional.empty();
        
        ccap.ifPresent((c) -> { copyFrom(c, world); } );
    }
    
    // ----------------------------------------------------------------------
    // For item drop and crafting processing

    default ItemStack check(World world, ItemStack before)
    {
        return before;
    }
    
    default void copyTo(List<ItemStack> drops, World world)
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

            LazyOptional<? extends T> destCap = getCap(after);

            destCap.ifPresent( (c) -> { c.copyFrom2(this, world); } );
        }
    }
    
    /*     
    default IThing check(World world, IThing before)
    {
        return before;
    }

    default void copyTo3(List<IThing> drops, World world)
    {
        for (int i = 0; i < drops.size(); ++i)
        {
            IThing before = drops.get(i);
            
            // only check before if world is available, since init depends on world
            IThing after = (world != null) ? check(world, before) : before;

            // in case check changes the stack, replace old in drops with new
            if (!after.sameAs(before))
            {
                drops.set(i, after);
            }

            T destCap = getCap(after);

            if (destCap != null)
            {
                destCap.copyFrom2(this, world);
            }
        }
    }
    */
    
    // ----------------------------------------------------------------------
    // For block placement processing
    // Example: (Item+ItemStack to TileEntity+Block)
    
    default void copyTo(BlockPos pos, World world)
    {
        TileEntity tileentity = world.getTileEntity(pos);

        /*
        // PORT
        if (ModConfig.zdebugging.usePlaceholderTileEntity)
        {
            if (tileentity == null)
            {
                tileentity = getNewTE(world);
                if (tileentity != null)
                {
                    Chunk chunk = world.getChunk(pos);
                    // Obvious method doesn't work: world.setTileEntity(pos, tileentity);
                    // Block.hasTileEntity() false would cause Chunk.addTileEntity() to reject
                    tileentity.setWorld(world);
                    tileentity.setPos(pos);
                    tileentity.validate();
                    // THIS CAUSES ConcurrentModificationException !!!
                    // However World.processingLoadedTiles that World.setTileEntity uses is private
                    chunk.getTileEntityMap().put(pos, tileentity);
                    chunk.markDirty();
                    world.addTileEntity(tileentity);
                    // TODO: tileentity will not persist - loading/saving will strip it out since Block.hasTileEntity() false
                }
            }
        }
        */
        
        if (tileentity != null)
        {
            copyTo(tileentity, world);
        }
        // else failure: no TileEntity to copy cap to
    }
    
    /*
    // PORT
    default TileEntity getNewTE(World world)
    {
        return new TileEntityPlaceholder(world);
    }
    */
    
    /*
    default void copyTo(TileEntity tileentity)
    {
        T destCap = getCap(tileentity);

        if (destCap != null)
        {
            destCap.copyFrom2(this);
        }
    }
    */
    
    default void copyTo(TileEntity tileentity, World world)
    {
        LazyOptional<? extends T> destCap = getCap(tileentity);

        destCap.ifPresent((c) -> 
        {
            c.copyFrom2(this, world);
        });
    }
}