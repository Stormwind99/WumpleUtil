package com.wumple.util.nameable;

import net.minecraft.world.IWorldNameable;

public interface INameable extends IWorldNameable
{
    void setCustomName(String nameIn);

    String getRealName();
}