package com.wumple.util.tooltip;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ITooltipProvider
{
    void doTooltip(ItemStack stack, EntityPlayer entity, boolean advanced, List<String> tips);
    default void doTooltipAddon(ItemStack stack, EntityPlayer entity, boolean advanced, List<String> tips)
        { doTooltip(stack, entity, advanced, tips); }
}
