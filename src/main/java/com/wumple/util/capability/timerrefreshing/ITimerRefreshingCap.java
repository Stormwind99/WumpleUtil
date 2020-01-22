package com.wumple.util.capability.timerrefreshing;

import java.util.List;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.eventtimed.Expiration;
import com.wumple.util.capability.tickingthing.ITickingThingCap;
import com.wumple.util.tooltip.ITooltipProvider;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public interface ITimerRefreshingCap<T extends IThing, W extends Expiration> extends ITickingThingCap<T>, ITooltipProvider
{
    int getRatio();

    default void doTooltip(ItemStack stack, PlayerEntity entity, boolean advanced, List<ITextComponent> tips)
    {
        if (advanced)
        {
            tips.add(new TranslationTextComponent("misc.wumpleutil.tooltip.advanced.refreshing", getRatio()));
        }
    }
}