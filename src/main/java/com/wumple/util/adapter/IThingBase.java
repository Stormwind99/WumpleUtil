package com.wumple.util.adapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing);

    @Nullable
    <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing);

    @Nullable
    <T> T fetchCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing);

    void markDirty();

    void invalidate();

    boolean sameAs(IThing entity);
    
    Object object();
    <T> T as(Class<T> t);
    
    default int getCount()
    { return 1; }
    
}
