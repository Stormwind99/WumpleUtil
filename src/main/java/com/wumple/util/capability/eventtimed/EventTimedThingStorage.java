package com.wumple.util.capability.eventtimed;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class EventTimedThingStorage<T extends IEventTimedThingCap<?,?> > implements IStorage<T>
{
    @Override
    public INBT writeNBT(Capability<T> capability, T instance, Direction side)
    {
        CompoundNBT tags = new CompoundNBT();

        // some other mod doing bad things?
        if (instance != null)
        {
            tags.putLong("start", instance.getDate());
            tags.putLong("time", instance.getTime());
        }

        return tags;
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt)
    {
        CompoundNBT tags = (CompoundNBT) nbt;

        // some other mod doing bad things?
        if ((tags != null) && (instance != null))
        {
            // handle backwards compatibility for now
            if (tags.contains("EM_ROT_DATE"))
            {
                instance.setDate(tags.getLong("EM_ROT_DATE"));
                instance.setTime(tags.getLong("EM_ROT_TIME"));
            }
            else if (tags.contains("rotStartTimestamp"))
            {
                instance.setDate(tags.getLong("rotStartTimestamp"));
                instance.setTime(tags.getLong("rotLengthTime"));
            }
            else if (tags.contains("rotStart"))
            {
                instance.setDate(tags.getLong("rotStart"));
                instance.setTime(tags.getLong("rotTime"));
            }
            else
            {
                instance.setDate(tags.getLong("start"));
                instance.setTime(tags.getLong("time"));
            }
        }
    }
}
