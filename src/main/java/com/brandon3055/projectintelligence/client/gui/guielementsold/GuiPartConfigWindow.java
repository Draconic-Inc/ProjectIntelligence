package com.brandon3055.projectintelligence.client.gui.guielementsold;

import codechicken.lib.asm.ObfMapping;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.*;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiButtonSolid;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.StylePreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Created by brandon3055 on 18/09/2016.
 */
public class GuiPartConfigWindow extends MGuiElementBase<GuiPartConfigWindow> implements IGuiEventListener {

    public static volatile boolean requiresSave = false;
//    public GuiSelectDialog selector;

    public GuiPartConfigWindow(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    //region Init

    @Override
    public void addChildElements() {
        int xPos = xPos();
        int yPos = yPos();
        int xSize = xSize();

        int size = (xSize - 34) / 3;

        addChild(new MGuiButtonSolid("COLOUR_NAV", xPos + 15, yPos + 30, size, 12, I18n.format("modwiki.style.navWindow")) {
            @Override
            public int getFillColour(boolean hovering, boolean disabled) {
                return PIConfig.NAV_WINDOW;
            }

            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return PIConfig.NAV_WINDOW;
            }
        }.setListener(this));
        addChild(new MGuiButtonSolid("COLOUR_MAIN", xPos + 17 + (size), yPos + 30, size, 12, I18n.format("modwiki.style.mainWindow")) {
            @Override
            public int getFillColour(boolean hovering, boolean disabled) {
                return PIConfig.CONTENT_WINDOW;
            }

            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return PIConfig.CONTENT_WINDOW;
            }
        }.setListener(this));
        addChild(new MGuiButtonSolid("COLOUR_MENU", xPos + 19 + (size * 2), yPos + 30, size, 12, I18n.format("modwiki.style.menu")) {
            @Override
            public int getFillColour(boolean hovering, boolean disabled) {
                return PIConfig.MENU_BAR;
            }

            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return PIConfig.MENU_BAR;
            }
        }.setListener(this));

        addChild(new MGuiButtonSolid("COLOUR_NAV_TEXT", xPos + 15, yPos + 44, size, 12, I18n.format("modwiki.style.navText")) {
            @Override
            public int getFillColour(boolean hovering, boolean disabled) {
                return mixColours(PIConfig.NAV_TEXT, 0xFF000000);
            }

            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return mixColours(PIConfig.NAV_TEXT, 0xFF000000);
            }
        }.setListener(this));
        addChild(new MGuiButtonSolid("COLOUR_MISC_TEXT", xPos + 17 + (size), yPos + 44, size, 12, I18n.format("modwiki.style.text")) {
            @Override
            public int getFillColour(boolean hovering, boolean disabled) {
                return mixColours(PIConfig.TEXT_COLOUR, 0xFF000000);
            }

            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return mixColours(PIConfig.TEXT_COLOUR, 0xFF000000);
            }
        }.setListener(this));
        addChild(new MGuiButtonSolid("PRESETS", xPos + 19 + (size * 2), yPos + 44, size, 12, I18n.format("modwiki.style.presets")) {
            @Override
            public int getFillColour(boolean hovering, boolean disabled) {
                return 0xFFFFFFFF;
            }

            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FFFF : 0xFF000000;
            }
        }.setListener(this));

        if (!ObfMapping.obfuscated) {
            addChild(new MGuiButtonSolid("TOGGLE_EDIT", xPos + 15, yPos + 77, size, 12, "Edit Mode") {
                @Override
                public int getFillColour(boolean hovering, boolean disabled) {
                    return 0xFFFFFFFF;
                }

                @Override
                public int getBorderColour(boolean hovering, boolean disabled) {
                    return hovering ? 0xFF00FFFF : 0xFF000000;
                }
            }.setListener(this));
            addChild(new GuiLabel(xPos + 17 + size, yPos + 77, size, 12, "Edit Directory"));
            addChild(new GuiTextField(xPos + 15, yPos + 91, size * 3 + 4, 12).setMaxStringLength(1024).setText(PIConfig.editingRepoLoc).setListener(this));
        }

        super.addChildElements();
    }

    //endregion

    //region Render

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        int xPos = xPos();
        int yPos = yPos();
        int xSize = xSize();
        int ySize = ySize();

        drawBorderedRect(xPos, yPos, xSize, ySize, 1, 0xff707070, 0xff000000);
        drawCenteredString(fontRenderer, I18n.format("generic.options.txt"), xPos + (xSize / 2), yPos + 4, 0xFFFFFF, true);
        drawColouredRect(xPos + 15, yPos + 13, xSize - 30, 0.5, 0xFFFFFFFF);
        drawColouredRect(xPos + 20, yPos + 13.5, xSize - 40, 0.5, 0xFF000000);

        drawCenteredString(fontRenderer, I18n.format("modwiki.label.style"), xPos + (xSize / 2), yPos + 18, 0x00FFFF, true);
        drawColouredRect(xPos + 15, yPos + 60, xSize - 30, 0.5, 0xFF00FFFF);
        drawColouredRect(xPos + 20, yPos + 60.5, xSize - 40, 0.5, 0xFF000000);

        drawCenteredString(fontRenderer, I18n.format("modwiki.label.edit"), xPos + (xSize / 2), yPos + 65, 0xFF0000, false);
        drawColouredRect(xPos + 15, yPos + 107, xSize - 30, 0.5, 0xFFFF0000);
        drawColouredRect(xPos + 20, yPos + 107.5, xSize - 40, 0.5, 0xFF000000);

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    //endregion

    //region Interact

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventElement) {
        int xPos = xPos();
        int yPos = yPos();
        int xSize = xSize();
        int ySize = ySize();

        GuiPickColourDialog picker = new GuiPickColourDialog(this).setDragBar(10);

        if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.equals("COLOUR_NAV")) {
            picker.setColour(PIConfig.NAV_WINDOW);
            picker.setColourChangeListener(integer -> PIConfig.NAV_WINDOW = integer);
            picker.showCenter();
        }
        else if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.equals("COLOUR_MAIN")) {
            picker.setColour(PIConfig.CONTENT_WINDOW);
            picker.setColourChangeListener(integer -> PIConfig.CONTENT_WINDOW = integer);
            picker.showCenter();
        }
        else if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.equals("COLOUR_MENU")) {
            picker.setColour(PIConfig.MENU_BAR);
            picker.setColourChangeListener(integer -> PIConfig.MENU_BAR = integer);
            picker.showCenter();
        }
        else if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.equals("COLOUR_NAV_TEXT")) {
            picker.setIncludeAlpha(false);
            picker.setColour(PIConfig.NAV_TEXT);
            picker.setColourChangeListener(integer -> PIConfig.NAV_TEXT = integer);
            picker.showCenter();
        }
        else if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.equals("COLOUR_MISC_TEXT")) {
            picker.setIncludeAlpha(false);
            picker.setColour(PIConfig.TEXT_COLOUR);
            picker.setColourChangeListener(integer -> PIConfig.TEXT_COLOUR = integer);
            picker.showCenter();
        }
        else if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.equals("TOGGLE_EDIT")) {
//            PIConfig.editMode = !PIConfig.editMode;
            PIConfig.save();
            Minecraft.getMinecraft().displayGuiScreen(new GuiProjectIntelligence());
        }
        else if (event.isColour()) {
            PIConfig.save();
        }
        else if (eventElement instanceof GuiButton && ((GuiButton) eventElement).buttonName.equals("PRESETS")) {
            GuiSelectDialog<StylePreset> selector = new GuiSelectDialog(eventElement.xPos(), eventElement.yPos() + eventElement.ySize(), this);
            selector.setSize(eventElement.xSize(), 140);
            for (StylePreset preset : StylePreset.PRESETS) {
                GuiLabel label = new GuiLabel(preset.getName());
                label.addChild(new GuiBorderedRect().bindSize(label, false).setBorderColour(0xFF000000).setFillColours(0xFF707070, 0xFF207020));
                label.setYSize(12);
                selector.addItem(preset, label);
            }

            selector.setSelectionListener(StylePreset::apply);

//            List<MGuiElementBase> list = new LinkedList<>();
//            int y = 0;
//            for (StylePreset preset : StylePreset.PRESETS) {
//                GuiLabel label = new GuiLabel(0, 0, fontRenderer.getStringWidth(preset.getName()) + 4, 12, preset.getName());
//                label.linkedObject = preset;
//                list.add(label);
//                y += label.ySize();
//            }
//
//            selector.setYPos(Math.min(y + 3, ySize - eventElement.yPos() - eventElement.ySize()));
//            selector.addChildElements();
//            selector.addItem(list);
////            selector.setOptions(list);
//            selector.setListener(this);
//            modularGui.getManager().add(selector, displayZLevel + 1);

            selector.setNoScrollBar();
            selector.show();
        }
        else if (event.isSelector() && eventElement.linkedObject instanceof StylePreset) {
//            ((StylePreset) eventElement.linkedObject).apply();
//            if (selector != null) {
//                modularGui.getManager().remove(selector);
//                selector = null;
//            }
        }
        else if (event.isTextFiled() && event.asTextField().textChanged() && eventElement instanceof GuiTextField) {
            PIConfig.editingRepoLoc = ((GuiTextField) eventElement).getText();
            PIConfig.save();
        }

    }

    //endregion

    @Override
    public boolean onUpdate() {
        if (requiresSave) {
            requiresSave = false;
            PIConfig.save();
        }

        return super.onUpdate();
    }
}
