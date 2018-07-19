package com.wumple.util.misc;

import com.wumple.util.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class TimeUtil
{
    // MAYBE re-init on WorldEvent.Load instead of waiting for first world tick to do so via handler
    // the last world time/tick count/timestamp received during world tick
    //    needed since no access to world later
    protected static long lastWorldTimestamp = 0;
    protected static long lastClientWorldTimestamp = 0;
    protected static int lastClientWorld = 0;
    protected static long lastServerWorldTimestamp = 0;
    protected static int lastServerWorld = 0;
    
    public static void setLastWorldTimestamp(long timestamp)
    {
        //assert((lastWorldTimestamp == 0) || ((timestamp - lastWorldTimestamp) < 1000));
        if (timestamp > lastWorldTimestamp)
        {
            lastWorldTimestamp = timestamp;
        }
    }

    public static long getLastWorldTimestamp()
    {
        assert(lastWorldTimestamp > 0);
        return lastWorldTimestamp;
    }
    
    protected static void setTimestamp(World world)
    {
        long timestamp = world.getTotalWorldTime();
        setLastWorldTimestamp(timestamp);

        if (world.isRemote)
        {
            lastClientWorldTimestamp = timestamp;
            lastClientWorld = world.provider.getDimension();
        }
        else
        {
            lastServerWorldTimestamp = timestamp;
            lastServerWorld = world.provider.getDimension();            
        }
    }
    
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
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        World world = Minecraft.getMinecraft().world;
        if (world != null)
        {
            setTimestamp(world);
        }
    }
    
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event)
    {
        World world = event.getWorld();
        setTimestamp(world);
    }
}
