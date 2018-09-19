package com.brandon3055.projectintelligence.client.gui;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler.PropertyGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.function.Supplier;

/**
 * Created by brandon3055 on 23/08/18.
 */
public class PIPartRenderer {

    public PropertyGroup props;
    private boolean[] sides = new boolean[]{true, true, true, true};
    private boolean squareTex = true;
    private int tabRender = 0;
    private boolean buttonRender = false;

    public PIPartRenderer(PropertyGroup props) {
        this.props = props;
    }

    public PIPartRenderer setSideTrims(boolean top, boolean left, boolean bottom, boolean right) {
        this.sides = new boolean[]{top, left, bottom, right};
        return this;
    }

    public PIPartRenderer setSquareTex(boolean squareTex) {
        this.squareTex = squareTex;
        return this;
    }

    public PIPartRenderer setButtonRender(boolean buttonRender) {
        this.buttonRender = buttonRender;
        return this;
    }

    public PIPartRenderer setTabRender(int tabRender) {
        this.tabRender = tabRender;
        return this;
    }

    public void render(MGuiElementBase parent, boolean mouseOver) {
        render(parent, parent.xPos(), parent.yPos(), parent.xSize(), parent.ySize(), mouseOver);
    }

    public void render(MGuiElementBase parent) {
        render(parent, parent.xPos(), parent.yPos(), parent.xSize(), parent.ySize(), false);
    }

    public void render(MGuiElementBase parent, int x, int y, int width, int height) {
        render(parent, x, y, width, height, false);
    }

    public void render(MGuiElementBase parent, int x, int y, int width, int height, boolean mouseOver) {
        boolean vanillaTex = props.hasPropVanillaTex() && props.vanillaTex();
        boolean thickBorders = props.hasPropThickBorders() && props.thickBorders();
        int colour = props.hasPropColourHover() && mouseOver ? props.colourHover() : props.colour();
        int border = props.hasPropBorderHover() && mouseOver ? props.borderHover() : props.border();

        if (vanillaTex) {
            if (props.hasPropColourHover() && mouseOver) {
                props.glColourHover();
            }
            else {
                props.glColour();
            }

            if (buttonRender) {
                int texV = 48 + ((mouseOver ? 2 : 1) * 20);
                ResourceHelperBC.bindTexture(PITextures.PI_PARTS);
                parent.drawTiledTextureRectWithTrim(x, y, width, height, 2, 2, 2, 2, 0, texV, 200, 20);
                GlStateManager.color(1, 1, 1, 1);
                parent.drawBorderedRect(x, y, width, height, 1, 0, border);
            }
            else {
                ResourceHelperBC.bindTexture(squareTex ? PITextures.VANILLA_GUI_SQ : PITextures.VANILLA_GUI);

                int texU = left() ? 0 : 4;
                int texV = (thickBorders ? 0 : 128) + (top() ? 0 : 4);
                int texW = 256 - (left() ? 0 : 4) - (right() ? 0 : 4);
                int texH = 128 - (top() ? 0 : 4) - (bottom() ? 0 : 4);

                parent.drawTiledTextureRectWithTrim(x, y, width, height, top() ? 4 : 0, left() ? 4 : 0, bottom() ? 4 : 0, right() ? 4 : 0, texU, texV, texW, texH);

                GlStateManager.color(1, 1, 1, 1);
                if (squareTex) {
                    drawShadedRect(parent, x, y, width, height, 1, 0, border, border, border);
                }

                if (tabRender > 0) {
                    parent.drawTiledTextureRectWithTrim(x + 1, y + height - 1, width - 2, tabRender + 1, 0, 4, 0, 4, texU + 1, texV + 4, texW - 2, texH - 8);
                }
            }
        }
        else {
            boolean shadedBorders = props.hasPropShadeBorders() && props.shadeBorders();
            boolean invertShade = props.hasPropInvertShade() && props.invertShade();
            int bw = thickBorders ? 2 : 1;

            if (shadedBorders) {
                int light = parent.changeShade(border, 0.2);
                int dark = parent.changeShade(border, -0.2);
                drawShadedRect(parent, x, y, width, height, bw, colour, invertShade ? dark : light, invertShade ? light : dark, border);
                if (tabRender > 0) {
                    parent.drawColouredRect(x + bw, y + height, width - (bw * 2), tabRender, colour);
                    parent.drawColouredRect(x + width - bw, y + height, bw, bw, invertShade ? light : dark);
                }
            }
            else {
                drawShadedRect(parent, x, y, width, height, bw, colour, border, border, border);
                if (tabRender > 0) {
                    parent.drawColouredRect(x + bw, y + height, width - (bw * 2), tabRender, colour);
                }
            }
        }
    }

    public void drawShadedRect(MGuiElementBase element, int x, int y, int width, int height, int bw, int fill, int topLeftColour, int bottomRightColour, int cornerMixColour) {
        //Fill
        element.drawColouredRect(x + (left() ? bw : 0), y + (top() ? bw : 0), width - (left() ? bw : 0) - (right() ? bw : 0), height - (top() ? bw : 0) - (bottom() ? bw : 0), fill);
        //Top
        if (top()) {
            element.drawColouredRect(x, y, width - (right() ? bw : 0), bw, topLeftColour);
        }
        //Left
        if (left()) {
            element.drawColouredRect(x, y + (top() ? bw : 0), bw, height - (top() ? bw : 0) - (bottom() ? bw : 0), topLeftColour);
        }
        //Bottom
        if (bottom()) {
            element.drawColouredRect(x + (left() ? bw : 0), y + height - bw, width - (left() ? bw : 0), bw, bottomRightColour);
        }
        //Right
        if (right()) {
            element.drawColouredRect(x + width - bw, y + (top() ? bw : 0), bw, height - (top() ? bw : 0) - (bottom() ? bw : 0), bottomRightColour);
        }
        //Top Right Corner
        if (top() && right()) {
            element.drawColouredRect(x + width - bw, y, bw, bw, cornerMixColour);
        }
        //Bottom Left Corner
        if (bottom() && left()) {
            element.drawColouredRect(x, y + height - bw, bw, bw, cornerMixColour);
        }
    }

    private boolean top() {
        return sides[0];
    }

    private boolean left() {
        return sides[1];
    }

    private boolean bottom() {
        return sides[2];
    }

    private boolean right() {
        return sides[3];
    }

    public StyledElement asElement() {
        return new StyledElement(this);
    }

    public static class StyledElement extends MGuiElementBase<StyledElement> {

        private Supplier<Boolean> hoverStateSupplier = null;
        private PIPartRenderer renderer;

        public StyledElement(PIPartRenderer renderer) {
            this.renderer = renderer;
        }

        public StyledElement setHoverStateSupplier(Supplier<Boolean> hoverStateSupplier) {
            this.hoverStateSupplier = hoverStateSupplier;
            return this;
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            renderer.render(this, xPos(), yPos(), xSize(), ySize(), isMouseOver(mouseX, mouseY) || (hoverStateSupplier != null && hoverStateSupplier.get()));
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
    }
}
