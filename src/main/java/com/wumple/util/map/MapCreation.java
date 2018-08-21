package com.wumple.util.map;

import java.util.List;

import javax.annotation.Nullable;

import com.wumple.util.base.misc.MathUtil;

import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class MapCreation
{
    /**
     * Find a rectangle containing all the input maps
     * 
     * @param worldIn
     * @param inputs
     * @return Rect containing all input maps
     */
    @Nullable
    public static Rect checkCreateRect(World worldIn, List<ItemStack> inputs)
    {
        Rect overallRect = null;

        for (ItemStack inputStack : inputs)
        {
            if (MapUtil.isItemMap(inputStack))
            {
                MapData inputMapData = MapUtil.getMapData(inputStack, worldIn);
                Rect inputMapRect = MapUtil.getMapRect(inputMapData);
                if (overallRect == null)
                {
                    overallRect = inputMapRect.clone();
                }
                else
                {
                    overallRect = Rect.union(overallRect, inputMapRect);
                }
            }
            else if ((inputStack != null) && !inputStack.isEmpty())
            {
                return null;
            }
        }

        return overallRect;
    }

    /**
     * Create a new map that attempts to include all the input maps (if they can fit given map scale ranges possible)
     * 
     * @param worldIn
     * @param inputs
     * @return new map ItemStack that trys to include data from all input maps
     */
    @Nullable
    public static ItemStack doCreate(World worldIn, List<ItemStack> inputs)
    {
        Rect overallRect = checkCreateRect(worldIn, inputs);

        if (overallRect == null)
        {
            return null;
        }

        // find largest dimension
        int width = overallRect.x2 - overallRect.x1;
        int height = overallRect.z2 - overallRect.z1;
        int max = Math.max(width, height);

        // semi-reverse of MapData.calculateMapCenter()
        int max128 = max / 128;
        double logMax128 = MathUtil.log2(max128);
        int rawScale = (int) Math.ceil(logMax128);
        int scale = MathHelper.clamp(rawScale, 0, 4);

        // find center
        int worldX = overallRect.x1 + width / 2;
        int worldZ = overallRect.z1 + height / 2;

        // copy input maps onto new map
        ItemStack newStack = ItemMap.setupNewMap(worldIn, (double) worldX, (double) worldZ, (byte) scale, false, false);
        MapTranscription.doTranscribe(worldIn, newStack, inputs);

        return newStack;
    }

    /**
     * Create a copy of a source map
     * 
     * @param srcStack the source map
     * @return the new map or null if not possible
     */
    @Nullable
    public static ItemStack copyMap(ItemStack srcStack)
    {
        if (!MapUtil.isItemMap(srcStack))
        {
            return null;
        }

        ItemStack destStack = new ItemStack(Items.FILLED_MAP, 1, srcStack.getMetadata());

        if (srcStack.hasDisplayName())
        {
            destStack.setStackDisplayName(srcStack.getDisplayName());
        }

        if (srcStack.hasTagCompound())
        {
            destStack.setTagCompound(srcStack.getTagCompound());
        }

        return destStack;
    }
}