package com.wumple.util.misc;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.wumple.util.base.misc.Util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class EquipmentUtil
{
	public static <T> Pair<ItemStack, T> findHeldItemOf(LivingEntity entity, Class<T> t)
	{
		ItemStack stack = null;
		T item = null;
		
        // get a held map
        if (entity != null)
        {
            stack = entity.getHeldItemMainhand();
            item = (stack != null) ? Util.as(stack.getItem(), t) : null;
            
            if (item == null)
            {
            	stack = entity.getHeldItemOffhand();
                item = (stack != null) ? Util.as(stack.getItem(), t) : null;
            }
        }
        
        Pair<ItemStack, T> pair = ImmutablePair.of(stack, item);

        return pair;
	}
}
