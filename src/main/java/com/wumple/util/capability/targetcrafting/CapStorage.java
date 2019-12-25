package com.wumple.util.capability.targetcrafting;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class CapStorage<U extends IContainerCraftingOwner> implements IStorage<U>
{
    @Override
    public NBTBase writeNBT(Capability<U> capability, U instance, EnumFacing side)
    {
        if (instance != null)
        {
            return instance.serializeNBT();
        }

        // Attempted workaround for https://github.com/Stormwind99/FoodFunk/issues/56
        // Some mods are trying to serialize capabilities during Forge mod init!
        // Then they crash with foodfunk:rot NBT data
        // Used to:
        // return null;
        NBTTagCompound tags = new NBTTagCompound();
        return tags;
    }

    @Override
    public void readNBT(Capability<U> capability, U instance, EnumFacing side, NBTBase nbt)
    {
        if ((nbt != null) && (instance != null))
        {
            instance.deserializeNBT(nbt);
        }
    }
}