package com.wumple.util.placeholder;

import com.wumple.util.misc.RegistrationHelpers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder("wumpleutil")
public class MyObjectHolder
{
    // ----------------------------------------------------------------------
    // Blocks

    //@ObjectHolder("wumpleutil:placeholder")
    public static /*final*/ Block placeholder = null;

    // ----------------------------------------------------------------------
    // Events

    @EventBusSubscriber
    public static class RegistrationHandler
    {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event)
        {
            final IForgeRegistry<Block> registry = event.getRegistry();

            placeholder = RegistrationHelpers.regHelper(registry, new BlockPlaceholder());
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
