package com.wumple.util.adapter;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class TUtil
{
    static public IThing to(ItemStack other)
    {
        return new ItemStackThing(other);
    }
    
    static public IThing to(TileEntity other)
    {
        return new TileEntityThing(other);
    }

    static public IThing to(Entity other)
    {
        return new EntityThing(other);
    }
}
