package com.wumple.util;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MOD_ID)
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModConfig
{
    @Name("Matching config")
    @Config.Comment("Options for the MatchingConfig classes that match strings to item types, etc.")
    public static MatchingConfig matchingConfig = new MatchingConfig();

    public static class MatchingConfig
    {
        @Name("Add OreDict names to nameKeys")
        @Config.Comment("Add the OreDict names for object to nameKeys for matching.")
        public boolean addOreDictNames = true;
        
        @Name("Add class names to nameKeys")
        @Config.Comment("Add the class names for entire class hierarchy of object to nameKeys for matching.")
        public boolean addClassNames = false;
    }    

    @Name("Debugging")
    @Config.Comment("Debugging options")
    public static Debugging zdebugging = new Debugging();

    public static class Debugging
    {
        @Name("Debug mode")
        @Config.Comment("Enable debug features on this menu, display extra debug info.")
        public boolean debug = false;
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    private static class EventHandler
    {
        /**
         * Inject the new values and save to the config file when the config has been changed from the GUI.
         *
         * @param event
         *            The event
         */
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(Reference.MOD_ID))
            {
                ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
