package com.brandon3055.projectintelligence.client.gui;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElementManager;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiDraggable;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiDraggable.PositionRestraint;
import com.brandon3055.projectintelligence.client.DisplayController;
import com.brandon3055.projectintelligence.client.gui.guielements.GuiPartMDWindow;
import com.brandon3055.projectintelligence.client.gui.guielements.GuiPartMenu;
import com.brandon3055.projectintelligence.client.gui.guielements.GuiPartPageList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 7/25/2018.
 */
public class PIGuiContainer implements IModularGui<Screen> {

    private Minecraft mc;
    private int xPos;
    private int yPos;
    private int xSize;
    private int ySize;
    private int screenWidth;
    private int screenHeight;
    private GuiElementManager manager = new GuiElementManager(this);
    private Screen gui;
    private DisplayController controller;
    private int zLevel = 0;
    private boolean enableMenu = true;
    private Supplier<Integer> listMaxWidth = () -> 150;
    private boolean enablePageList = true;
    private boolean enableContentWindow = true;
    private GuiDraggable partContainer = null;
    private GuiPartMenu menu = null;
    private GuiPartMDWindow mdWindow = null;
    private GuiPartPageList pageList = null;
    private Runnable sizeChangeHandler = null;
    private Runnable closeHandler = null;
    private Supplier<Boolean> canDrag = () -> false;
    private Consumer<GuiDraggable> onMoved = null;
    private PositionRestraint positionRestraint = GuiElement::normalizePosition;

    public PIGuiContainer(Screen gui, DisplayController controller) {
        this.gui = gui;
        this.controller = controller;
        onGuiInit();
    }

    //region Setup

    public void initContainer() {
        mdWindow = null;
        pageList = null;
        manager.reinitialize(mc, screenWidth, screenHeight);
    }

    public void onGuiInit() {
        this.mc = Minecraft.getInstance();
        this.screenWidth = mc.mainWindow.getScaledWidth();
        this.screenHeight = mc.mainWindow.getScaledHeight();
        manager.setWorldAndResolution(mc, screenWidth, screenHeight);
    }

    /**
     * Must call initContainer after setting this
     */
    public void setMenuEnabled(boolean enableMenu) {
        this.enableMenu = enableMenu;
    }

    /**
     * Must call initContainer after setting this
     */
    public void setPageListEnabled(boolean enablePageList) {
        this.enablePageList = enablePageList;
    }

    /**
     * Must call initContainer after setting this
     */
    public void setContentWindowEnabled(boolean enableContentWindow) {
        this.enableContentWindow = enableContentWindow;
    }

    public void setMenuElement(GuiPartMenu menu) {
        if (this.menu != null && partContainer != null) {
            partContainer.removeChild(this.menu);
        }
        this.menu = menu;
        if (menu != null && partContainer != null) {
            partContainer.addChild(menu);
        }
        updatePartArrangement(true);
    }

    @Override
    public void addElements(GuiElementManager manager) {
        partContainer = new GuiDraggable();
        partContainer.setCanDrag(canDrag);
        partContainer.setPositionRestraint(positionRestraint);
        partContainer.setOnMovedCallback(onMoved == null ? null : () -> onMoved.accept(partContainer));
        manager.addChild(partContainer);

        if (enableMenu && menu != null) {
            partContainer.addChild(menu);
        }
        if (enablePageList) {
            pageList = new GuiPartPageList(this, controller);
            partContainer.addChild(pageList);
        }
        if (enableContentWindow) {
            mdWindow = new GuiPartMDWindow(controller);
            partContainer.addChild(mdWindow);
        }
        updatePartArrangement(true);

        controller.addChangeListener(this, () -> {
            if (enablePageList) pageList.reloadElement();
            if (enableContentWindow) mdWindow.reloadElement();
        });
    }

    public void dispose() {
        controller.removeChangeListener(this);
    }

    public void updatePos(int xPos, int yPos) {
        updatePos(xPos, yPos, true);
    }

    public void updatePos(int xPos, int yPos, boolean reload) {
        this.xPos = xPos;
        this.yPos = yPos;
        updatePartArrangement(reload);
    }

    public void updateSize(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
        updatePartArrangement(true);
    }

    public void setListMaxWidth(Supplier<Integer> pageListMaxWidth) {
        this.listMaxWidth = pageListMaxWidth;
    }

    public void updatePartArrangement(boolean reload) {
        if (!manager.isInitialized()) {
            return;
        }

        partContainer.setPosAndSize(xPos, yPos, xSize, ySize);

        int yPos = this.yPos;
        int xPos = this.xPos;
        int ySize = this.ySize;
        int xSize = this.xSize;

        if (menu != null) {
            menu.setPosAndSize(xPos, yPos, xSize, 20);
            yPos = menu.maxYPos();
            ySize -= menu.ySize();
            if (reload) {
                menu.reloadElement();
            }
        }

        if (pageList != null) {
            pageList.setPosAndSize(xPos, yPos, pageList.extended ? getListMaxWidth().get() : pageList.HIDDEN_X_SIZE, ySize);
            xPos = pageList.maxXPos();
            xSize -= pageList.xSize();
            if (reload) {
                pageList.reloadElement();
            }
        }

        if (mdWindow != null) {
            mdWindow.setPosAndSize(xPos, yPos, xSize, ySize);
            if (reload) {
                mdWindow.reloadElement();
            }
        }
    }

    public void pageListMotionUpdate() {
        if (mdWindow != null && pageList != null) {
            mdWindow.setPosAndSize(pageList.maxXPos(), mdWindow.yPos(), xSize - pageList.xSize(), mdWindow.ySize());
            mdWindow.reloadElement();
        }
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    public void setCanDrag(Supplier<Boolean> canDrag) {
        this.canDrag = canDrag;
        if (partContainer != null) {
            partContainer.setCanDrag(canDrag);
        }
    }

    public void setPositionRestraint(PositionRestraint positionRestraint) {
        this.positionRestraint = positionRestraint;
        if (partContainer != null) {
            partContainer.setPositionRestraint(positionRestraint);
        }
    }

    public void setOnMoved(Consumer<GuiDraggable> onMoved) {
        this.onMoved = onMoved;
        if (partContainer != null) {
            partContainer.setOnMovedCallback(onMoved == null ? null : () -> onMoved.accept(partContainer));
        }
    }

    public void setGui(Screen gui) {
        this.gui = gui;
    }

    //endregion

    public Supplier<Integer> getListMaxWidth() {
        return listMaxWidth;
    }

    public void closeButtonPressed() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    public GuiPartPageList getPageList() {
        return pageList;
    }

    public GuiPartMenu getMenu() {
        return menu;
    }

    public GuiPartMDWindow getMdWindow() {
        return mdWindow;
    }

    public GuiDraggable getPartContainer() {
        return partContainer;
    }

    //region Gui Method Pass-through

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return manager.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return manager.mouseReleased(mouseX, mouseY, button);
    }

    public void mouseMoved(double mouseX, double mouseY) {
        manager.mouseMoved(mouseX, mouseY);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        return manager.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        return manager.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return manager.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return manager.keyReleased(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char typedChar, int keyCode) {
        boolean ret = manager.charTyped(typedChar, keyCode);
        InputMappings.Input key = InputMappings.Type.MOUSE.getOrMakeInput(keyCode);
        if (!ret && (keyCode == 1 || mc.gameSettings.keyBindInventory.isActiveAndMatches(key)) && closeHandler != null) {
            closeHandler.run();
            return true;
        }
        return ret;
    }

    public void renderElements(int mouseX, int mouseY, float partialTicks) {
        manager.renderElements(mc, mouseX, mouseY, partialTicks);
    }

    public void renderOverlayLayer(int mouseX, int mouseY, float partialTicks) {
        manager.renderOverlayLayer(mc, mouseX, mouseY, partialTicks);
    }

    public void updateScreen() {
        manager.onUpdate();
//        if (!PIGuiHelper.errorCache.isEmpty() && !manager.getElements().contains(errorDialog)) {
//            errorDialog.showCenter(700);
//            updateErrorDialog = false;
//            return;
//        }
//        else if (updateErrorDialog) {
//            errorDialog.reloadElement();
//            updateErrorDialog = false;
//        }
    }

    //endregion

    //region IModular Gui

    @Override
    public Screen getScreen() {
        return gui;
    }

    @Override
    public int xSize() {
        return xSize;
    }

    @Override
    public int ySize() {
        return ySize;
    }

    @Override
    public int guiLeft() {
        return 0;
    }

    @Override
    public int guiTop() {
        return 0;
    }

    @Override
    public GuiElementManager getManager() {
        return manager;
    }

    @Override
    public void setZLevel(int zLevel) {
        this.zLevel = zLevel;
    }

    @Override
    public int getZLevel() {
        return zLevel;
    }

    //endregion
}
