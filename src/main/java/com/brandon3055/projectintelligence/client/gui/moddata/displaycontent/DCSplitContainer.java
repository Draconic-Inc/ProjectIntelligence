package com.brandon3055.projectintelligence.client.gui.moddata.displaycontent;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTextField;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.Vertical;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiButtonSolid;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiHoverText;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiList;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.moddata.WikiDocManager;
import com.brandon3055.projectintelligence.client.gui.moddata.guidoctree.TreeBranchRoot;
import com.brandon3055.projectintelligence.utils.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.LinkedList;

import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.CENTER;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.LEFT;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.Vertical.*;

/**
 * Created by brandon3055 on 8/09/2016.
 */
public class DCSplitContainer extends DisplayComponentBase {
    public static final String ATTRIB_VALIGNMENT = "vAlignment";
    public static final String ATTRIB_WIDTH = "width";
    public int splitMinWidth = 100;
    public DisplayComponentBase componentLeft;
    public DisplayComponentBase componentRight;
    public Vertical vAlignment = TOP;
    public MGuiList dummyListLeft;
    public MGuiList dummyListRight;

    public DCSplitContainer(GuiProjectIntelligence modularGui, String componentType, TreeBranchRoot branch) {
        super(modularGui, componentType, branch);
        setYSize(8);
//        dummyListLeft = new MGuiList(modularGui);
//        dummyListRight = new MGuiList(modularGui);
    }

    //region List

    @Override
    public DCSplitContainer setXSize(int xSize) {
        super.setXSize(xSize);
        int xPos = xPos();
        int yPos = yPos();
        int ySize = ySize();
        int xSplit = alignment == LEFT ? xPos + splitMinWidth : alignment == CENTER ? xPos + (xSize / 2) : xPos + xSize - splitMinWidth;

        if (xSize - xPos < 16) {
            xSplit = xPos + 16;
        }
        else if (xPos + xSize - xSplit < 16) {
            xSplit = xPos + xSize - 16;
        }

        if (componentLeft != null) {
            componentLeft.setXPos(xPos);
            //            componentLeft.xPos = xPos;
            componentLeft.setXSize(xSplit - xPos);
            setYSize(componentLeft.ySize());
        }
        if (componentRight != null) {
            componentRight.setXPos(xSplit);
//            componentRight.xPos = x;
//            componentRight.translate(xSplit - componentRight.xPos, 0);
            componentRight.setXSize(xPos + xSize - xSplit);
            if (componentRight.ySize() > ySize) {
                setYSize(componentRight.ySize());
            }
        }

        DisplayComponentBase smaller = null;
        if (componentLeft != null && componentLeft.ySize() < ySize) {
            smaller = componentLeft;
        }
        else if (componentRight != null && componentRight.ySize() < ySize) {
            smaller = componentRight;
        }

        if (smaller != null) {
            int newY = 0;
            switch (vAlignment) {
                case TOP:
                    newY = yPos;
                    break;
                case MIDDLE:
                    newY = yPos + (ySize / 2) - (smaller.ySize() / 2);
                    break;
                case BOTTOM:
                    newY = yPos + ySize - smaller.ySize();
                    break;
            }
            smaller.translate(0, newY - smaller.yPos());
        }
        return this;
    }

    @Override
    public void setList(MGuiList list) {
        super.setList(list);
        if (componentLeft != null) {
            componentLeft.setList(list);
        }
        if (componentRight != null) {
            componentRight.setList(list);
        }
    }

    //endregion

    //region Render

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        if (PIConfig.editMode() && PIConfig.drawEditInfo) {
            zOffset += 10;
//            if (branch.guiWiki.contentWindow.editingComponent == this) {
//                drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 0.8, 0, 0xFF0000FF);
//            }
            int x = alignment == LEFT ? xPos() + splitMinWidth : alignment == CENTER ? xPos() + (xSize() / 2) : xPos() + xSize() - splitMinWidth;
            drawColouredRect(x, yPos(), 0.8, ySize(), 0xFF0000FF);
            zOffset -= 10;
        }
    }

    //endregion

    //region Interact

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    //endregion

    //region Edit

    @Override
    public LinkedList<MGuiElementBase> getEditControls() {
        LinkedList<MGuiElementBase> list = super.getEditControls();

        list.add(new MGuiButtonSolid("TOGGLE_ALIGN", 0, 0, fontRenderer.getStringWidth("Horizontal Align") + 4, 12, "Horizontal Align") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Horizontal divider Alignment between Fixed Left Column Size, 50/50 and Fixed Right Column Size", "Current: " + alignment));

        list.add(new MGuiButtonSolid("TOGGLE_V_ALIGN", 0, 0, fontRenderer.getStringWidth("Vertical Align") + 4, 12, "Vertical Align") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Vertical alignment of smallest component between Top, Middle and Bottom.", "Current: " + vAlignment));


        if (alignment != CENTER) {
            list.add(new GuiLabel(0, 0, 48, 12, "Min Width:").setAlignment(GuiAlign.CENTER));
            GuiTextField colourField = new GuiTextField(0, 0, 45, 12).setListener(this).setMaxStringLength(6);
            colourField.setId("WIDTH");
            String s = alignment == LEFT ? "Set width of left column" : "Set width of right column";
            colourField.addChild(new MGuiHoverText(new String[]{s}, colourField));
            colourField.setText(String.valueOf(splitMinWidth));
            colourField.setValidator(input -> {
                try {
                    Integer.parseInt(input);
                }
                catch (Exception e) {
                    return false;
                }
                return true;
            });
            list.add(colourField);
        }

        list.add(new MGuiButtonSolid("SET_LEFT", 0, 0, fontRenderer.getStringWidth("Set Left") + 4, 12, "Set Left") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Set the left sub display component."));
        list.add(new MGuiButtonSolid("SET_RIGHT", 0, 0, fontRenderer.getStringWidth("Set Right") + 4, 12, "Set Right") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Set the right sub display component."));

        return list;
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventElement) {
        super.onMGuiEvent(event, eventElement);
        if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.equals("TOGGLE_V_ALIGN")) {
            vAlignment = vAlignment == TOP ? MIDDLE : vAlignment == MIDDLE ? BOTTOM : TOP;
            element.setAttribute(ATTRIB_VALIGNMENT, vAlignment.name());
            save();
        }
        else if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.startsWith("SET_")) {
//            GuiPartContentWindowOld cw = branch.guiWiki.contentWindow;
//            if (cw.addSelector != null) {
//                modularGui.getManager().remove(cw.addSelector);
//                cw.addSelector = null;
//                return;
//            }
//            cw.addSelector = new GuiSelectDialog(cw.xPos(), cw.yPos(), 10, 50, this);
//            cw.addSelector.setId(((GuiButton) eventElement).buttonName);
//
//            List<MGuiElementBase> list = new ArrayList<MGuiElementBase>();
//            int width = 10;
//
//            for (String option : DisplayComponentRegistry.REGISTRY.keySet()) {
//                if (option.equals("splitContainer")) {
//                    continue;
//                }
//                int optionWidth = fontRenderer.getStringWidth(option) + 4;
//                if (optionWidth + 10 > width) {
//                    width = optionWidth + 10;
//                }
//                list.add(new GuiLabel(0, 0, optionWidth, 12, option));
//            }
//
//            list.add(new GuiLabel(0, 0, fontRenderer.getStringWidth("CLEAR") + 4, 12, "CLEAR"));
//
//            cw.addSelector.setPos(eventElement.xPos(), eventElement.yPos() + 12);
//            cw.addSelector.setYSize(Math.min((list.size() * 12) + 4, cw.ySize() - 12));
//            cw.addSelector.addChildElements();
//
////            cw.addSelector.setOptions(list);
//            cw.addSelector.setListener(this);
//            modularGui.getManager().add(cw.addSelector, 2);
        }
        else if (eventElement.getId().equals("WIDTH") && event.isTextFiled() && event.asTextField().textChanged() && eventElement instanceof GuiTextField) {
            if (StringUtils.isNullOrEmpty(((GuiTextField) eventElement).getText())) {
                return;
            }

            int newWidth = 1;
            try {
                newWidth = Integer.parseInt(((GuiTextField) eventElement).getText());
            }
            catch (Exception e) {
            }

            if (newWidth < 10) {
                newWidth = 10;
            }

            element.setAttribute(ATTRIB_WIDTH, String.valueOf(newWidth));
            int pos = ((GuiTextField) eventElement).getCursorPosition();
            save();

//            for (MGuiElementBase element : branch.guiWiki.contentWindow.editControls) {
//                if (element instanceof GuiTextField && element.getId().equals("WIDTH")) {
//                    ((GuiTextField) element).setFocused(true);
//                    ((GuiTextField) element).setCursorPosition(pos);
//                    break;
//                }
//            }
        }
        else if (event.isSelector() && event.asSelect().getSelectedItem() instanceof GuiLabel) {
//            GuiPartContentWindowOld cw = branch.guiWiki.contentWindow;
            String side;

//            if (cw.addSelector.getId().endsWith("LEFT")) {
//                side = "LEFT";
//                if (componentLeft != null) {
//                    element.removeChild(componentLeft.element);
//                }
//            }
//            else if (cw.addSelector.getId().endsWith("RIGHT")) {
//                side = "RIGHT";
//                if (componentRight != null) {
//                    element.removeChild(componentRight.element);
//                }
//            }
//            else {
//                return;
//            }
//
//            String type = ((GuiLabel) event.asSelect().getSelectedItem()).getLabelText();
//            if (!type.equals("CLEAR")) {
//                Element newElement = element.getOwnerDocument().createElement(WikiDocManager.ELEMENT_CONTENT);
//
//                newElement.setAttribute("splitSide", side);
//                newElement.setAttribute("index", "0");
//                newElement.setAttribute(WikiDocManager.ATTRIB_TYPE, type);
//
//                element.appendChild(newElement);
//
//
//                DisplayComponentBase component = DisplayComponentRegistry.createComponent(branch.guiWiki, type, newElement, branch);
//                if (component == null) {
//                    LogHelper.error("Failed to create display component... This should not happen.");
//                    return;
//                }
//                component.container = this;
//
//                component.onCreated();
//            }
//
//            modularGui.getManager().remove(cw.addSelector);
//            cw.addSelector = null;
//
//            try {
//                branch.save();
//                WikiDocManager.reload(false, true, true);
//                branch.guiWiki.wikiDataTree.reOpenLast();
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void onCreated() {
        element.setAttribute(ATTRIB_VALIGNMENT, TOP.name());
    }

    //endregion

    //region XML & Factory

    @Override
    public void loadFromXML(Element element) {
        super.loadFromXML(element);
        if (element.hasAttribute(ATTRIB_VALIGNMENT)) {
            String align = element.getAttribute(ATTRIB_VALIGNMENT);
            vAlignment = Vertical.valueOf(align.toUpperCase());
        }
        if (element.hasAttribute(ATTRIB_WIDTH)) {
            try {
                splitMinWidth = Integer.parseInt(element.getAttribute(ATTRIB_WIDTH));
            }
            catch (Exception e) {
            }
        }

        if (splitMinWidth < 16) {
            splitMinWidth = 16;
        }

        NodeList nodeList = element.getElementsByTagName(WikiDocManager.ELEMENT_CONTENT);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getParentNode() != element) {
                continue;
            }

            if (!(node instanceof Element)) {
                LogHelper.dev("Node Is Not An Element: " + node);
                continue;
            }

            Element content = (Element) node;
            String type = content.getAttribute(WikiDocManager.ATTRIB_TYPE);

            DisplayComponentBase displayComponent = DisplayComponentRegistry.createComponent(branch.guiWiki, type, content, branch);

            if (displayComponent == null) {
                continue;
            }
            displayComponent.container = this;
            displayComponent.setList(list);

            if (content.hasAttribute("splitSide") && content.getAttribute("splitSide").equals("LEFT")) {
                componentLeft = displayComponent;
                addChild(componentLeft);
            }
            else if (content.hasAttribute("splitSide") && content.getAttribute("splitSide").equals("RIGHT")) {
                componentRight = displayComponent;
                addChild(componentRight);
            }
            else {
                LogHelper.error("Found un-designated content element for SplitContainer in " + branch.branchID);
            }
        }
    }

    public static class Factory implements IDisplayComponentFactory {
        @Override
        public DisplayComponentBase createNewInstance(GuiProjectIntelligence guiWiki, TreeBranchRoot branch, int screenWidth, int screenHeight) {
            DisplayComponentBase component = new DCSplitContainer(guiWiki, getID(), branch);
            component.applyGeneralElementData(guiWiki, guiWiki.mc, screenWidth, screenHeight, BCFontRenderer.convert(guiWiki.mc.fontRendererObj));
            return component;
        }

        @Override
        public String getID() {
            return "splitContainer";
        }
    }

    //endregion
}
