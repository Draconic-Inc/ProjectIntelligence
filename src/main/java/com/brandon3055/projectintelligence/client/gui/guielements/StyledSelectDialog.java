package com.brandon3055.projectintelligence.client.gui.guielements;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiSelectDialog;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.StyleHandler.StyleType;
import com.brandon3055.projectintelligence.docdata.LanguageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by brandon3055 on 14/08/2017.
 */
public class StyledSelectDialog<T> extends GuiSelectDialog<T> {

    private String prop;
    private final String heading;
    private String selected = "";

    public StyledSelectDialog(MGuiElementBase parent, String prop, String heading) {
        super(parent);
        this.prop = prop;
        this.heading = heading;
        setSize(150, 200);
        setInsets(14, 2, 2, 2);
        setDragBar(13);

        setRendererBuilder(t -> {
            GuiButton button = new StyledGuiButton(prop + "." + StyleType.BUTTON_STYLE.getName()).setText(LanguageManager.LANG_NAME_MAP.getOrDefault(String.valueOf(t), String.valueOf(t)));
            if (t.equals(selected)) {
                button.setToggleMode(true).setToggleState(true);
            }
            button.setInsets(0, 2, 1, 2).setWrap(true).setShadow(false).setTextColGetter((hovering, disabled) -> hovering ? 0x0000FF : 0);
            button.setYSizeMod((guiLabel, integer) -> guiLabel.fontRenderer.getWordWrappedHeight(button.getDisplayString(), Math.max(10, guiLabel.xSize() - button.getInsets().left - button.getInsets().right)) + 6);
            return button;
        });
    }

    @Override
    public void reloadElement() {
        getScrollElement().setListSpacing(1);
        super.reloadElement();
    }

    public StyledSelectDialog<T> setSelected(String selected) {
        this.selected = selected;
        return this;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        boolean thickBorders = StyleHandler.getBoolean(prop + "." + StyleType.THICK_BORDERS.getName());
        int border = StyleHandler.getInt(prop + "." + StyleType.BORDER.getName());

        if (StyleHandler.getBoolean(prop + "." + StyleType.VANILLA_TEXTURE.getName())) {
            StyleHandler.getColour(prop + "." + StyleType.COLOUR.getName()).glColour();
            ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI);
            drawTiledTextureRectWithTrim(xPos(), yPos(), xSize(), ySize(), 4, 4, 4, 4, 0, thickBorders ? 0 : 128, 256, 128);
            GlStateManager.color(1, 1, 1, 1);
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, border);
        }
        else {
            int fill = StyleHandler.getInt(prop + "." + StyleType.COLOUR.getName());
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), thickBorders ? 3 : 1, fill, border);
        }

        drawCenteredString(fontRenderer, heading, xPos() + xSize() / 2F, yPos() + 4, StyleHandler.getInt(prop + "." + StyleType.TEXT_COLOUR.getName()), false);

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }
}
