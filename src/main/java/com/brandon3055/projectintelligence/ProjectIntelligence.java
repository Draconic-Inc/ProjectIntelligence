package com.brandon3055.projectintelligence;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.projectintelligence.utils.LogHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ProjectIntelligence.MODID, name = ProjectIntelligence.MODNAME, version = ProjectIntelligence.VERSION, /*guiFactory = ProjectIntelligence.GUI_FACTORY,*/ dependencies = ProjectIntelligence.DEPENDENCIES)
public class ProjectIntelligence {
    public static final String MODID = "projectintelligence";
    public static final String MODNAME = "Project Intelligence";
    public static final String VERSION = "${mod_version}";
    public static final String PROXY_CLIENT = "com.brandon3055.projectintelligence.client.ClientProxy";
    public static final String PROXY_SERVER = "com.brandon3055.projectintelligence.CommonProxy";
    public static final String DEPENDENCIES = "required-after:brandonscore@[" + BrandonsCore.VERSION + ",);";
//    public static final String GUI_FACTORY = "com.brandon3055.projectintelligence.PIGuiFactory";

    //endregion

    @Mod.Instance(ProjectIntelligence.MODID)
    public static ProjectIntelligence instance;

    @SidedProxy(clientSide = ProjectIntelligence.PROXY_CLIENT, serverSide = ProjectIntelligence.PROXY_SERVER)
    public static CommonProxy proxy;

    public ProjectIntelligence() {
        LogHelper.info("Hello Minecraft!!!");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}