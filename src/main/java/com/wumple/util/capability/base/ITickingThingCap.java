package com.wumple.util.capability.base;

import com.wumple.util.adapter.IThing;

public interface ITickingThingCap<T extends IThing>
{

    long TIME_NOT_SET = -1;

    long getLastCheckTime();

    void setLastCheckTime(long time);

    /**
     * Automatically adjust the use-by date on food items stored within to slow or stop rot
     */
    void evaluate();

}