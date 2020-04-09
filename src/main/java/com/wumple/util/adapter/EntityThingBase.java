package com.wumple.util.adapter;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class EntityThingBase extends ThingBase<Entity>
{
    public EntityThingBase(Entity ownerIn)
    {
        super(ownerIn);
    }
    
    @Override
    public World getWorld()
    {
        return isValid() ? get().getEntityWorld() : null;
    }
    
    @Override
    public BlockPos getPos()
    {
        return isValid() ? get().getPosition() : null;
    }
    
    @Override
    public boolean isInvalid()
    {
        return super.isInvalid() || !get().isAlive();
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public void invalidate()
    {
        if (get() != null) { get().remove(); }
        super.invalidate();
    }

    @Override
    public boolean sameAs(IThing entity)
    {
        if (entity instanceof EntityThingBase)
        {
            return get() == ((EntityThingBase) entity).get();
        }
        return false;
    }
    
    @Override
    public ICapabilityProvider capProvider()
    { return get(); }
    
    @Override
    public void forceUpdate()
    { 
        if (isValid())
        {
            BlockPos pos = getPos();
            get().setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
        }
    }
}