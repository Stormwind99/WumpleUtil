package com.wumple.util.map;

/**
 * A simple rectangle (x1,z1) to (x2,z2)
 */
public class Rect
{
    /**
     * x1 top left x of rect z1 top left z of rect x2 bottom right x of rect z2 bottom right z of rect
     */
    public int x1, z1, x2, z2;

    public Rect()
    {
        x1 = z1 = x2 = z2 = 0;
    }

    /**
     * @param _x1
     *            top left x of rect
     * @param _z1
     *            top left z of rect
     * @param _x2
     *            bottom right x of rect
     * @param _z2
     *            bottom right z of rect
     */
    public Rect(int _x1, int _z1, int _x2, int _z2)
    {
        x1 = _x1;
        z1 = _z1;
        x2 = _x2;
        z2 = _z2;
    }
    
    /**
     * Construct rect with copy of other rect
     * 
     * @param other Rect to copy
     */
    public Rect(Rect other)
    {
        x1 = other.x1;
        z1 = other.z1;
        x2 = other.x2;
        z2 = other.z2;
    }
    
    /**
     * Clone a copy of this rect
     */
    public Rect clone()
    {
        Rect newRect = new Rect(this);
        return newRect;
    }

    /**
     * Pretty print rect info
     * 
     * @return string of rect info
     */
    public String toString()
    {
        return "(" + x1 + "," + z1 + ") (" + x2 + "," + z2 + ")";
    }
    
    /**
     * Pretty print rect info
     * 
     * @return string of rect info
     */
    public String str()
    {
        return toString();
    }

    /**
     * Get intersection of two Rects
     * 
     * @param r1
     *            first rectangle to test
     * @param r2
     *            second rectangle to test
     * @return intersection of r1 and r2 as Rect, or null if no intersection
     */
    public static Rect intersection(Rect r1, Rect r2)
    {
        int xmin = Math.max(r1.x1, r2.x1);
        int xmax = Math.min(r1.x2, r2.x2);
        if (xmax > xmin)
        {
            int zmin = Math.max(r1.z1, r2.z1);
            int zmax = Math.min(r1.z2, r2.z2);
            if (zmax > zmin)
            {
                return new Rect(xmin, zmin, xmax, zmax);
            }
        }
        return null;
    }
    
    /**
     * Create union of two rects (a min rect that encloses both rects)
     * @param r1 first Rect
     * @param r2 second Rect
     * @return new Rect that includes r1 and r2
     */
    public static Rect union(Rect r1, Rect r2)
    {
        int nx1 = Math.min(r1.x1, r2.x1);
        int nz1 = Math.min(r1.z1, r2.z1);
        int nx2 = Math.max(r1.x2, r2.x2);
        int nz2 = Math.max(r1.z2, r2.z2);
        return new Rect(nx1, nz1, nx2, nz2);
    }

    /**
     * Create union of this and another rect (a rect that encloses both rects)
     * @param r2 second Rect
     * @return new Rect that includes this rect and r2
     */
    public Rect union(Rect r2)
    {
        return union(this, r2);
    }
}
