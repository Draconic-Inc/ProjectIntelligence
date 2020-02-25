package com.brandon3055.projectintelligence.client;

import com.brandon3055.brandonscore.integration.ModHelperBC;
import com.brandon3055.projectintelligence.CommonProxy;
import com.brandon3055.projectintelligence.api.PiAPI;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.guielements.GuiPartPageList;
import com.brandon3055.projectintelligence.client.keybinding.KeyInputHandler;
import com.brandon3055.projectintelligence.docmanagement.DocumentationManager;
import com.brandon3055.projectintelligence.docmanagement.DocumentationPage;
import com.brandon3055.projectintelligence.internal.PiAPIImpl;
import com.brandon3055.projectintelligence.registry.PluginLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLCommonSetupEvent event) {
        super.preInit(event);
        PIConfig.initialize();
        StyleHandler.initialize();
        DocumentationManager.initialize();
        //TODO Check on this
        ObfuscationReflectionHelper.setPrivateValue(PiAPI.class, null, PiAPIImpl.INSTANCE, "INSTANCE");
        PluginLoader.preInit(ModList.get().getAllScanData());

        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
        KeyInputHandler.init();
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        super.loadComplete(event);
        PluginLoader.loadComplete();
    }

    @Override
    public void openMainGui(Screen parentScreen, @Nullable String modid, @Nullable String page) {
        if (!(parentScreen instanceof GuiProjectIntelligence)) {
            Minecraft.getInstance().displayGuiScreen(new GuiProjectIntelligence(parentScreen));
        }

        //This is first launch and no documentation has been downloaded yet so return to avoid displaying errors
        if (!PIConfig.downloadsAllowed && !PIConfig.tutorialDisplayed) {
            return;
        }

        if (modid != null) {
            if (DocumentationManager.hasModPage(modid)) {
                DocumentationPage modPage = DocumentationManager.getModPage(modid);
                DisplayController.MASTER_CONTROLLER.getActiveTab().changePage(modPage.getPageURI());
            }
            else {
                String name = ModHelperBC.getModName(modid);
                PiAPIImpl.INSTANCE.displayError("Attempted to open documentation for mod " + (name == null ? modid : name) + " \nBut there is no documentation avalible for this mod.");
            }
        }
        else if (page != null) {
            if (DocumentationManager.hasPage(page)) {
                DisplayController.MASTER_CONTROLLER.getActiveTab().changePage(page);
            }
            else {
                PiAPIImpl.INSTANCE.displayError("Attempted to open documentation page " + page + " \nBut this page does not exist.");
            }
        }
    }

    @Override
    public void openMainGui(Screen parentScreen, List<String> pageURIs) {
        if (pageURIs.isEmpty()) return;
        GuiProjectIntelligence gui = new GuiProjectIntelligence(parentScreen);
        if (parentScreen instanceof GuiProjectIntelligence) {
            gui = (GuiProjectIntelligence) parentScreen;
        }
        else {
            Minecraft.getInstance().displayGuiScreen(gui);
        }

        List<String> validPages = new ArrayList<>();
        for (String page : pageURIs) {
            if (DocumentationManager.hasPage(page)) {
                validPages.add(page);
            }
            else {
                PiAPIImpl.INSTANCE.displayError("Attempted to open documentation page " + page + " \nBut this page does not exist.");
            }
        }

        if (validPages.isEmpty()) return;

        GuiPartPageList list = gui.getContainer().getPageList();
        if (list != null) {
            String page = validPages.get(0);
            DisplayController.MASTER_CONTROLLER.getActiveTab().changePage(page);
            list.setPageFilter(validPages);
        }
        else {
            PiAPIImpl.INSTANCE.displayError("An unknown error occurred while attempting to display pages!");
        }
    }
}
