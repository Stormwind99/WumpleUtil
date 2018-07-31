package com.wumple.util.adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.wumple.util.capability.CapabilityUtils;
import com.wumple.util.misc.Util;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class EntityThingBase implements IThingBase
{
    public Entity owner = null;

    public EntityThingBase(Entity ownerIn)
    {
        owner = ownerIn;
    }

    public World getWorld()
    {
        return (owner != null) ? owner.getEntityWorld() : null;
    }
    
    public BlockPos getPos()
    {
        return (owner != null) ? owner.getPosition() : null;
    }

    public boolean isInvalid()
    {
        return (owner == null) || owner.isDead;
    }

    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return (owner != null) ? owner.hasCapability(capability, facing) : false;
    }

    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return (owner != null) ? owner.getCapability(capability, facing) : null;
    }

    @Nullable
    public <T> T fetchCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return (owner != null) ? CapabilityUtils.fetchCapability(owner, capability, facing) : null;
    }

    public void markDirty()
    {
    }

    public void invalidate()
    {
        owner = null;
    }

    public boolean sameAs(IThing entity)
    {
        if (entity instanceof EntityThingBase)
        {
            return owner == ((EntityThingBase) entity).owner;
        }
        return false;
    }
    
    public Object object()
    {
        return owner;
    }
    
    public <T> T as(Class<T> t)
    {
        return Util.as(owner, t);
    }
}