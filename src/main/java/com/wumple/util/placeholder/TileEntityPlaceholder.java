package com.wumple.util.placeholder;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityPlaceholder extends TileEntity
{   
    public TileEntityPlaceholder() { super(); }
    
    public TileEntityPlaceholder(World worldIn)
    {
        super();
        setWorld(worldIn);
    }
    
    public void ensureInitialized(World world)
    {
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        
        World myWorld = getWorld();
        
        if (myWorld != null)
        {
            ensureInitialized(myWorld);
        }
    }   
}
