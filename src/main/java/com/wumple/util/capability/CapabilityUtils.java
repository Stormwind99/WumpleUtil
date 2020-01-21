// by Choonster
// from https://github.com/Choonster-Minecraft-Mods/TestMod3

package com.wumple.util.capability;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Utility methods for Capabilities.
 *
 * @author Choonster
 */
public class CapabilityUtils
{
    @Nullable
    public static <T> LazyOptional<T> fetchCapability(@Nullable ICapabilityProvider provider, Capability<T> cap,
            @Nullable Direction side)
    {
    	return (provider != null) ? provider.getCapability(cap, side) : LazyOptional.empty();
    }

    @Nullable
    public static <T> LazyOptional<T> fetchCapability(World worldIn, BlockPos pos, Capability<T> cap,
            @Nullable Direction side)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return fetchCapability(tileentity, cap, side);
    }
}
