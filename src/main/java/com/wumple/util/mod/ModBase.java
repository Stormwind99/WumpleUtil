package com.wumple.util.mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wumple.util.Reference;

import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModBase
{
    public static Logger logger;

    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    public void onFingerprintViolation(FMLFingerprintViolationEvent event)
    {
        logFingerprintViolationMessage(event, forceGetLogger());
    }

    public Logger forceGetLogger()
    {
        return (logger != null) ? logger : LogManager.getLogger(Reference.MOD_ID);
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