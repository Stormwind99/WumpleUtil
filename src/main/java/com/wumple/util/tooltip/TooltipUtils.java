package com.wumple.util.tooltip;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class TooltipUtils
{
    public static void doTooltip(ITooltipProvider[] providers, ItemStack stack, PlayerEntity entity, boolean advanced, List<ITextComponent> tips)
    {
        for (ITooltipProvider provider : providers)
        {
            if (provider != null)
            {
                provider.doTooltip(stack, entity, advanced, tips);
            }
        }
    }
    
    public static void doTooltipAddon(ITooltipProvider[] providers, ItemStack stack, PlayerEntity entity, boolean advanced, List<ITextComponent> tips)
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
