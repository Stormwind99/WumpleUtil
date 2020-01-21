package com.wumple.util.adapter;

import java.util.ArrayList;

import com.wumple.util.config.MatchingConfig;

import net.minecraft.item.ItemStack;

public class ItemStackThing extends ItemStackThingBase implements IThing
{
    public ItemStackThing(ItemStack ownerIn)
    {
        super(ownerIn);
    }
    
    public ArrayList<String> getNameKeys()
    {
        return MatchingConfig.getNameKeys(owner);
    }
}