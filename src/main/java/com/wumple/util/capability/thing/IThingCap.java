package com.wumple.util.capability.thing;

import com.wumple.util.adapter.IThing;

import net.minecraft.entity.EntityLiving;

public interface IThingCap<T extends IThing>
{

    void checkInit(T ownerIn);
    EntityLiving getOwner();

}