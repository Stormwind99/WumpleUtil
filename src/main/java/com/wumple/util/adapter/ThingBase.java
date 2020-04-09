package com.wumple.util.adapter;

import java.lang.ref.WeakReference;

abstract public class ThingBase<T> implements IThingBase
{
	// using a weak reference to avoid TileEntity->Capability->TileEntity circular dependency
	protected WeakReference<T> wowner = null;

	public ThingBase(T ownerIn)
	{
		wowner = new WeakReference<T>(ownerIn);
	}

	public T get()
	{
		return wowner.get();
	}

	@Override
	public Object object()
	{
		return get();
	}
	
    @Override
    public boolean isInvalid()
    {
        return (get() == null);
    }
    
    @Override
    public void invalidate()
    {
        wowner.clear();
    }
}
