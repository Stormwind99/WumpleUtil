package com.wumple.util.tileentity.placeholder;

public interface ICopyableCap<T extends ICopyableCap<T> >
{
    public void copyFrom(T other);
}