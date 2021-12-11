package com.brandon3055.projectintelligence.client.gui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.PIGuiContainer;
import com.brandon3055.projectintelligence.client.gui.PIPartRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Created by brandon3055 on 31/08/2016.
 */
public class GuiPartMenu extends GuiElement<GuiPartMenu> {
    public static StyleHandler.PropertyGroup menuProps = new StyleHandler.PropertyGroup("menu");
    public static StyleHandler.PropertyGroup closeBtnProps = new StyleHandler.PropertyGroup("menu.close_button");
    public static StyleHandler.PropertyGroup settingsBtnProps = new StyleHandler.PropertyGroup("menu.settings_button");
    public static StyleHandler.PropertyGroup sizeBtnProps = new StyleHandler.PropertyGroup("menu.size_buttons");
    public PIPartRenderer menuRender = new PIPartRenderer(menuProps).setSideTrims(true, true, false, true);

    private PIGuiContainer container;
    public GuiStyleEditor styleEditor = null;
    public GuiPIConfig configUI = null;
    private GuiLabel title;
    private Runnable resizeHandler;
    public GuiButton settingsButton;
    public GuiButton minimizeButton;

    public GuiPartMenu(PIGuiContainer container, @Nullable Runnable resizeHandler) {
        this.container = container;
        this.resizeHandler = resizeHandler;
    }

    @Override
    public void addChildElements() {
        styleEditor = new GuiStyleEditor(this);
        configUI = new GuiPIConfig(this, styleEditor);

        title = new GuiLabel(I18n.get("pi.gui.project_intelligence.title")) {
            @Override
            public boolean hasShadow() {
                return menuProps.textShadow();
            }
        };
        title.setSize(xSize() - 50, 20).setPos(this).translate(8, 1).setAlignment(GuiAlign.LEFT);
        title.setHoverableTextCol(hovering -> menuProps.textColour());
        title.setWidthFromText().setTrim(false);
        addChild(title);

        GuiButton closeButton = new GuiButton().setSize(16, 16).setHoverText(I18n.get("pi.button.close.info"));
        GuiTexture closeTex = new GuiTexture(0, 0, 16, 16, PITextures.PI_PARTS);
        closeTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> closeBtnProps.glColour(mouseOver));
        closeTex.setPostDrawCallback(IDrawCallback::resetColour);
        closeButton.addChild(closeTex);
        closeButton.addAndFireReloadCallback(guiButton -> guiButton.setPos(maxXPos() - 20, yPos() + 3));
//        closeButton.setRectBorderColourGetter((hovering, disabled) -> 0);
//        closeButton.setRectFillColourGetter((hovering, disabled) -> 0);
        closeButton.onPressed(() -> container.closeButtonPressed());
        addChild(closeButton);


        settingsButton = new GuiButton().setSize(16, 16).setHoverText(I18n.get("pi.button.settings.info"));
        GuiTexture settingsTex = new GuiTexture(16, 0, 16, 16, PITextures.PI_PARTS);
        settingsTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> settingsBtnProps.glColour(mouseOver));
        settingsTex.setPostDrawCallback(IDrawCallback::resetColour);
        settingsButton.addChild(settingsTex);
        settingsButton.addAndFireReloadCallback(guiButton -> {
            guiButton.setPos(closeButton.xPos() - guiButton.xSize() - 4, yPos() + 3);
        });
//        settingsButton.setRectBorderColourGetter((hovering, disabled) -> 0);
//        settingsButton.setRectFillColourGetter((hovering, disabled) -> 0);
        settingsButton.onPressed(() -> configUI.toggleShown());
        addChild(settingsButton);

        GuiButton maximizeButton = new GuiButton().setSize(16, 16).setHoverText(I18n.get("pi.button.maximize.info"));
        GuiTexture maximizeTex = new GuiTexture(32, 0, 16, 16, PITextures.PI_PARTS);
        maximizeTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> sizeBtnProps.glColour(mouseOver));
        maximizeTex.setPostDrawCallback(IDrawCallback::resetColour);
        maximizeButton.addChild(maximizeTex);
        maximizeButton.addAndFireReloadCallback(guiButton -> guiButton.setPos(settingsButton.xPos() - guiButton.xSize() - 4, yPos() + 3));
//        maximizeButton.setRectBorderColourGetter((hovering, disabled) -> 0);
//        maximizeButton.setRectFillColourGetter((hovering, disabled) -> 0);
        maximizeButton.setEnabledCallback(() -> PIConfig.screenMode > 0);
        maximizeButton.onPressed(() -> setGuiSize(true));

        minimizeButton = new GuiButton().setSize(16, 16).setHoverText(I18n.get("pi.button.minimize.info"));
        GuiTexture minimizeTex = new GuiTexture(48, 0, 16, 16, PITextures.PI_PARTS);
        minimizeTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> sizeBtnProps.glColour(mouseOver));
        minimizeTex.setPostDrawCallback(IDrawCallback::resetColour);
        minimizeButton.addChild(minimizeTex);
        minimizeButton.addAndFireReloadCallback(guiButton -> guiButton.setPos(maximizeButton.xPos() - guiButton.xSize() - 4, yPos() + 3));
//        minimizeButton.setRectBorderColourGetter((hovering, disabled) -> 0);
//        minimizeButton.setRectFillColourGetter((hovering, disabled) -> 0);
        minimizeButton.setEnabledCallback(() -> PIConfig.screenMode < 6);
        minimizeButton.onPressed(() -> setGuiSize(false));
        if (resizeHandler != null) {
            addChild(maximizeButton);
            addChild(minimizeButton);
        }

        GuiButton editorButton = new GuiButton(TextFormatting.UNDERLINE + ""+ TextFormatting.RED + I18n.get("pi.config.open_editor")).setSize(100, 14);
        editorButton.addAndFireReloadCallback(guiButton -> guiButton.setPos(xPos() + (xSize() / 2) - 50, yPos() + (ySize() / 2) - 7));
        editorButton.setFillColour(0);
        editorButton.setBorderColours(0xFF707070, 0xFFA0A0A0);
        editorButton.setEnabledCallback(PIConfig::editMode);
        editorButton.onPressed(PIGuiHelper::displayEditor);
        addChild(editorButton);

        super.addChildElements();
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        title.setLabelText(I18n.get("pi.gui.project_intelligence.title") + (PIConfig.editMode() ? TextFormatting.RED + " (Edit Mode)" : ""));
    }

    private void setGuiSize(boolean maximize) {
        if (maximize && PIConfig.screenMode > 0) {
            if (Screen.hasShiftDown()) PIConfig.screenMode = 0;
            else PIConfig.screenMode--;
        }
        else if (!maximize && PIConfig.screenMode < 6) {
            if (Screen.hasShiftDown()) PIConfig.screenMode = 6;
            else PIConfig.screenMode++;
        }
        PIConfig.screenPosOverride = false;
        PIConfig.save();
        resizeHandler.run();
    }

    //region Render

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        menuRender.render(this);
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    //endregion
}
