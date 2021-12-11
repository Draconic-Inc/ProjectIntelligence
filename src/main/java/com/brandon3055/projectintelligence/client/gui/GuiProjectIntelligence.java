package com.brandon3055.projectintelligence.client.gui;

import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiManipulable;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiPopupDialogs;
import com.brandon3055.projectintelligence.client.DisplayController;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.gui.guielements.*;
import com.brandon3055.projectintelligence.client.keybinding.KeyInputHandler;
import com.brandon3055.projectintelligence.docmanagement.PIUpdateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created by brandon3055 on 7/25/2018.
 */
public class GuiProjectIntelligence extends Screen {

    private Screen parent;
    private PIGuiContainer container = new PIGuiContainer(this, DisplayController.MASTER_CONTROLLER);
    public static boolean requiresEditReload = false;

    public static volatile boolean updateErrorDialog = false;
    private GuiErrorDialog errorDialog;

    public GuiProjectIntelligence() {
        this(null);
    }

    public GuiProjectIntelligence(Screen parent) {
        super(new TranslationTextComponent("pi.gui.project_intelligence.title"));
        this.parent = parent;
        container.setMenuElement(new GuiPartMenu(container, () -> updateSizeAndPos(true)));
        container.initContainer();
        container.setCloseHandler(this::closeGui);
        container.setListMaxWidth(() -> Math.min(150, container.xSize() / 3));
        container.setPositionRestraint(this::validatePosition);
        container.setCanDrag(() -> PIConfig.screenMode != 0);
        container.setOnMoved(this::savePosition);
        GuiActiveDownloads downloadsUI = new GuiActiveDownloads(container);
        GuiNotifications notificationUI = new GuiNotifications(container, downloadsUI);
        errorDialog = new GuiErrorDialog(container.getPartContainer());

        container.getManager().addChild(downloadsUI, 800, false);
        container.getManager().addChild(notificationUI, 810, false);
    }

    @Override
    protected void init() {
        super.init();
        container.onGuiInit();
        updateSizeAndPos(false);

        if (!PIConfig.downloadsAllowed) {
            GuiPopupDialogs dialog = GuiPopupDialogs.createDialog(container.getPartContainer(), GuiPopupDialogs.DialogType.OK_CANCEL_OPTION, I18n.get("pi.internet_access_info.txt"));
            dialog.setCloseOnOutsideClick(false);
            dialog.cancelButton.setText(I18n.get("pi.button.more_information"));
            dialog.setOkListener(() -> {
                PIConfig.downloadsAllowed = true;
                PIConfig.save();
                PIUpdateManager.performFullUpdateCheck();
                checkFirstLaunch();
            });
            dialog.setCancelListener(() -> {
                GuiPopupDialogs dialog2 = GuiPopupDialogs.createDialog(container.getPartContainer(), GuiPopupDialogs.DialogType.OK_CANCEL_OPTION, I18n.get("pi.internet_access_more_info.txt"));
                dialog2.setCloseOnOutsideClick(false);
                dialog2.cancelButton.setText(I18n.get("pi.button.deny_access"));
                dialog2.setOkListener(() -> {
                    PIConfig.downloadsAllowed = true;
                    PIConfig.save();
                    PIUpdateManager.performFullUpdateCheck();
                    checkFirstLaunch();
                });
                dialog2.setCancelListener(() -> {
                    GuiPopupDialogs dialog3 = GuiPopupDialogs.createDialog(container.getPartContainer(), GuiPopupDialogs.DialogType.OK_OPTION, I18n.get("pi.internet_access_denied.txt"));
                    dialog3.setCloseOnOutsideClick(false);
                    dialog3.setOkListener(() -> {
                        closeGui();
                    });
                    dialog3.showCenter(850);
                });
                dialog2.showCenter(850);
            });
            dialog.setEscapeCallback(this::closeGui);
            dialog.setBlockOutsideClicks(true);
            dialog.showCenter(850);
        }
        else {
            checkFirstLaunch();
        }
    }

    @Override
    public void init(Minecraft p_init_1_, int p_init_2_, int p_init_3_) {
        super.init(p_init_1_, p_init_2_, p_init_3_);
    }

    private void checkFirstLaunch() {
        if (!PIConfig.showTutorialLater && !PIConfig.tutorialDisplayed) {
            GuiPIIntroduction guiIntro = new GuiPIIntroduction(container);
            container.getManager().addChild(guiIntro, 820, false);
        }
    }

    private void closeGui() {
        container.dispose();
        minecraft.setScreen(parent);
        if (minecraft.screen == null) {
            minecraft.popGuiLayer();
        }
    }

    private void updateSizeAndPos(boolean userChangedSize) {
        int mode = PIConfig.screenMode;
        if (mode == 0) {
            container.updateSize(width, height);
        }
        else {
            double s = mode == 1 ? 0.1 : mode == 2 ? 0.2 : mode == 3 ? 0.3 : mode == 4 ? 0.4 : mode == 5 ? 0.5 : 0.6;
            container.updateSize((int) (width - (width * s)), (int) (height - (height * s)));
        }

        if (userChangedSize) {
            PIConfig.screenPosOverride = false;
            PIConfig.save();
        }

        if (PIConfig.screenPosOverride) {
            container.updatePos(PIConfig.screenPosX, PIConfig.screenPosY);
        }
        else {
            container.updatePos(width / 2 - container.xSize() / 2, height / 2 - container.ySize() / 2);
        }
    }

    private void savePosition(GuiManipulable draggable) {
        PIConfig.screenPosOverride = true;
        PIConfig.screenPosX = draggable.xPos();
        PIConfig.screenPosY = draggable.yPos();
        PIConfig.save();
    }

    private void validatePosition(GuiManipulable draggable) {
        boolean invalid = false;
        if (PIConfig.screenMode == 0) {
            draggable.setPos(0, 0);
        }
        if (draggable.xPos() + 100 > draggable.screenWidth) {
            draggable.setXPos(draggable.screenWidth - 100);
            invalid = true;
        }
        if (draggable.maxXPos() - 100 < 0) {
            draggable.setXPos(-draggable.xSize() + 100);
            invalid = true;
        }
        if (draggable.yPos() < 0) {
            draggable.setYPos(0);
            invalid = true;
        }
        if (draggable.yPos() + 100 > draggable.screenHeight) {
            draggable.setYPos(draggable.screenHeight - 100);
            invalid = true;
        }

        if (invalid) {
            savePosition(draggable);
        }
    }

    public PIGuiContainer getContainer() {
        return container;
    }

    public static GuiPartMenu getMenuPart() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiProjectIntelligence) {
            return ((GuiProjectIntelligence) screen).container.getMenu();
        }
        return null;
    }

    //region Mouse & Key

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (container.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (container.mouseReleased(mouseX, mouseY, button)) {
            return requiresEditReload;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        container.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        if (container.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (container.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (container.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char charTyped, int charCode) {
        boolean captured = container.charTyped(charTyped, charCode);
        InputMappings.Input key = InputMappings.Type.MOUSE.getOrCreate(charCode);
        if (!captured && KeyInputHandler.openPI.isActiveAndMatches(key)) {
            closeGui();
        }
        else if (!captured && charCode == 59) {
            GuiPIIntroduction guiIntro = new GuiPIIntroduction(container);
            container.getManager().addChild(guiIntro, 820, false);
        }
        return captured;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (container.mouseScrolled(mouseX, mouseY, scrollAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    //endregion

    @Override
    public void render(MatrixStack mStack, int mouseX, int mouseY, float partialTicks) {
        container.renderElements(mouseX, mouseY, partialTicks);
        super.render(mStack, mouseX, mouseY, partialTicks);
        container.renderOverlayLayer(mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();
        container.updateScreen();
        if (requiresEditReload) {
            container.getManager().reloadElements();
            requiresEditReload = false;
            return;
        }

        if (!PIGuiHelper.errorCache.isEmpty() && !container.getManager().getElements().contains(errorDialog)) {
            errorDialog.showCenter(900);
            updateErrorDialog = false;
            return;
        }
        else if (updateErrorDialog) {
            errorDialog.reloadElement();
            updateErrorDialog = false;
        }
    }

    //endregion
}
