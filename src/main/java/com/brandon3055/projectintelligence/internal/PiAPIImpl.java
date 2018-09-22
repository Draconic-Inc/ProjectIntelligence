package com.brandon3055.projectintelligence.internal;

import com.brandon3055.projectintelligence.ProjectIntelligence;
import com.brandon3055.projectintelligence.api.internal.IPiAPI;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.keybinding.KeyInputHandler;
import com.brandon3055.projectintelligence.docmanagement.DocumentationManager;
import com.brandon3055.projectintelligence.docmanagement.DocumentationPage;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by brandon3055 on 7/24/2018.
 */
public class PiAPIImpl implements IPiAPI {

    public static final PiAPIImpl INSTANCE = new PiAPIImpl();

    @Override
    public void openGui(@Nullable GuiScreen parentScreen) {
        ProjectIntelligence.proxy.openMainGui(parentScreen, null, null);
    }

    @Override
    public void openGui(@Nullable GuiScreen parentScreen, String pageURI) {
        ProjectIntelligence.proxy.openMainGui(parentScreen, null, pageURI);
    }

    @Override
    public void openGui(@Nullable GuiScreen parentScreen, List<String> pageURIs) {
        ProjectIntelligence.proxy.openMainGui(parentScreen, pageURIs);
    }

    @Override
    public void openModPage(@Nullable GuiScreen parentScreen, String modid) {
        ProjectIntelligence.proxy.openMainGui(parentScreen, modid, null);
    }

    @Override
    public List<String> getPageList() {
        return ImmutableList.copyOf(DocumentationManager.getAllPageURIs());
    }

    @Override
    public List<String> getModPageList(String modid) {
        List<String> pages = new ArrayList<>();

        for (DocumentationPage page : DocumentationManager.getAllPages()) {
            if (page.getModid().equals(modid)) {
                pages.add(page.getPageURI());
            }
        }

        return ImmutableList.copyOf(pages);
    }

    @Override
    public List<String> getRelatedPages(ItemStack stack) {
        return ImmutableList.copyOf(DocumentationManager.getRelatedPages(stack).stream().map(DocumentationPage::getPageURI).collect(Collectors.toList()));
    }

    @Override
    public List<String> getRelatedPages(String entityName) {
        return ImmutableList.copyOf(DocumentationManager.getRelatedPages(entityName).stream().map(DocumentationPage::getPageURI).collect(Collectors.toList()));
    }

    @Override
    public List<String> getRelatedPages(Fluid fluid) {
        return ImmutableList.copyOf(DocumentationManager.getRelatedPages(fluid).stream().map(DocumentationPage::getPageURI).collect(Collectors.toList()));
    }

    @Override
    public void displayError(String error, boolean noRepeat) {
        PIGuiHelper.displayError(error, noRepeat);
    }

    @Override
    public List<String> getSupportedMods() {
        return ImmutableList.copyOf(DocumentationManager.getDocumentedMods());
    }

    @Override
    public KeyBinding getPIGuiKey() {
        return KeyInputHandler.openPI;
    }

    @Override
    public KeyBinding getETGuiKey() {
        return KeyInputHandler.etGUI;
    }

    @Override
    public KeyBinding getETWorldKey() {
        return KeyInputHandler.etWorld;
    }
}
