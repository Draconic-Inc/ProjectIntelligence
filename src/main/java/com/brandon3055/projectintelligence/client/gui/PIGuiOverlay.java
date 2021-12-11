package com.brandon3055.projectintelligence.client.gui;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElementManager;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiBorderedRect;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementFactory;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.PiMarkdownReader;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.brandon3055.projectintelligence.api.PiAPI;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.gui.guielements.GuiStyleEditor;
import com.brandon3055.projectintelligence.docmanagement.DocumentationManager;
import com.brandon3055.projectintelligence.docmanagement.DocumentationPage;
import com.brandon3055.projectintelligence.docmanagement.RootPage;
import com.brandon3055.projectintelligence.registry.GuiDocRegistry.GuiDocHelper;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl.SliderRotation.VERTICAL;

/**
 * Created by brandon3055 on 7/25/2018.
 */
public class PIGuiOverlay implements IModularGui<Screen> {

    private static final StyleHandler.PropertyGroup windowProps = new StyleHandler.PropertyGroup("gui_docs");
    private static final StyleHandler.PropertyGroup settingsProps = new StyleHandler.PropertyGroup("gui_docs.settings_button");
    private static final StyleHandler.PropertyGroup closeProps = new StyleHandler.PropertyGroup("gui_docs.close_button");
    private static final StyleHandler.PropertyGroup scrollProps = new StyleHandler.PropertyGroup("gui_docs.scroll_bar");
    private static final StyleHandler.PropertyGroup scrollSliderProps = new StyleHandler.PropertyGroup("gui_docs.scroll_bar.scroll_slider");
    private static final StyleHandler.PropertyGroup headerProps = new StyleHandler.PropertyGroup("gui_docs.header");

    private Minecraft mc;
    private int zLevel = 0;
    private int screenWidth;
    private int screenHeight;
    private GuiElementManager manager = new GuiElementManager(this);
    private Screen gui;
    private GuiDocHelper guiDocHelper;
    private DocElement docElement;

    public PIGuiOverlay(Screen gui, GuiDocHelper guiDocHelper) {
        this.gui = gui;
        this.guiDocHelper = guiDocHelper;
        onGuiInit();
    }

    public void onGuiInit() {
        this.mc = Minecraft.getInstance();
        this.screenWidth = mc.getWindow().getGuiScaledWidth();
        this.screenHeight = mc.getWindow().getGuiScaledHeight();
        manager.onGuiInit(mc, screenWidth, screenHeight);
    }

    @Override
    public void addElements(GuiElementManager manager) {
        if (docElement != null) {
            manager.removeChild(docElement);
        }

        docElement = new DocElement(this, guiDocHelper);
        manager.addChild(docElement, 200, false);
    }

    //region Gui Method Pass-through

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return manager.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        return manager.mouseReleased(mouseX, mouseY, state);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        return manager.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
    }
//
//        public boolean mouseClickMove(double mouseX, double mouseY, int clickedMouseButton, long timeSinceLastClick) {
//        return manager.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
//    }



    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return manager.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return manager.keyReleased(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char charTyped, int charCode) {
        return manager.charTyped(charTyped, charCode);
    }

    public void renderElements(int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableDepthTest();
        manager.renderElements(mc, mouseX, mouseY, partialTicks);
    }

    public boolean renderOverlayLayer(int mouseX, int mouseY, float partialTicks) {
        return manager.renderOverlayLayer(mc, mouseX, mouseY, partialTicks);
    }

    public void updateScreen() {
        manager.onUpdate();
    }

    //endregion

    //region IModular Gui

    @Override
    public Screen getScreen() {
        return gui;
    }

    @Override
    public int xSize() {
        return 0;
    }

    @Override
    public int ySize() {
        return 0;
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

    public boolean isMouseOver(int mouseX, int mosueY) {
        return docElement != null && docElement.isMouseOver(mouseX, mosueY);
    }

    public Rectangle getDocBounds() {
        return docElement == null ? null : docElement.getRect();
    }

    //endregion

    private static class DocElement extends GuiElement<DocElement> {
        private static final BookModel MODEL_BOOK = new BookModel();
        private PIPartRenderer windowRenderer = new PIPartRenderer(windowProps).setSquareTex(false);
        private PIPartRenderer scrollRenderer = new PIPartRenderer(scrollProps);
        private PIPartRenderer scrollSlideRenderer = new PIPartRenderer(scrollSliderProps);

        private GuiButton heading;
        private GuiButton prevPage;
        private GuiButton nextPage;
        private GuiButton openInPI;
        private GuiSlideControl scrollBar;
        private GuiScrollElement scrollElement;
        private MDElementContainer markdownContainer;
        private GuiButton settings;
        private GuiButton close;
        private PIGuiOverlay overlay;
        private GuiDocHelper guiDocHelper;
        private String pageName = "";
        private double animState = 0;
        private boolean visible = false;

        private float hoverAnim = 0;
        private float pageAnim = 0;

        public DocElement(PIGuiOverlay overlay, GuiDocHelper guiDocHelper) {
            this.overlay = overlay;
            this.guiDocHelper = guiDocHelper;
            this.visible = guiDocHelper.isDocVisible();
            this.animState = visible ? 1 : 0;
        }

        @Override
        public void addChildElements() {
            super.addChildElements();

            heading = new GuiButton();
            heading.setTextColGetter((hovering, disabled) -> headerProps.textColour(hovering));
            heading.setAlignment(GuiAlign.CENTER);
            heading.setTrim(true);
            heading.setShadow(false);
            heading.onPressed(this::showPageList);
            addChild(heading);

            prevPage = new GuiButton().setSize(10, 10).setHoverText(I18n.get("pi.button.prev_page"));
            GuiTexture tex = new GuiTexture(1, 24, 6, 8, PITextures.PI_PARTS).setPos(2, 1);
            tex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> headerProps.glTextColour(mouseOver));
            tex.setPostDrawCallback(IDrawCallback::resetColour);
            prevPage.addChild(tex);
            addChild(prevPage);
            prevPage.onPressed(() -> changePage(true));

            nextPage = new GuiButton().setSize(10, 10).setHoverText(I18n.get("pi.button.next_page"));
            tex = new GuiTexture(9, 24, 6, 8, PITextures.PI_PARTS).setPos(2, 1);
            tex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> headerProps.glTextColour(mouseOver));
            tex.setPostDrawCallback(IDrawCallback::resetColour);
            nextPage.addChild(tex);
            addChild(nextPage);
            nextPage.onPressed(() -> changePage(false));

            openInPI = new GuiButton().setSize(10, 10).setHoverText(I18n.get("pi.button.open_in_pi_main"));
            tex = new GuiTexture(32, 25, 7, 7, PITextures.PI_PARTS).setPos(2, 1);
            tex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> headerProps.glTextColour(mouseOver));
            tex.setPostDrawCallback(IDrawCallback::resetColour);
            openInPI.addChild(tex);
            addChild(openInPI);
            openInPI.onPressed(() -> PiAPI.openGui(overlay.gui, guiDocHelper.getPages()));

            settings = new GuiButton().setSize(8, 8).setHoverText(I18n.get("pi.config.open_style_settings"));
            GuiTexture settingsTex = new GuiTexture(16, 0, 8, 8, PITextures.PI_PARTS);
            settingsTex.setTexSizeOverride(16, 16);
            settingsTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> settingsProps.glColour(mouseOver));
            settingsTex.setPostDrawCallback(IDrawCallback::resetColour);
            settings.addChild(settingsTex);
            settings.onPressed(this::showSettings);
            addChild(settings);

            close = new GuiButton().setSize(8, 8).setHoverText(I18n.get("pi.button.close"));
            GuiTexture closeTex = new GuiTexture(0, 0, 8, 8, PITextures.PI_PARTS);
            closeTex.setTexSizeOverride(16, 16);
            closeTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> closeProps.glColour(mouseOver));
            closeTex.setPostDrawCallback(IDrawCallback::resetColour);
            close.addChild(closeTex);
            close.onPressed(() -> guiDocHelper.setDocVisible(!guiDocHelper.isDocVisible()));
            addChild(close);

            scrollBar = new GuiSlideControl(VERTICAL);
            scrollBar.setBackgroundElement(scrollRenderer.asElement().setHoverStateSupplier(() -> scrollBar.isDragging()));
            scrollBar.setSliderElement(scrollSlideRenderer.asElement().setHoverStateSupplier(() -> scrollBar.isDragging()));

            scrollElement = new GuiScrollElement();
            scrollElement.setVerticalScrollBar(scrollBar);
            scrollElement.setAllowedScrollAxes(true, false);
            scrollElement.setStandardScrollBehavior();
            scrollElement.setInsets(2, windowProps.thickBorders() ? 5 : 4, 6, 2);
            addChild(scrollElement);

            markdownContainer = new MDElementContainer(this);
            markdownContainer.setInsets(4, 0, 4, 0);
            markdownContainer.setLinkClickCallback(this::openLink);
            markdownContainer.linkDisplayTarget = scrollElement;
            scrollElement.addElement(markdownContainer);
            updateBounds();
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
                    PiAPI.openGui(overlay.gui);
                }
                else {
                    PiAPI.openGui(overlay.gui, page.getPageURI());
                }
            }
        }

        @Override
        public void reloadElement() {
            super.reloadElement();
            int scrollBarX = maxXPos() - 10;
            int scrollBarY = yPos() + 13;
            int scrollBarW = 6;
            int scrollBarH = ySize() - 17;
            int topGap = 11;

            settings.setPos(scrollBarX - 1, scrollBarY - 9);
            close.setPos(xPos() + 5, scrollBarY - 9);
            heading.setPos(xPos() + 27, yPos() + 4).setSize(xSize() - (27 * 2) - 10, 8);
            prevPage.setPos(xPos() + 15, yPos() + 3);
            nextPage.setPos(maxXPos() - 35, yPos() + 3);
            openInPI.setPos(maxXPos() - 25, yPos() + 3);

            scrollElement.setPos(xPos(), yPos() + topGap);
            scrollElement.setSize(xSize() - 10, ySize() - topGap);

            scrollBar.setPos(scrollBarX, scrollBarY);
            scrollBar.setSize(scrollBarW, scrollBarH);
            scrollBar.getBackgroundElement().setPos(scrollBarX, scrollBarY).setSize(scrollBarW, scrollBarH);
            scrollBar.getSliderElement().setXPos(scrollBarX + 1).setXSize(scrollBarW - 2);

            markdownContainer.setPosAndSize(scrollElement.getInsetRect());
            reloadDoc();
        }

        private void changePage(boolean prev) {
            int selected = guiDocHelper.getSelectedIndex();
            if (prev) {
                guiDocHelper.setSelected(Math.max(0, selected - 1));
            }
            else {
                guiDocHelper.setSelected(Math.min(guiDocHelper.getPages().size() - 1, selected + 1));
            }
            reloadDoc();
        }

        private void reloadDoc() {
            String pageURI = guiDocHelper.getSelected();
            DocumentationPage page = DocumentationManager.getPage(pageURI);
            List<String> lines;

            if (page != null) {
                lines = page.getMarkdownLines();
                pageName = page.getDisplayName();
            }
            else {
                lines = Arrays.asList("Could not find specified documentation page!", "Either the documentation failed to download or this documentation version is not compatible with the installed mod version.", "Or it's just plain broken.", "Broken Page: " + pageURI);
                pageName = pageURI;
            }

            heading.setText(pageName);
            markdownContainer.clearContainer();
            PiMarkdownReader reader = new PiMarkdownReader(lines);
            MDElementFactory factory = new MDElementFactory(markdownContainer);
            factory.setColourSupplier(windowProps::textColour);
            reader.accept(factory);
            markdownContainer.reloadElement();
            markdownContainer.layoutMarkdownElements();
            scrollElement.getVerticalScrollBar().setEnabled(true);
            updatePageButtons();
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            Rectangle animRect = getRect();
            double d = animState * 4D;
            if (d < 1 && guiDocHelper.enableButton()) {
                IRenderTypeBuffer.Impl getter = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
                IVertexBuilder buffer = getter.getBuffer(BCSprites.GUI_TYPE);
                renderBook(new MatrixStack(), buffer);
                RenderUtils.endBatch(getter);

                RenderSystem.pushMatrix();
                RenderSystem.translated(xPos() + (animRect.width / 2D) * (1D - d), yPos() + (animRect.height / 2D) * (1D - d), 0);
                RenderSystem.scaled(d, d, 1);
                RenderSystem.translated(-xPos(), -yPos(), 0);
//                zOffset += 500;
            }

            windowRenderer.render(this, xPos(), yPos(), xSize(), ySize(), false);
            drawColouredRect(xPos() + 4, yPos() + 3, xSize() - 16, 10, headerProps.background());
            drawColouredRect(xPos() + 4, maxYPos() - 6, xSize() - 16, 2, headerProps.background());

            if (d < 1) {
//                zOffset -= 500;
                RenderSystem.popMatrix();
            }

            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0x4000FFFF);
        }

        private void renderBook(MatrixStack mStack, IVertexBuilder builder) {
            double scale = ySize() * (1D - Math.min(hoverAnim, 0.25D));
            double xPos = xPos() + (ySize() * Math.max(0.2D, hoverAnim / 2));
            double yPos = yPos() + (ySize() / 2D);

            bindTexture(PITextures.BOOK);
            RenderSystem.pushMatrix();
            RenderSystem.translated(xPos, yPos, 100);
            RenderSystem.scaled(scale, scale, scale);
            RenderSystem.rotatef(-(90 * hoverAnim), 0, 1, 0);
            RenderSystem.rotatef(-30 * hoverAnim, 0, 0, 1);

//            RenderHelper.enableStandardItemLighting(); TODO
            RenderSystem.enableRescaleNormal();
            MODEL_BOOK.setupAnim(pageAnim % 1, 0, hoverAnim, 0);
            MODEL_BOOK.render(mStack, builder, 0, 0, 0, 0, 0, 0.1F);
            RenderSystem.disableRescaleNormal();
//            RenderHelper.enableGUIStandardItemLighting();
            RenderSystem.popMatrix();
        }

        @Override
        public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            if (animState == 0 && isMouseOver(mouseX, mouseY)) {
                drawHoveringText(Lists.newArrayList(I18n.get("pi.gui_in_gui.display_doc.info")), mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                return true;
            }
            return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            if (isMouseOver(mouseX, mouseY)) {
                if (!guiDocHelper.isDocVisible() && guiDocHelper.enableButton()) {
                    GuiButton.playGenericClick(mc);
                    guiDocHelper.setDocVisible(true);
                }
                else {
                    super.mouseClicked(mouseX, mouseY, mouseButton);
                }
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        private void showSettings() {
            GuiStyleEditor editor = new GuiStyleEditor(this);
            editor.show(displayZLevel + 250);
            editor.setXPos(xPos() + (xSize() / 2) - (editor.xSize() / 2));
            editor.setYPos(yPos() + (ySize() / 2) - (editor.ySize() / 2));
        }

        private void showPageList() {
            if (guiDocHelper.getPages().size() <= 1) {
                return;
            }

            GuiPopUpDialogBase dialog = new GuiPopUpDialogBase(this);
            dialog.setSize(heading.xSize(), ((guiDocHelper.getPages().size() - 1) * 10) + 2);
            dialog.addChild(new GuiBorderedRect().setFillColour(headerProps.background())).setPosAndSize(dialog);
            dialog.setPos(heading.xPos(), heading.maxYPos() + 1);
            dialog.setCloseOnOutsideClick(true);
            dialog.setCloseOnCapturedClick(true);

            int y = dialog.yPos() + 1;
            for (String pageURI : guiDocHelper.getPages()) {
                if (guiDocHelper.getSelected().equals(pageURI)) continue;
                DocumentationPage page = DocumentationManager.getPage(pageURI);
                GuiButton button = new GuiButton(page == null ? pageURI : page.getDisplayName());
                button.setTextColGetter((hovering, disabled) -> headerProps.textColour(hovering));
                button.setAlignment(GuiAlign.CENTER);
                button.setShadow(false);
                button.setTrim(true);
                button.setPos(dialog.xPos() + 3, y).setSize(dialog.xSize() - 6, 10);
                button.setToggleMode(true).setToggleStateSupplier(() -> guiDocHelper.getSelected().equals(pageURI));
                button.onPressed(() -> {
                    guiDocHelper.setSelected(pageURI);
                    reloadDoc();
                });
                dialog.addChild(button);
                y += 10;
            }

            dialog.show(displayZLevel + 50);
        }

        @Override
        public boolean onUpdate() {
            visible = guiDocHelper.isDocVisible();
            if (visible && animState < 1) {
                animState = Math.min(1, animState + 0.1);
                if (!guiDocHelper.enableAnimation()) {
                    animState = 1;
                }
                updateBounds();
            }
            else if (!visible && animState > 0) {
                animState = Math.max(0, animState - 0.1);
                if (!guiDocHelper.enableAnimation()) {
                    animState = 0;
                }
                updateBounds();
            }

            boolean mouseover = hoverTime > 0 || animState > 0;
            hoverAnim = (float) MathHelper.approachLinear(hoverAnim, mouseover ? 1 : 0, 0.1);
            pageAnim = (float) MathHelper.approachExp(pageAnim, mouseover ? 0.1 : 5.9, mouseover ? 0.05 : 0.05);

            return super.onUpdate();
        }

        private void updateBounds() {
            if (animState == 1) {
                setPosAndSize(guiDocHelper.getExpandedArea());
                scrollElement.setEnabled(true);
            }
            else if (animState == 0) {
                setPosAndSize(guiDocHelper.getCollapsedArea());
                scrollElement.setEnabled(false);
            }
            else {
                Rectangle minRect = guiDocHelper.getCollapsedArea();
                Rectangle maxRect = guiDocHelper.getExpandedArea();
                double stage1 = MathHelper.clip((animState - 0.25) * 4, 0, 1);
                double stage2 = Math.max(0, (animState - 0.5) * 2);
                double x = MathHelper.map(stage2, 0, 1, minRect.x, maxRect.x);
                double y = MathHelper.map(stage1, 0, 1, minRect.y, maxRect.y);
                double w = MathHelper.map(stage2, 0, 1, minRect.width, maxRect.width);
                double h = MathHelper.map(stage1, 0, 1, minRect.height, maxRect.height);

                setPosAndSize((int) x, (int) y, (int) w, (int) h);
                scrollElement.setEnabled(stage1 == 1);
            }
            scrollBar.setEnabled(animState == 1);
            settings.setEnabled(animState == 1);
            close.setEnabled(animState == 1);
            openInPI.setEnabled(animState == 1);
            updatePageButtons();
            reloadElement();
        }

        private void updatePageButtons() {
            prevPage.setEnabled(animState == 1 && guiDocHelper.getSelectedIndex() > 0);
            nextPage.setEnabled(animState == 1 && guiDocHelper.getSelectedIndex() + 1 < guiDocHelper.getPages().size());
        }
    }
}
