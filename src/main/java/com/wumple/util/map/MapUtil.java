package com.wumple.util.map;

import java.util.function.Consumer;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multisets;

import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class MapUtil
{
    /**
     * Magic number for width and height of map data - not a constant elsewhere
     * 
     * @see MapData
     */
    public static final int pixLength = 128;
    
    /**
     * debug logger
     */
    protected static Consumer<String> logger = null;

    /**
     * set debug logger
     * 
     * @param loggerIn
     *            new logger
     */
    public static void setLogger(Consumer<String> loggerIn)
    {
        logger = loggerIn;
    }
    
    /**
     * debug logging
     * 
     * @param msg
     *            message to log
     */
    public static void log(final String msg)
    {
        if (logger != null)
        {
            logger.accept(msg);
        }
    }
    
    public static boolean isItemMap(ItemStack itemStack)
    {
        return itemStack.getItem() == Items.FILLED_MAP;
    }

    public static boolean isItemEmptyMap(ItemStack itemStack)
    {
        return itemStack.getItem() == Items.MAP;
    }
    
    /**
     * given a pixel coordinate i,j and scale, find the most common pixel within range
     * 
     * @param mapData
     *            map's pixels to search
     * @param i
     *            pixel coordinate x
     * @param j
     *            pixel coordinate y
     * @param scaleDiff
     *            scale difference of i,j
     * @return most common pixel color within (i,j) - [i+size,j+size]
     */
    public static byte getMapPixel(final MapData mapData, final int i, final int j, final int scaleDiff)
    {

        // case: multiple pixels being scaled down to one, choose the most
        // common pixel color like maps do normally
        if (scaleDiff < 0)
        {
            final int width = 1 << (scaleDiff * -1);
            final HashMultiset<Integer> hashmultiset = HashMultiset.create();
            for (int x = i; x < i + width; x++)
            {
                for (int y = j; y < j + width; y++)
                {
                    if ((x >= 0) && (x < pixLength) && (y >= 0) && (y < pixLength))
                        if ((i >= 0) && (i < pixLength) && (j >= 0) && (j < pixLength))
                        {
                            final int index = y * pixLength + x;
                            final byte color = mapData.colors[index];
                            // don't add transparent pixels
                            if (!isUnexploredColor(color))
                            {
                                hashmultiset.add(new Integer(color));
                            }
                        }
                }
            }

            final Integer pixelValue = Iterables.getFirst(Multisets.copyHighestCountFirst(hashmultiset), 0);
            return pixelValue.byteValue();
        }

        // MAYBETODO if scale > 1, blend between pixel values to avoid blockiness

        // case: return one pixel for one or fewer pixels
        if ((i >= 0) && (i < pixLength) && (j >= 0) && (j < pixLength))
        {
            final int index = j * pixLength + i;
            return mapData.colors[index];
        }

        // case: out of range
        return 0;
    }
    
    /**
     * get map pixel color given world coord x,y in mapData
     * 
     * @param mapData
     *            map's pixels to search
     * @param x
     *            world coord x
     * @param z
     *            world cord y
     * @param scaleDiff
     *            scale difference
     * @return map pixel color for x,y or 0 if not in map
     */
    static public byte getPixelValueForWorldCoord(final MapData mapData, final int x, final int z, final int scaleDiff)
    {
        final int scale = 1 << mapData.scale;
        final int size = pixLength * scale;

        // world scale 0 .. size
        final int wsx0 = x - mapData.xCenter + (size / 2);
        final int wsz0 = z - mapData.zCenter + (size / 2);

        // pixel scale 0 .. pixLength
        final int psx0 = wsx0 / scale;
        final int psz0 = wsz0 / scale;

        // get pixel value if coord within range
        return getMapPixel(mapData, psx0, psz0, scaleDiff);
    }

    /**
     * get rect representing a map's area
     * 
     * @param srcMapData
     *            map
     * @return Rect of map area in world coords
     */
    static public Rect getMapRect(final MapData srcMapData)
    {
        final int srcScale = 1 << srcMapData.scale;
        final int srcSize = pixLength * srcScale;
        final Rect r2 = new Rect();
        r2.x1 = srcMapData.xCenter - srcSize / 2;
        r2.z1 = srcMapData.zCenter - srcSize / 2;
        r2.x2 = srcMapData.xCenter + srcSize / 2;
        r2.z2 = srcMapData.zCenter + srcSize / 2;

        return r2;
    }

    /**
     * get intersection in of two map datas
     * 
     * @param destMapData
     *            map 1
     * @param srcMapData
     *            map 2
     * @return Rect of intersection in world coords, or null if no intersection
     */
    static public Rect getMapDataIntersection(final MapData destMapData, final MapData srcMapData)
    {
        if (srcMapData == null || destMapData == null)
        {
            return null;
        }

        // if maps are for different dimensions, nothing to do
        if (srcMapData.dimension != destMapData.dimension)
        {
            return null;
        }

        // calculate map corners
        final Rect r1 = getMapRect(destMapData);
        final Rect r2 = getMapRect(srcMapData);

        log("destMap area: " + r1.str());
        log("srcMap area:  " + r2.str());

        // find intersection
        Rect intersection = Rect.intersection(r1, r2);
        log("intersection: " + ((intersection == null) ? "none" : intersection.str()));
        return intersection;
    }

    /**
     * get MapData for given ItemStack
     * 
     * @param dest
     *            Itemstack to get MapData from
     * @param worldIn
     *            current world
     * @return MapData for a dest ItemStack, or null if not correct
     */
    static public MapData getMapData(final ItemStack dest, final World worldIn)
    {
        if (dest != null && dest != ItemStack.EMPTY && dest.getItem() instanceof ItemMap)
        {
            return Items.FILLED_MAP.getMapData(dest, worldIn);
        }

        return null;
    }

    /**
     * is color an unexplored map block color?
     * 
     * @param color
     *            to check
     * @return true if color represents unexplored color, false if not
     */
    static public boolean isUnexploredColor(final byte color)
    {
        // this is calc to detemine unexplored pixels from
        // net/minecraft/client/gui/MapItemRenderer.java
        return (color / 4 == 0);
    }
}
