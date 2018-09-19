package com.brandon3055.projectintelligence.client.gui.guielements;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence_old;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

/**
 * Created by brandon3055 on 31/08/2016.
 */
public class GuiPartMenu_old extends MGuiElementBase<GuiPartMenu_old> {
    private GuiProjectIntelligence_old guiMain;
    public GuiStyleEditor styleEditor = null;
    public GuiPIConfig configUI = null;
    private GuiLabel title;

    public GuiPartMenu_old(GuiProjectIntelligence_old parentGui) {
        guiMain = parentGui;
    }

    @Override
    public void addChildElements() {
        styleEditor = new GuiStyleEditor(this);
        configUI = new GuiPIConfig(this, styleEditor);

        title = new GuiLabel(I18n.format("pi.gui.project_intelligence.title"));
        title.setSize(xSize() - 50, 20).setPos(this).translate(8, 1).setAlignment(GuiAlign.LEFT).setShadow(false);
        title.setTextColGetter(hovering -> menuProps.textColour());
        title.setWidthFromText().setTrim(false);
        addChild(title);

        GuiButton closeButton = new GuiButton().setSize(16, 16).setHoverText(I18n.format("pi.button.close.info"));
        GuiTexture closeTex = new GuiTexture(0, 0, 16, 16, PITextures.PI_PARTS);
        closeTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> closeBtnProps.glColour(mouseOver));
        closeTex.setPostDrawCallback(IDrawCallback::resetColour);
        closeButton.addChild(closeTex);
        closeButton.addAndFireReloadCallback(guiButton -> guiButton.setPos(maxXPos() - 20, yPos() + 3));
//        closeButton.setRectBorderColourGetter((hovering, disabled) -> 0);
//        closeButton.setRectFillColourGetter((hovering, disabled) -> 0);
        closeButton.setListener(() -> guiMain.closeGui());
        addChild(closeButton);

        GuiButton settingsButton = new GuiButton().setSize(16, 16).setHoverText(I18n.format("pi.button.settings.info"));
        GuiTexture settingsTex = new GuiTexture(16, 0, 16, 16, PITextures.PI_PARTS);
        settingsTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> settingsBtnProps.glColour(mouseOver));
        settingsTex.setPostDrawCallback(IDrawCallback::resetColour);
        settingsButton.addChild(settingsTex);
        settingsButton.addAndFireReloadCallback(guiButton -> {
            guiButton.setPos(closeButton.xPos() - guiButton.xSize() - 4, yPos() + 3);
        });
//        settingsButton.setRectBorderColourGetter((hovering, disabled) -> 0);
//        settingsButton.setRectFillColourGetter((hovering, disabled) -> 0);
        settingsButton.setListener(() -> configUI.toggleShown());
        addChild(settingsButton);

        GuiButton maximizeButton = new GuiButton().setSize(16, 16).setHoverText(I18n.format("pi.button.maximize.info"));
        GuiTexture maximizeTex = new GuiTexture(32, 0, 16, 16, PITextures.PI_PARTS);
        maximizeTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> sizeBtnProps.glColour(mouseOver));
        maximizeTex.setPostDrawCallback(IDrawCallback::resetColour);
        maximizeButton.addChild(maximizeTex);
        maximizeButton.addAndFireReloadCallback(guiButton -> guiButton.setPos(settingsButton.xPos() - guiButton.xSize() - 4, yPos() + 3));
//        maximizeButton.setRectBorderColourGetter((hovering, disabled) -> 0);
//        maximizeButton.setRectFillColourGetter((hovering, disabled) -> 0);
        maximizeButton.setEnabledCallback(() -> PIConfig.screenMode > 0);
        maximizeButton.setListener(() -> setGuiSize(true));
        addChild(maximizeButton);

        GuiButton minimizeButton = new GuiButton().setSize(16, 16).setHoverText(I18n.format("pi.button.minimize.info"));
        GuiTexture minimizeTex = new GuiTexture(48, 0, 16, 16, PITextures.PI_PARTS);
        minimizeTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> sizeBtnProps.glColour(mouseOver));
        minimizeTex.setPostDrawCallback(IDrawCallback::resetColour);
        minimizeButton.addChild(minimizeTex);
        minimizeButton.addAndFireReloadCallback(guiButton -> guiButton.setPos(maximizeButton.xPos() - guiButton.xSize() - 4, yPos() + 3));
//        minimizeButton.setRectBorderColourGetter((hovering, disabled) -> 0);
//        minimizeButton.setRectFillColourGetter((hovering, disabled) -> 0);
        minimizeButton.setEnabledCallback(() -> PIConfig.screenMode < 6);
        minimizeButton.setListener(() -> setGuiSize(false));
        addChild(minimizeButton);

        GuiButton editorButton = new GuiButton(TextFormatting.UNDERLINE + ""+ TextFormatting.RED + I18n.format("pi.config.open_editor")).setSize(100, 14);
        editorButton.addAndFireReloadCallback(guiButton -> guiButton.setPos(xPos() + (xSize() / 2) - 50, yPos() + (ySize() / 2) - 7));
        editorButton.setFillColour(0);
        editorButton.setBorderColours(0xFF707070, 0xFFA0A0A0);
        editorButton.setEnabledCallback(PIConfig::editMode);
        editorButton.setListener((event, eventSource) -> PIGuiHelper.displayEditor());
        addChild(editorButton);

        super.addChildElements();
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        title.setLabelText(I18n.format("pi.gui.project_intelligence.title") + (PIConfig.editMode() ? TextFormatting.RED + " (Edit Mode)" : ""));
    }

    @Override
    protected boolean keyTyped(char typedChar, int keyCode) throws IOException {
        return super.keyTyped(typedChar, keyCode);
    }

    private void setGuiSize(boolean maximize) {
        if (maximize && PIConfig.screenMode > 0) {
            if (GuiScreen.isShiftKeyDown()) PIConfig.screenMode = 0;
            else PIConfig.screenMode--;
        }
        else if (!maximize && PIConfig.screenMode < 6) {
            if (GuiScreen.isShiftKeyDown()) PIConfig.screenMode = 6;
            else PIConfig.screenMode++;
        }
        PIConfig.screenPosOverride = false;
        PIConfig.save();
        guiMain.reloadGui();
        guiMain.reloadGui();//TODO Figure out why this is needed. Im guessing there is an issue with reload order that results in the page list scrolling element not getting updated properly.
    }

    //region Render

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        boolean thickBorders = menuProps.thickBorders();
        boolean shadedBorders = menuProps.shadeBorders();
        int border = menuProps.border();
        int colour = menuProps.colour();

        if (shadedBorders) {
            int light = changeShade(border, 0.2);
            int dark = changeShade(border, -0.2);
            drawColouredRect(xPos(), yPos(), xSize(), ySize(), colour);
            int b = thickBorders ? 2 : 1;

            drawColouredRect(xPos(), yPos(), xSize(), b, light);
            drawColouredRect(xPos(), yPos(), b, ySize(), light);
            drawColouredRect(xPos(), yPos() + ySize() - 1, xSize(), 1, dark);
            drawColouredRect(xPos() + xSize() - b, yPos(), b, ySize(), dark);

        }
        else {
            if (menuProps.vanillaTex()) {
                menuProps.glColour();
                ResourceHelperBC.bindTexture(PIConfig.screenMode == 0 ? PITextures.VANILLA_GUI_SQ : PITextures.VANILLA_GUI);
                drawTiledTextureRectWithTrim(xPos(), yPos(), xSize(), ySize(), 4, 4, 0, 4, 0, thickBorders ? 0 : 128, 256, 128);
                GlStateManager.color(1, 1, 1, 1);
            }
            else {
                int b = thickBorders ? 2 : 1;
                drawColouredRect(xPos(), yPos(), xSize(), ySize(), border);
                drawColouredRect(xPos() + b, yPos() + b, xSize() - (b * 2), ySize() - b, colour);
            }
        }

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    //endregion

//    private static void updateStyles() {
//        menuProps.updateProps();
//        closeBtnProps.updateProps();
//        settingsBtnProps.updateProps();
//        sizeBtnProps.updateProps();
//    }

    public static StyleHandler.PropertyGroup menuProps = new StyleHandler.PropertyGroup("menu");
    public static StyleHandler.PropertyGroup closeBtnProps = new StyleHandler.PropertyGroup("menu.close_button");
    public static StyleHandler.PropertyGroup settingsBtnProps = new StyleHandler.PropertyGroup("menu.settings_button");
    public static StyleHandler.PropertyGroup sizeBtnProps = new StyleHandler.PropertyGroup("menu.size_buttons");

//    static {
//        StyleHandler.addReloadListener(GuiPartMenu::updateStyles);
//        updateStyles();
//    }

}
