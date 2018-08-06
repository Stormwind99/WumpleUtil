package com.wumple.util.placeholder;

import com.wumple.util.misc.RegistrationHelpers;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityPlaceholder extends TileEntity
{
    public static void register()
    {
        RegistrationHelpers.registerTileEntity(TileEntityPlaceholder.class, "wumpleutil:placeholder");
    }
    
    public void ensureInitialized(World world)
    {
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        
        World world = getWorld();
        
        if (world != null)
        {
            ensureInitialized(world);
        }
    }   
}
