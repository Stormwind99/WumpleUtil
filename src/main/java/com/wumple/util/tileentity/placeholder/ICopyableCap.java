package com.wumple.util.tileentity.placeholder;

public interface ICopyableCap<T extends ICopyableCap<T> >
{
    void copyFrom(T other);
}