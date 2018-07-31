package com.wumple.util.tileentity.placeholder;

import com.wumple.util.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

abstract public class TickingTileEntityPlaceholder extends TileEntityPlaceholder
{
    protected long ticks;
    
    protected void handleOnTick(World world)
    {
        ticks++;
        if (ticks >= getEvaluationInterval())
        {
            ticks = 0;
            doIt(world);
        }
    }
    
    abstract public void doIt(World world);
    
    protected long getEvaluationInterval()
    {
        return ModConfig.tileEntityPlaceholderEvaluationInterval;
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent event)
    {
        handleOnTick(event.world);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        World world = Minecraft.getMinecraft().world;
        if ((world != null) && (world.isRemote == true))
        {
            handleOnTick(world);
        }
    }
}
