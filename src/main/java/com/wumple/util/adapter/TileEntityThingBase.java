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

    @Override
    public World getWorld()
    {
        return (owner != null) ? owner.getWorld() : null;
    }
    
    @Override
    public BlockPos getPos()
    {
        return (owner != null) ? owner.getPos() : null;
    }

    @Override
    public boolean isInvalid()
    {
        return (owner == null) || owner.isInvalid();
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
        if (owner != null) { owner.markDirty(); }
    }

    @Override
    public void invalidate()
    {
        if (owner != null)
        {
            World world = getWorld();
            BlockPos pos = getPos();
            if ((world != null) && (pos != null))
            {
                world.setBlockToAir(pos);
                world.removeTileEntity(pos);
            }
            owner.invalidate();
        }
        owner = null;
    }

    @Override
    public boolean sameAs(IThing entity)
    {
        if (entity instanceof TileEntityThingBase)
        {
            return owner == ((TileEntityThingBase) entity).owner;
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
    public <T> boolean is(Class<T> t)
    {
        return t.isInstance(owner);
    }
}
