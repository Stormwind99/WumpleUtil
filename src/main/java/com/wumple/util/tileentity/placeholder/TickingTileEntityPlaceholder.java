package com.wumple.util.tileentity.placeholder;

import com.wumple.util.ModConfig;

import net.minecraft.util.ITickable;
import net.minecraft.world.World;

abstract public class TickingTileEntityPlaceholder extends TileEntityPlaceholder implements ITickable
{
    protected long ticks = 0;
  
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
}
