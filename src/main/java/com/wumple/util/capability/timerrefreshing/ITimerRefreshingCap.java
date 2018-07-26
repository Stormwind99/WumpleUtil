package com.wumple.util.capability.timerrefreshing;

import com.wumple.util.adapter.IThing;
import com.wumple.util.capability.eventtimed.Expiration;
import com.wumple.util.capability.tickingthing.ITickingThingCap;

public interface ITimerRefreshingCap<T extends IThing, W extends Expiration> extends ITickingThingCap<T>
{

    int getRatio();

}