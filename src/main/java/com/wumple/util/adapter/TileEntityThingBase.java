package com.wumple.util.adapter;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class TileEntityThingBase implements IThingBase
{
    public TileEntity owner = null;

    public TileEntityThingBase(TileEntity ownerIn)
    {
        owner = ownerIn;
    }

    @Override
    public World getWorld()
    {
        return (owner != null) ? owner.getWorld() : null;
    }
    
    @Override
    public BlockPos getPos()
    {
        return (owner != null) ? owner.getPos() : null;
    }

    @Override
    public boolean isInvalid()
    {
        return (owner == null) || owner.isInvalid();
    }

    @Override
    public void markDirty()
    {
        if (owner != null) { owner.markDirty(); }
    }

    @Override
    public void invalidate()
    {
        if (owner != null)
        {
            World world = getWorld();
            BlockPos pos = getPos();
            if ((world != null) && (pos != null))
            {
                world.setBlockToAir(pos);
                world.removeTileEntity(pos);
            }
            owner.invalidate();
            owner.updateContainingBlockInfo();
        }
        owner = null;
    }

    @Override
    public boolean sameAs(IThing entity)
    {
        if (entity instanceof TileEntityThingBase)
        {
            return owner == ((TileEntityThingBase) entity).owner;
        }
        return false;
    }
    
    @Override
    public Object object()
    { return owner; }
        
    @Override
    public ICapabilityProvider capProvider()
    { return owner; }
    
    @Override
    public void forceUpdate()
    {
        if (owner != null)
        {
            BlockPos pos = getPos();
            World world = getWorld();
            if ((world != null) && (pos != null))
            {
                IBlockState state = world.getBlockState(pos);
                world.markBlockRangeForRenderUpdate(pos, pos);
                world.notifyBlockUpdate(pos, state, state, 3);
                world.scheduleBlockUpdate(pos,owner.getBlockType(),0,0);
            }
            owner.markDirty();
        }
    }
}
