package com.brandon3055.projectintelligence.client.gui.moddata.displaycontent;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiSlotRender;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiStackIcon;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTextField;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.*;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.guielementsold.StackSelector;
import com.brandon3055.projectintelligence.client.gui.moddata.guidoctree.TreeBranchRoot;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 8/09/2016.
 */
public class DCStack extends DisplayComponentBase {

    public static final String ATTRIB_SCALE = "scale";
    public static final String ATTRIB_TIP = "tooltip";
    public static final String ATTRIB_SLOT = "renderSlot";

    public GuiStackIcon stackIcon;
    private MGuiElementBase iconBackground;
    public int scale = 100;
    public boolean toolTip = true;
    public boolean renderSlot = false;
    public String stackString;
    private StackSelector selector;

    public DCStack(GuiProjectIntelligence modularGui, String componentType, TreeBranchRoot branch) {
        super(modularGui, componentType, branch);
        setYSize(20);
        stackIcon = new GuiStackIcon(0, 0, 18, 18, new StackReference("null"));
        addChild(stackIcon);
        iconBackground = new GuiSlotRender();
        stackIcon.setBackground(iconBackground);
    }

    //region List

    @Override
    public DCStack setXSize(int xSize) {
        super.setXSize(xSize);
        int size = Math.min(xSize - 4, (int) ((scale / 100D) * 18D));
        stackIcon.setSize(size, size);
        iconBackground.setSize(size, size);
        iconBackground.setEnabled(renderSlot);
        iconBackground.setYPos(stackIcon.yPos());

        int xOffset = 0;
        switch (alignment) {
            case LEFT:
                xOffset = 2;
                break;
            case CENTER:
                xOffset = (xSize / 2) - (size / 2);
                break;
            case RIGHT:
                xOffset = xSize - size - 2;
                break;
        }

        stackIcon.setXPos(xPos() + xOffset);
        setYPos(stackIcon.ySize());

        iconBackground.setPos(stackIcon);
        return this;
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
        }.setListener(this).setHoverText("Toggle Horizontal Alignment"));

        list.add(new MGuiButtonSolid("SELECT_STACK", 0, 0, 56, 12, "Pick Stack") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Select a stack from your inventory"));

        String s = "Turn ToolTip: " + (toolTip ? "Off" : "On");
        list.add(new MGuiButtonSolid("TOGGLE_TOOLTIP", 0, 0, fontRenderer.getStringWidth(s) + 4, 12, s) {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle item tool tip on or off"));

        s = "Turn Slot: " + (toolTip ? "Off" : "On");
        list.add(new MGuiButtonSolid("TOGGLE_SLOT", 0, 0, fontRenderer.getStringWidth(s) + 4, 12, s) {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Slot Renderer on or off"));

        list.add(new GuiLabel(0, 0, 30, 12, "Scale:").setAlignment(GuiAlign.CENTER));
        GuiTextField scaleField = new GuiTextField(0, 0, 36, 12).setListener(this).setMaxStringLength(2048).setText(String.valueOf(scale));
        scaleField.addChild(new MGuiHoverText(new String[]{"Set the stack scale (100 = normal stack size)", TextFormatting.GOLD + "The size of the image will be limited by both this and the width of the GUI.", TextFormatting.GOLD + "Whichever value is smaller will take priority.", TextFormatting.GREEN + "Will save as you type."}, scaleField));
        scaleField.setId("SCALE");
        scaleField.setValidator(input -> {
            try {
                Integer.parseInt(input);
            }
            catch (Exception e) {
            }
            return true;
        });
        list.add(scaleField);

        return list;
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventElement) {
        super.onMGuiEvent(event, eventElement);


        if (eventElement.getId().equals("SCALE") && event.isTextFiled() && event.asTextField().textChanged() && eventElement instanceof GuiTextField) {
            if (StringUtils.isNullOrEmpty(((GuiTextField) eventElement).getText())) {
                return;
            }

            int newScale = 1;
            try {
                newScale = Integer.parseInt(((GuiTextField) eventElement).getText());
            }
            catch (Exception ignored) {
            }

            if (newScale < 30) {
                newScale = 30;
            }

            element.setAttribute(ATTRIB_SCALE, String.valueOf(newScale));
            int pos = ((GuiTextField) eventElement).getCursorPosition();
            save();

//            for (MGuiElementBase element : branch.guiWiki.contentWindow.editControls) {
//                if (element instanceof GuiTextField && element.getId().equals("SCALE")) {
//                    ((GuiTextField) element).setFocused(true);
//                    ((GuiTextField) element).setCursorPosition(pos);
//                    break;
//                }
//            }
        }
        else if (eventElement instanceof MGuiButtonSolid && ((MGuiButtonSolid) eventElement).buttonName.equals("TOGGLE_TOOLTIP")) {
            element.setAttribute(ATTRIB_TIP, String.valueOf(!toolTip));
            save();
        }
        else if (eventElement instanceof MGuiButtonSolid && ((MGuiButtonSolid) eventElement).buttonName.equals("TOGGLE_SLOT")) {
            element.setAttribute(ATTRIB_SLOT, String.valueOf(!renderSlot));
            save();
        }
        else if (eventElement instanceof MGuiButtonSolid && ((MGuiButtonSolid) eventElement).buttonName.equals("SELECT_STACK")) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            selector = new StackSelector(list.xPos() + list.leftPadding, list.yPos() + list.topPadding, list.xSize() - list.leftPadding - list.rightPadding, list.ySize() - list.topPadding - list.bottomPadding);
            selector.setListener(this);

            List<ItemStack> stacks = new LinkedList<ItemStack>();
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            selector.setStacks(stacks);
            selector.addChild(new MGuiButtonSolid("CANCEL_PICK", selector.xPos() + selector.xSize() - 42, selector.yPos() + selector.ySize() - 22, 40, 20, "Cancel").setListener(this).setId("CANCEL_PICK"));
            selector.addChildElements();
            modularGui.getManager().add(selector, 2);
        }
        else if (eventElement.getId().equals("CANCEL_PICK") && selector != null) {
            modularGui.getManager().remove(selector);
        }
        else if (event.isSelector()) {
            boolean shouldSave = false;
            if (eventElement instanceof GuiStackIcon) {
                StackReference reference = new StackReference(((GuiStackIcon) eventElement).getStack());
                stackIcon.setStack(reference);
                element.setTextContent(reference.toString());
                shouldSave = true;
            }

            modularGui.getManager().remove(selector);
            if (shouldSave) {
                save();
            }
        }

    }

    @Override
    public void onCreated() {
        element.setAttribute(ATTRIB_SCALE, "100");
        element.setAttribute(ATTRIB_TIP, "true");
        element.setAttribute(ATTRIB_SLOT, "true");
    }

    //endregion

    //region XML & Factory

    @Override
    public void loadFromXML(Element element) {
        super.loadFromXML(element);
        toolTip = !element.hasAttribute(ATTRIB_TIP) || Boolean.parseBoolean(element.getAttribute(ATTRIB_TIP));
        renderSlot = !element.hasAttribute(ATTRIB_SLOT) || Boolean.parseBoolean(element.getAttribute(ATTRIB_SLOT));
        stackString = element.getTextContent();
        StackReference ref = StackReference.fromString(stackString);
        if (ref == null) {
            ref = new StackReference("null");
        }
        stackIcon.setStack(ref);
        stackIcon.setToolTip(toolTip);
        iconBackground.setEnabled(renderSlot);

        try {
            scale = Integer.parseInt(element.getAttribute(ATTRIB_SCALE));
        }
        catch (Exception e) {
        }

        if (scale < 30) {
            scale = 30;
        }
    }

    public static class Factory implements IDisplayComponentFactory {
        @Override
        public DisplayComponentBase createNewInstance(GuiProjectIntelligence guiWiki, TreeBranchRoot branch, int screenWidth, int screenHeight) {
            DisplayComponentBase component = new DCStack(guiWiki, getID(), branch);
            component.applyGeneralElementData(guiWiki, guiWiki.mc, screenWidth, screenHeight, BCFontRenderer.convert(guiWiki.mc.fontRendererObj));
            return component;
        }

        @Override
        public String getID() {
            return "stack";
        }
    }

    //endregion
}
