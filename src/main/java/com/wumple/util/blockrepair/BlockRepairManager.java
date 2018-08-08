package com.wumple.util.blockrepair;

import com.wumple.util.Reference;
import com.wumple.util.misc.RegistrationHelpers;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/*
 * Based on CoroUtil's BlockRepairingBlock
 * from https://github.com/Corosauce/CoroUtil
 */
public class BlockRepairManager
{
    public static final String block_repairing_name = "repairing_block";

    @GameRegistry.ObjectHolder(Reference.MOD_ID + ":" + block_repairing_name)
    public static final Block blockRepairingBlock = null;

    @GameRegistry.ObjectHolder(Reference.MOD_ID + ":blank")
    public static final Block blockBlank = null;
    
    public static void log(String msg)
    {
        
    }
    
    public static boolean isDebugEnabled()
    {
        return false;
    }

    public BlockRepairManager()
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

    // override if usig a different TileEntityRepairingBlock class
    protected Class<? extends TileEntity> TileEntityRepairingBlockClass()
    {
        return TileEntityRepairingBlock.class;
    }

    /*
     * Register blocks, items, etc
     */

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        // used for replacing foliage with blank for shaders
        addBlock(event, BlockBlankFactory(), "blank");
        addBlock(event, BlockRepairingBlockFactory(), TileEntityRepairingBlockClass(), block_repairing_name);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event)
    {
        addItemBlock(event, new ItemBlock(blockRepairingBlock).setRegistryName(blockRepairingBlock.getRegistryName()));
    }

    protected void addBlock(RegistryEvent.Register<Block> event, Block block, Class<? extends TileEntity> tEnt, String unlocalizedName)
    {
        addBlock(event, block, tEnt, unlocalizedName, true);
    }

    protected void addBlock(RegistryEvent.Register<Block> event, Block block, Class<? extends TileEntity> tEnt, String unlocalizedName, boolean creativeTab)
    {
        addBlock(event, block, unlocalizedName, creativeTab);
        RegistrationHelpers.registerTileEntity(tEnt, unlocalizedName);
    }

    protected void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String unlocalizedName)
    {
        addBlock(event, parBlock, unlocalizedName, true);
    }

    protected void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String unlocalizedName, boolean creativeTab)
    {
        parBlock.setTranslationKey(getRegistryName(unlocalizedName));
        parBlock.setRegistryName(getNameDomained(unlocalizedName));

        parBlock.setCreativeTab(CreativeTabs.MISC);

        if (event != null)
        {
            event.getRegistry().register(parBlock);
        }
    }

    protected void addItemBlock(RegistryEvent.Register<Item> event, Item item)
    {
        event.getRegistry().register(item);
    }

    protected void addItem(RegistryEvent.Register<Item> event, Item item, String name)
    {
        item.setTranslationKey(getRegistryName(name));
        item.setRegistryName(getNameDomained(name));

        item.setCreativeTab(CreativeTabs.MISC);

        if (event != null)
        {
            event.getRegistry().register(item);
        }
    }

    public static String getRegistryName(String name)
    {
        return Reference.MOD_ID + "." + name;
    }

    public static String getNameDomained(String name)
    {
        return Reference.MOD_ID + ":" + name;
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

        world.setBlockState(pos, blockRepairingBlock.getDefaultState());
        TileEntity tEnt = world.getTileEntity(pos);
        if (tEnt instanceof TileEntityRepairingBlock)
        {
            // IBlockState state = world.getBlockState(pos);
            BlockRepairManager.log("set repairing block for pos: " + pos + ", " + oldState.getBlock());
            TileEntityRepairingBlock repairing = ((TileEntityRepairingBlock) tEnt);
            repairing.init(world, ticksToRepair, oldState, oldHardness, oldExplosionResistance);
            return repairing;
        }
        else
        {
            BlockRepairManager.log("failed to set repairing block for pos: " + pos);
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
            BlockRepairManager.log("cant use repairing block on: " + state);
            return false;
        }

        replaceBlockAndBackup(world, pos, ticks);
        return true;
    }
}