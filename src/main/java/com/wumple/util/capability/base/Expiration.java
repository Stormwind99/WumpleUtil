package com.wumple.util.capability.base;

import com.wumple.util.misc.TimeUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Wrapper class used to encapsulate information about an expiring timer.
 */
abstract public class Expiration
{
    public static final long TICKS_PER_DAY = 24000L;
    public static final int NO_EXPIRATION = -1;
    public static final int DIMENSIONRATIO_DEFAULT = 100;
    
    /*
     * The timestamp at which the timer is at 0% - creation time at first, but can be advanced (example: rot by preserving containers aka the fresh date)
     */
    public long date;
    
    /*
     * The amount of time the timer takes to expire. The timestamp at which expires is date + time
     */
    public long time;

    public Expiration()
    {
    }

    public Expiration(long date, long time)
    {
        set(date, time);
    }

    /*
    public RotInfo(IRot tank)
    {
        set(tank.getDate(), tank.getTime());
    }
    */

    public Expiration(Expiration other)
    {
        set(other.date, other.time);
    }

    public Expiration(NBTTagCompound tags)
    {
        this.readFromNBT(tags);
    }

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

    public void setDateSafe(long dateIn)
    {
        setDate(dateIn);
    }

    public void setTimeSafe(long timeIn)
    {
        assert (timeIn >= 0);
        setTime(timeIn);
    }

    public void setSafe(long dateIn, long timeIn)
    {
        setDateSafe(dateIn);
        setTimeSafe(timeIn);
    }

    public long getCurTime()
    {
        return TimeUtil.getLastWorldTimestamp();
    }    

    public long getExpirationTimestamp()
    {
        return date + time;
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

    public int getDaysTotal()
    {
        return MathHelper.floor((double) time / TICKS_PER_DAY);
    }

    public int getUseBy()
    {
        return MathHelper.floor((double) (date + time) / TICKS_PER_DAY);
    }

    public boolean isSet()
    {
        return (date > 0);
    }

    public boolean isNonExpiring()
    {
        return (time == NO_EXPIRATION);
    }

    public void setRelative(int dimensionRatio, ItemStack owner)
    {
        ratioShiftInternal(DIMENSIONRATIO_DEFAULT, dimensionRatio, owner);
    }

    protected long getDefaultTime(ItemStack owner)
    {
        long defaultTime = 0;
        long prop = getTimerLength(owner);

        if (prop != NO_EXPIRATION)
        {
            defaultTime = prop;
        }
        return defaultTime;
    }

    /*
    protected long getDefaultTime(ItemStack owner)
    {
        long defaultTime = 0;
        RotProperty rotProps = ConfigHandler.rotting.getRotProperty(owner);

        if ((rotProps != null) && rotProps.doesRot())
        {
            defaultTime = rotProps.getRotTime();
        }
        return defaultTime;
    }
    */

    public void ratioShift(int fromRatio, int toRatio, ItemStack owner)
    {
        // if fromRatio is 0 then time value info was lost, so restore from props and apply toRatio
        if ((fromRatio == 0) && (toRatio != 0))
        {
            initTime(toRatio, owner);
            return;
        }

        ratioShiftInternal(fromRatio, toRatio, owner);
    }

    protected void ratioShiftInternal(int fromRatio, int toRatio, ItemStack owner)
    {
        if (toRatio == 0)
        {
            if ((fromRatio != 0) && (time != 0))
            {
                // this would loose any relative to fresh date expiration info
                // but we encode any current non-zero expiration time into start date before zeroing
                // that newdate+defaultTime = oldDate+oldTime
                long defaultTime = getDefaultTime(owner);
                long newDate = Math.max(1, date + time - defaultTime);

                // set without range checking
                set(newDate, NO_EXPIRATION);
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

    protected void ratioShiftBase(int dimensionRatioShift, ItemStack owner)
    {
        long localTime = alterTime(dimensionRatioShift, getCurTime(), date, time);

        // debug ratio was already applied to time in checkInitialized() at initialization

        long clampedLocalTime = (localTime < 0) ? 0 : localTime;

        setTimeSafe(clampedLocalTime);
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
    
    abstract protected long getTimerLength(ItemStack stack);
    
    /*
    protected long getTimerLength(ItemStack owner)
    {
        long defaultTime = NO_EXPIRATION;
        RotProperty rotProps = ConfigHandler.rotting.getRotProperty(owner);

        if ((rotProps != null) && rotProps.doesRot())
        {
            defaultTime = rotProps.getRotTime();
        }
        return defaultTime;
    }
    */
    
    protected void initTime(int dimensionRatio, ItemStack stack)
    {
        long timerLength = getTimerLength(stack);

        initTime(timerLength, dimensionRatio, stack);
    }

    protected void initTime(long timerLength, int dimensionRatio, ItemStack stack)
    {
        if (timerLength != NO_EXPIRATION)
        {
            setTimeSafe(timerLength);
            setRelative(dimensionRatio, stack);
        }
    }
    
    /*
    protected void initTime(int dimensionRatio, ItemStack stack)
    {
        RotProperty rotProps = ConfigHandler.rotting.getRotProperty(stack);

        initTime(rotProps, dimensionRatio, stack);
    }
    
    protected void initTime(RotProperty rotProps, int dimensionRatio, ItemStack stack)
    {
        if ((rotProps != null) && rotProps.doesRot())
        {
            setTimeSafe(rotProps.getRotTime());
            setRelative(dimensionRatio, stack);
        }
    }
    */
    
    public int getChunkingPercent()
    {
        return 0;
    }
    /*
    public int getChunkingPercent()
    {
        return ConfigContainer.rotting.chunkingPercentage;
    }
    */

    public boolean checkInitialized(World world, ItemStack stack)
    {
        // if initialization not yet done (stack just created or was missed somehow), then do/fix it
        if (date == 0)
        {
            long timerLength = getTimerLength(stack);

            int ratio = getDimensionRatio(world);
            long curTime = getCurTime();
            long newTime = curTime;

            // chunk the start date of new items to increments of x% of local timer length time
            // that way same items created close in time will usually stack because they have the same expiration date and time
            int chunkingPercent = getChunkingPercent();
            if ((timerLength != NO_EXPIRATION) && (chunkingPercent != 0))
            {
                int ratioShift = ratio - DIMENSIONRATIO_DEFAULT;
                long shiftedTime = shiftTime(ratioShift, timerLength);
                long xPercentOfExpTime  = (shiftedTime * chunkingPercent) / 100;
                long chunk = (curTime / xPercentOfExpTime) + 1;
                
                newTime = Math.max(1, chunk * xPercentOfExpTime);
            }
            
            setDateSafe(newTime);
           
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
    /*
    public int getDimensionRatio(String dimensionKey)
    {
        return ConfigContainer.modifiers.dimensionRatios.getOrDefault(dimensionKey, ConfigHandler.DIMENSIONRATIO_DEFAULT);
    }
    */

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
        if (isNonExpiring())
        {
            return false;
        }
        
        long worldTimeStamp = getCurTime();
        long relativeExpirationTimeStamp = getExpirationTimestamp();

        return (worldTimeStamp >= relativeExpirationTimeStamp);
    }
    
    public void reschedule(long timeIn)
    {
        // skip reschedule if in no expiration mode - it would effectively double the amount of preservation when in a no-expiration dimension
        if (!isNonExpiring())
        {
            long worldTimeStamp = getCurTime();
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
            
            // don't go negative date (or even special value 0) when negative-preserving
            if (newDate < 1)
            {
                // experiment: try reducing time if date would be <= 0
                long newTime = newDate - 1 + time;
                if (newTime < 0) newTime = 0;
                time = newTime;
                newDate = 1;
            }
            
            date = newDate;
        }
    }
}