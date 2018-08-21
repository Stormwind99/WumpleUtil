// by Choonster
// from https://github.com/Choonster-Minecraft-Mods/TestMod3

package com.wumple.util.capability;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * Utility methods for Capabilities.
 *
 * @author Choonster
 */
public class CapabilityUtils
{
    /**
     * Get a capability handler from an {@link ICapabilityProvider} if it exists.
     *
     * @param provider
     *            The provider
     * @param capability
     *            The capability
     * @param facing
     *            The facing
     * @param <T>
     *            The handler type
     * @return The handler, if any.
     */
    @Nullable
    public static <T> T fetchCapability(@Nullable ICapabilityProvider provider, Capability<T> capability,
            @Nullable EnumFacing facing)
    {
        return provider != null && provider.hasCapability(capability, facing)
                ? provider.getCapability(capability, facing)
                : null;
    }

    @Nullable
    public static <T> T fetchCapability(World worldIn, BlockPos pos, Capability<T> capability,
            @Nullable EnumFacing facing)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return fetchCapability(tileentity, capability, facing);
    }
}
