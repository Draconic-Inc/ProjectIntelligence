package com.brandon3055.projectintelligence;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.command.BCUtilCommands;
import com.brandon3055.brandonscore.command.CommandTPX;
import com.brandon3055.projectintelligence.client.ClientProxy;
import com.brandon3055.projectintelligence.utils.LogHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.brandon3055.projectintelligence.utils.SSLFix;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;


//@Mod(modid = ProjectIntelligence.MODID, name = ProjectIntelligence.MODNAME, version = ProjectIntelligence.VERSION, /*guiFactory = ProjectIntelligence.GUI_FACTORY,*/ dependencies = ProjectIntelligence.DEPENDENCIES)
@Mod(ProjectIntelligence.MODID)
public class ProjectIntelligence {
    public static final String MODID = "projectintelligence";
    public static final String MODNAME = "Project Intelligence";
    public static final String VERSION = "${mod_version}";
    public static final String PROXY_CLIENT = "com.brandon3055.projectintelligence.client.ClientProxy";
    public static final String PROXY_SERVER = "com.brandon3055.projectintelligence.CommonProxy";
    public static final String DEPENDENCIES = "required-after:brandonscore@[" + BrandonsCore.VERSION + ",);before:nei;";
//    public static final String GUI_FACTORY = "com.brandon3055.projectintelligence.PIGuiFactory";

    public static CommonProxy proxy;

    public ProjectIntelligence() {
        LogHelper.info("Hello Minecraft!!!");
        SSLFix.fixSSL();

        proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
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


    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerSetup(FMLDedicatedServerSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {

    }
}