package com.wumple.util.capability.tickingthing;

import com.wumple.util.capability.eventtimed.Expiration;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class TickingThingCapStorage<T extends ITickingThingCap<?>, W extends Expiration> implements IStorage<T>
{
    @Override
    public INBT writeNBT(Capability<T> capability, T instance, Direction side)
    {
        CompoundNBT tags = new CompoundNBT();

        if (instance != null)
        {
            tags.putLong("lastCheckTime", instance.getLastCheckTime());
        }

        return tags;
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt)
    {
        CompoundNBT tags = (CompoundNBT) nbt;

        if ((tags != null) && (instance != null))
        {
            instance.setLastCheckTime(tags.getLong("lastCheckTime"));
        }
    }
}