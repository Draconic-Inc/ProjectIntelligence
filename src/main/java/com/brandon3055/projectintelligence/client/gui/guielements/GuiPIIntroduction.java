package com.brandon3055.projectintelligence.client.gui.guielements;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.TransformingVertexBuilder;
import codechicken.lib.util.SneakyUtils;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.Vertex5;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.lib.vec.uv.UV;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.brandon3055.brandonscore.client.utils.GuiHelperOld;
import com.brandon3055.brandonscore.lib.DelayedTask;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.StyleHandler.PropertyGroup;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.PIGuiContainer;
import com.brandon3055.projectintelligence.client.gui.PIPartRenderer;
import com.brandon3055.projectintelligence.client.keybinding.KeyInputHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 7/21/2018.
 */
public class GuiPIIntroduction extends GuiElement<GuiPIIntroduction> {
    private static PropertyGroup windowProps = new PropertyGroup("user_dialogs");
    private static PropertyGroup buttonProps = new PropertyGroup("user_dialogs.button_style");
    private PIGuiContainer container;
    private List<InfoScreen> screens = new LinkedList<>();
    private int selectedScreen = 0;

    public GuiPIIntroduction(PIGuiContainer container) {
        this.container = container;
        addInfoScreens();
    }

    private void addInfoScreens() {
        screens.add(new InfoScreen(this, "pi.info_screen.welcome.title", "pi.info_screen.welcome.text").setSize(250, 200));
        screens.add(new InfoScreen(this, "pi.info_screen.guide_intro.title", "pi.info_screen.guide_intro.text").setSize(250, 120));
        screens.add(new OverviewScreen(this, "pi.info_screen.basic_overview.title", "pi.info_screen.basic_overview.text").setSize(150, 90));
        screens.add(new InfoScreen(this, "pi.info_screen.pi_interaction.title", "pi.info_screen.pi_interaction.text", KeyInputHandler.openPI.getTranslatedKeyMessage().getString(), KeyInputHandler.etGUI.getTranslatedKeyMessage().getString(), KeyInputHandler.etWorld.getTranslatedKeyMessage().getString()).setSize(250, 200));
        screens.add(new StyleScreen(this, "pi.info_screen.ui_style.title", "pi.info_screen.ui_style.text").setSize(250, 100));
        screens.add(new InfoScreen(this, "pi.info_screen.contributing.title", "pi.info_screen.contributing.text").setSize(250, 100));
    }

    @Override
    public void addChildElements() {
        super.addChildElements();
        addChildren(screens);
        screens.forEach(screen -> screen.setEnabledCallback(() -> screens.indexOf(screen) == selectedScreen));
    }

    private void nextScreen() {
        if (selectedScreen + 1 == screens.size()) {
            close(false);
            return;
        }
        selectedScreen = MathHelper.clip(selectedScreen + 1, 0, screens.size() - 1);
        screens.get(selectedScreen).onScreenDisplayed();
    }

    private void prevScreen() {
        if (selectedScreen == 0) {
            close(false);
            return;
        }
        selectedScreen = MathHelper.clip(selectedScreen - 1, 0, screens.size() - 1);
        screens.get(selectedScreen).onScreenDisplayed();
    }

    private void close(boolean showLater) {
        if (showLater) {
            PIConfig.showTutorialLater = true;
            GuiNotifications.addNotification(I18n.get("pi.notification.show_tutorial_later.txt"), 5);
        }
        else {
            PIConfig.tutorialDisplayed = true;
            PIConfig.save();
        }
        modularGui.getManager().removeChild(this);
    }

    @Override
    protected boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 59 || keyCode == 1) {
            close(!PIConfig.tutorialDisplayed);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return true;
    }

    @Override
    public boolean handleMouseScroll(double mouseX, double mouseY, double scrollDirection) {
        super.handleMouseScroll(mouseX, mouseY, scrollDirection);
        return true;
    }

    private static class InfoScreen extends GuiElement<InfoScreen> {
        protected PIPartRenderer windowRenderer = new PIPartRenderer(windowProps);
        protected PIPartRenderer buttonRenderer = new PIPartRenderer(buttonProps).setButtonRender(true);

        protected GuiPIIntroduction parent;
        protected final GuiLabel title;
        protected final GuiLabel info;
        protected GuiScrollElement infoContainer;
        protected StyledGuiButton nextButton;
        protected StyledGuiButton prevButton;
        protected GuiButton later;

        private InfoScreen(GuiPIIntroduction parent, String unLocalTitle, String unLocalInfo, Object... localParamaters) {
            this.parent = parent;
            this.title = new GuiLabel(I18n.get(unLocalTitle));
            this.info = new GuiLabel(I18n.get(unLocalInfo, localParamaters).replace("  ", "\n\n"));
        }

        @Override
        public void addChildElements() {
            title.setAlignment(GuiAlign.CENTER);
            title.setWrap(true).setShadow(false);
            title.setTextColour(windowProps.textColour());

            info.setWrap(true).setShadow(false);
            info.setTextColour(windowProps.textColour());

            infoContainer = new GuiScrollElement();
            infoContainer.setListMode(GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH);
            infoContainer.setInsets(3, 5, 1, 5);
            infoContainer.setStandardScrollBehavior();

            addChild(title);
            addChild(infoContainer);
            infoContainer.addElement(info);

            int index = parent.screens.indexOf(this);
            addChild(nextButton = new StyledGuiButton(buttonRenderer)).setText(I18n.get(index == parent.screens.size() - 1 ? "pi.button.close" : "pi.button.next"));
            addChild(prevButton = new StyledGuiButton(buttonRenderer)).setText(I18n.get(index == 0 ? "pi.button.skip" : "pi.button.previous"));

            if (index == 0 && PIConfig.tutorialDisplayed && !PIConfig.showTutorialLater) {
                later = new StyledGuiButton(buttonRenderer).setText(I18n.get("pi.button.show_me_later"));
                later.onPressed(() -> parent.close(true));
                later.setTrim(false);
                addChild(later);
            }

            nextButton.onPressed(() -> parent.nextScreen());
            prevButton.onPressed(() -> parent.prevScreen());

            super.addChildElements();
        }

        @Override
        public void reloadElement() {
            setPos(screenWidth / 2 - xSize() / 2, screenHeight / 2 - ySize() / 2);
            title.setPos(xPos() + 3, yPos() + 4).setHeightForText(xSize() - 6);
            infoContainer.setPos(xPos(), title.maxYPos() + 2);
            infoContainer.setSize(xSize() - 4, ySize() - title.ySize() - 26);
            info.setHeightForText(infoContainer.getInsetRect().width);
            infoContainer.updateScrollElement();

            prevButton.setSize(60, 14);
            prevButton.setPos(xPos() + 4, maxYPos() - prevButton.ySize() - 4);

            nextButton.setSize(60, 14);
            nextButton.setPos(maxXPos() - nextButton.xSize() - 4, maxYPos() - prevButton.ySize() - 4);

            if (later != null) {
                later.setSize(100, 14);
                later.setPos(xPos() + xSize() / 2 - later.xSize() / 2, maxYPos() - prevButton.ySize() - 4);
            }

            super.reloadElement();
        }

        protected void onScreenDisplayed() {
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            windowRenderer.render(this);
            drawColouredRect(title.xPos(), title.yPos() - 1, title.xSize(), title.ySize() + 1, changeShade(windowProps.colour(), -0.2));
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
    }

    private static class OverviewScreen extends InfoScreen {

        private List<InfoArrow> arrows = new LinkedList<>();

        private OverviewScreen(GuiPIIntroduction parent, String unLocalTitle, String unLocalInfo) {
            super(parent, unLocalTitle, unLocalInfo);
        }

        @Override
        public void reloadElement() {
            super.reloadElement();
            DelayedTask.run(1, this::onScreenDisplayed);//Need to wait until after the reload call has finished.
        }

        private InfoArrow addArrow(int targetX, int targetY, String info) {
            InfoArrow arrow = new InfoArrow(targetX, targetY, info);
            addChild(arrow);
            arrows.add(arrow);
            return arrow;
        }

        private void addArrow(GuiElement target, String info) {
            addArrow(target.xPos() + target.xSize() / 2, target.yPos() + target.ySize() / 2, info).setHighlight(target.getRect());
        }

        @Override
        protected void onScreenDisplayed() {
            toRemove.addAll(arrows);
            arrows.forEach(infoArrow -> infoArrow.setEnabled(false));

            GuiPartPageList list = parent.container.getPageList();
            GuiPartMenu menu = parent.container.getMenu();
            GuiPartMDWindow content = parent.container.getMdWindow();

            if (list != null) {
                list.setFullyExtended();
                Rectangle r = (Rectangle)list.backButton.getRect().clone();
                r.add(list.forwardButton.getRect());
                addArrow(list.backButton.maxXPos(), list.backButton.yPos() + list.backButton.ySize() / 2, "pi.intro.arrow_info.back_forward.txt").setHighlight(r);
                addArrow(list.toggleView, "pi.intro.arrow_info.toggle_nav.txt");
                addArrow(list.scrollElement.xPos() + list.scrollElement.xSize() / 2, screenHeight / 3, "pi.intro.arrow_info.button_list.txt").setHighlight(list.scrollElement.getRect());
                addArrow(7, screenHeight / 2, "pi.intro.arrow_info.current_directory.txt").setHighlight(new Rectangle(0, list.scrollElement.yPos(), list.scrollElement.xPos(), list.scrollElement.ySize()));
                addArrow(list.searchBox, "pi.intro.arrow_info.search_box.txt");
                addArrow(list.searchSettings, "pi.intro.arrow_info.search_settings.txt");
            }

            if (menu != null) {
                addArrow(menu.settingsButton, "pi.intro.arrow_info.settings.txt");
                addArrow(menu.minimizeButton, "pi.intro.arrow_info.minimize.txt");
            }
            addArrow(content.xPos() + content.xSize() / 2, content.yPos() + 8, "pi.intro.arrow_info.content_tabs.txt").setHighlight(new Rectangle(content.xPos(), content.yPos(), content.xSize(), 12));
            addArrow(content.xPos() + content.xSize() / 2, content.yPos() + content.ySize() / 4, "pi.intro.arrow_info.content_window.txt").setHighlight(new Rectangle(content.xPos(), content.yPos() + 12, content.xSize(), content.ySize() - 12));

        }

        private static class InfoArrow extends GuiElement<InfoArrow> {
            private final int targetX;
            private final int targetY;
            private final String info;
            private double animProgress = 0;
            private static final CCModel arrowModel;
            private CCModel activeModel = arrowModel.copy();
            private Rectangle highlight;

            static {
                arrowModel = CCModel.quadModel(4);
                double px = 1 / 256D;
                arrowModel.verts[3] = new Vertex5(new Vector3(0, 16, 0), new UV(px * 16, px * 48));
                arrowModel.verts[2] = new Vertex5(new Vector3(0, 0, 0), new UV(px * 16, px * 32));
                arrowModel.verts[1] = new Vertex5(new Vector3(24, 0, 0), new UV(px * 40, px * 32));
                arrowModel.verts[0] = new Vertex5(new Vector3(24, 16, 0), new UV(px * 40, px * 48));
                arrowModel.computeNormals();
            }

            public InfoArrow(int targetX, int targetY, String info) {
                this.targetX = targetX;
                this.targetY = targetY;
                this.info = info;
            }

            public static RenderType PARTS_TYPE = RenderType.create("pi_parts_tex", DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 256, RenderType.State.builder()
                    .setTextureState(new RenderState.TextureState(PITextures.PI_PARTS, false, false))
                    .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderState.NO_CULL)
                    .setAlphaState(RenderState.DEFAULT_ALPHA)
                    .setTexturingState(new RenderState.TexturingState("lighting", RenderSystem::disableLighting, SneakyUtils.none()))
                    .createCompositeState(false)
            );

            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                super.renderElement(minecraft, mouseX, mouseY, partialTicks);

                IRenderTypeBuffer getter = RenderUtils.getTypeBuffer();
                CCRenderState ccrs = CCRenderState.instance();
                ccrs.reset();
                ccrs.bind(PARTS_TYPE, getter);
                ccrs.baseColour = 0xFFFFFFFF;
                activeModel.render(ccrs);
                RenderUtils.endBatch(getter);
            }

            public InfoArrow setHighlight(Rectangle highlight) {
                this.highlight = highlight;
                return this;
            }

            @Override
            public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                Vertex5[] v = activeModel.verts;
                int[] x = new int[]{(int) v[0].vec.x, (int) v[1].vec.x, (int) v[2].vec.x, (int) v[3].vec.x};
                int[] y = new int[]{(int) v[0].vec.y, (int) v[1].vec.y, (int) v[2].vec.y, (int) v[3].vec.y};
                Polygon polygon = new Polygon(x, y, 4);
                if (polygon.contains(mouseX, mouseY)) {
                    if (highlight != null) {
                        drawColouredRect(highlight.x, highlight.y, highlight.width, highlight.height, 0x4000FF00);
                    }
                    drawHoveringText(Collections.singletonList(I18n.get(info)), mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                    return true;
                }
                return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
            }

            @Override
            public boolean onUpdate() {
                if (animProgress < 1) {
                    animProgress = MathHelper.clip(animProgress + 0.15, 0, 1);
                    double d = MathHelper.clip(animProgress, 0, 1);
                    int centerX = screenWidth / 2;
                    int centerY = screenHeight / 2;
                    double dist = Utils.getDistanceAtoB(centerX, centerY, targetX, targetY) * d;

                    activeModel = arrowModel.copy();
                    Vector3 offset = new Vector3(targetX - (screenWidth / 2D), targetY - (screenHeight / 2D), 0).normalize();
                    activeModel.apply(new Translation(-dist, -8, 0));
                    activeModel.apply(new Rotation(Math.atan2(offset.x, offset.y) + (90 * MathHelper.torad), 0, 0, -1));
                    activeModel.apply(new Translation(centerX, centerY, displayZLevel));
                }

                return super.onUpdate();
            }
        }
    }

    private static class StyleScreen extends InfoScreen {
        private GuiButton nextStyle;

        private StyleScreen(GuiPIIntroduction parent, String unLocalTitle, String unLocalInfo) {
            super(parent, unLocalTitle, unLocalInfo);
        }

        @Override
        public void addChildElements() {
            super.addChildElements();
            nextStyle = new StyledGuiButton(buttonRenderer);
            nextStyle.setText(I18n.get("pi.button.next_style"));
            nextStyle.onPressed(this::nextStyle);
            nextStyle.setSize(80, 14);
            addChild(nextStyle);
        }

        @Override
        public void reloadElement() {
            super.reloadElement();
            nextStyle.setPos(xPos() + xSize() / 2 - nextStyle.xSize() / 2, prevButton.yPos());
            nextButton.setDisabled(StyleHandler.getDefaultPresets().isEmpty());
        }

        int index = 0;
        private void nextStyle() {
            index++;
            List<String> presets = new ArrayList<>(StyleHandler.getDefaultPresets());
            String preset = presets.get(index % presets.size());
            StyleHandler.loadPreset(preset, false);
        }
    }
}
