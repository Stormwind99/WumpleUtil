package com.wumple.util;

import com.wumple.util.misc.RegistrationHelpers;
import com.wumple.util.placeholder.BlockPlaceholder;
import com.wumple.util.placeholder.TileEntityPlaceholder;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder("wumpleutil")
public class ModObjectHolder
{
    // ----------------------------------------------------------------------
    // Blocks

    @ObjectHolder("wumpleutil:placeholder")
    public static final Block placeholder = null;

    // ----------------------------------------------------------------------
    // Events

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class RegistrationHandler
    {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event)
        {
            final IForgeRegistry<Block> registry = event.getRegistry();

            RegistrationHelpers.regHelper(registry, new BlockPlaceholder());
        }
        
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event)
        {
            registerTileEntities();
        }
    
        public static void registerTileEntities()
        {
            RegistrationHelpers.registerTileEntity(TileEntityPlaceholder.class, "wumpleutil:placeholder");
        }
    }
}
