package com.brandon3055.projectintelligence.client.gui.guielements;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.GuiMarkdownElement;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.TabManager;
import com.brandon3055.projectintelligence.client.gui.TabManager.TabData;
import com.brandon3055.projectintelligence.docdata.DocumentationPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl.SliderRotation.VERTICAL;

/**
 * Created by brandon3055 on 10/07/2017.
 */
public class GuiPartMDWindow extends MGuiElementBase<GuiPartMDWindow> {

    private GuiProjectIntelligence mainWindow;
    protected Map<TabData, PageTab> tabMap = new HashMap<>();
    protected Map<PageTab, GuiScrollElement> pageElementMap = new HashMap<>();

    public GuiPartMDWindow(GuiProjectIntelligence mainWindow) {
        this.mainWindow = mainWindow;
        this.disableOnRemove = true;
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
        for (TabData tabData : TabManager.getOpenTabs()) {
            if (!tabMap.containsKey(tabData)) {
                addTab(tabData, new PageTab(this, tabData));
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
        if (!TabManager.getOpenTabs().contains(tabData)) {
            if (tabMap.containsKey(tabData)) {
                removeChild(tabMap.get(tabData));
            }
            return true;
        }
        return false;
    }

    @Override
    public <C extends MGuiElementBase> C removeChild(C child) {
        if (pageElementMap.containsKey(child)) {
            removeChild(pageElementMap.remove(child));
        }
        return super.removeChild(child);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        int tabBarSize = 12;
        //Header
        {
            boolean shadedBorders = StyleHandler.getBoolean("md_window.header." + StyleHandler.StyleType.SHADED_BORDERS.getName());
            boolean vanillaT = StyleHandler.getBoolean("md_window.header." + StyleHandler.StyleType.VANILLA_TEXTURE.getName());
            int border = StyleHandler.getInt("md_window.header." + StyleHandler.StyleType.BORDER.getName());

            if (shadedBorders || !vanillaT) {
                int colour = StyleHandler.getInt("md_window.header." + StyleHandler.StyleType.COLOUR.getName());
                int light = changeShade(border, 0.2);
                int dark = changeShade(border, -0.2);

                if (shadedBorders) {
                    boolean thickBorders = StyleHandler.getBoolean("md_window.header.shaded_borders." + StyleHandler.StyleType.THICK_BORDERS.getName());
                    double b = thickBorders ? 1 : 0.5;

                    drawColouredRect(xPos(), yPos(), xSize(), 12, colour);                     //Bar
                    drawColouredRect(xPos(), yPos(), xSize(), b, light);                       //Bar Top Accent
                    drawColouredRect(xPos(), yPos() + 12 - b, xSize(), b, dark);               //Bar Bottom Accent
                    drawColouredRect(xPos() + xSize() - b, yPos(), b, tabBarSize, dark);               //Right Accent
                }
                else {
                    int fill = StyleHandler.getInt("md_window.header." + StyleHandler.StyleType.COLOUR.getName());
                    drawColouredRect(xPos(), yPos(), xSize(), tabBarSize, border);
                    drawColouredRect(xPos() + 1, yPos() + 1, xSize() - 2, tabBarSize - 1, fill);
                }

            }
            else {
                StyleHandler.getColour("md_window.header." + StyleHandler.StyleType.COLOUR.getName()).glColour();
                ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI_SQ);
                drawTiledTextureRectWithTrim(xPos(), yPos(), xSize(), tabBarSize, 4, 4, 0, 4, 1, 128, 255, 128);
                GlStateManager.color(1, 1, 1, 1);
            }
        }

        //Body
        {
            if (bodyShadedBorders || !bodyVanillaT) {
//                int light = changeShade(bodyBorder, 0.15);
                int dark = changeShade(bodyBorder, -0.15);
                boolean tb = !bodyShadedBorders || StyleHandler.getBoolean("md_window.body.shaded_borders." + StyleHandler.StyleType.THICK_BORDERS.getName());
                double b = tb ? 1 : 0.5; //Right Accent Width

                if (bodyShadedBorders) {
                    drawColouredRect(xPos(), yPos() + tabBarSize, xSize(), ySize() - tabBarSize, bodyColour.argb());             //Background
//                    drawColouredRect(xPos(), yPos() + tabBarSize, b, ySize() - tabBarSize, light);                  //Left Accent
                    drawColouredRect(xPos() + xSize() - b, yPos() + tabBarSize, b, ySize() - tabBarSize, dark);       //Right Accent
                }
                else {
                    drawBorderedRect(xPos(), yPos() + tabBarSize, xSize(), ySize() - tabBarSize, 1, bodyColour.argb(), bodyBorder);
                }
            }
            else {
                bodyColour.glColour();
                ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI_SQ);
                drawTiledTextureRectWithTrim(xPos(), yPos() + tabBarSize, xSize(), ySize() - tabBarSize, 4, 4, 4, 4, 1, 128, 255, 124);
                GlStateManager.color(1, 1, 1, 1);
            }
        }

        TabData selectedData = TabManager.getActiveTab();
        MGuiElementBase selectedTab = tabMap.get(selectedData);

        if (selectedTab != null) {
            selectedTab.preDraw(minecraft, mouseX, mouseY, partialTicks);
            selectedTab.renderElement(minecraft, mouseX, mouseY, partialTicks);
            selectedTab.postDraw(minecraft, mouseX, mouseY, partialTicks);
        }

        for (MGuiElementBase element : childElements) {
            if (element.isEnabled()) {
                if (element instanceof PageTab && selectedData == ((PageTab) element).tabData) {
                    continue;
                }
                element.preDraw(minecraft, mouseX, mouseY, partialTicks);
                element.renderElement(minecraft, mouseX, mouseY, partialTicks);
                element.postDraw(minecraft, mouseX, mouseY, partialTicks);
            }
        }

        if (!bodyVanillaT) {
            zOffset += 500;
            drawMultiPassGradientRect(xPos() + 1, yPos() + 14, maxXPos() - 11, yPos() + 18, bodyColour.argb(), bodyColour.argb() & 0x00FFFFFF, 2);
            drawMultiPassGradientRect(xPos() + 1, maxYPos() - 6, maxXPos() - 11, maxYPos() - 2, bodyColour.argb() & 0x00FFFFFF, bodyColour.argb(), 2);
            zOffset -= 500;
        }
    }

    @Override
    public boolean onUpdate() {
        updateStyle();
        return super.onUpdate();
    }

    private void updateStyle() {
//        boolean shadedBorders = StyleHandler.getBoolean("md_window.header." + StyleHandler.StyleType.SHADED_BORDERS.getName());
//        boolean vanillaT = StyleHandler.getBoolean("md_window.header." + StyleHandler.StyleType.VANILLA_TEXTURE.getName());
//        int colour = StyleHandler.getInt("md_window.header." + StyleHandler.StyleType.COLOUR.getName());
//        int border = StyleHandler.getInt("md_window.header." + StyleHandler.StyleType.BORDER.getName());

//        bodyShadedBorders = StyleHandler.getBoolean("md_window.body." + StyleHandler.StyleType.SHADED_BORDERS.getName());
        bodyVanillaT = StyleHandler.getBoolean("md_window.body." + StyleHandler.StyleType.VANILLA_TEXTURE.getName());
        bodyColour = StyleHandler.getColour("md_window.body." + StyleHandler.StyleType.COLOUR.getName());
        bodyBorder = StyleHandler.getInt("md_window.body." + StyleHandler.StyleType.BORDER.getName());
        baseTextColour = StyleHandler.getInt("md_window.body." + StyleHandler.StyleType.TEXT_COLOUR.getName());

//        btnVanillaTex = StyleHandler.getBoolean("page_list.page_buttons." + StyleHandler.StyleType.VANILLA_TEXTURE.getName());
//        btnShadedBorders = StyleHandler.getBoolean("page_list.page_buttons." + StyleHandler.StyleType.SHADED_BORDERS.getName());
//        btnThickBorders = StyleHandler.getBoolean("page_list.page_buttons.shaded_borders." + StyleHandler.StyleType.THICK_BORDERS.getName());
//        btnColour = StyleHandler.getColour("page_list.page_buttons." + StyleHandler.StyleType.COLOUR.getName());
//        btnColourHover = StyleHandler.getColour("page_list.page_buttons." + StyleHandler.StyleType.HOVER.getName());
//        btnBorder = StyleHandler.getColour("page_list.page_buttons." + StyleHandler.StyleType.BORDER.getName());
//        btnBorderHover = StyleHandler.getColour("page_list.page_buttons." + StyleHandler.StyleType.BORDER_HOVER.getName());
//        btnTextColour = StyleHandler.getInt("page_list.page_buttons." + StyleHandler.StyleType.TEXT_COLOUR.getName());
//        btnTextColourHover = StyleHandler.getInt("page_list.page_buttons." + StyleHandler.StyleType.TEXT_HOVER.getName());
//        btnTextShadow = StyleHandler.getBoolean("page_list.page_buttons." + StyleHandler.StyleType.TEXT_SHADOW.getName());
//        btnIconVanillaTex = StyleHandler.getBoolean("page_list.page_buttons.page_icon." + StyleType.VANILLA_TEXTURE.getName());
//        btnIconBackground = StyleHandler.getColour("page_list.page_buttons.page_icon." + StyleType.BACKGROUND.getName());
//        btnIconBorder = StyleHandler.getColour("page_list.page_buttons.page_icon." + StyleType.BORDER.getName());
    }

    public static boolean bodyShadedBorders = false;
    public static boolean bodyVanillaT = false;
    public static Colour bodyColour = new ColourARGB(0);
    public static int bodyBorder = 0;
    public static int baseTextColour = 0;
//    public static boolean btnThickBorders = false;
//    public static Colour btnColour = new ColourARGB(0);
//    public static Colour btnColourHover = new ColourARGB(0);
//    public static Colour btnBorder = new ColourARGB(0);
//    public static Colour btnBorderHover = new ColourARGB(0);
//    public static int btnTextColour = 0;
//    public static int btnTextColourHover = 0;
//    public static boolean btnIconVanillaTex = false;
//    public static boolean btnTextShadow = false;
//    public static Colour btnIconBackground = new ColourARGB(0);
//    public static Colour btnIconBorder = new ColourARGB(0);

    public static class PageTab extends MGuiElementBase<PageTab> {

        protected int moveX;
        protected int dragOffset = 0;
        protected String name;
        protected String pageURI;
        protected TabData tabData;
        protected boolean isDragging = false;
        protected boolean shouldReloadMD = false;
        protected GuiButton closeButton;
        protected GuiPartMDWindow mdWindow;
        protected GuiSlideControl scrollBar;
        protected GuiScrollElement scrollElement;
        protected GuiMarkdownElement markdownElement;

        public PageTab(GuiPartMDWindow mdWindow, TabData tabData) {
            this.mdWindow = mdWindow;
            this.pageURI = tabData.pageURI;
            this.name = tabData.getDocPage().getDisplayName();
            this.tabData = tabData;
            this.setYSize(12);
        }

        @Override
        public void addChildElements() {
            super.addChildElements();

            closeButton = new GuiButton().setXPosMod((guiButton, integer) -> maxXPos() - 11).setYPos(yPos() + 1).setSize(10, 10);
            closeButton.setListener((event, eventSource) -> TabManager.closeTab(tabData));
            closeButton.setHoverText(I18n.format("pi.button.close"));
            closeButton.addChild(new GuiTexture(64, 16, 5, 5, PITextures.PI_PARTS).setRelPos(0, 3).setXPosMod((guiButton, integer) -> maxXPos() - 9));
            addChild(closeButton);

            scrollBar = new GuiSlideControl(VERTICAL);
            scrollBar.setXPosMod((guiSlideControl, integer) -> mdWindow.maxXPos() - (scrollBar.xSize() + (scrollBar.xSize() == 4 ? 2 : 1)));
            scrollBar.setYPosMod((guiSlideControl, integer) -> mdWindow.yPos() + 12);
            scrollBar.setSize(10, mdWindow.ySize() - 12);

            scrollBar.setBackgroundElement(new StyledGuiRect("md_window.scroll_bar") {
                @Override
                public boolean isMouseOver(int mouseX, int mouseY) {
                    return super.isMouseOver(mouseX, mouseY) || scrollBar.isDragging();
                }
            });
            scrollBar.setSliderElement(new StyledGuiRect("md_window.scroll_bar.scroll_slider") {
                @Override
                public boolean isMouseOver(int mouseX, int mouseY) {
                    return super.isMouseOver(mouseX, mouseY) || scrollBar.isDragging();
                }
            });
            scrollBar.getBackgroundElement().setXPosMod((o, integer) -> scrollBar.xPos()).setYPosMod((o, integer) -> scrollBar.yPos());
            scrollBar.getSliderElement().setXPosMod((o, integer) -> scrollBar.getInsetRect().x);
            scrollBar.setInputListener(guiSlideControl -> tabData.scrollPosition = guiSlideControl.getRawPos());

            scrollElement = new GuiScrollElement();//mdWindow.xPos(), mdWindow.yPos() + 12, mdWindow.xSize(), mdWindow.ySize() - 12);
            scrollElement.setVerticalScrollBar(scrollBar);
            scrollElement.setAllowedScrollAxes(true, false);
            scrollElement.setXPosMod((e, i) -> mdWindow.xPos()).setYPosMod((e, i) -> mdWindow.yPos() + 12);
            scrollElement.setXSizeMod((e, i) -> mdWindow.xSize()).setYSizeMod((e, i) -> mdWindow.ySize() - 12);
            scrollElement.setEnabledCallback(() -> TabManager.getActiveTab() == tabData);
            scrollElement.setInsets(2, 0, 2, 10);
            scrollElement.setStandardScrollBehavior();
            mdWindow.addChild(scrollElement);
            mdWindow.pageElementMap.put(this, scrollElement);

            markdownElement = new GuiMarkdownElement();
            markdownElement.setColourProvider(() -> baseTextColour);
            markdownElement.setInsets(6, 6, 6, 2);
            markdownElement.addAndFireReloadCallback(guiMarkdownElement -> guiMarkdownElement.setPosAndSize(scrollElement.getInsetRect()));
//            markdownElement.setXPosMod((e, i) -> mdWindow.xPos()).setYPosMod((e, i) -> mdWindow.yPos() + 12);
//            markdownElement.setXSizeMod((e, i) -> mdWindow.xSize()).setYSizeMod((e, i) -> mdWindow.ySize() - 12);
            scrollElement.addElement(markdownElement);
        }

        @Override
        public void reloadElement() {
            moveX = 0;
            name = tabData.getDocPage().getDisplayName();
            scrollBar.setYSize(mdWindow.ySize() - 12);
            scrollBar.getBackgroundElement().setYSize(mdWindow.ySize() - 12);
            scrollElement.resetScrollPositions();
            updateSizePosition(false);
            super.reloadElement();
            closeButton.setEnabled(TabManager.getOpenTabs().size() > 1);
            shouldReloadMD = true;
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            boolean clickCaptured = super.mouseClicked(mouseX, mouseY, mouseButton);
            if (!clickCaptured && isMouseOver(mouseX, mouseY)) {
                isDragging = true;
                closeButton.setEnabled(false);
                dragOffset = mouseX - xPos();
                if (TabManager.getActiveTab() != tabData) {
                    TabManager.switchTab(tabData);
                    GuiButton.playGenericClick(mc);
                }
                return true;
            }
            return clickCaptured;
        }

        @Override
        public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
            if (isDragging) {
                int tabWidth = Math.min(130, mdWindow.xSize() / TabManager.getOpenTabs().size());
                int xPos = mdWindow.xPos() + (TabManager.getOpenTabs().indexOf(tabData) * tabWidth);
                int dragAmount = xPos - (mouseX - dragOffset);
                if (Math.abs(dragAmount) > (xSize() * 0.5)) {
                    TabManager.dragTab(tabData, dragAmount > 0 ? -1 : 1);
                }
            }
            return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }

        @Override
        public boolean mouseReleased(int mouseX, int mouseY, int state) {
            isDragging = false;
            closeButton.setEnabled(TabManager.getOpenTabs().size() > 1);
            return super.mouseReleased(mouseX, mouseY, state);
        }

        @Override
        public boolean handleMouseScroll(int mouseX, int mouseY, int scrollDirection) {
            return super.handleMouseScroll(mouseX, mouseY, scrollDirection);
        }

        public void updateSizePosition(boolean animated) {
            int tabWidth = Math.min(130, mdWindow.xSize() / TabManager.getOpenTabs().size());
            if (xSize() != tabWidth) {
                setXSize(tabWidth);
            }

            int xPos = mdWindow.xPos() + (TabManager.getOpenTabs().indexOf(tabData) * tabWidth);
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

            if (!bodyVanillaT) {
                drawColouredRect(renderX, yPos(), xSize(), ySize(), bodyBorder);
                drawColouredRect(renderX + 1, yPos() + 1, xSize() - 2, ySize() + (selected ? 1 : -1), bodyColour.argb());
            }
            else {
                StyleHandler.getColour("md_window.body." + StyleHandler.StyleType.COLOUR.getName()).glColour();
                ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI_SQ);
                drawTiledTextureRectWithTrim(renderX, yPos(), xSize(), ySize(), 4, 4, 0, 4, 1, 128, 255, 120);
                if (selected) {
                    drawTiledTextureRectWithTrim(renderX, yPos(), xSize() - 1, ySize() + 2, 4, 4, 4, 4, 1, 128, 254, 120);
                }
            }


            drawCustomString(fontRenderer, name, renderX + 3, yPos() + 3, xSize() - (closeButton.isEnabled() ? 10 : 3), baseTextColour, GuiAlign.LEFT, GuiAlign.TextRotation.NORMAL, false, true, false);
            GlStateManager.color(1, 1, 1, 1);

            if (isDragging) {
                zOffset -= 10;
            }

            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean onUpdate() {
            //Detect if the page has changes
            if (!pageURI.equals(tabData.pageURI)) {
                pageURI = tabData.pageURI;
                name = tabData.getDocPage().getDisplayName();
                reloadElement();
            }

            //region scrollBar
            if (StyleHandler.getBoolean("md_window.scroll_bar." + StyleHandler.StyleType.COMPACT_BAR.getName())) {
                if (scrollBar.xSize() != 4) {
                    scrollElement.resetScrollPositions();
                    scrollBar.setXSize(4);
                    scrollBar.getBackgroundElement().setXSize(4);
                    scrollBar.getSliderElement().setXSize(4);
                    scrollBar.setInsets(1, 0, 1, 0);
                    scrollElement.reloadElement();
                }
            }
            else if (scrollBar.xSize() != 10) {
                scrollElement.resetScrollPositions();
                scrollBar.setXSize(10);
                scrollBar.getBackgroundElement().setXSize(10);
                scrollBar.getSliderElement().setXSize(8);
                scrollBar.setInsets(1, 1, 1, 1);
                scrollElement.reloadElement();
            }

            if (scrollBar.getRawPos() != tabData.scrollPosition) {
                scrollBar.updateRawPos(tabData.scrollPosition, true);
            }

            //endregion

            //region Reload
            //Reloading is done like this because it is an expensive task and the regular reloadElement method may be called multiple times during a reload operation.
            if (shouldReloadMD && isEnabled()) {
                shouldReloadMD = false;
                markdownElement.clear();
                DocumentationPage page = tabData.getDocPage();

                if (page == null) {
                    markdownElement.parseMarkdown(new String[]{ //
                            "An error occurred while loading the page!", //
                            "Page at " + tabData.pageURI + " could not be found.", //
                            "This error should not be possible unless PI is in an invalid state.", //
                            "I suggest you try reloading PI."});
                }
                else {
                    markdownElement.parseMarkdown(page.getMarkdownLines());
                }
            }
            //endregion

            updateSizePosition(true);

            return super.onUpdate();
        }
    }
}
