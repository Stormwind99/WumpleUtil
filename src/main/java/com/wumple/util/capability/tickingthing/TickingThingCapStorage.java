package com.wumple.util.capability.tickingthing;

import com.wumple.util.capability.eventtimed.Expiration;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class TickingThingCapStorage<T extends ITickingThingCap<?>, W extends Expiration> implements IStorage<T>
{
    @Override
    public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side)
    {
        NBTTagCompound tags = new NBTTagCompound();

        if (instance != null)
        {
            tags.setLong("lastCheckTime", instance.getLastCheckTime());
        }

        return tags;
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt)
    {
        NBTTagCompound tags = (NBTTagCompound) nbt;

        if ((tags != null) && (instance != null))
        {
            instance.setLastCheckTime(tags.getLong("lastCheckTime"));
        }
    }
}