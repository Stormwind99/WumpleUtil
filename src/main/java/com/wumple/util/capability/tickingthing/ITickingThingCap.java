package com.wumple.util.capability.tickingthing;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.thing.IThingCap;

public interface ITickingThingCap<T extends IThing> extends IThingCap<T>
{

    long TIME_NOT_SET = -1;

    long getLastCheckTime();

    void setLastCheckTime(long time);

    /**
     * Do every tick
     */
    void always();
    
    /**
     * Do only so often
     */
    void evaluate();
}