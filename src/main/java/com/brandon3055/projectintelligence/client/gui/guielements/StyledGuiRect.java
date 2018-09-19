package com.brandon3055.projectintelligence.client.gui.guielements;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.StyleHandler.StyleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by brandon3055 on 14/08/2017.
 */
public class StyledGuiRect extends MGuiElementBase<StyledGuiRect> {

    private String prop;
    private boolean allowBorderSize;
    private boolean allowVanilla;
    private boolean fillMouseOver;
    private boolean borderMouseOver;

    public StyledGuiRect(String prop) {
        this.prop = prop;
        this.allowBorderSize = StyleHandler.hasProp(prop + "." + StyleType.THICK_BORDERS.getName());
        this.allowVanilla = StyleHandler.hasProp(prop + "." + StyleType.VANILLA_TEXTURE.getName());
        this.fillMouseOver = StyleHandler.hasProp(prop + "." + StyleType.HOVER.getName());
        this.borderMouseOver = StyleHandler.hasProp(prop + "." + StyleType.BORDER_HOVER.getName());
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        boolean thickBorders = allowBorderSize && StyleHandler.getBoolean(prop + "." + StyleType.THICK_BORDERS.getName());
        boolean mouseOver = isMouseOver(mouseX, mouseY);
        int border = borderMouseOver && mouseOver ? StyleHandler.getInt(prop + "." + StyleType.BORDER_HOVER.getName()) : StyleHandler.getInt(prop + "." + StyleType.BORDER.getName());

        if (allowVanilla && StyleHandler.getBoolean(prop + "." + StyleType.VANILLA_TEXTURE.getName())) {
            if (fillMouseOver && mouseOver) {
                StyleHandler.getColour(prop + "." + StyleType.HOVER.getName()).glColour();
            }
            else {
                StyleHandler.getColour(prop + "." + StyleType.COLOUR.getName()).glColour();
            }
            ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI);
            drawTiledTextureRectWithTrim(xPos(), yPos(), xSize(), ySize(), 4, 4, 4, 4, 0, thickBorders ? 0 : 128, 256, 128);
            GlStateManager.color(1, 1, 1, 1);
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, border);
        }
        else {
            int fill = fillMouseOver && mouseOver ? StyleHandler.getInt(prop + "." + StyleType.HOVER.getName()) : StyleHandler.getInt(prop + "." + StyleType.COLOUR.getName());
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), thickBorders ? 3 : 1, fill, border);
        }

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }
//
//    //Version 2 styled rectangle. //TODO replace and remove V1 with V2
//    public static class V2 extends MGuiElementBase<V2> {
//
//        private PropertyGroup props;
//        private Supplier<Boolean> hoverStateSupplier = null;
//
//        public V2(PropertyGroup props) {
//            this.props = props;
//        }
//
//        public V2(PropertyGroup props, Supplier<Boolean> hoverStateSupplier) {
//            this.props = props;
//            this.hoverStateSupplier = hoverStateSupplier;
//        }
//
//        @Override
//        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//            boolean thickBorders = props.hasPropThickBorders() && props.thickBorders();
//            boolean mouseOver = isMouseOver(mouseX, mouseY) || (hoverStateSupplier != null && hoverStateSupplier.get());
//            int border = props.hasPropBorderHover() && mouseOver ? props.borderHover() : props.border();
//
//            if (props.hasPropVanillaTex() && props.vanillaTex()) {
//                if (props.hasPropColourHover() && mouseOver) {
//                    props.glColourHover();
//                }
//                else {
//                    props.glColour();
//                }
//                ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI);
//                drawTiledTextureRectWithTrim(xPos(), yPos(), xSize(), ySize(), 4, 4, 4, 4, 0, thickBorders ? 0 : 128, 256, 128);
//                GlStateManager.color(1, 1, 1, 1);
//                drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, border);
//            }
//            else {
//                int fill = props.hasPropColourHover() && mouseOver ? props.colourHover() : props.colour();
//                if (props.hasPropShadeBorders() && props.shadeBorders()) {
//                    int light = changeShade(border, 0.2);
//                    int dark = changeShade(border, -0.2);
//                    boolean invertShade = props.hasPropInvertShade() && props.invertShade();
//                    drawShadedRect(xPos(), yPos(), xSize(), ySize(), thickBorders ? 2 : 1, fill, invertShade ? dark : light, invertShade ? light : dark, border);
//                }
//                else {
//                    drawBorderedRect(xPos(), yPos(), xSize(), ySize(), thickBorders ? 2 : 1, fill, border);
//                }
//            }
//
//            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//        }
//    }
}
