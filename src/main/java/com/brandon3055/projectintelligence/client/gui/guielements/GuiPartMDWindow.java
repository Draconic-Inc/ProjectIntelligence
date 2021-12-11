package com.brandon3055.projectintelligence.client.gui.guielements;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementFactory;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.PiMarkdownReader;
import com.brandon3055.projectintelligence.client.DisplayController;
import com.brandon3055.projectintelligence.client.DisplayController.TabData;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler.PropertyGroup;
import com.brandon3055.projectintelligence.client.gui.PIPartRenderer;
import com.brandon3055.projectintelligence.docmanagement.DocumentationManager;
import com.brandon3055.projectintelligence.docmanagement.DocumentationPage;
import com.brandon3055.projectintelligence.docmanagement.ModStructurePage;
import com.brandon3055.projectintelligence.docmanagement.RootPage;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl.SliderRotation.VERTICAL;

/**
 * Created by brandon3055 on 10/07/2017.
 */
public class GuiPartMDWindow extends GuiElement<GuiPartMDWindow> {
    public static PropertyGroup headerProps = new PropertyGroup("md_window.header");
    public static PropertyGroup bodyProps = new PropertyGroup("md_window.body");
    public static PropertyGroup scrollProps = new PropertyGroup("md_window.scroll_bar");
    public static PropertyGroup scrollSliderProps = new PropertyGroup("md_window.scroll_bar.scroll_slider");

    public PIPartRenderer headerRenderer = new PIPartRenderer(headerProps).setSideTrims(true, true, false, true);
    public PIPartRenderer bodyRenderer = new PIPartRenderer(bodyProps);
    public PIPartRenderer tabRenderer = new PIPartRenderer(bodyProps).setSideTrims(true, true, false, true);

    protected Map<TabData, PageTab> tabMap = new HashMap<>();
    protected Map<PageTab, GuiScrollElement> pageElementMap = new HashMap<>();
    private DisplayController controller;

    public GuiPartMDWindow(DisplayController controller) {
        this.controller = controller;
        this.disableOnRemove = true;
    }

    @Override
    public void addChildElements() {
        super.addChildElements();
    }

    @Override
    public void reloadElement() {
        updateDisplay();
        super.reloadElement();
    }

    /**
     * Universal method that updates the tabs and md window (if required)
     */
    public void updateDisplay() {
        //Validate existing tabs
        tabMap.entrySet().removeIf(entry -> validateTab(entry.getKey()));

        //Add any missing tabs
        for (TabData tabData : controller.getOpenTabs()) {
            if (!tabMap.containsKey(tabData)) {
                addTab(tabData, new PageTab(this, tabData, controller));
            }
        }

        //Force immediate page position updates
        tabMap.forEach((tabData, tab) -> tab.updateSizePosition(false));
    }

    private void addTab(TabData tabData, PageTab tab) {
        tabMap.put(tabData, tab);
        tab.setYPosMod((t, i) -> yPos());
        addChild(tab);
    }

    /**
     * Checks if the specified tab is still valid and if not removes the tab element from the gui and returns true.
     * This does not remove the tab from the tab map.
     *
     * @param tabData the tab data to check.
     * @return true if the tab is invalid and should be removed from the tabMap
     */
    private boolean validateTab(TabData tabData) {
        if (!controller.getOpenTabs().contains(tabData)) {
            if (tabMap.containsKey(tabData)) {
                removeChild(tabMap.get(tabData));
            }
            return true;
        }
        return false;
    }

    @Override
    public <C extends GuiElement> C removeChild(C child) {
        if (pageElementMap.containsKey(child)) {
            removeChild(pageElementMap.remove(child));
        }
        return super.removeChild(child);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        int tabBarSize = 12;
        headerRenderer.render(this, xPos(), yPos(), xSize(), tabBarSize);
        bodyRenderer.render(this, xPos(), yPos() + tabBarSize, xSize(), ySize() - tabBarSize);

        TabData selectedData = controller.getActiveTab();
        GuiElement selectedTab = tabMap.get(selectedData);

        if (selectedTab != null) {
            selectedTab.preDraw(minecraft, mouseX, mouseY, partialTicks);
            selectedTab.renderElement(minecraft, mouseX, mouseY, partialTicks);
            selectedTab.postDraw(minecraft, mouseX, mouseY, partialTicks);
        }

        for (GuiElement element : childElements) {
            if (element.isEnabled()) {
                if (element instanceof PageTab && selectedData == ((PageTab) element).tabData) {
                    continue;
                }
                element.preDraw(minecraft, mouseX, mouseY, partialTicks);
                element.renderElement(minecraft, mouseX, mouseY, partialTicks);
                element.postDraw(minecraft, mouseX, mouseY, partialTicks);
            }
        }

        if (!bodyProps.vanillaTex()) {
            zOffset += 100;
            drawMultiPassGradientRect(xPos() + 3, yPos() + 14, maxXPos() - 13, yPos() + 18, bodyProps.colour(), bodyProps.colour() & 0x00FFFFFF, 2);
            drawMultiPassGradientRect(xPos() + 3, maxYPos() - 6, maxXPos() - 13, maxYPos() - 2, bodyProps.colour() & 0x00FFFFFF, bodyProps.colour(), 2);
            zOffset -= 100;
        }
    }

    public static class PageTab extends GuiElement<PageTab> {
        public PIPartRenderer scrollRenderer = new PIPartRenderer(scrollProps);
        public PIPartRenderer scrollSlideRenderer = new PIPartRenderer(scrollSliderProps);

        protected int moveX;
        protected int dragOffset = 0;
        protected String name;
        protected String pageURI;
        protected TabData tabData;
        private DisplayController controller;
        protected boolean isDragging = false;
        protected boolean shouldReloadMD = false;
        protected GuiButton closeButton;
        protected GuiPartMDWindow mdWindow;
        protected GuiSlideControl scrollBar;
        protected GuiScrollElement scrollElement;
        protected MDElementContainer markdownContainer;

        public PageTab(GuiPartMDWindow mdWindow, TabData tabData, DisplayController controller) {
            this.mdWindow = mdWindow;
            this.pageURI = tabData.pageURI;
            this.name = tabData.getDocPage().getDisplayName();
            this.tabData = tabData;
            this.controller = controller;
            this.setYSize(12);
        }

        @Override
        public void addChildElements() {
            super.addChildElements();

            closeButton = new GuiButton().setXPosMod((guiButton, integer) -> maxXPos() - 11).setYPos(yPos() + 2).setSize(9, 9);
            closeButton.onPressed(() -> controller.closeTab(tabData));
            closeButton.setHoverText(I18n.get("pi.button.close"));
            closeButton.addChild(new GuiTexture(64, 16, 5, 5, PITextures.PI_PARTS).setRelPos(0, 2).setXPosMod((guiButton, integer) -> maxXPos() - 9));
            addChild(closeButton);

            scrollBar = new GuiSlideControl(VERTICAL);
            scrollBar.setXPosMod((guiSlideControl, integer) -> mdWindow.maxXPos() - scrollBar.xSize() - 3);
            scrollBar.setYPosMod((guiSlideControl, integer) -> mdWindow.yPos() + 14);
            scrollBar.setXSize(8);

            scrollBar.setBackgroundElement(scrollRenderer.asElement().setHoverStateSupplier(() -> scrollBar.isDragging()));
            scrollBar.setSliderElement(scrollSlideRenderer.asElement().setHoverStateSupplier(() -> scrollBar.isDragging()));
            scrollBar.getBackgroundElement().setXPosMod((o, integer) -> scrollBar.xPos()).setYPosMod((o, integer) -> scrollBar.yPos());
            scrollBar.getSliderElement().setXPosMod((o, integer) -> scrollBar.getInsetRect().x);
            scrollBar.setInputListener(guiSlideControl -> tabData.scrollPosition = guiSlideControl.getRawPos());


            scrollElement = new GuiScrollElement();//mdWindow.xPos(), mdWindow.yPos() + 12, mdWindow.xSize(), mdWindow.ySize() - 12);
            scrollElement.setVerticalScrollBar(scrollBar);
            scrollElement.setAllowedScrollAxes(true, false);
            scrollElement.setXPosMod((e, i) -> mdWindow.xPos()).setYPosMod((e, i) -> mdWindow.yPos() + 12);
            scrollElement.setXSizeMod((e, i) -> mdWindow.xSize()).setYSizeMod((e, i) -> mdWindow.ySize() - 12);
            scrollElement.setEnabledCallback(() -> controller.getActiveTab() == tabData);
            //This padding makes room for the scroll bar and insets the top and bottom of the md container
            //so the "fade out" effect works
            scrollElement.setInsets(2, 1, 2, 11);
            scrollElement.setStandardScrollBehavior();
            mdWindow.addChild(scrollElement);
            mdWindow.pageElementMap.put(this, scrollElement);

            markdownContainer = new MDElementContainer(this);
            markdownContainer.setInsets(6, 6, 6, 6);
            markdownContainer.addAndFireReloadCallback(guiMarkdownElement -> guiMarkdownElement.setPosAndSize(scrollElement.getInsetRect()));
            markdownContainer.setLinkClickCallback(this::openLink);
            markdownContainer.linkDisplayTarget = scrollElement;
            scrollElement.addElement(markdownContainer);
        }

        private void openLink(String link, int button) {
            GuiButton.playGenericClick(mc);
            if (link.startsWith("https://") || link.startsWith("http://") || !link.contains(":")) {
                PIGuiHelper.displayLinkConfirmDialog(this, link);
                return;
            }
            DocumentationPage page = DocumentationManager.getPage(link);
            if (!(page instanceof RootPage)) {
                if (page == null) {
                    PIGuiHelper.displayError("The specified page \"" + link + "\" could not be found!\nThis is ether a broken link or the mod to which the specified page belongs is not installed.");
                }
                else {
                    controller.openPage(link, button == 2);
                }
            }
        }

        @Override
        public void reloadElement() {
            moveX = 0;
            if (tabData == null || tabData.getDocPage() == null) {
                LogHelper.bigDev("Null Tab Data");
                return;
            }
            name = tabData.getDocPage().getDisplayName();
            scrollBar.setYSize(mdWindow.ySize() - 17);
            scrollBar.getBackgroundElement().setSize(scrollBar);
            scrollElement.resetScrollPositions();
            updateSizePosition(false);
            super.reloadElement();
            closeButton.setEnabled(controller.getOpenTabs().size() > 1);
            shouldReloadMD = true;
        }



        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean clickCaptured = super.mouseClicked(mouseX, mouseY, button);
            if (!clickCaptured && isMouseOver(mouseX, mouseY)) {
                isDragging = true;
                closeButton.setEnabled(false);
                dragOffset = (int) (mouseX - xPos());
                if (controller.getActiveTab() != tabData) {
                    controller.switchTab(tabData);
                    GuiButton.playGenericClick(mc);
                }
                return true;
            }
            return clickCaptured;
        }


        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
            if (isDragging) {
                int tabWidth = Math.min(130, mdWindow.xSize() / controller.getOpenTabs().size());
                int xPos = mdWindow.xPos() + (controller.getOpenTabs().indexOf(tabData) * tabWidth);
                double dragAmount = xPos - (mouseX - dragOffset);
                if (Math.abs(dragAmount) > (xSize() * 0.5)) {
                    controller.dragTab(tabData, dragAmount > 0 ? -1 : 1);
                }
            }
            return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            isDragging = false;
            closeButton.setEnabled(controller.getOpenTabs().size() > 1);
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean handleMouseScroll(double mouseX, double mouseY, double scrollDirection) {
            return super.handleMouseScroll(mouseX, mouseY, scrollDirection);
        }


        public void updateSizePosition(boolean animated) {
            int tabWidth = Math.min(130, mdWindow.xSize() / controller.getOpenTabs().size());
            if (xSize() != tabWidth) {
                setXSize(tabWidth);
            }

            int xPos = mdWindow.xPos() + (controller.getOpenTabs().indexOf(tabData) * tabWidth);
            if (xPos() != xPos) {
                if (!animated) {
                    setXPos(xPos);
                }
                else {
                    moveX = xPos - xPos();
                }
            }

            if (moveX > 0) {
                setXPos(xPos() + Math.max(1, moveX / 2));
                moveX -= Math.max(1, moveX / 2);
            }
            else if (moveX < 0) {
                setXPos(xPos() + Math.min(-1, moveX / 2));
                moveX -= Math.max(1, moveX / 2);
            }
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            int renderX = xPos();
            boolean selected = scrollElement.isEnabled();

            if (isDragging) {
                renderX = MathHelper.clip(mouseX - dragOffset, mdWindow.xPos(), mdWindow.maxXPos() - xSize());
                zOffset += 10;
            }

            mdWindow.tabRenderer.setTabRender(selected ? 2 : 0).render(this, renderX, yPos(), xSize(), ySize());

            drawCustomString(fontRenderer, name, renderX + 3, yPos() + 3, xSize() - (closeButton.isEnabled() ? 10 : 3), bodyProps.textColour(), GuiAlign.LEFT, GuiAlign.TextRotation.NORMAL, false, true, false);
            RenderSystem.color4f(1, 1, 1, 1);

            if (isDragging) {
                zOffset -= 10;
            }

            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            if (super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks)) {
                return true;
            }

            if (isMouseOver(mouseX, mouseY) && hoverTime > 10 && !isDragging) {
                List<String> list = new ArrayList<>();
                DocumentationPage page = tabData.getDocPage();
                if (page instanceof RootPage) {
                    page = ((RootPage) page).getHomePage();
                }

                if (page != null) {
                    if (page instanceof ModStructurePage) {
                        list.add(TextFormatting.BLUE + page.getModName());
                    }
                    else {
                        list.add(TextFormatting.BLUE + page.getModName() + ": " + TextFormatting.GOLD + name);
                    }
                    list.add(TextFormatting.GRAY + page.getPageURI());

                    drawHoveringText(list, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                }
                return true;
            }

            return false;
        }

        @Override
        public boolean onUpdate() {

            //Detect if the page has changes
            if (!pageURI.equals(tabData.pageURI)) {
                pageURI = tabData.pageURI;
                name = tabData.getDocPage().getDisplayName();
                reloadElement();
            }

            if (scrollBar.getRawPos() != tabData.scrollPosition) {
                scrollBar.updateRawPos(tabData.scrollPosition, true);
            }

            //endregion

            if (tabData.requiresEditReload) {
                tabData.requiresEditReload = false;
                shouldReloadMD = true;
            }

            //region Reload
            //Reloading is done like this because it is an expensive task and the regular reloadElement method may be called multiple times during a reload operation.
            if (shouldReloadMD && isEnabled()) {
                shouldReloadMD = false;
                DocumentationPage page = tabData.getDocPage();

                if (page == null) {
                    String[] errorText = new String[]{ //
                            "An error occurred while loading the page!", //
                            "Page at " + tabData.pageURI + " could not be found.", //
                            "This error should not be possible unless PI is in an invalid state.", //
                            "I suggest you try reloading PI."};

                    updateMarkdown(Lists.newArrayList(errorText));
                }
                else {
                    updateMarkdown(page.getMarkdownLines());
                }
            }
            //endregion

            updateSizePosition(true);

            return super.onUpdate();
        }

        private void updateMarkdown(List<String> mdLines) {
            markdownContainer.clearContainer();

            PiMarkdownReader reader = new PiMarkdownReader(mdLines);
            MDElementFactory factory = new MDElementFactory(markdownContainer);
            factory.setColourSupplier(() -> bodyProps.textColour());
            reader.accept(factory);

            markdownContainer.layoutMarkdownElements();
        }
    }
}