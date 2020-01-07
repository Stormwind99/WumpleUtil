package com.wumple.util.map;

import java.util.List;

import javax.annotation.Nullable;

import com.wumple.util.base.misc.MathUtil;
import com.wumple.util.xmap.XMapAPI;

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
            if (XMapAPI.getInstance().isFilledMap(inputStack))
            {
                MapData inputMapData = XMapAPI.getInstance().getMapData(inputStack, worldIn);
                Rect inputMapRect = MapDataUtil.getMapRect(inputMapData);
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
    
    public static class MapProps
    {
        public MapProps(int worldXIn, int worldZIn, int scaleIn)
            { worldX = worldXIn; worldZ = worldZIn; scale = scaleIn; }
        
        public int worldX;
        public int worldZ;
        public int scale;
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
        MapProps mapProps = getCreateMap(worldIn, inputs);

        if (mapProps == null)
        {
            return null;
        }

        int worldX = mapProps.worldX;
        int worldZ = mapProps.worldZ;
        int scale = MathHelper.clamp(mapProps.scale, 0, XMapAPI.getInstance().getMaxScale());

        // copy input maps onto new map
        ItemStack newStack = XMapAPI.getInstance().setupNewMap(worldIn, worldX, worldZ, (byte) scale, false, false);
        MapTranscription.doTranscribe(worldIn, newStack, inputs);

        return newStack;
    }
    
    /**
     * Create a new map that attempts to include all the input maps (if they can fit given map scale ranges possible)
     * 
     * @param worldIn
     * @param inputs
     * @return new map ItemStack that trys to include data from all input maps
     */
    @Nullable
    public static MapProps getCreateMap(World worldIn, List<ItemStack> inputs)
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

        // find center
        int worldX = overallRect.x1 + width / 2;
        int worldZ = overallRect.z1 + height / 2;

        return new MapProps(worldX, worldZ, rawScale);
    }
}
