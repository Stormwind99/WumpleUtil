package com.wumple.util.adapter;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class TileEntityThingBase extends ThingBase<TileEntity>
{
    public TileEntityThingBase(TileEntity ownerIn)
    {
        super(ownerIn);
    }
    
    @Override
    public World getWorld()
    {
        return isValid() ? get().getWorld() : null;
    }
    
    @Override
    public BlockPos getPos()
    {
        return isValid() ? get().getPos() : null;
    }

    @Override
    public boolean isInvalid()
    {
        return super.isInvalid() || get().isRemoved()
        	// HACK to avoid tick on cap owned by unloaded chunk
        	|| (get().getWorld() == null)
        	|| (get().getPos() == null)
            || (!get().getWorld().isAreaLoaded(get().getPos(), 1))
        	;
    }

    @Override
    public void markDirty()
    {
        if (isValid())
        {
        	get().markDirty();
        }
    }

    @Override
    public void invalidate()
    {
        if (isValid())
        {
            World world = getWorld();
            BlockPos pos = getPos();
            if ((world != null) && (pos != null))
            {
            	world.removeBlock(pos, false);
                world.removeTileEntity(pos);
            }
            get().remove();
            get().updateContainingBlockInfo();
        }
        super.invalidate();
    }

    @Override
    public boolean sameAs(IThing entity)
    {
        if (entity instanceof TileEntityThingBase)
        {
            return get() == ((TileEntityThingBase) entity).get();
        }
        return false;
    }
    
    @Override
    public ICapabilityProvider capProvider()
    { return get(); }
    
    @Override
    public void forceUpdate()
    {
        if (isValid())
        {
            BlockPos pos = getPos();
            World world = getWorld();
            if ((world != null) && (pos != null))
            {
                BlockState state = world.getBlockState(pos);
                // PORT world.markBlockRangeForRenderUpdate(pos, pos);
                world.notifyBlockUpdate(pos, state, state, 3);
                // PORT world.scheduleBlockUpdate(pos,owner.getBlockType(),0,0);
            }
            get().markDirty();
        }
    }
}
