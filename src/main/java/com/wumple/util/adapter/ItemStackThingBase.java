package com.wumple.util.adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.wumple.util.capability.CapabilityUtils;
import com.wumple.util.misc.Util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class ItemStackThingBase implements IThingBase
{
    public ItemStack owner = null;

    public ItemStackThingBase(ItemStack ownerIn)
    {
        owner = ownerIn;
    }

    @Override
    public World getWorld()
    {
        return null; // owner.getEntityWorld();
    }
    
    @Override
    public BlockPos getPos()
    {
        return null; // owner.getPosition();
    }

    @Override
    public boolean isInvalid()
    {
        return false;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return (owner != null) ? owner.hasCapability(capability, facing) : false;
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return (owner != null) ? owner.getCapability(capability, facing) : null;
    }

    @Override
    @Nullable
    public <T> T fetchCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return CapabilityUtils.fetchCapability(owner, capability, facing);
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public void invalidate()
    {
        owner = null;
    }

    @Override
    public boolean sameAs(IThing entity)
    {
        if (entity instanceof ItemStackThingBase)
        {
            return owner == ((ItemStackThingBase) entity).owner;
        }
        return false;
    }
    
    @Override
    public Object object()
    {
        return owner;
    }
    
    @Override
    public <T> T as(Class<T> t)
    {
        return Util.as(owner, t);
    }
    
    @Override
    public int getCount()
    {
        return (owner != null) ? owner.getCount() : 0;
    }
}