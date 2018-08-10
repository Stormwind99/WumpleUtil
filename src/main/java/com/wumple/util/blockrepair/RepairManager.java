package com.wumple.util.blockrepair;

import com.wumple.util.ModConfig;
import com.wumple.util.WumpleUtil;
import com.wumple.util.base.misc.Util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

/*
 * Based on CoroUtil's BlockRepairingBlock
 * from https://github.com/Corosauce/CoroUtil
 */
public class RepairManager
{   
    public static void log(String msg)
    {
        if (isDebugEnabled())
        {
            WumpleUtil.logger.info(msg);
        }
    }
    
    public static boolean isDebugEnabled()
    {
        return ModConfig.zdebugging.debug;
    }

    public RepairManager()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    protected Block getRepairingBlock()
    {
        return MyObjectHolder.blockRepairingBlock;
    }

    /*
     * Place repair block methods
     */

    /**
     *
     * Some mod blocks might require getting data only while their block is still around, so we get it here and save it rather than on the fly later
     *
     * @param world
     * @param pos
     */
    protected IRepairingTimes replaceBlockAndBackup(World world, BlockPos pos, int ticksToRepair)
    {
        IBlockState oldState = world.getBlockState(pos);
        float oldHardness = oldState.getBlockHardness(world, pos);
        float oldExplosionResistance = 1;
        try
        {
            oldExplosionResistance = oldState.getBlock().getExplosionResistance(world, pos, null, null);
        }
        catch (Exception ex)
        {

        }

        world.setBlockState(pos, getRepairingBlock().getDefaultState());
        TileEntity tEnt = world.getTileEntity(pos);
        IRepairing repairing = Util.as(tEnt, IRepairing.class);
        if (repairing != null)
        {
            // IBlockState state = world.getBlockState(pos);
            RepairManager.log("set repairing block for pos: " + pos + ", " + oldState.getBlock());
            repairing.init(world, ticksToRepair, oldState, oldHardness, oldExplosionResistance);
            return repairing;
        }
        else
        {
            RepairManager.log("failed to set repairing block for pos: " + pos);
            return null;
        }
    }

    protected boolean canConvertToRepairingBlock(World world, IBlockState state)
    {
        // should cover most all types we dont want to put into repairing state
        if (!state.isFullCube())
        {
            return false;
        }
        return true;
    }

    public boolean replaceBlock(World world, IBlockState state, BlockPos pos, int ticks)
    {
        if (!canConvertToRepairingBlock(world, state))
        {
            RepairManager.log("cant use repairing block on: " + state);
            return false;
        }

        replaceBlockAndBackup(world, pos, ticks);
        return true;
    }
    
    public boolean shouldRepair(EntityPlayer player, Block block, IBlockState blockstate, World world, BlockPos pos)
    {
        // override to change behavior
        return false;
    }
    
    public int getTicksToRepair()
    {
        // override to change behavior
        return 200;
    }

    // idea from LilRichy's RegrowableLeaves EventHandler.breakEvent()
    public void onBreak(BlockEvent.BreakEvent event)
    {
        BlockPos pos = event.getPos();
        IBlockState blockstate = event.getState();
        World world = event.getWorld();
        Block block = blockstate.getBlock();
        EntityPlayer player = event.getPlayer();
        
        // don't allow breaking repairing blocks normally
        TileEntity tEnt = world.getTileEntity(pos);
        IRepairing repairing = Util.as(tEnt, IRepairing.class);
        if (repairing != null)
        {
            event.setCanceled(true);
            return;
        }
        
        // only handle replacement on server
        if (world.isRemote)
        {
            return;
        }
        
        // check if should replace with repairing block
        if (shouldRepair(player, block, blockstate, world, pos))
        {
            // replace with repairing block
            replaceBlock(world, blockstate, pos, getTicksToRepair());
            
            // harvest original block
            block.harvestBlock(world, player, pos, blockstate, null, ItemStack.EMPTY);
            
            // don't handle event later since we did handled it
            event.setCanceled(true);
        }
    }

}