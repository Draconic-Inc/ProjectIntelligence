package com.brandon3055.projectintelligence.client.gui.guielements;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementFactory;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.PiMarkdownReader;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.PIHelpers;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.TabManager;
import com.brandon3055.projectintelligence.client.gui.TabManager.TabData;
import com.brandon3055.projectintelligence.docdata.DocumentationManager;
import com.brandon3055.projectintelligence.docdata.DocumentationPage;
import com.brandon3055.projectintelligence.docdata.ModStructurePage;
import com.brandon3055.projectintelligence.docdata.RootPage;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        bodyVanillaT = StyleHandler.getBoolean("md_window.body." + StyleHandler.StyleType.VANILLA_TEXTURE.getName());
        bodyColour = StyleHandler.getColour("md_window.body." + StyleHandler.StyleType.COLOUR.getName());
        bodyBorder = StyleHandler.getInt("md_window.body." + StyleHandler.StyleType.BORDER.getName());
        baseTextColour = StyleHandler.getInt("md_window.body." + StyleHandler.StyleType.TEXT_COLOUR.getName());
    }

    public static boolean bodyShadedBorders = false;
    public static boolean bodyVanillaT = false;
    public static Colour bodyColour = new ColourARGB(0);
    public static int bodyBorder = 0;
    public static int baseTextColour = 0;

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
        //        protected GuiMarkdownElement markdownElement;
        protected MDElementContainer markdownContainer;

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

            closeButton = new GuiButton().setXPosMod((guiButton, integer) -> maxXPos() - 11).setYPos(yPos() + 2).setSize(9, 9);
            closeButton.setListener(() -> TabManager.closeTab(tabData));
//            closeButton.consumeHoverOverlay = true;
            closeButton.setHoverText(I18n.format("pi.button.close"));
            closeButton.setFillColour(0).setBorderColours(0, 0xFF000000);
            closeButton.addChild(new GuiTexture(64, 16, 5, 5, PITextures.PI_PARTS).setRelPos(0, 2).setXPosMod((guiButton, integer) -> maxXPos() - 9));
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
            //This padding makes room for the scroll bar and insets the top and bottom of the md container
            //so the "fade out" effect works
            scrollElement.setInsets(2, 1, 2, 11);
            scrollElement.setStandardScrollBehavior();
//            scrollElement.setListMode(GuiScrollElement.ListMode.VERT_LOCK_POS);
            mdWindow.addChild(scrollElement);
            mdWindow.pageElementMap.put(this, scrollElement);

//            markdownElement = new GuiMarkdownElement();
//            markdownElement.setLinkListener(this::openLink);
//            markdownElement.setImageListener(this::openLink);
//            markdownElement.setColourProvider(() -> baseTextColour);
//            markdownElement.setInsets(6, 6, 6, 2);

            markdownContainer = new MDElementContainer(this);
            markdownContainer.setInsets(6, 6, 6, 6);
            markdownContainer.addAndFireReloadCallback(guiMarkdownElement -> guiMarkdownElement.setPosAndSize(scrollElement.getInsetRect()));
            markdownContainer.setLinkClickCallback(this::openLink);
            markdownContainer.linkDisplayTarget = scrollElement;
//            markdownElement.addAndFireReloadCallback(guiMarkdownElement -> guiMarkdownElement.setPos(scrollElement.getInsetRect().x, scrollElement.getInsetRect().y).setXSize(scrollElement.getInsetRect().width));
            scrollElement.addElement(markdownContainer);
        }

        //TODO add custom link confirmation dialog
        private void openLink(String link, int button) {
            GuiButton.playGenericClick(mc);
            if (link.startsWith("https://") || link.startsWith("http://") || !link.contains(":")) {
                URI uri;
                try {
                     uri = new URI(link);
                }
                catch (URISyntaxException e) {
                    e.printStackTrace();
                    PIHelpers.displayError("Failed to open link due to unknown error!\n"+e.getMessage());
                    return;
                }
                GuiPopUpDialogBase dialog = new GuiPopUpDialogBase(this);
                dialog.setXSize(300);
                dialog.setDragBar(12);
                dialog.setCloseOnCapturedClick(true);
                MGuiElementBase background;
                dialog.addChild(background = new StyledGuiRect("user_dialogs"));

                GuiLabel infoLabel = new GuiLabel(I18n.format("pi.md.link_confirmation.txt"));
                infoLabel.setWrap(true).setShadow(false);
                infoLabel.setTextColour(StyleHandler.getInt("user_dialogs." + StyleHandler.StyleType.TEXT_COLOUR.getName()));
                infoLabel.setXSize(dialog.xSize() - 30);
                infoLabel.setHeightForText();
                infoLabel.setRelPos(15, 10);
                dialog.addChild(infoLabel);

                GuiLabel urlLabel = new GuiLabel("\"" + link + "\"");
                urlLabel.setWrap(true).setShadow(false);
                urlLabel.setTextColour(StyleHandler.getInt("user_dialogs." + StyleHandler.StyleType.TEXT_COLOUR.getName()));
                urlLabel.setXSize(dialog.xSize() - 30);
                urlLabel.setHeightForText();
                urlLabel.setPos(infoLabel.xPos(), infoLabel.maxYPos() + 10);
                dialog.addChild(urlLabel);

                GuiButton yesButton = new StyledGuiButton("user_dialogs." + StyleHandler.StyleType.BUTTON_STYLE.getName());
                yesButton.setText(I18n.format("pi.button.yes"));
                yesButton.setSize(80, 15);
                yesButton.setPos(dialog.xPos() + 15, urlLabel.maxYPos() + 10);
                yesButton.setListener(() -> Utils.openWebLink(uri));
                dialog.addChild(yesButton);

                GuiButton copyButton = new StyledGuiButton("user_dialogs." + StyleHandler.StyleType.BUTTON_STYLE.getName());
                copyButton.setText(I18n.format("pi.button.copy_to_clipboard"));
                copyButton.setSize(108, 15);
                copyButton.setRelPos(yesButton, 81, 0);
                copyButton.setListener(() -> Utils.setClipboardString(link));
                dialog.addChild(copyButton);

                GuiButton cancelButton = new StyledGuiButton("user_dialogs." + StyleHandler.StyleType.BUTTON_STYLE.getName());
                cancelButton.setText(I18n.format("pi.button.cancel"));
                cancelButton.setSize(80, 15);
                cancelButton.setRelPos(copyButton, 109, 0);
                dialog.addChild(cancelButton);

                dialog.setYSize((cancelButton.maxYPos() + 10) - dialog.yPos());
                background.setPosAndSize(dialog);
                dialog.showCenter();
                return;
            }
            DocumentationPage page = DocumentationManager.getPage(link);
            if (!(page instanceof RootPage)) {
                if (page == null) {
                    PIHelpers.displayError("The specified page \"" + link + "\" could not be found!\nThis is ether a broken link or the mod to which the specified page belongs is not installed.");
                }
                else {
                    TabManager.openPage(link, button == 2);
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

            //region scrollBar
            if (StyleHandler.getBoolean("md_window.scroll_bar." + StyleHandler.StyleType.COMPACT_BAR.getName())) {
                if (scrollBar.xSize() != 4) {
                    scrollElement.resetScrollPositions();
                    scrollBar.setXSize(4);
                    scrollBar.getBackgroundElement().setXSize(4);
                    scrollBar.getSliderElement().setXSize(4);
                    scrollBar.setInsets(1, 0, 1, 0);
                    scrollElement.setInsets(2, 1, 2, 6);
                    markdownContainer.reloadElement();
                    markdownContainer.layoutMarkdownElements();
                }
            }
            else if (scrollBar.xSize() != 10) {
                scrollElement.resetScrollPositions();
                scrollBar.setXSize(10);
                scrollBar.getBackgroundElement().setXSize(10);
                scrollBar.getSliderElement().setXSize(8);
                scrollBar.setInsets(1, 1, 1, 1);
                scrollElement.setInsets(2, 1, 2, 11);
                markdownContainer.reloadElement();
                markdownContainer.layoutMarkdownElements();
            }

            if (scrollBar.getRawPos() != tabData.scrollPosition) {
                scrollBar.updateRawPos(tabData.scrollPosition, true);
            }

            //endregion

            //region Reload
            //Reloading is done like this because it is an expensive task and the regular reloadElement method may be called multiple times during a reload operation.
            if (shouldReloadMD && isEnabled()) {
                shouldReloadMD = false;
//                markdownElement.clear();
//                markdownElement.clearContainer();
                DocumentationPage page = tabData.getDocPage();

//                MDElementFactory factory = new MDElementFactory(markdownElement);
//                factory.setColourSupplier(() -> baseTextColour);

                if (page == null) {
                    String[] errorText = new String[]{ //
                            "An error occurred while loading the page!", //
                            "Page at " + tabData.pageURI + " could not be found.", //
                            "This error should not be possible unless PI is in an invalid state.", //
                            "I suggest you try reloading PI."};

                    updateMarkdown(Lists.newArrayList(errorText));
//                    markdownElement.parseMarkdown(errorText);
//                    new PiMarkdownReader(errorText).accept(factory);
                }
                else {
//                    markdownElement.parseMarkdown(page.getMarkdownLines());
//                    new PiMarkdownReader(page.getMarkdownLines()).accept(factory);
                    updateMarkdown(page.getMarkdownLines());
                }
//                markdownElement.reloadElement();
//                scrollElement.updateScrollElement();
//                scrollElement.reloadElement();
//                markdownElement.updateLayout();
            }
            //endregion

            updateSizePosition(true);

            return super.onUpdate();
        }

        private void updateMarkdown(List<String> mdLines) {
            markdownContainer.clearContainer();

            PiMarkdownReader reader = new PiMarkdownReader(mdLines);
            MDElementFactory factory = new MDElementFactory(markdownContainer);
            factory.setColourSupplier(() -> baseTextColour);
            reader.accept(factory);

            markdownContainer.layoutMarkdownElements();
        }
    }
}


//        if (PIUpdateManager.downloadManager.running) {
//                int i = (ClientEventHandler.elapsedTicks / 10) % 3;
//                drawCenteredString(fontRenderer, "Downloading Updates" + (i == 0 ? "." : i == 1 ? ".." : "..."), xSize() / 2, 5, 0xFFFFFF, false);
//                }