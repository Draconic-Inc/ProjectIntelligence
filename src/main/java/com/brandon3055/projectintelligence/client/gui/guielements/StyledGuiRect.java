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
@Deprecated
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
}
