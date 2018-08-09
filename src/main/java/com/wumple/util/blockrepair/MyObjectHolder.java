package com.wumple.util.blockrepair;

import com.wumple.util.Reference;
import com.wumple.util.misc.RegistrationHelpers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder("wumpleutil")
public class MyObjectHolder
{    
    // ----------------------------------------------------------------------
    // Blocks, Items, etc.

    //@ObjectHolder("wumpleutil:repairing_block")
    public static /*final*/ Block blockRepairingBlock = null;

    //@ObjectHolder("wumpleutil:blank")
    public static /*final*/ Block blockBlank = null;
    
    //@ObjectHolder("flourishingfoilage:leaves_repairing")
    public static /*final*/ ItemBlock itemBlockRepairing = null;
    
    // ----------------------------------------------------------------------
    // Events

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class RegistrationHandler
    {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event)
        {
            final IForgeRegistry<Block> registry = event.getRegistry();
            
            // used for replacing foliage with blank for shaders
            blockBlank = RegistrationHelpers.regHelper(registry, new BlockBlank(), "wumpleutil:blank");
            blockRepairingBlock = RegistrationHelpers.regHelper(registry, new BlockRepairingBlock(), "wumpleutil:repairing_block");
        }
    
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event)
        {
            final IForgeRegistry<Item> registry = event.getRegistry();
            
            itemBlockRepairing = RegistrationHelpers.registerItemBlock(registry, blockRepairingBlock);
    
            registerTileEntities();
        }
    
        public static void registerTileEntities()
        {
            RegistrationHelpers.registerTileEntity(TileEntityRepairingBlock.class, "wumpleutil:repairing_block");
        }
    }
}