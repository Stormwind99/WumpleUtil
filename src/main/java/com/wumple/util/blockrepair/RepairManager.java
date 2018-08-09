package com.wumple.util.blockrepair;

import com.wumple.util.ModConfig;
import com.wumple.util.WumpleUtil;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

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

    // override if using a different BlankBlock class
    protected BlockBlank BlockBlankFactory()
    {
        return new BlockBlank(Material.AIR);
    }

    // override if using a different BlockRepairingBlock class
    protected BlockRepairingBlock BlockRepairingBlockFactory()
    {
        return new BlockRepairingBlock();
    }

    // override if using a different TileEntityRepairingBlock class
    protected Class<? extends TileEntity> TileEntityRepairingBlockClass()
    {
        return TileEntityRepairingBlock.class;
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
    protected TileEntityRepairingBlock replaceBlockAndBackup(World world, BlockPos pos, int ticksToRepair)
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

        world.setBlockState(pos, MyObjectHolder.blockRepairingBlock.getDefaultState());
        TileEntity tEnt = world.getTileEntity(pos);
        if (tEnt instanceof TileEntityRepairingBlock)
        {
            // IBlockState state = world.getBlockState(pos);
            RepairManager.log("set repairing block for pos: " + pos + ", " + oldState.getBlock());
            TileEntityRepairingBlock repairing = ((TileEntityRepairingBlock) tEnt);
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
}