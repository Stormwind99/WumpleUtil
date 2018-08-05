package com.wumple.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wumple.util.mod.ModBase;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, dependencies = Reference.DEPENDENCIES,
        updateJSON = Reference.UPDATEJSON, certificateFingerprint = Reference.FINGERPRINT)
public class WumpleUtil extends ModBase
{
    @Mod.Instance(Reference.MOD_ID)
    public static WumpleUtil instance;

    @EventHandler
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
    }
    
    @EventHandler
    @Override
    public void onFingerprintViolation(FMLFingerprintViolationEvent event)
    {
        logFingerprintViolationMessage(event, forceGetLogger());
    }

    @Override
    public Logger forceGetLogger()
    {
        return (logger != null) ? logger : LogManager.getLogger(Reference.MOD_ID);
    }
}
