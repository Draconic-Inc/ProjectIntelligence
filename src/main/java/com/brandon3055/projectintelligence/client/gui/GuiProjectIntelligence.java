package com.brandon3055.projectintelligence.client.gui;

import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiDraggable;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiPopupDialogs;
import com.brandon3055.projectintelligence.client.DisplayController;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.gui.guielements.*;
import com.brandon3055.projectintelligence.client.keybinding.KeyInputHandler;
import com.brandon3055.projectintelligence.docmanagement.PIUpdateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

/**
 * Created by brandon3055 on 7/25/2018.
 */
public class GuiProjectIntelligence extends GuiScreen {

    private GuiScreen parent;
    private PIGuiContainer container = new PIGuiContainer(this, DisplayController.MASTER_CONTROLLER);
    public static boolean requiresEditReload = false;

    public static volatile boolean updateErrorDialog = false;
    private GuiErrorDialog errorDialog;

    public GuiProjectIntelligence() {
        this(null);
    }

    public GuiProjectIntelligence(GuiScreen parent) {
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

        container.getManager().add(downloadsUI, 800);
        container.getManager().add(notificationUI, 810);
    }

    @Override
    public void initGui() {
        super.initGui();
        container.onGuiInit();
        updateSizeAndPos(false);

        if (!PIConfig.downloadsAllowed) {
            GuiPopupDialogs dialog = GuiPopupDialogs.createDialog(container.getPartContainer(), GuiPopupDialogs.DialogType.OK_CANCEL_OPTION, I18n.format("pi.internet_access_info.txt"));
            dialog.setCloseOnOutsideClick(false);
            dialog.cancelButton.setText(I18n.format("pi.button.more_information"));
            dialog.setOkListener((guiButton, pressed) -> {
                PIConfig.downloadsAllowed = true;
                PIConfig.save();
                PIUpdateManager.performFullUpdateCheck();
                checkFirstLaunch();
            });
            dialog.setCancelListener((guiButton, pressed) -> {
                GuiPopupDialogs dialog2 = GuiPopupDialogs.createDialog(container.getPartContainer(), GuiPopupDialogs.DialogType.OK_CANCEL_OPTION, I18n.format("pi.internet_access_more_info.txt"));
                dialog2.setCloseOnOutsideClick(false);
                dialog2.cancelButton.setText(I18n.format("pi.button.deny_access"));
                dialog2.setOkListener((guiButton2, pressed2) -> {
                    PIConfig.downloadsAllowed = true;
                    PIConfig.save();
                    PIUpdateManager.performFullUpdateCheck();
                    checkFirstLaunch();
                });
                dialog2.setCancelListener((guiButton2, pressed2) -> {
                    GuiPopupDialogs dialog3 = GuiPopupDialogs.createDialog(container.getPartContainer(), GuiPopupDialogs.DialogType.OK_OPTION, I18n.format("pi.internet_access_denied.txt"));
                    dialog3.setCloseOnOutsideClick(false);
                    dialog3.setOkListener((guiButton3, pressed3) -> {
                        closeGui();
                    });
                    dialog3.showCenter(850);
                });
                dialog2.showCenter(850);
            });
            dialog.showCenter(850);
        }
        else {
            checkFirstLaunch();
        }
    }

    private void checkFirstLaunch() {
        if (!PIConfig.showTutorialLater && !PIConfig.tutorialDisplayed) {
            GuiPIIntroduction guiIntro = new GuiPIIntroduction(container);
            container.getManager().add(guiIntro, 820);
        }
    }

    private void closeGui() {
        mc.displayGuiScreen(parent);
        if (mc.currentScreen == null) {
            mc.setIngameFocus();
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

    private void savePosition(GuiDraggable draggable) {
        PIConfig.screenPosOverride = true;
        PIConfig.screenPosX = draggable.xPos();
        PIConfig.screenPosY = draggable.yPos();
        PIConfig.save();
    }

    private void validatePosition(GuiDraggable draggable) {
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
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiProjectIntelligence) {
            return ((GuiProjectIntelligence) screen).container.getMenu();
        }
        return null;
    }

    //region Pass-Through

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (container.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (container.mouseReleased(mouseX, mouseY, state)) {
            return;
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (container.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
            return;
        }

        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!container.keyTyped(typedChar, keyCode) && KeyInputHandler.openPI.isActiveAndMatches(keyCode)) {
            closeGui();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        if (container.handleMouseInput()) {
            return;
        }

        super.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        container.renderElements(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        container.renderOverlayLayer(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
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
