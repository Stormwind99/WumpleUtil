// by Choonster
// from https://github.com/Choonster-Minecraft-Mods/TestMod3

package com.wumple.util.container.capabilitylistener;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

// backwards compat
public class CapabilityUtils
{
    @Nullable
    public static <T> T fetchCapability(@Nullable ICapabilityProvider provider, Capability<T> capability,
            @Nullable EnumFacing facing)
    {
        return com.wumple.util.capability.CapabilityUtils.fetchCapability(provider, capability, facing);
    }
}