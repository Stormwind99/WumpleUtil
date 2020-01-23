package com.wumple.util;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.ConfigReloading;
import net.minecraftforge.fml.config.ModConfig.Loading;
//Name conflict: import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

// See
// https://github.com/McJty/YouTubeModding14/blob/master/src/main/java/com/mcjty/mytutorial/Config.java
// https://wiki.mcjty.eu/modding/index.php?title=Tut14_Ep6


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfiguration
{
	private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
	
	public static ForgeConfigSpec COMMON_CONFIG;
	public static ForgeConfigSpec CLIENT_CONFIG;
	
	public static final String CATEGORY_GENERAL = "General";
    public static final String CATEGORY_MATCHINGCONFIG = "MatchingConfig";
    public static final String CATEGORY_DEBUGGING = "Debugging";
   
    /*
    public static class General
    {
    	public static ForgeConfigSpec.IntValue tileEntityPlaceholderEvaluationInterval;
    	
        private static void setupConfig()
        {
        	COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        	
            //@Name("TileEntityPlaceholder default evaluation interval")       
            tileEntityPlaceholderEvaluationInterval = COMMON_BUILDER.comment("Default number of ticks between TileEntityPlaceholder evaluations")
                    .defineInRange("tileEntityPlaceholderEvaluationInterval", 20, 0, Integer.MAX_VALUE);
            
            COMMON_BUILDER.pop();
    	}
    }
    */
    
	public static class BlockRepairDebugging
	{
		public static ForgeConfigSpec.BooleanValue debug;
		public static ForgeConfigSpec.DoubleValue regrowModifier;
		public static ForgeConfigSpec.BooleanValue showRepairingBlocks;

		
		private static void setupConfig()
		{
			// @Config.Comment("Debugging options")
			COMMON_BUILDER.comment("Debugging settings").push(CATEGORY_DEBUGGING);

			// @Name("Debug mode")
			debug = COMMON_BUILDER.comment("Enable general debug features, display extra debug info").define("debug",
					false);
			
			showRepairingBlocks = COMMON_BUILDER.comment("Make repairing blocks visible").define("showRepairingBlocks",
					false);

			COMMON_BUILDER.pop();
		}
	}
    
    /*
    public static class MatchingConfig
    {
    	public static ForgeConfigSpec.BooleanValue addOreDictNames;
    	public static ForgeConfigSpec.BooleanValue addClassNames;
    	
        private static void setupConfig()
        {
        	// @Config.Comment("Options for the MatchingConfig classes that match strings to item types, etc.")
        	COMMON_BUILDER.comment("MatchingConfig settings").push(CATEGORY_MATCHINGCONFIG);

            //@Name("Add OreDict names to nameKeys")
        	addOreDictNames = COMMON_BUILDER.comment("Add the OreDict names for object to nameKeys for matching")
                    .define("addOreDictNames", true);
        	
            //@Name("Add class names to nameKeys")
        	addClassNames = COMMON_BUILDER.comment("Add the class names for entire class hierarchy of object to nameKeys for matching")
                    .define("addClassNames", false);
        	
            COMMON_BUILDER.pop();
        }
    }
    */
        
    public static class Debugging
    {
    	public static ForgeConfigSpec.BooleanValue debug;
    	public static ForgeConfigSpec.BooleanValue usePlaceholderTileEntity;
    
	    private static void setupConfig()
	    {   
	    	// @Config.Comment("Debugging options")
	    	COMMON_BUILDER.comment("Debugging settings").push(CATEGORY_DEBUGGING);
	    	
	        //@Name("Debug mode")
	     	debug = COMMON_BUILDER.comment("Enable general debug features, display extra debug info")
                    .define("debug", false);
	  
	     	/*
	        //@Name("Placeholder TileEntity")
	     	usePlaceholderTileEntity  = COMMON_BUILDER.comment("Use placeholder TileEntity to hold caps if block has none.  May crash.  May not persist.")
                    .define("usePlaceholderTileEntity", false);
			*/
	        	     
	    	COMMON_BUILDER.pop();
	    }
    }
    
    static
    {
    	//General.setupConfig();
    	//MatchingConfig.setupConfig();
        Debugging.setupConfig();
        BlockRepairDebugging.setupConfig();

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

          
    public static void loadConfig(ForgeConfigSpec spec, Path path) {

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    @SubscribeEvent
    public static void onLoad(final Loading configEvent) {

    }

    @SubscribeEvent
    public static void onReload(final ConfigReloading configEvent) {
    }
    
    public static void setupConfig(ModLoadingContext context)
    {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, COMMON_CONFIG);

        loadConfig(ModConfiguration.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve(Reference.MOD_ID + "-client.toml"));
        loadConfig(ModConfiguration.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve(Reference.MOD_ID + "-common.toml"));
    }

}
