package com.wumple.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLFingerprintViolationEvent;

@Mod(Reference.MOD_ID)
public class WumpleUtil /* PORT extends ModBase */ 
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public WumpleUtil()
    {
        ModConfig.setupConfig();
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onFingerprintViolation(final FMLFingerprintViolationEvent event)
    {
    	LOGGER.warn("Invalid fingerprint detected! The file " + event.getSource().getName()
    			+ " may have been tampered with. This version will NOT be supported by the author!");
    	LOGGER.warn("Expected " + event.getExpectedFingerprint() + " found " + event.getFingerprints().toString());
    }
}
