package com.wumple.util.tileentity.placeholder;

import com.wumple.util.ModConfig;

import net.minecraft.util.ITickable;
import net.minecraft.world.World;

abstract public class TickingTileEntityPlaceholder extends TileEntityPlaceholder implements ITickable
{
    protected long ticks = 0;
  
    /*
    public TickingTileEntityPlaceholder()
    {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void invalidate()
    {
        MinecraftForge.EVENT_BUS.unregister(this);
        super.invalidate();
    }
    */

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

    @Override
    public void update()
    {
        World world = getWorld();
        if ((world != null) && (world.isRemote != true))
        {
            handleOnTick(world);
        }
    }
    
    /*
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
    */
    
    
}
