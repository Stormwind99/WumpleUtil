package com.wumple.util.adapter;

import java.util.ArrayList;

import com.wumple.util.config.NameKeys;

import net.minecraft.entity.Entity;

public class EntityThing extends EntityThingBase implements IThing
{
    public EntityThing(Entity entity)
    {
        super(entity);
    }
    
    public ArrayList<String> getNameKeys()
    {
        return NameKeys.getNameKeys(get());
    }
}