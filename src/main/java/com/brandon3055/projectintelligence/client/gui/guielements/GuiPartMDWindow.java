package com.brandon3055.projectintelligence.client.gui.guielements;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
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

import java.util.HashMap;
import java.util.Map;

import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl.SliderRotation.VERTICAL;

/**
 * Created by brandon3055 on 10/07/2017.
 */
public class GuiPartMDWindow extends MGuiElementBase<GuiPartMDWindow> {

    private GuiProjectIntelligence mainWindow;
    protected Map<TabData, PageTab> tabMap = new HashMap<>();
    protected int baseTextColour = 0;

    public GuiPartMDWindow(GuiProjectIntelligence mainWindow) {
        this.mainWindow = mainWindow;
    }



    @Override
    public void addChildElements() {
        super.addChildElements();

        //TODO add tabs from previous session
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
//        updateTabs(true);
        updateDisplay();
    }

    /**
     * Universal method that updates the tabs and md window (if required)
     */
    public void updateDisplay() {
//        List<String> openPages = TabManager.getOpenTabs();

        //Validate existing tabs
        tabMap.entrySet().removeIf(entry -> !TabManager.getOpenTabs().contains(entry.getKey()));

//        Iterator<PageTab> i = tabMap.iterator();
//        while (i.hasNext()) {
//            PageTab tab = i.next();
//            if (!openPages.contains(tab.pageURI)) {
//                removeChild(tab);
//                i.remove();
//            }
//        }

        //Add any missing tabs
        for (TabData tabData : TabManager.getOpenTabs()) {
            if (!tabMap.containsKey(tabData)) {
                addTab(tabData, new PageTab(this, tabData));
            }
        }

//        for (String page : openPages) {
//            boolean pageExists = false;
//            for (PageTab tab : tabMap) {
//                if (tab.pageURI.equals(page)) {
//                    pageExists = true;
//                    break;
//                }
//            }
//            if (!pageExists) {
//                addTab(new PageTab(this, page));
//            }
//        }

        //Force immediate page position updates
        tabMap.forEach((tabData, tab) -> tab.updateSizePosition(false));
    }

    private void addTab(TabData tabData, PageTab tab) {
        tabMap.put(tabData, tab);
        tab.setYPosMod((t, i) -> yPos());
        addChild(tab);
    }

//
//    /**
//     * Validates and updates all tabs if needed.
//     * Will create the default tab if no tabs exist.
//     *
//     * @param reloadMD Forced the Markdown elements to be regenerated the next time their respective tab is selected.
//     */
//    public void updateTabs(boolean reloadMD) {
//        reloadMD = true;
//        if (tabs.isEmpty() || selectedTab == null) {
////            if (!tabs.isEmpty()) {//This should not be needed
////                selectedTab = tabs.getFirst();
////            }
////            else {
//                selectedTab = new PageTab(this, DocumentationManager.getSelectedPage().getPageURI());
//                addTab(selectedTab);
////            }
//        }
//
//        Supplier<Integer> tabWidth = () -> Math.min(100, xSize() / tabs.size());
//
//        for (int i = 0; i < tabs.size(); i++) {
//            int fi = i;
//            tabs.get(i).setXPosMod((tab, integer) -> xPos() + (fi * tabWidth.get())).setXSizeMod((tab, integer) -> tabWidth.get());
//            if (reloadMD) {
//                tabs.get(i).reloadElement();
//            }
//        }
//    }
//

//
//    /**
//     * Opens the specified page.
//     *
//     * @param pageURI  the uri of the  page to open or null to open the root (default) page.
//     * @param inNewTab if true the page will be opened in a new tab.
//     */
//    public void openPage(@Nullable String pageURI, boolean inNewTab) {
//        if (inNewTab) {
//            selectedTab = new PageTab(this, pageURI);
//            addTab(selectedTab);
//            updateTabs(false);
//        }
//        else {
//            selectedTab.switchPage(pageURI);
//        }
//    }

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
            boolean shadedBorders = StyleHandler.getBoolean("md_window.body." + StyleHandler.StyleType.SHADED_BORDERS.getName());
            boolean vanillaT = StyleHandler.getBoolean("md_window.body." + StyleHandler.StyleType.VANILLA_TEXTURE.getName());
            int border = StyleHandler.getInt("md_window.body." + StyleHandler.StyleType.BORDER.getName());

            if (shadedBorders || !vanillaT) {
                int colour = StyleHandler.getInt("md_window.body." + StyleHandler.StyleType.COLOUR.getName());
                int light = changeShade(border, 0.15);
                int dark = changeShade(border, -0.15);
                boolean tb = !StyleHandler.getBoolean("md_window.body." + StyleHandler.StyleType.SHADED_BORDERS.getName()) || StyleHandler.getBoolean("md_window.body.shaded_borders." + StyleHandler.StyleType.THICK_BORDERS.getName());
                double b = tb ? 1 : 0.5; //Right Accent Width

                if (shadedBorders) {
                    drawColouredRect(xPos(), yPos() + tabBarSize, xSize(), ySize() - tabBarSize, colour);             //Background
//                    drawColouredRect(xPos(), yPos() + tabBarSize, b, ySize() - tabBarSize, light);                  //Left Accent
                    drawColouredRect(xPos() + xSize() - b, yPos() + tabBarSize, b, ySize() - tabBarSize, dark);       //Right Accent
                }
                else {
                    drawBorderedRect(xPos(), yPos() + tabBarSize, xSize(), ySize() - tabBarSize, 1, colour, border);
                }
            }
            else {
                StyleHandler.getColour("md_window.body." + StyleHandler.StyleType.COLOUR.getName()).glColour();
                ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI_SQ);
                drawTiledTextureRectWithTrim(xPos(), yPos() + tabBarSize, xSize(), ySize() - tabBarSize, 4, 4, 4, 4, 1, 128, 255, 128);

                GlStateManager.color(1, 1, 1, 1);
            }
        }

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onUpdate() {
        baseTextColour = StyleHandler.getInt("md_window.body." + StyleHandler.StyleType.TEXT_COLOUR.getName());
        return super.onUpdate();
    }


    public static class PageTab extends MGuiElementBase<PageTab> {

        private String name;
        private String pageURI;
        private TabData tabData;
        private GuiPartMDWindow mdWindow;
        private GuiSlideControl scrollBar;
        private GuiScrollElement scrollElement;
        private GuiMarkdownElement markdownElement;
        private boolean shouldReloadMD = false;
        private int moveX;

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

            scrollBar = new GuiSlideControl(VERTICAL);
            scrollBar.setXPosMod((guiSlideControl, integer) -> mdWindow.maxXPos() - (scrollBar.xSize() + (scrollBar.xSize() == 4 ? 2 : 1)));
            scrollBar.setYPosMod((guiSlideControl, integer) -> mdWindow.yPos() + 12);
            scrollBar.setSize(10, mdWindow.ySize() - 12);

            scrollBar.setBackgroundElement(new StyledGuiRect("md_window.scroll_bar"){
                @Override
                public boolean isMouseOver(int mouseX, int mouseY) {
                    return super.isMouseOver(mouseX, mouseY) || scrollBar.isDragging();
                }
            });
            scrollBar.setSliderElement(new StyledGuiRect("md_window.scroll_bar.scroll_slider"){
                @Override
                public boolean isMouseOver(int mouseX, int mouseY) {
                    return super.isMouseOver(mouseX, mouseY) || scrollBar.isDragging();
                }
            });
            scrollBar.getBackgroundElement().setXPosMod((o, integer) -> scrollBar.xPos()).setYPosMod((o, integer) -> scrollBar.yPos());
            scrollBar.getSliderElement().setXPosMod((o, integer) -> scrollBar.getInsetRect().x);

            scrollElement = new GuiScrollElement();//mdWindow.xPos(), mdWindow.yPos() + 12, mdWindow.xSize(), mdWindow.ySize() - 12);
            scrollElement.setVerticalScrollBar(scrollBar);
            scrollElement.setAllowedScrollAxes(true, false);
            scrollElement.setXPosMod((e, i) -> mdWindow.xPos()).setYPosMod((e, i) -> mdWindow.yPos() + 12);
            scrollElement.setXSizeMod((e, i) -> mdWindow.xSize()).setYSizeMod((e, i) -> mdWindow.ySize() - 12);
            scrollElement.setEnabledCallback(() -> TabManager.getActiveTab() == tabData);
            scrollElement.setInsets(2, 0, 2, 10);
            scrollElement.setStandardScrollBehavior();
            addChild(scrollElement);

            markdownElement = new GuiMarkdownElement();
            markdownElement.setColourProvider(() -> mdWindow.baseTextColour);
            markdownElement.setInsets(6, 6, 6, 2);
            markdownElement.addAndFireReloadCallback(guiMarkdownElement -> guiMarkdownElement.setPosAndSize(scrollElement.getInsetRect()));
            scrollElement.addElement(markdownElement);
        }

        @Override
        public void reloadElement() {
            super.reloadElement();
            shouldReloadMD = true;
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0xFFFFFFFF);
            drawCustomString(fontRenderer, name, xPos() + 2, yPos() + 2, xSize() - 2, 0xFFFFFF, GuiAlign.LEFT, GuiAlign.TextRotation.NORMAL, false, true, false);
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
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
                setXPos(xPos() + 1);
                moveX--;
            }
            else if (moveX < 0) {
                setXPos(xPos() - 1);
                moveX++;
            }
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
            //endregion

            //region Reload
            //Reloading is done like this because it is an expensive task and the regular reloadElement method may be called multiple times during a reload operation.
            if (shouldReloadMD) {
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
