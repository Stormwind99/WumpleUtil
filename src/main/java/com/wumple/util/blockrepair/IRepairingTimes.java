package com.wumple.util.blockrepair;

public interface IRepairingTimes
{

    // override to change behavior when determining if repairing block has expired
    long getExpirationTimeLength();

    long getTimeToRepairAt();

    long getTimeToGiveUpAt();

}