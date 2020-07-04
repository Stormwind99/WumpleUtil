package com.wumple.util.blockrepair;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder("wumpleutil")
public class MyObjectHolder
{    
    // ----------------------------------------------------------------------
    // Blocks, Items, etc.

    //@ObjectHolder("wumpleutil:repairing_block")
    public static Block blockRepairingBlock = null;

    //@ObjectHolder("wumpleutil:blank")
    public static Block blockBlank = null;
    
    //@ObjectHolder("wumpleutil:repairing_block")
    public static BlockItem itemBlockRepairing = null;
    
    @ObjectHolder("wumpleutil:repairing_block")
	public static TileEntityType<TileEntityRepairingBlock> RepairingBlock_Tile;
    
    // ----------------------------------------------------------------------
    // Events

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler
    {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event)
        {
            final IForgeRegistry<Block> registry = event.getRegistry();
            
            // used for replacing foliage with blank for shaders
            blockBlank = new BlockBlank();
            blockBlank.setRegistryName("wumpleutil:blank");
            registry.register(blockBlank);
            
            blockRepairingBlock = new BlockRepairingBlock();
            blockRepairingBlock.setRegistryName("wumpleutil:repairing_block");
            RenderTypeLookup.setRenderLayer(blockRepairingBlock, RenderType.getCutout());
            registry.register(blockRepairingBlock);
        }
    
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event)
        {
            final IForgeRegistry<Item> registry = event.getRegistry();
            
            Item.Properties properties = new Item.Properties();
            
            itemBlockRepairing = new BlockItem(blockRepairingBlock, properties);
            itemBlockRepairing.setRegistryName("wumpleutil:repairing_block");
            
            registry.register(itemBlockRepairing);
        }
        
		@SubscribeEvent
		public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event)
		{
			event.getRegistry().register(TileEntityType.Builder.create(TileEntityRepairingBlock::new, blockRepairingBlock)
					.build(null).setRegistryName("wumpleutil:repairing_block"));
		}
    }
}