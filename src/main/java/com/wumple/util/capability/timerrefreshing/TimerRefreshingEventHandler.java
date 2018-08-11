package com.wumple.util.capability.timerrefreshing;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

abstract public class TimerRefreshingEventHandler<T extends TimerRefreshingCap<?,?,?> >
//<T extends TimerRefreshingCap<X extends IThing, Y extends Expiration, Z extends IThing> >
{
    /*
    @SubscribeEvent
    abstract public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event);
    */
    
    abstract protected T getCap(ICapabilityProvider stack);
    abstract public boolean isEnabled();
    abstract public boolean isDebugging();

    public TimerRefreshingEventHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        T cap = (stack != null) ? getCap(stack) : null;

        if (cap != null)
        {
            cap.doTooltip(stack, event.getEntityPlayer(), event.getFlags().isAdvanced(), event.getToolTip());
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDrawOverlay(final RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
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
    @SideOnly(Side.CLIENT)
    public void addTileEntityDebug(RenderGameOverlayEvent.Text e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        // tile entity
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && mc.objectMouseOver.getBlockPos() != null)
        {
            BlockPos blockpos = (mc.objectMouseOver == null) ? null : mc.objectMouseOver.getBlockPos();
            TileEntity te = (blockpos == null) ? null : mc.world.getTileEntity(blockpos);
            T cap = getCap(te);
            if (cap != null)
            {
                List<String> tips = new ArrayList<String>();
                cap.doTooltip(null, mc.player, true, tips);
                
                for (String tip : tips)
                {
                    e.getRight().add(tip);
                }
            }
        }
    }

}
