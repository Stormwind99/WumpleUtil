package com.wumple.util.map;

import java.util.List;

import com.wumple.util.xmap.XMapAPI;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class MapTranscription extends MapDataUtil
{
    /**
     * can we transcribe any map data from src to dest?
     * 
     * @param dest destination map
     * @param src source map
     * @param worldIn current world
     * @return true if any data would be copied, false if not
     */
    public static Boolean canTranscribeMap(final ItemStack dest, final ItemStack src, final World worldIn)
    {
        final MapData destMapData = XMapAPI.getInstance().getMapData(dest, worldIn);
        final MapData srcMapData = XMapAPI.getInstance().getMapData(src, worldIn);

        // find intersection
        final Rect ri = getMapDataIntersection(destMapData, srcMapData);

        return (ri != null);
    }

    /**
     * copy the map data from src to dest if possible (even with different scales or locations - as long as there is overlap)
     * 
     * @param dest destination map
     * @param src source map
     * @param worldIn current world
     */
    public static void transcribeMap(final ItemStack dest, final ItemStack src, final World worldIn)
    {
        log("transcribeMap begin");

        // TODO dest needs new unique map data
        final MapData destMapData = XMapAPI.getInstance().getMapData(dest, worldIn);
        final MapData srcMapData = XMapAPI.getInstance().getMapData(src, worldIn);

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

    /**
     * Given a set of input maps and a target map, can the input maps be transcribed onto the target map?
     * 
     * @param worldIn
     * @param targetStack the target map
     * @param inputs the input maps
     * @return true if possible, false if not
     */
    public static boolean checkTranscribe(World worldIn, ItemStack targetStack, List<ItemStack> inputs)
    {
        // first, must have a map in target slot
        if ( ! XMapAPI.getInstance().isFilledMap(targetStack) )
        {
            return false;
        }
        
        int count = 0;
        for (ItemStack inputStack : inputs)
        {
            if (XMapAPI.getInstance().isFilledMap(inputStack))
            {
                if ( MapTranscription.canTranscribeMap(targetStack, inputStack, worldIn)  )
                {
                    count++;
                }
                else
                {
                    return false;
                }
            }
        } 
        
        return (count > 0);
    }

    /**
     * Given a set of input maps and a target map, transcribed each input map onto the target map
     * 
     * @param worldIn
     * @param targetStack the target map
     * @param inputs the input maps
     * @return number of maps transcribed
     */
    public static int doTranscribe(World worldIn, ItemStack targetStack, List<ItemStack> inputs)
    {
        int count = 0;
        
        for (ItemStack inputStack : inputs)
        {
            if (XMapAPI.getInstance().isFilledMap(inputStack))
            {
                MapTranscription.transcribeMap(targetStack, inputStack, worldIn);
                count++;
            }
        }
        
        return count;
    }
    
    // transcribe src map onto a deep copy of dest's map and make that copy newDest's mapdata
	public static void transcribeMapWithCopy(ItemStack newDest, ItemStack dest, ItemStack src, World worldIn)
	{
		MapUtil.forceDeepCloneMap(newDest, dest, worldIn);

		// transcribe map data from src onto newDest
		MapTranscription.transcribeMap(newDest, src, worldIn);
	}
	
	// copy dest to new ItemStack newDest, deep transcribe src onto newDest, and return newDest
	public static ItemStack transcribeMapWithCopy(ItemStack dest, ItemStack src, World worldIn)
	{
		ItemStack newDest = dest.copy();
				
		transcribeMapWithCopy(newDest, dest, src, worldIn);
		
		return newDest;
	}
}


