package com.brandon3055.projectintelligence.client.gui.moddata.displaycontent;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTextField;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.*;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.moddata.guidoctree.TreeBranchRoot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 8/09/2016.
 */
public class DCHeading extends DisplayComponentBase {

    public int headingSize = 0;
    public String displayString = "";

    public static final String SIZE_ATTRIB = "size";

    public DCHeading(GuiProjectIntelligence modularGui, String componentType, TreeBranchRoot branch) {
        super(modularGui, componentType, branch);
        setYSize(12);
    }

    //region List

    @Override
    public int getEntryHeight() {
        return super.getEntryHeight();
    }

    @Override
    public DCHeading setXSize(int xSize) {
        super.setXSize(xSize);

        if (xSize < 10) {
            return this;
        }

        float scaleFactor = 1F + (headingSize / 2F);
        int split = fontRenderer.listFormattedStringToWidth(displayString, (int) (xSize / scaleFactor)).size();
        setYSize((int) (fontRenderer.FONT_HEIGHT * scaleFactor * split));
        return this;
    }

    //endregion

    //region Render

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        float scaleFactor = 1F + (headingSize / 2F);
        List<String> list = fontRenderer.listFormattedStringToWidth(displayString, (int) (xSize() / scaleFactor));

        int xPos = xPos();
        int yPos = yPos();
        int xSize = xSize();

        for (String string : list) {

            float x = 0;
            float y = yPos + (fontRenderer.FONT_HEIGHT * scaleFactor * list.indexOf(string));
            float scaledWidth = fontRenderer.getStringWidth(string) * scaleFactor;

            switch (alignment) {
                case LEFT:
                    x = xPos + (2 * scaleFactor);
                    break;
                case CENTER:
                    x = xPos + (xSize / 2F) - (scaledWidth / 2F);
                    break;
                case RIGHT:
                    x = xPos + (xSize - scaledWidth) - (2 * scaleFactor);
                    break;
            }

            if (headingSize > 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(scaleFactor, scaleFactor, 1);
                GlStateManager.translate(-x, -y, 0);
            }

            drawString(fontRenderer, string, x, y, getColour(), shadow && getColour() / 3 > 50);

            if (headingSize > 0) {
                GlStateManager.popMatrix();
            }

        }
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);

    }

    //endregion

    //region Edit

    @Override
    public LinkedList<MGuiElementBase> getEditControls() {
        LinkedList<MGuiElementBase> list = super.getEditControls();

        list.add(new MGuiButtonSolid("TOGGLE_ALIGN", 0, 0, 26, 12, "Align") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Text Alignment"));
        GuiTextField textField = new GuiTextField(0, 0, 100, 12).setListener(this).setMaxStringLength(2048).setText(displayString);
        textField.setId("TEXT");
        textField.addChild(new MGuiHoverText(new String[]{"Modify Heading Text", TextFormatting.GREEN + "Will Auto-Save 3 seconds after you stop typing."}, textField));
        list.add(textField);

        list.add(new GuiLabel(0, 0, 37, 12, "Colour:").setAlignment(GuiAlign.CENTER));
        GuiTextField colourField = new GuiTextField(0, 0, 45, 12).setListener(this).setMaxStringLength(6).setText("FFFFFF");
        colourField.addChild(new MGuiHoverText(new String[]{"Set the base colour. If left default this will be the text colour for the selected style", "If you change this the only way to to go back is to remove the colour attribute from the entry in the XML file."}, colourField));
        colourField.setId("COLOUR");
        colourField.setText(Integer.toHexString(getColour()));
        colourField.setValidator(input -> {
            try {
                Utils.parseHex(input, false);
            }
            catch (Exception e) {
                return false;
            }
            return true;
        });
        list.add(colourField);

        list.add(new MGuiButtonSolid("CYCLE_SIZE", 0, 0, 20, 12, "H:") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText(new String[]{"Cycle Trough Heading Sizes", "Hold Shift to reverse"}).setText("H:" + headingSize));

        list.add(new MGuiButtonSolid("SHADOW", 0, 0, 10, 12, "S") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Shadow"));

        list.add(new MGuiButtonSolid("OBFUSCATED", 0, 0, 10, 12, TextFormatting.OBFUSCATED + "O") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Obfuscated").addToGroup("STYLE"));
        list.add(new MGuiButtonSolid("BOLD", 0, 0, 10, 12, TextFormatting.BOLD + "B") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Bold").addToGroup("STYLE"));
        list.add(new MGuiButtonSolid("STRIKETHROUGH", 0, 0, 10, 12, TextFormatting.STRIKETHROUGH + "S") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Strike-through").addToGroup("STYLE"));
        list.add(new MGuiButtonSolid("UNDERLINE", 0, 0, 10, 12, TextFormatting.UNDERLINE + "U") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Underline").addToGroup("STYLE"));
        list.add(new MGuiButtonSolid("ITALIC", 0, 0, 10, 12, TextFormatting.ITALIC + "I") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Italic").addToGroup("STYLE"));

        return list;
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventElement) {
        super.onMGuiEvent(event, eventElement);

        if (eventElement.getId().equals("TEXT") && event.asTextField().textChanged() && eventElement instanceof GuiTextField) {
            if (StringUtils.isNullOrEmpty(((GuiTextField) eventElement).getText())) {
                return;
            }

            element.setTextContent(((GuiTextField) eventElement).getText());
            displayString = ((GuiTextField) eventElement).getText();
            requiresSave = true;
            saveTimer = 60;
//            int pos = ((MGuiTextField) eventElement).getCursorPosition();
//            save();
//
//            for (MGuiElementBase element : branch.guiWiki.contentWindow.editControls) {
//                if (element instanceof MGuiTextField) {
//                    ((MGuiTextField) element).setFocused(true);
//                    ((MGuiTextField) element).setCursorPosition(pos);
//                    break;
//                }
//            }
        }
        else if (event.isButton() && eventElement instanceof MGuiButtonSolid && ((MGuiButtonSolid) eventElement).buttonName.equals("CYCLE_SIZE")) {
            headingSize += GuiScreen.isShiftKeyDown() ? -1 : 1;

            if (headingSize > 10) {
                headingSize = 0;
            }
            else if (headingSize < 0) {
                headingSize = 0;
            }

            element.setAttribute(SIZE_ATTRIB, String.valueOf(headingSize));
            save();
        }
        else if (event.isButton() && eventElement instanceof MGuiButtonSolid && ((MGuiButtonSolid) eventElement).buttonName.equals("SHADOW")) {
            shadow = !shadow;

            element.setAttribute(ATTRIB_SHADOW, String.valueOf(shadow));
            save();
        }
        else if (eventElement.getId().equals("COLOUR") && eventElement instanceof GuiTextField && event.asTextField().textChanged()) {
            try {
                setColour(Utils.parseHex(((GuiTextField) eventElement).getText()));
                element.setAttribute(ATTRIB_COLOUR, Integer.toHexString(getColour()));
                requiresSave = true;
                saveTimer = 60;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (eventElement instanceof GuiButton && eventElement.isInGroup("STYLE")) {
            TextFormatting format = TextFormatting.valueOf(((GuiButton) eventElement).buttonName);

            if (displayString.contains(format.toString())) {
                displayString = displayString.replace(format.toString(), "");
            }
            else {
                displayString = format + displayString;
            }

//            for (MGuiElementBase element : branch.guiWiki.contentWindow.editControls) {
//                if (element instanceof GuiTextField) {
//                    ((GuiTextField) element).setText(displayString);
//                    break;
//                }
//            }
            element.setTextContent(displayString);

            save();
        }
    }

    @Override
    public void onCreated() {
        element.setAttribute(SIZE_ATTRIB, "0");
        element.setTextContent("Click To Edit");
//        element.setAttribute(ATTRIB_COLOUR, "FFFFFF");
        element.setAttribute(ATTRIB_SHADOW, "true");
        element.setAttribute(ATTRIB_ALIGNMENT, "CENTER");
    }

    //endregion

    //region XML & Factory

    @Override
    public void loadFromXML(Element element) {
        super.loadFromXML(element);
        displayString = element.getTextContent();
        if (element.hasAttribute(SIZE_ATTRIB)) {
            headingSize = Integer.parseInt(element.getAttribute(SIZE_ATTRIB));
        }
    }

    public static class Factory implements IDisplayComponentFactory {
        @Override
        public DisplayComponentBase createNewInstance(GuiProjectIntelligence guiWiki, TreeBranchRoot branch, int screenWidth, int screenHeight) {
            DisplayComponentBase component = new DCHeading(guiWiki, getID(), branch);
            component.applyGeneralElementData(guiWiki, guiWiki.mc, screenWidth, screenHeight, BCFontRenderer.convert(guiWiki.mc.fontRendererObj));
            return component;
        }

        @Override
        public String getID() {
            return "heading";
        }
    }

    //endregion
}
