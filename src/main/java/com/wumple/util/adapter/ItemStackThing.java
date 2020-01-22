package com.wumple.util.adapter;

import java.util.ArrayList;

import com.wumple.util.config.NameKeys;

import net.minecraft.item.ItemStack;

public class ItemStackThing extends ItemStackThingBase implements IThing
{
    public ItemStackThing(ItemStack ownerIn)
    {
        super(ownerIn);
    }
    
    public ArrayList<String> getNameKeys()
    {
        return NameKeys.getNameKeys(owner);
    }
}