package com.wumple.util.map;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class MapTranscription extends MapUtil
{
    /**
     * can we transcribe any map data from src to dest?
     * 
     * @param dest
     *            destination map
     * @param src
     *            source map
     * @param worldIn
     *            current world
     * @return true if any data would be copied, false if not
     */
    public static Boolean canTranscribeMap(final ItemStack dest, final ItemStack src, final World worldIn)
    {
        final MapData destMapData = getMapData(dest, worldIn);
        final MapData srcMapData = getMapData(src, worldIn);

        // find intersection
        final Rect ri = getMapDataIntersection(destMapData, srcMapData);

        return (ri != null);
    }

    /**
     * copy the map data from src to dest if possible
     * 
     * @param dest
     *            destination map
     * @param src
     *            source map
     * @param worldIn
     *            current world
     */
    public static void transcribeMap(final ItemStack dest, final ItemStack src, final World worldIn)
    {
        log("transcribeMap begin");

        final MapData destMapData = getMapData(dest, worldIn);
        final MapData srcMapData = getMapData(src, worldIn);

        // find intersection
        final Rect ri = getMapDataIntersection(destMapData, srcMapData);

        if (ri == null)
        {
            log("transcribeMap end - no intersection");
            return;
        }

        log("intersection area: " + ri.str());

        // now convert world space intersection into dest pixel space
        // dest pixel space is byte array of 128x128 with world xCenter and
        // zCenter in middle
        final int destScale = 1 << destMapData.scale;
        final int destSize = pixLength * destScale;
        final Rect dp = new Rect();
        dp.x1 = (ri.x1 - destMapData.xCenter + destSize / 2) / destScale;
        dp.z1 = (ri.z1 - destMapData.zCenter + destSize / 2) / destScale;
        dp.x2 = (ri.x2 - destMapData.xCenter + destSize / 2) / destScale;
        dp.z2 = (ri.z2 - destMapData.zCenter + destSize / 2) / destScale;

        final int dxsize = dp.x2 - dp.x1;
        final int dzsize = dp.z2 - dp.z1;
        final int scaleDiff = srcMapData.scale - destMapData.scale;

        log("destPixelSpace:   " + dp.str());
        log("size: (" + dxsize + "," + dzsize + ") scaleDiff " + scaleDiff);
        long pixelsEvaluated = 0;
        long pixelsCopied = 0;

        // walk dest pixels, copying appropriate pixel from src for each one
        for (int i = 0; i < dxsize; i++)
        {
            for (int j = 0; j < dzsize; j++)
            {

                // calculate world coord for pixel
                // destScale = destSize / pixLength
                final int wx = destMapData.xCenter - (destSize / 2) + ((dp.x1 + i) * destScale);
                final int wz = destMapData.zCenter - (destSize / 2) + ((dp.z1 + j) * destScale);

                // calculate destination index for pixel
                final int dz = (dp.z1 + j);
                final int dx = (dp.x1 + i);
                final int index = dz * pixLength + dx;

                if ((index >= 0) && (index < pixLength * pixLength))
                {
                    pixelsEvaluated++;
                    // only write to blank pixels in dest
                    if (isUnexploredColor(destMapData.colors[index]))
                    {
                        byte newColor = getPixelValueForWorldCoord(srcMapData, wx, wz, scaleDiff);
                        if (!isUnexploredColor(newColor))
                        {
                            destMapData.colors[index] = newColor;
                            destMapData.updateMapData(dx, dz);
                            pixelsCopied++;
                        }
                    }
                }
                /*
                 * // debug else { this.log("OOB2: ij ("+i+"," +j+") w ("+wx+","+wz+") index "+index); destMapData.markDirty(); return; }
                 */
            }
        }

        // mark map dirty so resent to clients and persisted
        if (pixelsCopied > 0)
        {
            destMapData.markDirty();
        }

        log("pixelsEvaluated: " + pixelsEvaluated);
        log("pixelsCopied: " + pixelsCopied);
        log("transcribeMap end - done");
    }

}
