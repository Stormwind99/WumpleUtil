package com.wumple.util.capability.eventtimed;

import com.wumple.util.adapter.IThing;
import com.wumple.util.misc.TimeUtil;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Wrapper class used to encapsulate information about an expiring timer.
 */
abstract public class Expiration extends ExpirationBase
{
    public static final int DIMENSIONRATIO_DEFAULT = 100;
    
    abstract protected long getTimerLength(IThing stack);
    
    public Expiration()
    {
        super();
    }

    public Expiration(long date, long time)
    {
        super(date, time);
    }

    public Expiration(Expiration other)
    {
        super(other);
    }

    public Expiration(NBTTagCompound tags)
    {
        super(tags);
    }

    public long getCurTime()
    {
        return TimeUtil.getLastWorldTimestamp();
    }    

    public int getPercent()
    {
        // make sure percent >= 0
        return Math.max(0, MathHelper.floor((double) (getCurTime() - date) / time * 100D));
    }

    public int getDaysLeft()
    {
        return Math.max(0, MathHelper.floor((double) (getCurTime() - date) / TICKS_PER_DAY));
    }

    public void setRelative(int dimensionRatio, IThing owner)
    {
        ratioShiftInternal(DIMENSIONRATIO_DEFAULT, dimensionRatio, owner);
    }

    protected long getDefaultTime(IThing owner)
    {
        long defaultTime = 0;
        long prop = getTimerLength(owner);

        if (prop != NO_EXPIRATION)
        {
            defaultTime = prop;
        }
        return defaultTime;
    }

    public void ratioShift(int fromRatio, int toRatio, IThing owner)
    {
        // if fromRatio is 0 then time value info was lost, so restore from props and apply toRatio
        if ((fromRatio == 0) && (toRatio != 0))
        {
            initTime(toRatio, owner);
            return;
        }

        ratioShiftInternal(fromRatio, toRatio, owner);
    }

    protected void ratioShiftInternal(int fromRatio, int toRatio, IThing owner)
    {
        if (toRatio == 0)
        {
            if ((fromRatio != 0) && (time != UNINITIALIZED))
            {
                // this would loose any relative to fresh date expiration info
                // but we encode any current non-zero expiration time into start date before zeroing
                // that newdate+defaultTime = oldDate+oldTime
                long defaultTime = getDefaultTime(owner);
                long addTime = (time == NO_EXPIRATION) ? 0 : time;
                // TODO date == UNINITIALIZED
                long newDate = date + addTime - defaultTime;
                
                setSafeValid(newDate, NO_EXPIRATION);
            }
            return;
        }

        if (isNonExpiring())
        {
            return;
        }

        int dimensionRatioShift = toRatio - fromRatio;

        // skip if no change - better precision than integer math
        if (dimensionRatioShift == 0)
        {
            return;
        }

        ratioShiftBase(dimensionRatioShift, owner);
    }

    protected void ratioShiftBase(int dimensionRatioShift, IThing owner)
    {
        long localTime = alterTime(dimensionRatioShift, getCurTime(), date, time);

        // debug ratio was already applied to time in checkInitialized() at initialization

        // USED TO: 
        // long clampedLocalTime = (localTime < 0) ? 0 : localTime;
        // setTimeSafe(clampedLocalTime);
        
        setTimeSafe(localTime);
    }
    
    public static long alterTime(int dimensionRatioShift, long now, long date, long time)
    {
        // let's alter time a bit for different dimensions
        long expirationTimeStamp = date + time;
        long left = expirationTimeStamp - now;
        long relativeLeft = shiftTime(dimensionRatioShift, left);
        long localTime = (now + relativeLeft) - date;

        return localTime;
    }
    
    public static long shiftTime(int dimensionRatioShift, long timeIn)
    {
        // skip if no change - better precision
        if (dimensionRatioShift == 0)
        {
            return timeIn;
        }

        double x = (double) dimensionRatioShift / DIMENSIONRATIO_DEFAULT;
        double y = (double) timeIn * Math.pow(2, x * -1);
        return (long) y;
    }
    
    protected void initTime(int dimensionRatio, IThing stack)
    {
        long timerLength = getTimerLength(stack);

        initTime(timerLength, dimensionRatio, stack);
    }

    protected void initTime(long timerLength, int dimensionRatio, IThing stack)
    {
        if (timerLength != NO_EXPIRATION)
        {
            setTimeSafe(timerLength);
            setRelative(dimensionRatio, stack);
        }
    }
    
    public int getChunkingPercent()
    {
        return 0;
    }

    public boolean checkInitialized(World world, IThing stack)
    {
        // if initialization not yet done (stack just created or was missed somehow), then do/fix it
        if (!isSet())
        {
            long timerLength = getTimerLength(stack);

            int ratio = getDimensionRatio(world);
            long curTime = getCurTime();
            long newDate = curTime;

            // chunk the start date of new items to increments of x% of local timer length time
            // that way same items created close in time will usually stack because they have the same expiration date and time
            int chunkingPercent = getChunkingPercent();
            if ((timerLength != NO_EXPIRATION) && (chunkingPercent != 0))
            {
                int ratioShift = ratio - DIMENSIONRATIO_DEFAULT;
                long shiftedTime = shiftTime(ratioShift, timerLength);
                long xPercentOfExpTime  = (shiftedTime * chunkingPercent) / 100;
                long chunk = (curTime / xPercentOfExpTime) + 1;
                
                // MAYBE newDate = Math.max(1, chunk * xPercentOfExpTime);
                newDate = chunk * xPercentOfExpTime;
            }
            
            setDateSafe(newDate);
           
            initTime(timerLength, ratio, stack);

            return false;
        }

        return true;
    }

    public int getDimensionRatio(int dimensionId)
    {
        String dimensionKey = Integer.toString(dimensionId);
        return getDimensionRatio(dimensionKey);
    }

    public int getDimensionRatio(String dimensionKey)
    {
        return DIMENSIONRATIO_DEFAULT;
    }

    public int getDimensionRatio(World world)
    {
        if (world == null)
        {
            return DIMENSIONRATIO_DEFAULT;
        }
            
        int dimensionId = world.provider.getDimension();
        return getDimensionRatio(dimensionId);
    }

    public boolean hasExpired()
    {
        long worldTimeStamp = getCurTime();
        return super.hasExpired(worldTimeStamp);
    }
    
    public void reschedule(long timeIn)
    {
        // skip reschedule if in no expiration mode - it would effectively double the amount of preservation when in a no-expiration dimension
        if (!isNonExpiring())
        {
            long worldTimeStamp = getCurTime();
            // TODO date == UNINITIALIZED
            long newDate = date + timeIn;
                                
            // don't allow items to go into negative expiration (example: with rot, aka super-fresh aka fresh date in future)
            long maxDate = worldTimeStamp;
            /*
            if (getChunkingPercent() != 0)
            {
                // experiment - factor chunking into comparison since chunking could make newDate in future
                // so if date already in future (from chunking), allow it to continue into future
                long oldDiff = worldTimeStamp - date;
                if (oldDiff < 1)
                {
                    // oldDiff will be negative, so this will increase maxDate
                    maxDate -= oldDiff;
                    maxDate += timeIn;
                    // maxData probably just equals newDate now
                }
            }
            */
            newDate = Math.min(newDate, maxDate);
            
            /*
            // setDateSafe avoiding UNINITIALIZED should take care of this
            // PAST: don't go negative date (or even special value 0) when negative-preserving
            if (newDate < 1)
            {
                // experiment: try reducing time if date would be <= 0
                long newTime = newDate - 1 + time;
                if (newTime < 0) newTime = 0;
                time = newTime;
                newDate = 1;
            }
            */
            
            setDateSafe(newDate);
        }
    }
}