package com.wumple.util.adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.wumple.util.base.misc.Util;
import com.wumple.util.container.capabilitylistener.CapabilityUtils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IThingBase extends ICapabilityProvider
{
    World getWorld();
    
    BlockPos getPos();

    boolean isInvalid();

    void markDirty();

    void invalidate();

    boolean sameAs(IThing entity);
    
    Object object();
    
    default World getWorldBackup(World otherWorld)
    {
        World myWorld = getWorld();
        return (myWorld != null) ? myWorld : otherWorld;
    }
    
    default int getCount()
    { return 1; }
        
    default <T> T as(Class<T> t)
    { return Util.as(object(), t); }
    
    default <T> boolean is(Class<T> t)
    { return t.isInstance(object()); }
    
    ICapabilityProvider capProvider();
    
    @Override
    default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        ICapabilityProvider provider = capProvider();
        return (provider != null) ? provider.hasCapability(capability, facing) : false;
    }

    @Override
    @Nullable
    default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        ICapabilityProvider provider = capProvider();
        return (provider != null) ? provider.getCapability(capability, facing) : null;
    }

    @Nullable
    default <T> T fetchCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        ICapabilityProvider provider = capProvider();
        return CapabilityUtils.fetchCapability(provider, capability, facing);
    }
    
    void forceUpdate();
}
