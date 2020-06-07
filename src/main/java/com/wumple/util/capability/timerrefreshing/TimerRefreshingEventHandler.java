package com.wumple.util.capability.timerrefreshing;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

abstract public class TimerRefreshingEventHandler<T extends ITimerRefreshingCap<?,?> >
{
    /*
    @SubscribeEvent
    abstract public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event);
    */
    
    abstract protected LazyOptional<? extends T> getCap(ICapabilityProvider stack);
    abstract public boolean isEnabled();
    abstract public boolean isDebugging();

    public TimerRefreshingEventHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        LazyOptional<? extends T> lcap = (stack != null) ? getCap(stack) : LazyOptional.empty();
        
        lcap.ifPresent(cap->
        {
            cap.doTooltip(stack, event.getPlayer(), event.getFlags().isAdvanced(), event.getToolTip());
        });
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDrawOverlay(final RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.gameSettings.showDebugInfo == true)
        {
            if (isDebugging())
            {
                addTileEntityDebug(e);
            }
        }
    }
    
    /*
     * Add TileEntity debug text to debug screen if looking at Block with a TileEntity
     */
    @OnlyIn(Dist.CLIENT)
    public void addTileEntityDebug(RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getInstance();

        // tile entity
        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK)
        {
            final BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) mc.objectMouseOver;
            BlockPos blockpos = rayTraceResult.getPos();

            TileEntity te = (blockpos == null) ? null : mc.world.getTileEntity(blockpos);

            LazyOptional<? extends T> lcap = getCap(te);
            lcap.ifPresent(cap->
            {
                List<ITextComponent> tips = new ArrayList<ITextComponent>();
                cap.doTooltip(null, mc.player, true, tips);
                
                for (ITextComponent tip : tips)
                {
                    e.getRight().add(tip.getFormattedText());
                }
            });
        }
    }

}
