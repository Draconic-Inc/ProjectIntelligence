package com.brandon3055.projectintelligence;

import com.brandon3055.projectintelligence.client.ClientProxy;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.brandon3055.projectintelligence.utils.SSLFix;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.awt.*;

@Mod(ProjectIntelligence.MODID)
public class ProjectIntelligence {
    public static final String MODID = "projectintelligence";
    public static final String MODNAME = "Project Intelligence";
    public static final String VERSION = "${mod_version}";

    public static CommonProxy proxy;

    public ProjectIntelligence() {
        LogHelper.info("Hello Minecraft!!!");
        if (System.getProperty("java.awt.headless").equals("true")) {
            System.setProperty("java.awt.headless", "false");
            LogHelper.info("Disabled AWT Headless Mode so that PI editor can function");
        }
        SSLFix.fixSSL();
        proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        proxy.construct();
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        proxy.preInit(event);
    }

    @SubscribeEvent
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete(event);
    }

//
//    public void onClientSetup(FMLClientSetupEvent event) {
//
//    }
//
//    public void onServerSetup(FMLDedicatedServerSetupEvent event) {
//
//    }
//
//    public void onServerStarting(FMLServerStartingEvent event) {
//
//    }
}