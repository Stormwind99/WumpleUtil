package com.wumple.util.misc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wumple.util.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

public class TimeStampUtil
{
    // the last world time/tick count/timestamp received during world tick
    // needed since no access to world later
    protected static long lastWorldTimestamp = 0;
    protected static long lastClientWorldTimestamp = 0;
    protected static int lastClientWorld = 0;
    protected static long lastServerWorldTimestamp = 0;
    protected static int lastServerWorld = 0;

    public static void clear()
    {
        lastWorldTimestamp = 0;
        lastClientWorldTimestamp = 0;
        lastClientWorld = 0;
        lastServerWorldTimestamp = 0;
        lastServerWorld = 0;
    }
    
    public static void setLastWorldTimestamp(long timestamp)
    {
        if (timestamp > lastWorldTimestamp)
        {
            lastWorldTimestamp = timestamp;
        }
    }

    public static long getLastWorldTimestamp()
    {
        // ugh - during MC startup, OreDict registration can create ItemStacks before a world time exists
        //assert (lastWorldTimestamp > 0);
        return lastWorldTimestamp;
    }

    protected static void setTimestamp(World world)
    {
        long timestamp = world.getGameTime();
        setLastWorldTimestamp(timestamp);

        if (world.isRemote)
        {
            lastClientWorldTimestamp = timestamp;
            lastClientWorld = world.getDimension().getType().getId();
        }
        else
        {
            lastServerWorldTimestamp = timestamp;
            lastServerWorld = world.getDimension().getType().getId();
        }
    }

    
	public static Logger getLogger()
	{
		return LogManager.getLogger(Reference.MOD_ID);
	}
	
    @Mod.EventBusSubscriber
    public static class EventHandler
    {
	    /**
	     * Update cached current timestamp on server
	     */
	    @SubscribeEvent
	    public static void onWorldTick(TickEvent.WorldTickEvent event)
	    {
	        World world = event.world;
	        if (world != null)
	        {
	            setTimestamp(world);
	        }
	    }
	
	    /**
	     * Update cached current timestamp on client
	     */
	    @OnlyIn(Dist.CLIENT)
	    @SubscribeEvent
	    public static void onClientTick(TickEvent.ClientTickEvent event)
	    {
	        World world = Minecraft.getInstance().world;
	        if (world != null)
	        {
	            setTimestamp(world);
	        }
	    }
	
	    @SubscribeEvent
	    public static void onWorldLoad(WorldEvent.Load event)
	    {
	        World world = event.getWorld().getWorld();
	        setTimestamp(world);
	    }
    
	    public static void onAboutToStart(FMLServerAboutToStartEvent event)
	    {
	        clear();
	    }
	    
	    public static void onStopped(FMLServerStoppedEvent event)
	    {
	        clear();
	    }
    }
}