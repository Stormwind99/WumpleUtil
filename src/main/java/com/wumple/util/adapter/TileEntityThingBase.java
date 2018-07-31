package com.wumple.util.adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.wumple.util.capability.CapabilityUtils;
import com.wumple.util.misc.Util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityThingBase implements IThingBase
{
    public TileEntity owner = null;

    public TileEntityThingBase(TileEntity ownerIn)
    {
        owner = ownerIn;
    }

    public World getWorld()
    {
        return (owner != null) ? owner.getWorld() : null;
    }
    
    public BlockPos getPos()
    {
        return (owner != null) ? owner.getPos() : null;
    }

    public boolean isInvalid()
    {
        return (owner == null) || owner.isInvalid();
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
        return CapabilityUtils.fetchCapability(owner, capability, facing);
    }

    public void markDirty()
    {
        if (owner != null) { owner.markDirty(); }
    }

    public void invalidate()
    {
        owner = null;
    }

    public boolean sameAs(IThing entity)
    {
        if (entity instanceof TileEntityThingBase)
        {
            return owner == ((TileEntityThingBase) entity).owner;
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
