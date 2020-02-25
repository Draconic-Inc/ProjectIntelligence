package com.brandon3055.projectintelligence.client;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import static com.brandon3055.projectintelligence.client.StyleHandler.StyleType.*;

/**
 * Created by brandon3055 on 12/08/2017.
 */
public class StyleHandler {

    public static boolean unsavedChanges = false;
    private static String highlight = "";

    private static File styleCfgFolder;
    private static File defaultPresetsFolder;
    private static File customPresetsFolder;
    private static File activeStyle;
    private static Map<String, StyleProperty> fullPropertyMap = new LinkedHashMap<>();
    private static Map<String, StyleProperty> rootPropertyMap = new LinkedHashMap<>();
    private static List<Runnable> reloadListeners = new ArrayList<>();

    static {
        reloadStyleProperties();
    }

    public static void reloadStyleProperties() {
        fullPropertyMap.clear();
        rootPropertyMap.clear();

        StyleProperty menu = addProperty(new StyleProperty(MENU));
        //# Menu
        //region //############################################################################

        menu.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        menu.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
        menu.addSubProp(new BooleanProperty(THICK_BORDERS, true));
        menu.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        menu.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        menu.addSubProp(new ColourProperty(TEXT_COLOUR, 0x404040, false));
        menu.addSubProp(new BooleanProperty(TEXT_SHADOW, false));

        StyleProperty closeButton = menu.addSubProp(new StyleProperty(CLOSE_BUTTON));
        closeButton.addSubProp(new ColourProperty(COLOUR, 0xB0B0B0, false));
        closeButton.addSubProp(new ColourProperty(HOVER, 0xFF3030, false));
//        closeButton.addSubProp(new ColourProperty(BACKGROUND, 0, true));
//        closeButton.addSubProp(new ColourProperty(BACKGROUND_HOVER, 0, true));
//        closeButton.addSubProp(new ColourProperty(BORDER, 0, true));
//        closeButton.addSubProp(new ColourProperty(BORDER_HOVER, 0xFFFF0000, true));

        StyleProperty sizeButtons = menu.addSubProp(new StyleProperty(SIZE_BUTTONS));
        sizeButtons.addSubProp(new ColourProperty(COLOUR, 0xB0B0B0, false));
        sizeButtons.addSubProp(new ColourProperty(HOVER, 0xC0C0C0, false));
//        sizeButtons.addSubProp(new ColourProperty(BACKGROUND, 0, true));
//        sizeButtons.addSubProp(new ColourProperty(BACKGROUND_HOVER, 0, true));
//        sizeButtons.addSubProp(new ColourProperty(BORDER, 0, true));
//        sizeButtons.addSubProp(new ColourProperty(BORDER_HOVER, 0, true));

        StyleProperty settingsButton = menu.addSubProp(new StyleProperty(SETTINGS_BUTTON));
        settingsButton.addSubProp(new ColourProperty(COLOUR, 0xFFFFFF, false));
        settingsButton.addSubProp(new ColourProperty(HOVER, 0xC0C0C0, false));
//        settingsButton.addSubProp(new ColourProperty(BACKGROUND, 0, true));
//        settingsButton.addSubProp(new ColourProperty(BACKGROUND_HOVER, 0, true));
//        settingsButton.addSubProp(new ColourProperty(BORDER, 0, true));
//        settingsButton.addSubProp(new ColourProperty(BORDER_HOVER, 0, true));

        //endregion

        StyleProperty pageList = addProperty(new StyleProperty(PAGE_LIST));
        //# Page List
        //region //############################################################################

        StyleProperty toggleHidden = pageList.addSubProp(new StyleProperty(HIDE_BUTTON));
        toggleHidden.addSubProp(new ColourProperty(COLOUR, 0xFFFFFF, false));
        toggleHidden.addSubProp(new ColourProperty(HOVER, 0xffffa0, false));

        StyleProperty header = pageList.addSubProp(new StyleProperty(HEADER));
        header.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        header.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
        header.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        header.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        header.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        header.addSubProp(new ColourProperty(TEXT_COLOUR, 0x404040, false));


        StyleProperty dirPath = pageList.addSubProp(new StyleProperty(DIR_PATH));
        dirPath.addSubProp(new ColourProperty(COLOUR, 0xFFc6c6c6, true));
        dirPath.addSubProp(new ColourProperty(BORDER, 0xFF000000, true));
        dirPath.addSubProp(new BooleanProperty(SHADED_BORDERS, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));

        StyleProperty dirButtons = dirPath.addSubProp(new StyleProperty(DIR_BUTTONS));
        dirButtons.addSubProp(new ColourProperty(COLOUR, 0xFF7b7b7b, true));
        dirButtons.addSubProp(new ColourProperty(HOVER, 0xFF8b8bc8, true));
        dirButtons.addSubProp(new ColourProperty(BORDER, 0xFF7b7b7b, true));
        dirButtons.addSubProp(new ColourProperty(BORDER_HOVER, 0xFF8b8bc8, true));
        dirButtons.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
        dirButtons.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));
        dirButtons.addSubProp(new ColourProperty(TEXT_HOVER, 0x000000, false));

        StyleProperty body = pageList.addSubProp(new StyleProperty(BODY));
        body.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        body.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
//        body.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        body.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        body.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        body.addSubProp(new ColourProperty(TEXT_COLOUR, 0x404040, false));

        StyleProperty footer = pageList.addSubProp(new StyleProperty(FOOTER));
        footer.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        footer.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
//        footer.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        footer.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        footer.addSubProp(new ColourProperty(BORDER, 0x00000000, true));

        StyleProperty search = pageList.addSubProp(new StyleProperty(SEARCH));
        search.addSubProp(new ColourProperty(TEXT_COLOUR, 0xFFFFFF, false));
        search.addSubProp(new ColourProperty(COLOUR, 0xFF000000, true));
        search.addSubProp(new ColourProperty(BORDER, 0xFF606060, true));
        StyleProperty searchSettings = search.addSubProp(new StyleProperty(SETTINGS_BUTTON));
        searchSettings.addSubProp(new ColourProperty(COLOUR, 0xFFA0A0A0, false));
        searchSettings.addSubProp(new ColourProperty(HOVER, 0xFFC0C0C0, false));

        StyleProperty pageButtons = pageList.addSubProp(new StyleProperty(PAGE_BUTTONS));
        pageButtons.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        pageButtons.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
        pageButtons.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        pageButtons.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        pageButtons.addSubProp(new ColourProperty(HOVER, 0xFFFFFFFF, true));
        pageButtons.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        pageButtons.addSubProp(new ColourProperty(BORDER_HOVER, 0x00000000, true));
        pageButtons.addSubProp(new ColourProperty(TEXT_COLOUR, 0xe0e0e0, false));
        pageButtons.addSubProp(new ColourProperty(TEXT_HOVER, 0xffffa0, false));
        pageButtons.addSubProp(new BooleanProperty(TEXT_SHADOW, true));

        StyleProperty pageBackButton = pageButtons.addSubProp(new StyleProperty(PAGE_BACK_BUTTON));
        pageBackButton.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        pageBackButton.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
        pageBackButton.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        pageBackButton.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        pageBackButton.addSubProp(new ColourProperty(HOVER, 0xFFFFFFFF, true));
        pageBackButton.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        pageBackButton.addSubProp(new ColourProperty(BORDER_HOVER, 0x00000000, true));
        pageBackButton.addSubProp(new ColourProperty(TEXT_COLOUR, 0xe0e0e0, false));
        pageBackButton.addSubProp(new ColourProperty(TEXT_HOVER, 0xffffa0, false));
        pageBackButton.addSubProp(new BooleanProperty(TEXT_SHADOW, true));
        StyleProperty pageBackButtonIcon = pageBackButton.addSubProp(new StyleProperty(ICON));
        pageBackButtonIcon.addSubProp(new ColourProperty(COLOUR, 0xFFFFFF, false));
        pageBackButtonIcon.addSubProp(new ColourProperty(HOVER, 0xffffa0, false));

        createScrollBarProp(pageList);

        //endregion

        StyleProperty mdWindow = addProperty(new StyleProperty(MD_WINDOW));
        //# Markdown Window
        //region //############################################################################

        header = mdWindow.addSubProp(new StyleProperty(HEADER));
        header.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        header.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
        header.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        header.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        header.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        header.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));

        body = mdWindow.addSubProp(new StyleProperty(BODY));
        body.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        body.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
        body.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        body.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        body.addSubProp(new ColourProperty(BORDER, 0xFF000000, true));
        body.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));


        createScrollBarProp(mdWindow);

        //endregion

        StyleProperty dialogs = addProperty(new StyleProperty(USER_DIALOGS));
        //# Dialogs
        //region //############################################################################

        //General Layout
        dialogs.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        dialogs.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        dialogs.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        dialogs.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        dialogs.addSubProp(new ColourProperty(TEXT_COLOUR, 0x404040, false));

        //Buttons
        StyleProperty editorButtons = dialogs.addSubProp(new StyleProperty(BUTTON_STYLE));
        editorButtons.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        editorButtons.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        editorButtons.addSubProp(new ColourProperty(HOVER, 0xFFFFFFFF, true));
        editorButtons.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        editorButtons.addSubProp(new ColourProperty(BORDER_HOVER, 0xFF000000, true));
        editorButtons.addSubProp(new ColourProperty(TEXT_COLOUR, 0xe0e0e0, false));
        editorButtons.addSubProp(new ColourProperty(TEXT_HOVER, 0xffffa0, false));

        //Sub elements
        StyleProperty subElements = dialogs.addSubProp(new StyleProperty(SUB_ELEMENTS));
        subElements.setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.sub_elements.info"));
//        StyleProperty presetWindow = styleEditor.addSubProp(new StyleProperty(PRESET_WINDOW));
        subElements.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        subElements.addSubProp(new ColourProperty(BORDER, 0x00000000, true));

        StyleProperty presetButtons = subElements.addSubProp(new StyleProperty(BUTTON_STYLE));
        presetButtons.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        presetButtons.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        presetButtons.addSubProp(new ColourProperty(HOVER, 0xFFFFFFFF, true));
        presetButtons.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        presetButtons.addSubProp(new ColourProperty(BORDER_HOVER, 0x00000000, true));
        presetButtons.addSubProp(new ColourProperty(TEXT_COLOUR, 0xe0e0e0, false));
        presetButtons.addSubProp(new ColourProperty(TEXT_HOVER, 0xffffa0, false));


        //endregion

        StyleProperty guiDoc = addProperty(new StyleProperty(GUI_DOCS).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.gui_docs.info")));
        //# Gui Doc
        //region //############################################################################

        //Overall window style
        guiDoc.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        guiDoc.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
        guiDoc.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        guiDoc.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        guiDoc.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        guiDoc.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));

        //Settings button style
        settingsButton = guiDoc.addSubProp(new StyleProperty(SETTINGS_BUTTON));
        settingsButton.addSubProp(new ColourProperty(COLOUR, 0xFFFFFF, false));
        settingsButton.addSubProp(new ColourProperty(HOVER, 0xC0C0C0, false));

        closeButton = guiDoc.addSubProp(new StyleProperty(CLOSE_BUTTON));
        closeButton.addSubProp(new ColourProperty(COLOUR, 0xFFFFFF, false));
        closeButton.addSubProp(new ColourProperty(HOVER, 0xFF3030, false));

        //Page buttons style
        header = guiDoc.addSubProp(new StyleProperty(HEADER));
//        header.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
//        header.addSubProp(new ColourProperty(HOVER, 0xFFFFFFFF, true));
        header.addSubProp(new ColourProperty(TEXT_COLOUR, 0xe0e0e0, false));
        header.addSubProp(new ColourProperty(TEXT_HOVER, 0xffffa0, false));
        header.addSubProp(new ColourProperty(BACKGROUND, 0x40000000, true));
//        buttons.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
//        buttons.addSubProp(new ColourProperty(BORDER, 0xFF000000, true));
//        buttons.addSubProp(new ColourProperty(BORDER_HOVER, 0xFF000000, true));

        //Scroll bar style
        createScrollBarProp(guiDoc);

        //endregion

        reloadListeners.forEach(Runnable::run);
    }

    private static StyleProperty addProperty(StyleProperty property) {
        fullPropertyMap.put(property.path, property);
        rootPropertyMap.put(property.path, property);
        return property;
    }

    public static Map<String, StyleProperty> getPropertyMap() {
        return ImmutableMap.copyOf(rootPropertyMap);
    }

    private static StyleProperty createScrollBarProp(StyleProperty parent) {
        StyleProperty scrollBar = parent.addSubProp(new StyleProperty(SCROLL_BAR));
//        if (allowCompact) {
//            scrollBar.addSubProp(new BooleanProperty(COMPACT_BAR, true));
//        }
        scrollBar.addSubProp(new ColourProperty(COLOUR, 0xFF8b8b8b, true));
        scrollBar.addSubProp(new ColourProperty(HOVER, 0xFF8b8b8b, true));
        scrollBar.addSubProp(new ColourProperty(BORDER, 0xFF8b8b8b, true));
        scrollBar.addSubProp(new ColourProperty(BORDER_HOVER, 0xFF8b8b8b, true));
        scrollBar.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
        StyleProperty scrollSlider = scrollBar.addSubProp(new StyleProperty(SCROLL_SLIDER));
        scrollSlider.addSubProp(new ColourProperty(COLOUR, 0xFF8b8b8b, true));
        scrollSlider.addSubProp(new ColourProperty(HOVER, 0xFF8b8bc8, true));
        scrollSlider.addSubProp(new ColourProperty(BORDER, 0xFF8b8b8b, true));
        scrollSlider.addSubProp(new ColourProperty(BORDER_HOVER, 0xFF8b8bc8, true));
        scrollSlider.addSubProp(new BooleanProperty(SHADED_BORDERS, true).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(INVERT_SHADE, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.invert_shade.info")));
        return scrollBar;
    }

    public static void setHighlight(String prop) {
        highlight = prop;
        reloadListeners.forEach(Runnable::run);
    }

    //# Getter's & Setters
    //region //############################################################################

    public static boolean getBoolean(String prop) {
        StyleProperty styleProperty = fullPropertyMap.get(prop);
        if (styleProperty != null && styleProperty.isBoolean()) {
            return ((BooleanProperty) styleProperty).get();
        }
        LogHelper.error("StyleHandler: Attempt to retrieve un-defined boolean: " + prop);
        return false;
    }

    public static void setBoolean(String prop, boolean value) {
        if (fullPropertyMap.containsKey(prop) && fullPropertyMap.get(prop).isBoolean()) {
            ((BooleanProperty) fullPropertyMap.get(prop)).set(value);
            saveStyle();
        }
        LogHelper.error("StyleHandler: Attempt to set un-defined boolean: " + prop);
    }

    public static int getInt(String prop) {
        StyleProperty styleProperty = fullPropertyMap.get(prop);
        if (styleProperty != null && styleProperty.isInteger()) {
            return ((IntegerProperty) styleProperty).get();
        }
        LogHelper.error("StyleHandler: Attempt to retrieve un-defined integer: " + prop);
        return 0;
    }

    public static void setInt(String prop, int value) {
        if (fullPropertyMap.containsKey(prop) && fullPropertyMap.get(prop).isInteger()) {
            ((IntegerProperty) fullPropertyMap.get(prop)).set(value);
            saveStyle();
        }
        LogHelper.error("StyleHandler: Attempt to set un-defined integer: " + prop);
    }

    public static Colour getColour(String prop) {
        StyleProperty styleProperty = fullPropertyMap.get(prop);
        if (styleProperty != null && styleProperty.isColour()) {
            return ((ColourProperty) styleProperty).getColour();
        }
        LogHelper.error("StyleHandler: Attempt to retrieve un-defined colour: " + prop);
        return new ColourARGB(0);
    }

    public static void setColour(String prop, Colour colour) {
        if (fullPropertyMap.containsKey(prop) && fullPropertyMap.get(prop).isColour()) {
            ((ColourProperty) fullPropertyMap.get(prop)).set(colour);
            saveStyle();
        }
        LogHelper.error("StyleHandler: Attempt to set un-defined colour: " + prop);
    }

    public static boolean hasProp(String prop) {
        return fullPropertyMap.containsKey(prop);
    }

    private static void addReloadListener(Runnable listener) {
        reloadListeners.add(listener);
    }

    public static BooleanProperty getBooleanProp(String prop) {
        StyleProperty styleProperty = fullPropertyMap.get(prop);
        if (styleProperty != null && styleProperty.isBoolean()) {
            return ((BooleanProperty) styleProperty);
        }
        LogHelper.error("StyleHandler: Attempt to retrieve un-defined boolean: " + prop);
        return new BooleanProperty(StyleType.BACKGROUND, false);
    }

    public static IntegerProperty getIntProp(String prop) {
        StyleProperty styleProperty = fullPropertyMap.get(prop);
        if (styleProperty != null && styleProperty.isInteger()) {
            return ((IntegerProperty) styleProperty);
        }
        LogHelper.error("StyleHandler: Attempt to retrieve un-defined integer: " + prop);
        return new IntegerProperty(StyleType.BACKGROUND, 0);
    }

    public static ColourProperty getColourProp(String prop) {
        StyleProperty styleProperty = fullPropertyMap.get(prop);
        if (styleProperty != null && styleProperty.isColour()) {
            return ((ColourProperty) styleProperty);
        }
        LogHelper.error("StyleHandler: Attempt to retrieve un-defined colour: " + prop);
        return new ColourProperty(StyleType.BACKGROUND, 0, true);
    }

    private static int getHighlightColour(int colour, boolean alpha) {
        Colour in = new ColourARGB(colour);
        Colour out = new ColourARGB(0xFF00FF00);
        double d = (Math.sin(TimeKeeper.getClientTick() / 10D) + 1D) / 2D;
        double r = ((in.r & 0xFF) * d) + ((out.r & 0xFF) * (1D - d));
        double g = ((in.g & 0xFF) * d) + ((out.g & 0xFF) * (1D - d));
        double b = ((in.b & 0xFF) * d) + ((out.b & 0xFF) * (1D - d));
        double a = alpha ? ((in.a & 0xFF) * d) + ((out.a & 0xFF) * (1D - d)) : 0xFF;
        return ((int) a & 0xFF) << 24 | ((int) r & 0xFF) << 16 | ((int) g & 0xFF) << 8 | ((int) b & 0xFF);
    }

    //endregion

    //# Load / Save / Presets
    //region //############################################################################

    public static void initialize() {
        styleCfgFolder = new File(FileHandler.brandon3055Folder, "ProjectIntelligence/GuiStyle");
        if (!styleCfgFolder.exists() && !styleCfgFolder.mkdirs()) {
            PIGuiHelper.displayError("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
            LogHelper.error("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
            return;
        }

        defaultPresetsFolder = new File(styleCfgFolder, "DefaultPresets");
        if (!defaultPresetsFolder.exists() && !defaultPresetsFolder.mkdirs()) {
            PIGuiHelper.displayError("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
            LogHelper.error("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
            return;
        }
        customPresetsFolder = new File(styleCfgFolder, "CustomPresets");
        if (!customPresetsFolder.exists() && !customPresetsFolder.mkdirs()) {
            PIGuiHelper.displayError("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
            LogHelper.error("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
            return;
        }
        activeStyle = new File(styleCfgFolder, "ActiveStyle.json");
        if (!activeStyle.exists()) {
            saveStyle();
        }
        else {
            loadStyle();
        }
    }

    public static void loadStyle() {
        try {
            JsonParser parser = new JsonParser();
            FileReader reader = new FileReader(activeStyle);
            JsonElement element = parser.parse(reader);
            IOUtils.closeQuietly(reader);
            if (!element.isJsonObject()) {
                PIGuiHelper.displayError("Failed to load gui style. Detected invalid style config.");
                return;
            }

            JsonObject jObj = element.getAsJsonObject();
            unsavedChanges = JSONUtils.getBoolean(jObj, "unsavedChanges", false);
            rootPropertyMap.forEach((s, property) -> property.load(jObj));
        }
        catch (Exception e) {
            PIGuiHelper.displayError("Error loading gui style: " + e.getMessage());
            LogHelper.error("Error loading gui style");
            e.printStackTrace();
        }
    }

    public static void saveStyle() {
        try {
            JsonObject jObj = new JsonObject();
            jObj.addProperty("unsavedChanges", unsavedChanges);
            rootPropertyMap.forEach((s, property) -> property.save(jObj));

            JsonWriter writer = new JsonWriter(new FileWriter(activeStyle));
            writer.setIndent("  ");
            Streams.write(jObj, writer);
            writer.flush();
            IOUtils.closeQuietly(writer);
        }
        catch (Exception e) {
            PIGuiHelper.displayError("Error saving gui style: " + e.getMessage());
            LogHelper.error("Error saving gui style");
            e.printStackTrace();
        }
    }

    public static List<String> getDefaultPresets() {
        List<String> list = new LinkedList<>();

        File[] files = defaultPresetsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                list.add(file.getName().substring(0, file.getName().indexOf(".json")));
            }
        }

        return list;
    }

    public static List<String> getCustomPresets() {
        List<String> list = new LinkedList<>();

        File[] files = customPresetsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                list.add(file.getName().substring(0, file.getName().indexOf(".json")));
            }
        }

        return list;
    }

    public static void loadPreset(String name, boolean isCustom) {
        File preset = new File(isCustom ? customPresetsFolder : defaultPresetsFolder, name + ".json");
        try {
            JsonParser parser = new JsonParser();
            FileReader reader = new FileReader(preset);
            JsonElement element = parser.parse(reader);
            IOUtils.closeQuietly(reader);
            if (!element.isJsonObject()) {
                PIGuiHelper.displayError("Failed to load gui style. Detected invalid style config.");
                return;
            }

            JsonObject jObj = element.getAsJsonObject();
            rootPropertyMap.forEach((s, property) -> property.load(jObj));
            unsavedChanges = false;
            saveStyle();
        }
        catch (Exception e) {
            PIGuiHelper.displayError("Something went wrong while attempting to load the custom preset: " + e.getMessage());
            LogHelper.error("Something went wrong while attempting to load the custom preset");
            e.printStackTrace();
        }
    }

    public static void savePreset(String name) {
        File preset = new File(customPresetsFolder, name + ".json");

        try {
            JsonObject jObj = new JsonObject();
            rootPropertyMap.forEach((s, property) -> property.save(jObj));

            JsonWriter writer = new JsonWriter(new FileWriter(preset));
            writer.setIndent("  ");
            Streams.write(jObj, writer);
            writer.flush();
            IOUtils.closeQuietly(writer);

            unsavedChanges = false;
            saveStyle();
        }
        catch (Exception e) {
            PIGuiHelper.displayError("Something went wrong while attempting to save the custom preset: " + e.getMessage());
            LogHelper.error("Something went wrong while attempting to save the custom preset");
            e.printStackTrace();
        }
    }

    public static void deletePreset(String name) {
        File preset = new File(customPresetsFolder, name + ".json");
        if (preset.exists()) preset.delete();
    }

    //endregion

    //# Prop Classes
    //region //############################################################################

    public static class StyleProperty {
        public Map<String, StyleProperty> subProps = new LinkedHashMap<>();
        protected StyleType type;
        protected String path;
        public String tip = null;

        private StyleProperty(StyleType type) {
            this.type = type;
            this.path = type.getName();
        }

        public StyleProperty addSubProp(StyleProperty property) {
            property.path = path + "." + property.path;
            subProps.put(property.path, property);
            fullPropertyMap.put(property.path, property);
            return property;
        }

        public void save(JsonObject jObj) {
            if (!subProps.isEmpty()) {
                JsonObject subPropObj = new JsonObject();
                subProps.forEach((s, property) -> property.save(subPropObj));
                jObj.add(type.getName() + "_subs", subPropObj);
            }
        }

        public void load(JsonObject jObj) {
            if (!subProps.isEmpty() && jObj.has(type.getName() + "_subs") && jObj.get(type.getName() + "_subs").isJsonObject()) {
                JsonObject subPropObj = jObj.get(type.getName() + "_subs").getAsJsonObject();
                subProps.forEach((s, property) -> property.load(subPropObj));
            }
        }

        public boolean isBoolean() {
            return this instanceof BooleanProperty;
        }

        public boolean isInteger() {
            return this instanceof IntegerProperty;
        }

        public boolean isColour() {
            return this instanceof ColourProperty;
        }

        public boolean canSet() {
            return isBoolean() || isInteger() || isColour();
        }

        public StyleType getType() {
            return type;
        }

        public String getPath() {
            return path;
        }

        public StyleProperty setTip(String tip) {
            this.tip = tip;
            return this;
        }
    }

    public static class BooleanProperty extends StyleProperty {
        protected boolean value;

        private BooleanProperty(StyleType type, boolean defaultValue) {
            super(type);
            this.value = defaultValue;
        }

        @Override
        public void save(JsonObject jObj) {
            jObj.addProperty(type.getName(), value);
            super.save(jObj);
        }

        @Override
        public void load(JsonObject jObj) {
            value = JSONUtils.getBoolean(jObj, type.getName(), value);
            super.load(jObj);
        }

        public boolean get() {
            return value;
        }

        public void set(boolean value) {
            this.value = value;
            unsavedChanges = true;
            saveStyle();
        }
    }

    public static class IntegerProperty extends StyleProperty {
        protected int value;

        private IntegerProperty(StyleType type, int defaultValue) {
            super(type);
            this.value = defaultValue;
        }

        @Override
        public void save(JsonObject jObj) {
            jObj.addProperty(type.getName(), value);
            super.save(jObj);
        }

        @Override
        public void load(JsonObject jObj) {
            value = JSONUtils.getInt(jObj, type.getName(), value);
            super.load(jObj);
        }

        public int get() {
            return value;
        }

        public void set(int value) {
            this.value = value;
            unsavedChanges = true;
            saveStyle();
        }
    }

    public static class ColourProperty extends IntegerProperty {
        private Colour colour;
        public final boolean alpha;

        private ColourProperty(StyleType type, int defaultValue, boolean alpha) {
            super(type, defaultValue);
            this.alpha = alpha;
            this.colour = new ColourARGB(alpha ? value : 0xFF000000 | value);
        }

        @Override
        public void load(JsonObject jObj) {
            super.load(jObj);
            colour = new ColourARGB(alpha ? value : 0xFF000000 | value);
        }

        public Colour getColour() {
            if (!highlight.isEmpty() && path.startsWith(highlight)) {
                return new ColourARGB(getHighlightColour(colour.argb(), alpha));
            }
            return colour;
        }

        public int get() {
            int argb = alpha ? colour.argb() : colour.rgb();

            if (!highlight.isEmpty() && path.startsWith(highlight)) {
                return getHighlightColour(argb, alpha);
            }

            return argb;
        }

        public void set(Colour colour) {
            this.colour = colour;
            this.set(alpha ? colour.argb() : colour.rgb());
        }

        @Override
        public void set(int value) {
            super.set(value);
            colour.set(alpha ? value : 0xFF000000 | value);
            unsavedChanges = true;
            saveStyle();
        }
    }

    public static class PropertyGroup {
        private String baseProp;
        private ColourProperty colour = null;
        private ColourProperty hover = null;
        private ColourProperty background = null;
        private ColourProperty backgroundHover = null;
        private ColourProperty border = null;
        private ColourProperty borderHover = null;

        private ColourProperty textColour = null;
        private ColourProperty textColourHover = null;
        private BooleanProperty vanillaTex = null;
        private BooleanProperty thickBorders = null;
        private BooleanProperty shadeBorders = null;
        private BooleanProperty invertShade = null;
        private BooleanProperty textShadow = null;
        private BooleanProperty compact = null;

        /**
         * Warning! Should only ever be instantiated in a static context to avoid memory leaks.
         * Once instantiated the style handler will permanently hold a reference to this object.
         * */
        public PropertyGroup(String baseProp) {
            this.baseProp = baseProp;
            StyleHandler.addReloadListener(this::updateProps);
            updateProps();
        }

        public void updateProps() {
            //@formatter:off
            colour = hasProp(baseProp + COLOUR.ext()) ? getColourProp(baseProp + COLOUR.ext()) : null;

            hover = hasProp(baseProp + HOVER.ext()) ? getColourProp(baseProp + HOVER.ext()) : null;
            background = hasProp(baseProp + BACKGROUND.ext()) ? getColourProp(baseProp + BACKGROUND.ext()) : null;
            backgroundHover = hasProp(baseProp + BACKGROUND_HOVER.ext()) ? getColourProp(baseProp + BACKGROUND_HOVER.ext()) : null;
            border = hasProp(baseProp + BORDER.ext()) ? getColourProp(baseProp + BORDER.ext()) : null;
            borderHover = hasProp(baseProp + BORDER_HOVER.ext()) ? getColourProp(baseProp + BORDER_HOVER.ext()) : null;

            textColour = hasProp(baseProp + TEXT_COLOUR.ext()) ? getColourProp(baseProp + TEXT_COLOUR.ext()) : null;
            textColourHover = hasProp(baseProp + TEXT_HOVER.ext()) ? getColourProp(baseProp + TEXT_HOVER.ext()) : null;
            vanillaTex = hasProp(baseProp + VANILLA_TEXTURE.ext()) ? getBooleanProp(baseProp + VANILLA_TEXTURE.ext()) : null;

            /*if (hasProp(baseProp + SHADED_BORDERS.ext() + THICK_BORDERS.ext())) {
                thickBorders = getBooleanProp(baseProp + SHADED_BORDERS.ext() + THICK_BORDERS.ext());
            }
            else */
            if (hasProp(baseProp + THICK_BORDERS.ext())) {
                thickBorders = getBooleanProp(baseProp + THICK_BORDERS.ext());
            }
            else {
                thickBorders = null;
            }

            /*if (hasProp(baseProp + THICK_BORDERS.ext() + SHADED_BORDERS.ext())) {
                shadeBorders = getBooleanProp(baseProp + THICK_BORDERS.ext() + SHADED_BORDERS.ext());
            }
            else */
            if (hasProp(baseProp + SHADED_BORDERS.ext())) {
                shadeBorders = getBooleanProp(baseProp + SHADED_BORDERS.ext());
            }
            else {
                shadeBorders = null;
            }

            invertShade = hasProp(baseProp + SHADED_BORDERS.ext() + INVERT_SHADE.ext()) ? getBooleanProp(baseProp + SHADED_BORDERS.ext() + INVERT_SHADE.ext()) : null;

            textShadow = hasProp(baseProp + TEXT_SHADOW.ext()) ? getBooleanProp(baseProp + TEXT_SHADOW.ext()) : null;
            compact = hasProp(baseProp + COMPACT_BAR.ext()) ? getBooleanProp(baseProp + COMPACT_BAR.ext()) : null;

            //@formatter:on
        }

        public int colour() {
            if (colour == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined colour prop for " + baseProp);
                return 0;
            }
            return colour.get();
        }

        public boolean hasPropColour() {
            return colour != null;
        }

        public int colourHover() {
            if (hover == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined hover prop for " + baseProp);
                return 0;
            }
            return hover.get();
        }

        public boolean hasPropColourHover() {
            return hover != null;
        }

        public int colour(boolean hover) {
            return hover ? colourHover() : colour();
        }

        public void glColour() {
            if (colour == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined colour prop for " + baseProp);
            }
            else {
                colour.getColour().glColour();
            }
        }

        public void glColourHover() {
            if (hover == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined hover prop for " + baseProp);
            }
            else {
                hover.getColour().glColour();
            }
        }

        public void glColour(boolean hover) {
            if (hover) {
                glColourHover();
            }
            else {
                glColour();
            }
        }

        public int background() {
            if (background == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined background prop for " + baseProp);
                return 0;
            }
            return background.get();
        }

        public int backgroundHover() {
            if (backgroundHover == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined bgckgroundHover prop for " + baseProp);
                return 0;
            }
            return backgroundHover.get();
        }

        public int background(boolean hover) {
            return hover ? backgroundHover() : background();
        }

        public void glBackground() {
            if (background == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined background prop for " + baseProp);
            }
            else {
                background.
                        getColour().
                        glColour();
            }
        }

        public void glBackgroundHover() {
            if (backgroundHover == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined bgckgroundHover prop for " + baseProp);
            }
            else {
                backgroundHover.getColour().glColour();
            }
        }

        public void glBackground(boolean hover) {
            if (hover) {
                glBackgroundHover();
            }
            else {
                glBackground();
            }
        }

        public int border() {
            if (border == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined border prop for " + baseProp);
                return 0;
            }
            return border.get();
        }

        public boolean hasPropBorder() {
            return border != null;
        }

        public int borderHover() {
            if (borderHover == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined boderHover prop for " + baseProp);
                return 0;
            }
            return borderHover.get();
        }

        public boolean hasPropBorderHover() {
            return borderHover != null;
        }

        public int border(boolean hover) {
            return hover ? borderHover() : border();
        }

        public void glBorder() {
            if (border == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined border prop for " + baseProp);
            }
            else {
                border.getColour().glColour();
            }
        }

        public void glBorderHover() {
            if (borderHover == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined boderHover prop for " + baseProp);
            }
            else {
                borderHover.getColour().glColour();
            }
        }

        public void glBorder(boolean hover) {
            if (hover) {
                glBorderHover();
            }
            else {
                glBorder();
            }
        }

        public int textColour() {
            if (textColour == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined textColour prop for " + baseProp);
                return 0;
            }
            return textColour.get();
        }

        public int textColourHover() {
            if (textColourHover == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined textColourHover prop for " + baseProp);
                return 0;
            }
            return textColourHover.get();
        }

        public int textColour(boolean hover) {
            return hover ? textColourHover() : textColour();
        }

        public boolean hasPropTextColour() {
            return textColour != null;
        }

        public boolean hasPropTextColourHover() {
            return textColourHover != null;
        }

        public void glTextColour() {
            if (textColour == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined textColour prop for " + baseProp);
            }
            else {
                textColour.getColour().glColour();
            }
        }

        public void glTextColourHover() {
            if (textColourHover == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined textColourHover prop for " + baseProp);
            }
            else {
                textColourHover.getColour().glColour();
            }
        }

        public void glTextColour(boolean hover) {
            if (hover) {
                glTextColourHover();
            }
            else {
                glTextColour();
            }
        }

        public boolean vanillaTex() {
            if (vanillaTex == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined vanillaTex prop for " + baseProp);
                return false;
            }
            return vanillaTex.get();
        }

        public boolean hasPropVanillaTex() {
            return vanillaTex != null;
        }

        public boolean thickBorders() {
            if (thickBorders == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined thickBorders prop for " + baseProp);
                return false;
            }
            return thickBorders.get();
        }

        public boolean hasPropThickBorders() {
            return thickBorders != null;
        }

        public boolean shadeBorders() {
            if (shadeBorders == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined shadeBorders prop for " + baseProp);
                return false;
            }
            return shadeBorders.get();
        }

        public boolean hasPropShadeBorders() {
            return shadeBorders != null;
        }

        public boolean invertShade() {
            if (invertShade == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined shadeBorders prop for " + baseProp);
                return false;
            }
            return invertShade.get();
        }

        public boolean hasPropInvertShade() {
            return invertShade != null;
        }

        public boolean textShadow() {
            if (textShadow == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined textShadow prop for " + baseProp);
                return false;
            }
            return textShadow.get();
        }

        public boolean compact() {
            if (compact == null) {
                LogHelper.error("StyleHandler: Attempt to retrieve un-defined compact prop for " + baseProp);
                return false;
            }
            return compact.get();
        }
    }

    //endregion

    public enum StyleType {
        //Menu
        MENU("menu"),
        CLOSE_BUTTON("close_button"),
        SIZE_BUTTONS("size_buttons"),
        SETTINGS_BUTTON("settings_button"),
        USER_DIALOGS("user_dialogs"),
        SUB_ELEMENTS("sub_elements"),
//        PRESET_WINDOW("preset_window"),

        //Page List
        PAGE_LIST("page_list"),
        PAGE_ICON("page_icon"),
        PAGE_BUTTONS("page_buttons"),
        PAGE_BACK_BUTTON("page_back_button"),
        HIDE_BUTTON("hide_button"),
        DIR_PATH("dir_path"),
        DIR_BUTTONS("dir_buttons"),

        //Markdown Window
        MD_WINDOW("md_window"),

        GUI_DOCS("gui_docs"),

        //Generic Colour
        COLOUR("colour"),
        HOVER("hover"),
        BACKGROUND("background"), //Not currently used. Meant for the background of things like the settings, exit, size buttons.
        BACKGROUND_HOVER("background_hover"),
        BORDER("border"),
        BORDER_HOVER("border_hover"),

        //General
        VANILLA_TEXTURE("vanilla_tex"),
        THICK_BORDERS("thick_borders"),
        SHADED_BORDERS("shaded_borders"),
        INVERT_SHADE("invert_shade"),
        BUTTON_STYLE("button_style"),
        TEXT_COLOUR("text_colour"),
        TEXT_HOVER("text_hover"),
        TEXT_SHADOW("text_shadow"),
        HEADER("header"),
        BODY("body"),
        FOOTER("footer"),
        SEARCH("search"),
        SCROLL_BAR("scroll_bar"),
        SCROLL_SLIDER("scroll_slider"),
        COMPACT_BAR("small_bar"),
        ICON("icon");

        private String name;

        StyleType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String ext() {
            return "." + name;
        }

        public String pre() {
            return name + ".";
        }
    }
}
