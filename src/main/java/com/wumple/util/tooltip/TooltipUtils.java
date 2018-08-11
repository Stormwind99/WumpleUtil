package com.wumple.util.tooltip;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class TooltipUtils
{
    public static void doTooltip(ITooltipProvider[] providers, ItemStack stack, EntityPlayer entity, boolean advanced, List<String> tips)
    {
        for (ITooltipProvider provider : providers)
        {
            if (provider != null)
            {
                provider.doTooltip(stack, entity, advanced, tips);
            }
        }
    }
    
    public static void doTooltipAddon(ITooltipProvider[] providers, ItemStack stack, EntityPlayer entity, boolean advanced, List<String> tips)
    {
        for (ITooltipProvider provider : providers)
        {
            if (provider != null)
            {
                provider.doTooltipAddon(stack, entity, advanced, tips);
            }
        }
    }
}
