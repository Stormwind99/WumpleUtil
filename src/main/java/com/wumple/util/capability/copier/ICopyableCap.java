package com.wumple.util.capability.copier;

import java.util.List;

import com.wumple.util.placeholder.TileEntityPlaceholder;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface ICopyableCap<T extends ICopyableCap<T>>
{
    T getCap(ICapabilityProvider provider);
    
    void copyFrom(T other);

    @SuppressWarnings("unchecked")
    default void copyFrom2(ICopyableCap<T> other)
    {
        copyFrom((T) other);
    }

    default ItemStack check(World world, ItemStack before)
    {
        return before;
    }

    default void copyFrom(List<T> stacks)
    {
        for (int i = 0; i < stacks.size(); i++)
        {
            T ccap = stacks.get(i);

            if (ccap != null)
            {
                copyFrom(ccap);
            }
        }
    }
    
    default <X extends ICapabilityProvider> void copyFromProviders(List<X> stacks)
    {
        for (int i = 0; i < stacks.size(); i++)
        {
            X provider = stacks.get(i);
            copyFromProvider(provider);
        }
    }
    
    default <X extends ICapabilityProvider> void copyFromProvider(X provider)
    {
        T ccap = (provider != null) ? getCap(provider) : null;
        
        if (ccap != null)
        {
            copyFrom(ccap);
        }        
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

            T destCap = getCap(after);

            if (destCap != null)
            {
                destCap.copyFrom2(this);
            }
        }
    }

    default TileEntity getNewTE()
    {
        return new TileEntityPlaceholder();
    }

    default void copyTo(BlockPos pos, World world)
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
        
        copyTo(tileentity);
    }
    
    default void copyTo(TileEntity tileentity)
    {
        T destCap = getCap(tileentity);

        if (destCap != null)
        {
            destCap.copyFrom2(this);
        }
    }
}