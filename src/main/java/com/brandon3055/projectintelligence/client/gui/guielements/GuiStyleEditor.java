package com.brandon3055.projectintelligence.client.gui.guielements;

import codechicken.lib.colour.Colour;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.*;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.StyleHandler.BooleanProperty;
import com.brandon3055.projectintelligence.client.StyleHandler.ColourProperty;
import com.brandon3055.projectintelligence.client.StyleHandler.IntegerProperty;
import com.brandon3055.projectintelligence.client.StyleHandler.StyleProperty;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.brandon3055.projectintelligence.client.StyleHandler.StyleType.*;

/**
 * Created by brandon3055 on 12/08/2017.
 */
public class GuiStyleEditor extends GuiPopUpDialogBase<GuiStyleEditor> {

    private GuiScrollElement editTree;
    private GuiScrollElement presetList;
    private String highlight = "";

    public GuiStyleEditor(GuiElement parent) {
        super(parent);
        setSize(200, 250);
        setDragBar(12);
        setCloseOnOutsideClick(false);
    }

    @Override
    public void addChildElements() {
        childElements.clear();
        StyleHandler.reloadStyleProperties();
        StyleHandler.loadStyle();

        //Background / Heading/ Close button
        addChild(new StyledGuiRect("user_dialogs").setPosAndSize(this));
        addChild(new GuiLabel(TextFormatting.UNDERLINE + I18n.format("pi.style.edit_style_properties.txt"))//
                .setPos(this).setSize(xSize(), 10).translate(4, 3).setHoverableTextCol(hovering -> StyleHandler.getColour("user_dialogs." + TEXT_COLOUR.getName()).rgb())//
                .setShadow(false).setAlignment(GuiAlign.CENTER));

        GuiButton close = new StyledGuiButton("user_dialogs.button_style").setPos(this).translate(xSize() - 14, 3).setSize(11, 11);
        close.onPressed(this::close);
        close.setHoverText(I18n.format("pi.button.close"));
        close.addChild(new GuiTexture(64, 16, 5, 5, PITextures.PI_PARTS).setRelPos(3, 3));
        addChild(close);


        //Presets
        int selOffst = 20;
        addChild(new GuiLabel(I18n.format("pi.style.load_preset.txt")).setPos(this).setSize(xSize(), 10).translate(4, selOffst)//
                .setHoverableTextCol(hovering -> StyleHandler.getColour("user_dialogs." + TEXT_COLOUR.getName()).rgb())//
                .setShadow(false).setAlignment(GuiAlign.LEFT).addToGroup("PRESETS"));

        GuiBorderedRect presetBackground;
        addChild(presetBackground = new GuiBorderedRect().setPos(this).translate(4, selOffst += 10).setSize(xSize() - 8, ySize() - 48 - selOffst).addToGroup("PRESETS"));
        presetBackground.setFillColourL(hovering -> StyleHandler.getInt("user_dialogs.sub_elements." + COLOUR.getName()));
        presetBackground.setBorderColourL(hovering -> StyleHandler.getInt("user_dialogs.sub_elements." + BORDER.getName()));

        addChild(new GuiLabel(I18n.format("pi.style.save_overwrite_preset.txt")).setPos(xPos() + 4, presetBackground.maxYPos() + 3).setSize(xSize() - 10, 10)//
                .setHoverableTextCol(hovering -> StyleHandler.getColour("user_dialogs." + TEXT_COLOUR.getName()).rgb())//
                .setShadow(false).setAlignment(GuiAlign.LEFT).addToGroup("PRESETS"));

        GuiTextField saveName = new GuiTextField().setPos(xPos() + 4, presetBackground.maxYPos() + 13).setSize(xSize() - 50, 14).addToGroup("PRESETS");
        saveName.setValidator(FileHandler.FILE_NAME_VALIDATOR.or(s -> s == null || s.isEmpty()));

        GuiButton savePreset = new StyledGuiButton("user_dialogs.button_style").setText(I18n.format("pi.button.save")).setPos(saveName.maxXPos() + 1, saveName.yPos()).setSize(41, 14).addToGroup("PRESETS");
        savePreset.setHoverText(I18n.format("pi.style.save_preset.info"));
        savePreset.onPressed(() -> {
            if (saveName.getText().isEmpty()) {
                GuiPopupDialogs.createDialog(this, GuiPopupDialogs.DialogType.OK_OPTION, I18n.format("pi.style.save_no_name.txt"), "").showCenter(displayZLevel + 50);
                return;
            }
            if (StyleHandler.getCustomPresets().contains(saveName.getText())) {
                GuiPopupDialogs.createDialog(this, GuiPopupDialogs.DialogType.YES_NO_OPTION, I18n.format("pi.style.save_overwrite.txt"), "").setYesListener(() -> {
                    StyleHandler.savePreset(saveName.getText());
                    saveName.setTextQuietly("");
                    reloadElement();
                }).showCenter(displayZLevel + 50);
                return;
            }
            StyleHandler.savePreset(saveName.getText());
            saveName.setTextQuietly("");
            reloadElement();
        });
        addChild(savePreset);
        addChild(saveName);

        GuiButton openEditor = new StyledGuiButton("user_dialogs.button_style").setText(I18n.format("pi.button.open_style_editor")).setPos(xPos() + 4, maxYPos() - 18).setSize(xSize() - 8, 14);
        openEditor.addToGroup("PRESETS");
        openEditor.onPressed(() -> setChildGroupEnabled("EDITOR_TREE", true).setChildGroupEnabled("PRESETS", false));
        addChild(openEditor);

        presetList = new GuiScrollElement();
        presetList.addToGroup("PRESETS");
        presetList.setRelPos(presetBackground, 1, 1).setSize(presetBackground.xSize() - 2, presetBackground.ySize() - 2);
        presetList.setStandardScrollBehavior();
        presetList.setListMode(GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH);
        presetList.getVerticalScrollBar().setHidden(true);
        presetList.setListSpacing(1);
        addChild(presetList);


        //Property Editor Tree
        editTree = new GuiScrollElement();
        editTree.addToGroup("EDITOR_TREE");
        editTree.setRelPos(3, 14).setSize(xSize() - 6, ySize() - 31);
        editTree.setStandardScrollBehavior();
        editTree.setListMode(GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH);
        editTree.getVerticalScrollBar().setHidden(true);

        editTree.clearElements();
        for (StyleProperty prop : StyleHandler.getPropertyMap().values()) {
            StyleSetting setting = new StyleSetting(prop);
            setting.displayZLevel = displayZLevel;
            editTree.addElement(setting);
        }

        addChild(editTree);

        GuiButton closeEditor = new StyledGuiButton("user_dialogs.button_style").setText(I18n.format("pi.button.close_style_editor")).setPos(xPos() + 3, maxYPos() - 15).setSize(xSize() - 6, 12);
        closeEditor.addToGroup("EDITOR_TREE");
        closeEditor.onPressed(() -> setChildGroupEnabled("EDITOR_TREE", false).setChildGroupEnabled("PRESETS", true));
        addChild(closeEditor);

        setChildGroupEnabled("EDITOR_TREE", false);
        super.addChildElements();
    }

    @Override
    public void reloadElement() {
        presetList.clearElements();

        for (String preset : StyleHandler.getCustomPresets()) {
            GuiButton button = new StyledGuiButton("user_dialogs.sub_elements.button_style").setShadow(false).setText(preset).setYSize(12).setAlignment(GuiAlign.LEFT);
            button.onPressed(() -> {
                if (StyleHandler.unsavedChanges) {
                    GuiPopupDialogs.createDialog(this, GuiPopupDialogs.DialogType.OK_CANCEL_OPTION, I18n.format("pi.style.confirm_load_unsaved.txt") + "\n" + preset, "")//
                            .setOkListener(() -> StyleHandler.loadPreset(preset, true)).showCenter(displayZLevel + 50);
                } else {
                    StyleHandler.loadPreset(preset, true);
                }
            });

            GuiButton delete = new StyledGuiButton("user_dialogs.button_style").setSize(9, 9);
            delete.onPressed(() -> GuiPopupDialogs.createDialog(this, GuiPopupDialogs.DialogType.YES_NO_OPTION, I18n.format("pi.style.delete_preset_confirm.txt") + "\n" + preset, I18n.format("pi.style.confirm_delete.txt"))//
                    .setYesListener(() -> {
                        StyleHandler.deletePreset(preset);
                        reloadElement();
                    }).showCenter(displayZLevel + 50));
            delete.setHoverText(I18n.format("pi.button.delete"));
            delete.addChild(new GuiTexture(64, 16, 5, 5, PITextures.PI_PARTS).setRelPos(2, 2));
            button.addChild(delete);

            presetList.addElement(button);
            delete.setPos(button.maxXPos() - 11, button.yPos() + 1);
        }

        for (String preset : StyleHandler.getDefaultPresets()) {
            GuiButton button = new StyledGuiButton("user_dialogs.sub_elements.button_style").setShadow(false).setText(I18n.format("pi.style.default." + preset)).setYSize(12).setAlignment(GuiAlign.LEFT);
            button.addChild(new GuiLabel(I18n.format("pi.style.builtin")).setShadow(false).setYSize(12).bindSize(button, false).setAlignment(GuiAlign.RIGHT).setHoverableTextCol(hovering -> StyleHandler.getInt("user_dialogs.sub_elements.button_style.text_colour")));
            button.onPressed(() -> {
                if (StyleHandler.unsavedChanges) {
                    GuiPopupDialogs.createDialog(this, GuiPopupDialogs.DialogType.OK_CANCEL_OPTION, I18n.format("pi.style.confirm_load_unsaved.txt") + "\n" + preset, "")//
                            .setOkListener(() -> StyleHandler.loadPreset(preset, false)).showCenter(displayZLevel + 50);
                } else {
                    StyleHandler.loadPreset(preset, false);
                }
            });
            presetList.addElement(button);
        }

        super.reloadElement();
    }

    @Override
    public boolean onUpdate() {
        StyleHandler.setHighlight(highlight);
        highlight = "";
        return super.onUpdate();
    }

    @Override
    protected boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 1) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public class StyleSetting extends GuiElement<StyleSetting> {

        private StyleProperty property;
        private List<StyleProperty> subProps = new LinkedList<>();
        private boolean hasSubs = false;
        private boolean subsShown = false;
        private int propSize = 12;
        private GuiButton showHide;

        public StyleSetting(StyleProperty property) {
            this.property = property;
            if (property.subProps.size() > 0) {
                hasSubs = true;
                subProps.addAll(property.subProps.values());
            }

            propSize = 13;

            setYSize(propSize);
            reportYSizeChange = true;
            setHoverTextDelay(5);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return GuiHelper.isInRect(xPos(), yPos(), xSize(), propSize, mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);//(getParent() == null || getParent().isMouseOver(mouseX, mouseY));
        }

        @Override
        public void addChildElements() {
            if (hasSubs) {
                showHide = new GuiButton().setYPos(1).setXPosMod((guiButton, integer) -> maxXPos() - 12).setSize(11, 10);
                showHide.onPressed(() -> {
                    if (subsShown) {
                        subsShown = false;
                        removeChildByGroup("SUB_PROPS");
                        setYSize(propSize);
                    } else {
                        subsShown = true;
                        for (StyleProperty subProp : subProps) {
                            StyleSetting child = new StyleSetting(subProp).setXPos(xPos() + 10).setXSize(xSize() - 10);
                            child.displayZLevel = displayZLevel;
                            child.addToGroup("SUB_PROPS");
                            addChild(child);
                        }
                        sortSubs();
                    }
                });
                showHide.setHoverText(element -> I18n.format(subsShown ? "pi.style.hide_subs.txt" : "pi.style.show_subs.txt"));
                showHide.addChild(new GuiTexture(0, 16, 7, 4, PITextures.PI_PARTS).setYPos(4).setXPosMod((guiTexture, integer) -> showHide.xPos() + 2).setTexXGetter(() -> subsShown ? 24 : 16));
                addChild(showHide);
            }

            List<String> tt = new ArrayList<>();

            if (property.isColour()) {
                tt.add(I18n.format("pi.style.set_colour.txt"));
                tt.add(TextFormatting.DARK_GRAY + I18n.format("pi.style.right_click_options.txt"));
            } else if (property.isInteger()) {
                tt.add(I18n.format("pi.style.set_value.txt"));
                tt.add(TextFormatting.DARK_GRAY + I18n.format("pi.style.right_click_options.txt"));
            } else if (property.isBoolean()) { tt.add(I18n.format("pi.style.set_boolean.txt")); } else if (hasSubs) tt.add(I18n.format("pi.style.set_edit_sub_values.txt"));

            if (property.tip != null) {
                tt.add(property.tip);
            }

            if (!tt.isEmpty()) setHoverText(tt);

            super.addChildElements();
        }

        private void sortSubs() {
            int y = yPos() + propSize;
            boolean hasSubs = false;
            for (GuiElement elementBase : getChildGroup("SUB_PROPS")) {
                y += elementBase.setYPos(y).ySize();
                hasSubs = true;
            }
            if (hasSubs) y++;
            setYSize(y - yPos());
        }

        @Override
        public void ySizeChanged(GuiElement elementChanged) {
            if (elementChanged != this) {
                sortSubs();
            }
            super.ySizeChanged(elementChanged);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            editTree.setSmoothScroll(false, 20);

            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }

            boolean mouseOver = GuiHelper.isInRect(xPos(), yPos(), xSize(), propSize, mouseX, mouseY);
            if (mouseOver && button == 0) {
                if (property.isColour()) {
                    StyleHandler.setHighlight("");
                    GuiPickColourDialog pickColour = new GuiPickColourDialog(this);
                    pickColour.setColour(((ColourProperty) property).getColour());
                    pickColour.setIncludeAlpha(((ColourProperty) property).alpha);
                    pickColour.setColourChangeListener(integer -> ((ColourProperty) property).set(pickColour.getColour()));
                    pickColour.showCenter(displayZLevel + 10);
                    mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                } else if (property.isInteger()) {
                    GuiTextFieldDialog textDialog = new GuiTextFieldDialog(this);
                    textDialog.setYSize(25);
                    textDialog.setDragBar(5);
                    textDialog.setText(String.valueOf(((IntegerProperty) property).get()));
                    textDialog.setValidator(s -> s.isEmpty() || s.equals("-") || Utils.validInteger(s));
                    textDialog.addTextChangeCallback(s -> ((IntegerProperty) property).set(s.isEmpty() || s.equals("-") ? 0 : Utils.parseInt(s)));
                    textDialog.addChild(new GuiBorderedRect().setPosAndSize(textDialog).setFillColour(0xFF000000));
                    textDialog.showCenter(displayZLevel + 10);
                    textDialog.textField.setYSize(20).translate(0, 5);
                    textDialog.okButton.setYSize(20).translate(0, 5);
                    mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                } else if (property.isBoolean()) {
                    ((BooleanProperty) property).set(!((BooleanProperty) property).get());
                    mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                } else if (showHide != null) {
                    showHide.onPressed(mouseX, mouseY, button);
                }
            } else if (mouseOver && button == 1 && property.isInteger()) {
                GuiPopUpDialogBase context = new GuiPopUpDialogBase(this);
                context.setPos((int) mouseX, (int) mouseY).setSize(100, property.isColour() ? 79 : 27).normalizePosition();
                context.setCloseOnScroll(true);
                context.addChild(new GuiBorderedRect().setPosAndSize(context).setColours(0xFF404040, 0xFF909090));

                int rely = 0;
                GuiButton copy = new GuiButton(I18n.format("pi.button.copy_value")).setAlignment(GuiAlign.LEFT).setRelPos(1, rely += 1).setSize(98, 12).setBorderColours(0, 0xFF707070);
                copy.onPressed(() -> {
                    Utils.setClipboardString(String.valueOf(((IntegerProperty) property).get()));
                    context.close();
                });
                context.addChild(copy);

                GuiButton paste = new GuiButton(I18n.format("pi.button.paste_value")).setAlignment(GuiAlign.LEFT).setRelPos(1, rely += 13).setSize(98, 12).setBorderColours(0, 0xFF707070);
                paste.onPressed(() -> {
                    try {
                        long value = Long.decode(Utils.getClipboardString());
                        ((IntegerProperty) property).set((int) value);
                    }
                    catch (NumberFormatException e) {
                        PIGuiHelper.displayError("Invalid value found in clipboard! " + Utils.getClipboardString() + " Must be an integer");
                    }
                    context.close();
                });
                context.addChild(paste);

                if (property.isColour()) {
                    GuiButton copyHex = new GuiButton(I18n.format("pi.button.copy_hex_value")).setAlignment(GuiAlign.LEFT).setRelPos(1, rely += 13).setSize(98, 12).setBorderColours(0, 0xFF707070);
                    copyHex.onPressed(() -> {
                        int value = ((IntegerProperty) property).get();
                        Utils.setClipboardString(Integer.toHexString(value));
                        context.close();
                    });
                    context.addChild(copyHex);

                    GuiButton pasteHex = new GuiButton(I18n.format("pi.button.paste_hex_value")).setAlignment(GuiAlign.LEFT).setRelPos(1, rely += 13).setSize(98, 12).setBorderColours(0, 0xFF707070);
                    pasteHex.onPressed(() -> {
                        try {
                            String value = Utils.getClipboardString();
                            if (value.startsWith("#") || value.toLowerCase().startsWith("0x")) {
                                ((IntegerProperty) property).set((int) (long) Long.decode(value));
                            } else {
                                ((IntegerProperty) property).set((int) Long.parseLong(value, 16));
                            }
                        }
                        catch (NumberFormatException e) {
                            PIGuiHelper.displayError("Invalid value found in clipboard! " + Utils.getClipboardString() + " Must be a hex value");
                        }
                        context.close();
                    });
                    context.addChild(pasteHex);

                    GuiButton lighten = new GuiButton(I18n.format("pi.button.lighten")).setAlignment(GuiAlign.LEFT).setRelPos(1, rely += 13).setSize(98, 12).setBorderColours(0, 0xFF707070);
                    lighten.onPressed(() -> {
                        Colour colour = ((ColourProperty) property).getColour();
                        ((ColourProperty) property).set(changeShade(colour.argb(), 0.05));
                    });
                    context.addChild(lighten);
                    GuiButton darken = new GuiButton(I18n.format("pi.button.darken")).setAlignment(GuiAlign.LEFT).setRelPos(1, rely + 13).setSize(98, 12).setBorderColours(0, 0xFF707070);
                    darken.onPressed(() -> {
                        Colour colour = ((ColourProperty) property).getColour();
                        ((ColourProperty) property).set(changeShade(colour.argb(), -0.05));
                    });
                    context.addChild(darken);
                }

                context.show(displayZLevel + 10);
            }

            return false;
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            boolean mouseOver = GuiHelper.isInRect(xPos(), yPos(), xSize(), propSize, mouseX, mouseY);
            int border = mouseOver ? 0xFF00FF00 : 0xFF5555FF;
            int fill = 0xFFD0D0D0;

            //Dropdown Items
            if (subsShown) {
                int i = yPos();
                for (GuiElement e : childElements) if (e.yPos() > i) i = e.yPos();
                drawBorderedRect(xPos() + 4, yPos() + propSize - 2, 3, (i - yPos()) - 4, 2, 0, 0xFF000000);
            }
            drawBorderedRect(xPos() - 3, yPos() + 4, 3, 3, 2, 0, 0xFF000000);

            //Background and border
            drawBorderedRect(xPos(), yPos(), xSize(), propSize - 1, 1, fill, border);

            //Draw Text
            drawString(fontRenderer, I18n.format("pi.style." + property.getType().getName() + ".prop"), xPos() + 12, yPos() + 2, 0);

            //Draw Graphic
            GlStateManager.color4f(1, 1, 1, 1);
            bindTexture(PITextures.PI_PARTS);
            int xIndex = property.isColour() ? 32 : property.isInteger() ? 40 : property.isBoolean() ? 48 : 56;
            drawTexturedModalRect(xPos() + 3, yPos() + 3, xIndex, 16, 5, 7);

            //Draw Colour Indicator
            if (property.isColour()) {
                drawString(fontRenderer, "T", maxXPos() - 19.5F, yPos() + 2.5F, 0);
                zOffset += 10;
                drawBorderedRect(maxXPos() - 22, yPos() + 1, 10, 10, 1, ((ColourProperty) property).getColour().argb(), 0xFF000000);
                zOffset -= 10;
            } else if (property.isInteger()) {
                drawCustomString(fontRenderer, String.valueOf(((IntegerProperty) property).get()), maxXPos() - 45, yPos() + 2.5F, 33, 0xFFAA00, GuiAlign.RIGHT, GuiAlign.TextRotation.NORMAL, false, true, true);
            } else if (property.isBoolean()) {
                drawString(fontRenderer, String.valueOf(((BooleanProperty) property).get()), maxXPos() - 38, yPos() + 2.5F, ((BooleanProperty) property).get() ? 0x00FF00 : 0xFF0000, ((BooleanProperty) property).get());
            }

            GlStateManager.color4f(1, 1, 1, 1);
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            List<String> tt = new ArrayList<>();
            if (property.isColour() && GuiHelper.isInRect(maxXPos() - 22, yPos() + 1, 10, 10, mouseX, mouseY)) {
                Colour colour = ((ColourProperty) property).getColour();
                tt.add(I18n.format("pi.style.colour_value.txt"));
                tt.add(TextFormatting.RED + "R: " + (colour.r & 0xFF));
                tt.add(TextFormatting.GREEN + "G: " + (colour.g & 0xFF));
                tt.add(TextFormatting.BLUE + "B: " + (colour.b & 0xFF));
                tt.add(I18n.format("pi.style." + (((colour.a & 0xFF) < 255) ? ((colour.a & 0xFF) == 0) ? "transparent" : "semi_transparent" : "opaque") + ".txt"));
            } else if ((property.isInteger() && !property.isColour()) && GuiHelper.isInRect(maxXPos() - 45, yPos() + 1, 33, 10, mouseX, mouseY)) {
                tt.add(I18n.format("pi.style.integer_value.txt"));
                tt.add(((IntegerProperty) property).get() + "");
            }

            if (!tt.isEmpty()) {
                drawHoveringText(tt, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                return true;
            }

            return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean onUpdate() {
            if (editTree.isEnabled() && GuiHelper.isInRect(xPos(), yPos(), xSize() / 2, propSize, getMouseX(), getMouseY())) {
                highlight = property.getPath();
            }
            return super.onUpdate();
        }

//        private int getMouseX() {
//            return Mouse.getEventX() * screenWidth / mc.displayWidth;
//        }
//
//        private int getMouseY() {
//            return screenHeight - Mouse.getEventY() * screenHeight / mc.displayHeight - 1;
//        }
    }
}
