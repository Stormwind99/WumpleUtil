package com.wumple.util.tileentity.placeholder;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface ICopyableCap<T extends ICopyableCap<T> >
{
    void copyFrom(T other);
    
    @SuppressWarnings("unchecked")
    default void copyFrom2(ICopyableCap<T> other)
    {
        copyFrom((T) other);
    }
    
    T getCap(ICapabilityProvider provider);
    
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
    
    default <Z extends ICapabilityProvider> void copyToFrom(List<Z> others)
    {
        for (int i = 0; i < others.size(); ++i)
        {
            ICapabilityProvider stack = others.get(i);

            T srcCap = getCap(stack);

            if (srcCap != null)
            {
                copyFrom(srcCap);
            }
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

}