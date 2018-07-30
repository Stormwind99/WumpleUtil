package com.wumple.util.capability.thing;

import com.wumple.util.adapter.IThing;

public interface IThingCap<T extends IThing>
{

    void checkInit(T ownerIn);
    public <X> X getOwnerAs(Class<X> x);
    
    public byte getForceId();
    public void setForceId(byte newid);
    public void forceUpdate();
    public T getOwner();
}