package com.wumple.util.blockrepair;

/*
 * BlockRepair library
 * 
 * Ways to use:
 * 
 * a. Default behavior: 
 *    1. Create an instance of BlockRepairManager
 *    2. Call BlockRepairManager#replaceBlock(world, state, pos, ticks) for block (at pos) to remove and be repaired in ticks
 *    
 * b. Custom behavior (can have multiple instances with same or different behavior)
 *    1. Subclass BlockRepairManager, BlockRepairingBlock, and/or TileEntityRepairingBlock, overriding methods like:
 *       * BlockRepairManager#BlockBlankFactory, BlockRepairingBlockFactory, TileEntityRepairingBlockClass
 *       * BlockRepairingBlock#createNewTileEntity
 *       * TileEntityRepairingBlock#canRepairBlock, onCantRepairBlock   
 *    2. Create an instance of YourBlockRepairManager
 *    3. Call YourBlockRepairManager#replaceBlock(world, state, pos, ticks) for block (at pos) to remove and be repaired in ticks
 */

public class RepairFactory
{
    public static RepairManager proxy = null;
    
    RepairManager getManager()
    {
        if (proxy == null)
        {
            proxy = new RepairManager();
        }
        
        return proxy;
    }
}