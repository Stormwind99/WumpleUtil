package com.wumple.util.adapter;

import java.util.ArrayList;

import com.wumple.util.config.NameKeys;

import net.minecraft.tileentity.TileEntity;

public class TileEntityThing extends TileEntityThingBase implements IThing
{
    public TileEntityThing(TileEntity ownerIn)
    {
        super(ownerIn);
    }
    
    public ArrayList<String> getNameKeys()
    {
        return NameKeys.getNameKeys(owner);
    }
}
