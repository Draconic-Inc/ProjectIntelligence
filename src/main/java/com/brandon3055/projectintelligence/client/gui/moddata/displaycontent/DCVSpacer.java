package com.brandon3055.projectintelligence.client.gui.moddata.displaycontent;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiButtonSolid;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.moddata.guidoctree.TreeBranchRoot;
import com.brandon3055.projectintelligence.utils.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.w3c.dom.Element;

import java.util.LinkedList;

/**
 * Created by brandon3055 on 8/09/2016.
 */
public class DCVSpacer extends DisplayComponentBase {

    public DCVSpacer(GuiProjectIntelligence modularGui, String componentType, TreeBranchRoot branch) {
        super(modularGui, componentType, branch);
        setYSize(8);
    }

    //region Render

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);

        if (PIConfig.editMode() && isMouseOver(mouseX, mouseY)) {
            String text = String.format("[Separator: %s Line%s]", ySize() / 8D, ySize() == 8 ? "" : "s");
            drawCenteredString(fontRenderer, text, xPos() + (xSize() / 2), yPos() + (ySize() / 2) - 4, 0x00FF00, true);
        }
    }


    //endregion

    //region Edit

    @Override
    public LinkedList<MGuiElementBase> getEditControls() {
        LinkedList<MGuiElementBase> list = super.getEditControls();
        list.add(new MGuiButtonSolid("SIZE_+", 0, 0, 20, 12, "+") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Increase Spacer Size (Hold shift for fine adjustment)"));
        list.add(new MGuiButtonSolid("SIZE_-", 0, 0, 20, 12, "-") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Decrease Spacer Size (Hold shift for fine adjustment)"));
        return list;
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventElement) {
        super.onMGuiEvent(event, eventElement);
        int modifier = GuiScreen.isShiftKeyDown() ? 1 : 8;

        if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.equals("SIZE_+")) {
            setYSize(ySize() + modifier);
            element.setAttribute("size", String.valueOf(ySize()));
            save();
        }
        else if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.equals("SIZE_-")) {
            setYSize(ySize() - modifier);
            if (ySize() < 4) {
                setYSize(4);
            }
            element.setAttribute("size", String.valueOf(ySize()));
            save();
        }
    }

    @Override
    public void onCreated() {
        element.setAttribute("size", "8");
    }

    //endregion

    //region XML & Factory

    @Override
    public void loadFromXML(Element element) {
        super.loadFromXML(element);
        if (!element.hasAttribute("size")) {
            LogHelper.error("No size found for space in " + branch.branchID);
            return;
        }
        setYSize(Integer.parseInt(element.getAttribute("size")));
    }

    public static class Factory implements IDisplayComponentFactory {
        @Override
        public DisplayComponentBase createNewInstance(GuiProjectIntelligence guiWiki, TreeBranchRoot branch, int screenWidth, int screenHeight) {
            DisplayComponentBase component = new DCVSpacer(guiWiki, getID(), branch);
            component.applyGeneralElementData(guiWiki, guiWiki.mc, screenWidth, screenHeight, BCFontRenderer.convert(guiWiki.mc.fontRendererObj));
            return component;
        }

        @Override
        public String getID() {
            return "vSpacer";
        }
    }

    //endregion
}
