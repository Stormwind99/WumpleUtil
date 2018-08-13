package com.wumple.util.capability.eventtimed;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

public class ExpirationBase
{
    public static final long TICKS_PER_DAY = 24000L;
    public static final int UNINITIALIZED = 0;
    public static final int NO_EXPIRATION = -1;
    
    /*
     * Values:
     * < 0 : rare but valid, caused by recrafting at beginning of world time
     * == 0 : UNINITIALIZED
     * > 0 : valid
     */
    public long date;
    
    /*
     * Values:
     * < -1: invalid!
     * -1 : NO_EXPIRATION
     * == 0: UNINITIALIZED
     * > 0: valid
     */
    public long time;

    public ExpirationBase()
    {
        super();
    }

    public ExpirationBase(long date, long time)
    {
        super();
        set(date, time);
    }

    public ExpirationBase(Expiration other)
    {
        super();
        set(other.date, other.time);
    }

    public ExpirationBase(NBTTagCompound tags)
    {
        super();
        this.readFromNBT(tags);
    }

    // MAYBE implement INBTSerializable

    public NBTTagCompound writeToNBT(NBTTagCompound tags)
    {
        if (tags != null)
        {
            tags.setLong("start", this.date);
            tags.setLong("time", this.time);
        }

        return tags;
    }

    public NBTTagCompound readFromNBT(NBTTagCompound tags)
    {
        if (tags != null)
        {
            setDate(tags.getLong("start"));
            setTime(tags.getLong("time"));
        }

        return tags;
    }

    /// direct setters and getters - no validation

    public long getDate()
    {
        return date;
    }

    public void setDate(long dateIn)
    {
        date = dateIn;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long timeIn)
    {
        time = timeIn;
    }

    public void set(long dateIn, long timeIn)
    {
        date = dateIn;
        time = timeIn;
    }

    // valid setters - allow setting only valid values

    public void setDateValid(long dateIn)
    {
        setDate(dateIn);
    }

    public void setTimeValid(long timeIn)
    {
        assert (timeIn >= NO_EXPIRATION);
        if (timeIn < NO_EXPIRATION)
        {
            timeIn = 1;
        }
        setTime(timeIn);
    }

    public void setValid(long dateIn, long timeIn)
    {
        setDateValid(dateIn);
        setTimeValid(timeIn);
    }

    // safe setters - allow setting only valid, initialized, expiring values

    public void setDateSafe(long dateIn)
    {
        // avoid UNINITIALIZED
        if (dateIn == UNINITIALIZED)
        {
            dateIn++;
        }

        setDate(dateIn);
    }

    public void setTimeSafe(long timeIn)
    {
        assert (timeIn > UNINITIALIZED);
        if (timeIn <= UNINITIALIZED)
        {
            timeIn = 1;
        }
        setTime(timeIn);
    }

    public void setSafe(long dateIn, long timeIn)
    {
        setDateSafe(dateIn);
        setTimeSafe(timeIn);
    }

    /// mixed setters

    public void setSafeValid(long dateIn, long timeIn)
    {
        setDateSafe(dateIn);
        setTimeValid(timeIn);
    }

    // misc getters

    public boolean isSet()
    {
        return (date != UNINITIALIZED);
    }

    public boolean isNonExpiring()
    {
        return (time == NO_EXPIRATION);
    }

    public long getExpirationTimestamp()
    {
        return date + time;
    }

    public int getDaysTotal()
    {
        return MathHelper.floor((double) time / TICKS_PER_DAY);
    }

    public int getUseBy()
    {
        return MathHelper.floor((double) (date + time) / TICKS_PER_DAY);
    }

    public boolean hasExpired(long worldTimeStamp)
    {
        if (isNonExpiring() || !isSet())
        {
            return false;
        }

        long relativeExpirationTimeStamp = getExpirationTimestamp();

        return (worldTimeStamp >= relativeExpirationTimeStamp);
    }
}