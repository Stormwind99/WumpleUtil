package com.wumple.util.adapter;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class EntityThingBase implements IThingBase
{
    public Entity owner = null;

    public EntityThingBase(Entity ownerIn)
    {
        owner = ownerIn;
    }

    @Override
    public World getWorld()
    {
        return (owner != null) ? owner.getEntityWorld() : null;
    }
    
    @Override
    public BlockPos getPos()
    {
        return (owner != null) ? owner.getPosition() : null;
    }
    
    @Override
    public boolean isInvalid()
    {
        return (owner == null) || owner.isDead;
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public void invalidate()
    {
        if (owner != null) { owner.setDead(); }
        owner = null;
    }

    @Override
    public boolean sameAs(IThing entity)
    {
        if (entity instanceof EntityThingBase)
        {
            return owner == ((EntityThingBase) entity).owner;
        }
        return false;
    }
    
    @Override
    public Object object()
    {
        return owner;
    }
    
    @Override
    public ICapabilityProvider capProvider()
    { return owner; }
    
    @Override
    public void forceUpdate()
    { 
        if (owner != null)
        {
            BlockPos pos = getPos();
            owner.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
        }
    }
}