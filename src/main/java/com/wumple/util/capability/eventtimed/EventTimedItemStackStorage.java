package com.wumple.util.capability.eventtimed;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class EventTimedItemStackStorage<T extends IEventTimedItemStackCap<?> > implements IStorage<T>
{
    @Override
    public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side)
    {
        NBTTagCompound tags = new NBTTagCompound();

        // some other mod doing bad things?
        if (instance != null)
        {
            tags.setLong("start", instance.getDate());
            tags.setLong("time", instance.getTime());
            tags.setByte("i", instance.getForceId());
        }

        return tags;
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt)
    {
        NBTTagCompound tags = (NBTTagCompound) nbt;

        // some other mod doing bad things?
        if ((tags != null) && (instance != null))
        {
            // handle backwards compatibility for now
            if (tags.hasKey("EM_ROT_DATE"))
            {
                instance.setDate(tags.getLong("EM_ROT_DATE"));
                instance.setTime(tags.getLong("EM_ROT_TIME"));
            }
            else if (tags.hasKey("rotStartTimestamp"))
            {
                instance.setDate(tags.getLong("rotStartTimestamp"));
                instance.setTime(tags.getLong("rotLengthTime"));
                instance.setForceId(tags.getByte("i"));
            }
            else if (tags.hasKey("rotStart"))
            {
                instance.setDate(tags.getLong("rotStart"));
                instance.setTime(tags.getLong("rotTime"));
                instance.setForceId(tags.getByte("i"));
            }
            else
            {
                instance.setDate(tags.getLong("start"));
                instance.setTime(tags.getLong("time"));
                instance.setForceId(tags.getByte("i"));
            }
        }
    }
}
