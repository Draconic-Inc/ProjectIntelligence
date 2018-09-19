package com.brandon3055.projectintelligence.client.gui.guielements;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.*;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence_old;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.PIPartRenderer;
import com.brandon3055.projectintelligence.docmanagement.DocumentationManager;
import com.brandon3055.projectintelligence.docmanagement.LanguageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import static com.brandon3055.projectintelligence.client.StyleHandler.StyleType.*;

/**
 * Created by brandon3055 on 12/08/2017.
 */
public class GuiPIConfig extends GuiPopUpDialogBase<GuiPIConfig> {
    public static StyleHandler.PropertyGroup buttonProps = new StyleHandler.PropertyGroup("user_dialogs.button_style");
    private GuiScrollElement configList;
    private GuiStyleEditor styleEditor;

    public GuiPIConfig(MGuiElementBase parent, GuiStyleEditor styleEditor) {
        super(parent);
        this.styleEditor = styleEditor;
        setSize(200, 200);
        setDragBar(12);
        setCloseOnOutsideClick(false);
    }


    public void reloadConfigProperties() {
        configList.clearElements();
        //Basic Settings
        configList.addElement(new GuiLabel(TextFormatting.UNDERLINE + I18n.format("pi.config.basic_config")).setYSize(12).setShadow(false).setTextColGetter(hovering -> StyleHandler.getInt("user_dialogs." + TEXT_COLOUR.getName())));

        addConfig(new ConfigProperty(this, "pi.config.open_style_settings").setAction(() -> styleEditor.toggleShown(true, 550)).setCloseOnClick(true));

        addConfig(new ConfigProperty(this, () -> "pi.config.set_pi_language", () -> (LanguageManager.isCustomUserLanguageSet() ? "" : I18n.format("pi.lang.mc_default") + " ") + LanguageManager.LANG_NAME_MAP.get(LanguageManager.getUserLanguage())).setHoverText(I18n.format("pi.config.set_pi_language.info"), TextFormatting.GRAY + I18n.format("pi.config.set_pi_language_note.info")).setAction(this::openLanguageSelector));

        addConfig(new ConfigProperty(this, () -> "pi.config.max_tabs", () -> String.valueOf(PIConfig.maxTabs)).setHoverText(I18n.format("pi.config.max_tabs.info")).setAction(() -> {
            GuiTextFieldDialog dialog = new GuiTextFieldDialog(this, I18n.format("pi.config.max_tabs.title"));
            dialog.setXSize(200).setMaxLength(4096);
            dialog.addChild(new StyledGuiRect("user_dialogs").setPosAndSize(dialog));
            dialog.setTitleColour(StyleHandler.getInt("user_dialogs." + StyleHandler.StyleType.TEXT_COLOUR.getName()));
            dialog.setText(String.valueOf(PIConfig.maxTabs));
            dialog.setValidator(value -> value.isEmpty() || Utils.validInteger(value));
            dialog.addTextConfirmCallback(s -> {
                PIConfig.maxTabs = MathHelper.clip(Utils.parseInt(s, true), 1, 64);
                PIConfig.save();
            });
            dialog.showCenter(displayZLevel + 50);
        }));

        addConfig(new ConfigProperty(this, () -> "pi.config.et_fluid", () -> PIConfig.etCheckFluid + "").setAction(() -> PIConfig.etCheckFluid = !PIConfig.etCheckFluid).setHoverText(I18n.format("pi.config.et_fluid.info")));


        //Advanced Settings
        configList.addElement(new GuiLabel(TextFormatting.UNDERLINE + I18n.format("pi.config.advanced_config")).setYSize(12).setShadow(false).setTextColGetter(hovering -> StyleHandler.getInt("user_dialogs." + TEXT_COLOUR.getName())));


        //region Edit Mode Settings
        addConfig(new ConfigProperty(this, () -> "pi.config.edit_mode", () -> PIConfig.editMode() + "").setAction(() -> {
            PIConfig.setEditMode(!PIConfig.editMode());
            if (!PIConfig.editMode()) {
                PIGuiHelper.closeEditor();
            }
            PIConfig.save();

            if (PIConfig.editMode() && !new File(PIConfig.editingRepoLoc).exists()) {
                reloadConfigProperties();
                displayRepoSetDialog();
            }
            else {
                DocumentationManager.checkAndReloadDocFiles();
                if (getParent() != null) getParent().reloadElement();
                reloadConfigProperties();
            }
        }));

        if (PIConfig.editMode()) {
            addConfig(new ConfigProperty(this, () -> "pi.config.edit_repo_loc", () -> PIConfig.editingRepoLoc.isEmpty() ? "[Not Set]" : PIConfig.editingRepoLoc).setHoverText(I18n.format("pi.config.edit_repo_loc.info")).setAction(() -> {
                displayRepoSetDialog();
            }));
        }

        addConfig(new ConfigProperty(this, () -> PIConfig.editMode() ? "pi.config.reload_from_disk" : "pi.config.reload_documentation").setHoverText(PIConfig.editMode() ? I18n.format("pi.config.reload_from_disk.info") : I18n.format("pi.config.reload_documentation.info")).setAction(DocumentationManager::checkAndReloadDocFiles));

        if (PIConfig.editMode()) {
            addConfig(new ConfigProperty(this, "pi.config.open_editor").setAction(PIGuiHelper::displayEditor));
        }

        //endregion

    }

    private void displayRepoSetDialog() {
        GuiTextFieldDialog dialog = new GuiTextFieldDialog(this, I18n.format("pi.config.edit_repo_select_title"));
        dialog.setXSize(280).setMaxLength(4096);
        dialog.addChild(new StyledGuiRect("user_dialogs").setPosAndSize(dialog));
        dialog.setTitleColour(StyleHandler.getInt("user_dialogs." + StyleHandler.StyleType.TEXT_COLOUR.getName()));
        dialog.setText(PIConfig.editingRepoLoc);
        dialog.addTextConfirmCallback(s -> {
            PIConfig.editingRepoLoc = s;
            PIConfig.save();
            if (PIConfig.editMode()) {
                DocumentationManager.checkAndReloadDocFiles();
            }
        });
        dialog.showCenter(displayZLevel + 50);
    }

    public void addConfig(ConfigProperty configProperty) {
        configList.addElement(configProperty);
    }

    @Override
    public void addChildElements() {
        childElements.clear();

        //Background Rectangle
        addChild(new StyledGuiRect("user_dialogs").setPosAndSize(this));

        // Window Title
        addChild(new GuiLabel(TextFormatting.UNDERLINE + I18n.format("pi.config.pi_configuration.title"))//
                .setPos(this).setSize(xSize(), 10).translate(4, 3).setTextColGetter(hovering -> StyleHandler.getInt("user_dialogs." + TEXT_COLOUR.getName())).setShadow(false).setAlignment(GuiAlign.CENTER));

        //Close Button
        GuiButton close = new StyledGuiButton("user_dialogs.button_style").setPos(this).translate(xSize() - 14, 3).setSize(11, 11);
        close.setListener(this::close);
        close.setHoverText(I18n.format("pi.button.close"));
        close.addChild(new GuiTexture(64, 16, 5, 5, PITextures.PI_PARTS).setRelPos(3, 3));
        addChild(close);

        //List Background
        GuiBorderedRect listBackground;
        addChild(listBackground = new GuiBorderedRect().setRelPos(4, 16).setSize(xSize() - 8, ySize() - 20));
        listBackground.setFillColourGetter(hovering -> StyleHandler.getInt("user_dialogs.sub_elements." + COLOUR.getName()));
        listBackground.setBorderColourGetter(hovering -> StyleHandler.getInt("user_dialogs.sub_elements." + BORDER.getName()));

        //Config List
        configList = new GuiScrollElement();
        configList.setRelPos(5, 17).setSize(xSize() - 10, ySize() - 22);
        configList.setStandardScrollBehavior();
        configList.setListMode(GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH);
        configList.getVerticalScrollBar().setHidden(true);
        configList.setListSpacing(1);

        configList.clearElements();
        reloadConfigProperties();

        addChild(configList);

        if (!GuiProjectIntelligence_old.devMode) {
            super.addChildElements();
        }
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
    }

    @Override
    protected boolean keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            close();
            return true;
        }
        return super.keyTyped(typedChar, keyCode);
    }

    public static class ConfigProperty extends MGuiElementBase<ConfigProperty> {
        public PIPartRenderer buttonRenderer = new PIPartRenderer(buttonProps).setButtonRender(true);
        private boolean playSound = true;
        private boolean closeOnClick = false;
        private Supplier<String> unlocalizedNameSupplier = null;
        private Supplier<String> valueSupplier = null;
        private Runnable clickAction = null;
        private GuiPIConfig gui;

        public ConfigProperty(GuiPIConfig gui) {
            this.gui = gui;
            setYSize(15);
        }

        public ConfigProperty(GuiPIConfig gui, Supplier<String> unlocalizedNameSupplier) {
            this(gui);
            this.unlocalizedNameSupplier = unlocalizedNameSupplier;
        }

        public ConfigProperty(GuiPIConfig gui, String unlocalizedName) {
            this(gui);
            this.unlocalizedNameSupplier = () -> unlocalizedName;
        }

        public ConfigProperty(GuiPIConfig gui, Supplier<String> unlocalizedNameSupplier, Supplier<String> valueSupplier) {
            this(gui, unlocalizedNameSupplier);
            this.valueSupplier = valueSupplier;
            setYSize(26);
        }

        public ConfigProperty(GuiPIConfig gui, Supplier<String> unlocalizedNameSupplier, Supplier<String> valueSupplier, Runnable clickAction) {
            this(gui, unlocalizedNameSupplier, valueSupplier);
            this.clickAction = clickAction;
        }

        public ConfigProperty setNameSupplier(Supplier<String> unlocalizedNameSupplier) {
            this.unlocalizedNameSupplier = unlocalizedNameSupplier;
            return this;
        }

        public ConfigProperty setValueSupplier(Supplier<String> valueSupplier) {
            this.valueSupplier = valueSupplier;
            setYSize(26);
            return this;
        }

        public ConfigProperty setAction(Runnable clickAction) {
            this.clickAction = clickAction;
            return this;
        }

        public ConfigProperty setCloseOnClick(boolean closeOnClick) {
            this.closeOnClick = closeOnClick;
            return this;
        }

        public ConfigProperty setSilent() {
            this.playSound = false;
            return this;
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            if (isMouseOver(mouseX, mouseY)) {
                if (clickAction != null) {
                    clickAction.run();
                }
                if (closeOnClick) {
                    gui.close();
                }
                if (playSound) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            boolean mouseOver = isMouseOver(mouseX, mouseY);
            buttonRenderer.render(this, mouseOver);

            if (unlocalizedNameSupplier != null) {
                drawString(fontRenderer, TextFormatting.UNDERLINE + I18n.format(unlocalizedNameSupplier.get()), xPos() + 4, yPos() + 3, buttonProps.textColour());
            }
            if (valueSupplier != null) {
                drawString(fontRenderer, valueSupplier.get(), xPos() + 4, yPos() + 15, buttonProps.textColourHover());
            }

            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
    }


    //Misc helper code
    public void openLanguageSelector() {
        StyledSelectDialog<String> langSelect = new StyledSelectDialog<>(this, "user_dialogs", "Select Language");
        langSelect.setInsets(14, 2, 18, 2);

        //Add Search Box
        GuiTextField filter = new GuiTextField();
        langSelect.addChild(filter);
        filter.setSize(langSelect.xSize() - 4, 14).setPos(langSelect.xPos() + 2, langSelect.maxYPos() - 16);
        filter.setListener((event, eventSource) -> langSelect.reloadElement());
        langSelect.setSelectionFilter(item -> {
            String ft = filter.getText().toLowerCase();
            return ft.isEmpty() || item.toLowerCase().contains(ft) || LanguageManager.LANG_NAME_MAP.getOrDefault(item, "").toLowerCase().contains(ft);
        });

        //Add Items
        String doTrans = I18n.format("pi.lang.disable_override");
        if (LanguageManager.isCustomUserLanguageSet()) {
            langSelect.addItem(doTrans);
        }
        langSelect.setSelected(LanguageManager.getUserLanguage());
        LanguageManager.ALL_LANGUAGES.forEach(langSelect::addItem);
        langSelect.setSelectionListener(lang -> {
            LanguageManager.setCustomUserLanguage(lang.equals(doTrans) ? null : lang);
            GuiButton.playGenericClick(mc);
            DocumentationManager.checkAndReloadDocFiles();
        });

        langSelect.setCloseOnSelection(true);
        langSelect.showCenter(this.displayZLevel + 50);


    }
}
