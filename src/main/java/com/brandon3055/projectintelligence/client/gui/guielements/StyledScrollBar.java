package com.brandon3055.projectintelligence.client.gui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiBorderedRect;
import com.brandon3055.projectintelligence.client.StyleHandler;
import net.minecraft.client.Minecraft;

import static com.brandon3055.projectintelligence.client.StyleHandler.StyleType.*;

/**
 * Created by brandon3055 on 22/08/2017.
 */
public class StyledScrollBar extends GuiSlideControl {

    private String prop;

    public StyledScrollBar(String prop) {
        this.prop = prop;
    }

    @Override
    public void addChildElements() {
        GuiBorderedRect background = new GuiBorderedRect();
        background.setFillColourL(hovering -> StyleHandler.getInt(prop + "." + (hovering ? BACKGROUND_HOVER : BACKGROUND).getName()));
        background.setBorderColourL(hovering -> StyleHandler.getInt(prop + "." + (hovering ? BORDER_HOVER : BORDER).getName()));
        setBackgroundElement(background);
        setSliderElement(new ScrollSlider(prop + ".scroll_slider"));

        super.addChildElements();
    }

    public class ScrollSlider extends GuiElement<ScrollSlider> {

        private String prop;

        public ScrollSlider(String prop) {this.prop = prop;}

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            int border = StyleHandler.getInt(prop + "." + StyleHandler.StyleType.BORDER.getName());
            int fill = StyleHandler.getInt(prop + "." + StyleHandler.StyleType.COLOUR.getName());
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, fill, border);

            //I may implement this. Not sure yet
//            if (StyleHandler.getBoolean(prop + "." + StyleHandler.StyleType.VANILLA_TEXTURE.getName())) {
//                StyleHandler.getColour(prop + "." + StyleHandler.StyleType.COLOUR.getName()).glColour();
//                ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI);
//                drawTiledTextureRectWithTrim(xPos(), yPos(), xSize(), ySize(), 4, 4, 4, 4, 0, thickBorders ? 0 : 128, 256, 128);
//                GlStateManager.color(1, 1, 1, 1);
//                drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, border);
//            }
//            else {
//                int fill = StyleHandler.getInt(prop + "." + StyleHandler.StyleType.COLOUR.getName());
//                drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, fill, border);
//            }

            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
    }
}
