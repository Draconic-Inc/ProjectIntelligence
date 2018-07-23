package com.brandon3055.projectintelligence.client.gui;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElementManager;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiScreen;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.projectintelligence.PIHelpers;
import com.brandon3055.projectintelligence.client.gui.guielements.*;
import com.brandon3055.projectintelligence.docdata.LanguageManager;

import java.io.IOException;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public class GuiProjectIntelligence extends ModularGuiScreen {
    public static GuiProjectIntelligence activeInstance = null;
    public static volatile boolean requiresReload = false;
    public static volatile boolean updateErrorDialog = false;
    public GuiPartContainer elementContainer;
    public GuiPartMenu guiMenu;
    public GuiPartPageList contentList;
    public GuiPartMDWindow contentWindow;
    private GuiErrorDialog errorDialog;

    /**
     * Enables certain functionality that is useful in dev.
     * Example. Styles tree is reloaded every time the style window opens to allow
     * hot swapping of style properties.
     */
    public static boolean devMode = true;

    public GuiProjectIntelligence() {
        GuiProjectIntelligence.activeInstance = this;
        new LanguageManager();
    }

    //############################################################################
    //# Initialization & Reload
    //region //############################################################################

    @Override
    public void addElements(GuiElementManager manager) {
        requiresReload = false;
        updateScreenSize();
        elementContainer = new GuiPartContainer();
        guiMenu = new GuiPartMenu(this);
        contentList = new GuiPartPageList(this);
        contentWindow = new GuiPartMDWindow(this);

        elementContainer.addAndFireReloadCallback(guiPartMenu -> elementContainer.setSize(xSize(), ySize()).setPos((width - xSize()) / 2, (height - ySize()) / 2));
        manager.add(elementContainer);
        errorDialog = new GuiErrorDialog(elementContainer);

        guiMenu.setYSize(20);
        guiMenu.addAndFireReloadCallback(guiPartMenu -> guiMenu.setXSize(xSize()).setPos((width - xSize()) / 2, (height - ySize()) / 2));
        elementContainer.addChild(guiMenu);

        contentList.addAndFireReloadCallback(contentList -> contentList.setYSize(ySize() - guiMenu.ySize()).setPos((width - xSize()) / 2, guiMenu.maxYPos()));
        elementContainer.addChild(contentList);

        contentWindow.setXPosMod((part, integer) -> contentList.maxXPos()).setXSizeMod((part, integer) -> xSize() - contentList.xSize());
        contentWindow.addAndFireReloadCallback(contentWindow -> contentWindow.setYPos(guiMenu.maxYPos()).setYSize(ySize() - guiMenu.ySize()));
        elementContainer.addChild(contentWindow);

        manager.add(new GuiActiveDownloads(this), 800);
    }

    @Override
    public void reloadGui() {
        updateScreenSize();
        if (PIConfig.editMode()) {
            if (mc.player != null) {
                PIHelpers.updatePlayerInventory(mc.player);
            }
        }
        super.reloadGui();
    }

    private void updateScreenSize() {
        int mode = PIConfig.screenMode;
        if (mode == 0) {
            xSize = width;
            ySize = height;
        }
        else {
            double s = mode == 1 ? 0.1 : mode == 2 ? 0.2 : mode == 3 ? 0.3 : mode == 4 ? 0.4 : mode == 5 ? 0.5 : 0.6;
            xSize = (int) (width - (width * s));
            ySize = (int) (height - (height * s));
        }
    }

    //endregion

    //############################################################################
    //# Getter's & Setters
    //region //############################################################################
    //Note there are a lot of useful helper methods in com.brandon3055.projectintelligence.PIHelpers

    @Override
    public int xSize() {
        return xSize;
    }

    @Override
    public int ySize() {
        return ySize;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    //endregion


    @Override
    public void updateScreen() {
        if (requiresReload) {
            requiresReload = false;
            String open = TabManager.getActiveTab().pageURI;
            reloadGui();
            TabManager.openPage(open, false);
            return;
        }
        if (!PIHelpers.errorCache.isEmpty() && !manager.getElements().contains(errorDialog)) {
            errorDialog.showCenter(700);
            updateErrorDialog = false;
            return;
        }
        else if (updateErrorDialog) {
            errorDialog.reloadElement();
            updateErrorDialog = false;
        }
        super.updateScreen();
    }

    public void closeGui() {
        this.mc.player.closeScreen();
        this.mc.displayGuiScreen(null);

        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
        this.mc.setIngameFocus();

//        mc.inGameHasFocus = true;
//        mc.mouseHelper.grabMouseCursor();
    }

    public static GuiPartMenu getMenuPart() {
        if (activeInstance == null || activeInstance.guiMenu == null) {
            return null;
        }
        return activeInstance.guiMenu;
    }

    public static GuiPartPageList getListPart() {
        if (activeInstance == null || activeInstance.contentList == null) {
            return null;
        }
        return activeInstance.contentList;
    }

    public static GuiPartMDWindow getMDPart() {
        if (activeInstance == null || activeInstance.contentWindow == null) {
            return null;
        }
        return activeInstance.contentWindow;
    }

    public static class GuiPartContainer extends MGuiElementBase {

        protected int dragXOffset = 0;
        protected int dragYOffset = 0;
        protected boolean dragging = false;

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            boolean captured = super.mouseClicked(mouseX, mouseY, mouseButton);

            if (PIConfig.screenMode != 0 && !captured && GuiHelper.isInRect(xPos(), yPos(), xSize(), 20, mouseX, mouseY)) {
                dragging = true;
                dragXOffset = mouseX - xPos();
                dragYOffset = mouseY - yPos();
            }

            return captured;
        }

        @Override
        public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
            if (dragging) {
                int xMove = (mouseX - dragXOffset) - xPos();
                int yMove = (mouseY - dragYOffset) - yPos();
                translate(xMove, yMove);

                validatePosition();

                PIConfig.screenPosOverride = true;
                PIConfig.screenPosX = xPos();
                PIConfig.screenPosY = yPos();
                PIConfig.save();
            }
            return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }

        @Override
        public boolean mouseReleased(int mouseX, int mouseY, int state) {
            dragging = false;
            return super.mouseReleased(mouseX, mouseY, state);
        }

        @Override
        public void reloadElement() {
            super.reloadElement();

            if (PIConfig.screenPosOverride) {
                setPos(PIConfig.screenPosX, PIConfig.screenPosY);
            }

            if (validatePosition()) {
                PIConfig.screenPosOverride = true;
                PIConfig.screenPosX = xPos();
                PIConfig.screenPosY = yPos();
                PIConfig.save();
            }
        }

        private boolean validatePosition() {
            boolean changed = false;
            if (PIConfig.screenMode == 0) {
                setPos(0, 0);
            }
            if (xPos() + 100 > screenWidth) {
                setXPos(screenWidth - 100);
                changed = true;
            }
            if (maxXPos() - 100 < 0) {
                setXPos(-xSize() + 100);
                changed = true;
            }
            if (yPos() < 0) {
                setYPos(0);
                changed = true;
            }
            if (yPos() + 100 > screenHeight) {
                setYPos(screenHeight - 100);
                changed = true;
            }
            return changed;
        }
    }
}
