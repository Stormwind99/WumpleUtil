package com.wumple.util.mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wumple.util.Reference;

import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModBase
{
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
    
    @EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event)
    {
        logFingerprintViolationMessage(event, forceGetLogger());
    }
    
    public Logger getLogger()
    {
        return logger;
    }
    
    public Logger getLoggerFromManager()
    {
        return LogManager.getLogger(Reference.MOD_ID);
    }

    public Logger forceGetLogger()
    {
        return (logger != null) ? logger : getLoggerFromManager();
    }

    public void logFingerprintViolationMessage(FMLFingerprintViolationEvent event, Logger log)
    {
        if (log != null)
        {
            log.warn("Invalid fingerprint detected! The file " + event.getSource().getName()
                    + " may have been tampered with. This version will NOT be supported by the author!");
            log.warn("Expected fingerprint \"" + event.getExpectedFingerprint() + "\" found \"" + event.getFingerprints().toString() + "\"");
        }
    }
}