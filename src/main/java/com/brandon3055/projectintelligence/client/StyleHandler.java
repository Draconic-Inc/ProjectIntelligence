package com.brandon3055.projectintelligence.client;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.projectintelligence.PIHelpers;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.brandon3055.projectintelligence.client.StyleHandler.StyleType.*;

/**
 * Created by brandon3055 on 12/08/2017.
 */
public class StyleHandler {

    public static boolean unsavedChanges = false;

    private static File styleCfgFolder;
    private static File defaultPresetsFolder;
    private static File customPresetsFolder;
    private static File activeStyle;
    private static Map<String, StyleProperty> fullPropertyMap = new LinkedHashMap<>();
    private static Map<String, StyleProperty> rootPropertyMap = new LinkedHashMap<>();

    static {reloadStyleProperties();}

    public static void reloadStyleProperties() {
        fullPropertyMap.clear();
        rootPropertyMap.clear();

        StyleProperty menu = addProperty(new StyleProperty(MENU));
        //############################################################################
        //# Menu
        //region //############################################################################

        menu.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
        menu.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        menu.addSubProp(new BooleanProperty(SHADED_BORDERS, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info")));
        menu.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        menu.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        menu.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));

        StyleProperty closeButton = menu.addSubProp(new StyleProperty(CLOSE_BUTTON));
        closeButton.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, false));
        closeButton.addSubProp(new ColourProperty(HOVER, 0xFFFF0000, false));
//        closeButton.addSubProp(new ColourProperty(BACKGROUND, 0, true));
//        closeButton.addSubProp(new ColourProperty(BACKGROUND_HOVER, 0, true));
//        closeButton.addSubProp(new ColourProperty(BORDER, 0, true));
//        closeButton.addSubProp(new ColourProperty(BORDER_HOVER, 0xFFFF0000, true));

        StyleProperty sizeButtons = menu.addSubProp(new StyleProperty(SIZE_BUTTONS));
        sizeButtons.addSubProp(new ColourProperty(COLOUR, 0xFFA0A0A0, false));
        sizeButtons.addSubProp(new ColourProperty(HOVER, 0xFFC0C0C0, false));
//        sizeButtons.addSubProp(new ColourProperty(BACKGROUND, 0, true));
//        sizeButtons.addSubProp(new ColourProperty(BACKGROUND_HOVER, 0, true));
//        sizeButtons.addSubProp(new ColourProperty(BORDER, 0, true));
//        sizeButtons.addSubProp(new ColourProperty(BORDER_HOVER, 0, true));

        StyleProperty settingsButton = menu.addSubProp(new StyleProperty(SETTINGS_BUTTON));
        settingsButton.addSubProp(new ColourProperty(COLOUR, 0xFFA0A0A0, false));
        settingsButton.addSubProp(new ColourProperty(HOVER, 0xFFC0C0C0, false));
//        settingsButton.addSubProp(new ColourProperty(BACKGROUND, 0, true));
//        settingsButton.addSubProp(new ColourProperty(BACKGROUND_HOVER, 0, true));
//        settingsButton.addSubProp(new ColourProperty(BORDER, 0, true));
//        settingsButton.addSubProp(new ColourProperty(BORDER_HOVER, 0, true));

        //endregion

        StyleProperty pageList = addProperty(new StyleProperty(PAGE_LIST));
        //############################################################################
        //# Page List
        //region //############################################################################

        StyleProperty toggleHidden = pageList.addSubProp(new StyleProperty(HIDE_BUTTON));
        toggleHidden.addSubProp(new ColourProperty(COLOUR, 0xFFA0A0A0, false));
        toggleHidden.addSubProp(new ColourProperty(HOVER, 0xFFC0C0C0, false));

        StyleProperty header = pageList.addSubProp(new StyleProperty(HEADER));
        header.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
        header.addSubProp(new BooleanProperty(SHADED_BORDERS, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(THICK_BORDERS, false));
        header.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        header.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        header.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));

        StyleProperty body = pageList.addSubProp(new StyleProperty(BODY));
        body.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
        body.addSubProp(new BooleanProperty(SHADED_BORDERS, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(THICK_BORDERS, false));
        body.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        body.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        body.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));

        StyleProperty footer = pageList.addSubProp(new StyleProperty(FOOTER));
        footer.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
        footer.addSubProp(new BooleanProperty(SHADED_BORDERS, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(THICK_BORDERS, false));
        footer.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        footer.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        footer.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));

        StyleProperty pageButtons = pageList.addSubProp(new StyleProperty(PAGE_BUTTONS));
        pageButtons.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
        pageButtons.addSubProp(new BooleanProperty(SHADED_BORDERS, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(THICK_BORDERS, false));
        pageButtons.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        pageButtons.addSubProp(new ColourProperty(HOVER, 0xFFa0a0a0, true));
        pageButtons.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        pageButtons.addSubProp(new ColourProperty(BORDER_HOVER, 0x00000000, true));
        pageButtons.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));
        pageButtons.addSubProp(new ColourProperty(TEXT_HOVER, 0x000000, false));
        pageButtons.addSubProp(new BooleanProperty(TEXT_SHADOW, true));
//        StyleProperty pageIcon = pageButtons.addSubProp(new StyleProperty(PAGE_ICON));
//        pageIcon.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
//        pageIcon.addSubProp(new ColourProperty(BACKGROUND, 0xFFa0a0a0, true));
//        pageIcon.addSubProp(new ColourProperty(BORDER, 0x00000000, true));

        StyleProperty dirButtons = pageList.addSubProp(new StyleProperty(DIR_BUTTONS));
        dirButtons.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
//        dirButtons.addSubProp(new BooleanProperty(SHADED_BORDERS, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
//                .addSubProp(new BooleanProperty(THICK_BORDERS, false));
        dirButtons.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        dirButtons.addSubProp(new ColourProperty(HOVER, 0xFFa0a0a0, true));
        dirButtons.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        dirButtons.addSubProp(new ColourProperty(BORDER_HOVER, 0x00000000, true));
        dirButtons.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));
        dirButtons.addSubProp(new ColourProperty(TEXT_HOVER, 0x000000, false));
//        dirButtons.addSubProp(new BooleanProperty(TEXT_SHADOW, true));


        StyleProperty scrollBar = pageList.addSubProp(new StyleProperty(SCROLL_BAR));
        scrollBar.addSubProp(new BooleanProperty(COMPACT_BAR, true));
        scrollBar.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        scrollBar.addSubProp(new ColourProperty(HOVER, 0xFFa0a0a0, true));
        scrollBar.addSubProp(new ColourProperty(BORDER, 0xFFa0a0a0, true));
        scrollBar.addSubProp(new ColourProperty(BORDER_HOVER, 0xFFa0a0a0, true));
        StyleProperty scrollSlider = scrollBar.addSubProp(new StyleProperty(SCROLL_SLIDER));
        scrollSlider.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        scrollSlider.addSubProp(new ColourProperty(HOVER, 0xFFa0a0a0, true));
        scrollSlider.addSubProp(new ColourProperty(BORDER, 0xFFa0a0a0, true));
        scrollSlider.addSubProp(new ColourProperty(BORDER_HOVER, 0xFFa0a0a0, true));

        //endregion

        StyleProperty mdWindow = addProperty(new StyleProperty(MD_WINDOW));
        //############################################################################
        //# Markdown Window
        //region //############################################################################

        header = mdWindow.addSubProp(new StyleProperty(HEADER));
        header.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
        header.addSubProp(new BooleanProperty(SHADED_BORDERS, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(THICK_BORDERS, false));
        header.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        header.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        header.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));

        body = mdWindow.addSubProp(new StyleProperty(BODY));
        body.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
        body.addSubProp(new BooleanProperty(SHADED_BORDERS, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info"))) //
                .addSubProp(new BooleanProperty(THICK_BORDERS, false));
        body.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        body.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        body.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));

//        mdWindow.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
////        mdWindow.addSubProp(new BooleanProperty(THICK_BORDERS, false));
//        mdWindow.addSubProp(new BooleanProperty(SHADED_BORDERS, false).setTip(TextFormatting.DARK_GRAY + I18n.format("pi.style.shade_borders.info")));
//        mdWindow.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
//        mdWindow.addSubProp(new ColourProperty(BORDER, 0x00000000, true));

        scrollBar = mdWindow.addSubProp(new StyleProperty(SCROLL_BAR));
        scrollBar.addSubProp(new BooleanProperty(COMPACT_BAR, true));
        scrollBar.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        scrollBar.addSubProp(new ColourProperty(HOVER, 0xFFa0a0a0, true));
        scrollBar.addSubProp(new ColourProperty(BORDER, 0xFFa0a0a0, true));
        scrollBar.addSubProp(new ColourProperty(BORDER_HOVER, 0xFFa0a0a0, true));
        scrollSlider = scrollBar.addSubProp(new StyleProperty(SCROLL_SLIDER));
        scrollSlider.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        scrollSlider.addSubProp(new ColourProperty(HOVER, 0xFFa0a0a0, true));
        scrollSlider.addSubProp(new ColourProperty(BORDER, 0xFFa0a0a0, true));
        scrollSlider.addSubProp(new ColourProperty(BORDER_HOVER, 0xFFa0a0a0, true));

        //endregion

        StyleProperty dialogs = addProperty(new StyleProperty(USER_DIALOGS));
        //############################################################################
        //# Dialogs
        //region //############################################################################

        //General Layout
        dialogs.addSubProp(new BooleanProperty(VANILLA_TEXTURE, false));
        dialogs.addSubProp(new BooleanProperty(THICK_BORDERS, false));
        dialogs.addSubProp(new ColourProperty(COLOUR, 0xFFa0a0a0, true));
        dialogs.addSubProp(new ColourProperty(BORDER, 0x00000000, true));
        dialogs.addSubProp(new ColourProperty(TEXT_COLOUR, 0x000000, false));

        //Buttons
        StyleProperty editorButtons = dialogs.addSubProp(new StyleProperty(BUTTON_STYLE));
        editorButtons.addSubProp(new BooleanProperty(VANILLA_TEXTURE, true));
        editorButtons.addSubProp(new ColourProperty(COLOUR, 0xFFFFFFFF, true));
        editorButtons.addSubProp(new ColourProperty(HOVER, 0xFFFFFFFF, true));
        editorButtons.addSubProp(new ColourProperty(BORDER, 0xFF000000, true));
        editorButtons.addSubProp(new ColourProperty(BORDER_HOVER, 0xFF000000, true));
        editorButtons.addSubProp(new ColourProperty(TEXT_COLOUR, 14737632, false));
        editorButtons.addSubProp(new ColourProperty(TEXT_HOVER, 16777120, false));

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
        presetButtons.addSubProp(new ColourProperty(BORDER, 0xFF000000, true));
        presetButtons.addSubProp(new ColourProperty(BORDER_HOVER, 0xFF000000, true));
        presetButtons.addSubProp(new ColourProperty(TEXT_COLOUR, 14737632, false));
        presetButtons.addSubProp(new ColourProperty(TEXT_HOVER, 16777120, false));


        //endregion
    }

    private static StyleProperty addProperty(StyleProperty property) {
        fullPropertyMap.put(property.path, property);
        rootPropertyMap.put(property.path, property);
        return property;
    }

    public static Map<String, StyleProperty> getPropertyMap() {
        return ImmutableMap.copyOf(rootPropertyMap);
    }

    //############################################################################
    //# Getter's & Setters
    //region //############################################################################

    public static boolean getBoolean(String prop) {
        StyleProperty styleProperty = fullPropertyMap.get(prop);
        if (styleProperty != null && styleProperty.isBoolean()) {
            return ((BooleanProperty) styleProperty).getValue();
        }
        LogHelper.error("StyleHandler: Attempt to retrieve un-defined boolean: " + prop);
        return false;
    }

    public static void setBoolean(String prop, boolean value) {
        if (fullPropertyMap.containsKey(prop) && fullPropertyMap.get(prop).isBoolean()) {
            ((BooleanProperty) fullPropertyMap.get(prop)).setValue(value);
            saveStyle();
        }
        LogHelper.error("StyleHandler: Attempt to set un-defined boolean: " + prop);
    }

    public static int getInt(String prop) {
        StyleProperty styleProperty = fullPropertyMap.get(prop);
        if (styleProperty != null && styleProperty.isInteger()) {
            return ((IntegerProperty) styleProperty).getValue();
        }
        LogHelper.error("StyleHandler: Attempt to retrieve un-defined integer: " + prop);
        return 0;
    }

    public static void setInt(String prop, int value) {
        if (fullPropertyMap.containsKey(prop) && fullPropertyMap.get(prop).isInteger()) {
            ((IntegerProperty) fullPropertyMap.get(prop)).setValue(value);
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
            ((ColourProperty) fullPropertyMap.get(prop)).setColour(colour);
            saveStyle();
        }
        LogHelper.error("StyleHandler: Attempt to set un-defined colour: " + prop);
    }

    public static boolean hasProp(String prop) {
        return fullPropertyMap.containsKey(prop);
    }

    //endregion

    //############################################################################
    //# Load / Save / Presets
    //region //############################################################################

    public static void initialize() {
        styleCfgFolder = new File(FileHandler.brandon3055Folder, "ProjectIntelligence/GuiStyle");
        if (!styleCfgFolder.exists() && !styleCfgFolder.mkdirs()) {
            PIHelpers.displayError("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
            LogHelper.error("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
            return;
        }

        defaultPresetsFolder = new File(styleCfgFolder, "DefaultPresets");
        if (!defaultPresetsFolder.exists() && !defaultPresetsFolder.mkdirs()) {
            PIHelpers.displayError("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
            LogHelper.error("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
            return;
        }
        customPresetsFolder = new File(styleCfgFolder, "CustomPresets");
        if (!customPresetsFolder.exists() && !customPresetsFolder.mkdirs()) {
            PIHelpers.displayError("Failed to create GuiStyle folder! Gui styles will be broken! " + styleCfgFolder);
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
                PIHelpers.displayError("Failed to load gui style. Detected invalid style config.");
                return;
            }

            JsonObject jObj = element.getAsJsonObject();
            unsavedChanges = JsonUtils.getBoolean(jObj, "unsavedChanges", false);
            rootPropertyMap.forEach((s, property) -> property.load(jObj));
        }
        catch (Exception e) {
            PIHelpers.displayError("Error loading gui style: " + e.getMessage());
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
            PIHelpers.displayError("Error saving gui style: " + e.getMessage());
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
                PIHelpers.displayError("Failed to load gui style. Detected invalid style config.");
                return;
            }

            JsonObject jObj = element.getAsJsonObject();
            rootPropertyMap.forEach((s, property) -> property.load(jObj));
            unsavedChanges = false;
            saveStyle();
        }
        catch (Exception e) {
            PIHelpers.displayError("Something went wrong while attempting to load the custom preset: " + e.getMessage());
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
            PIHelpers.displayError("Something went wrong while attempting to save the custom preset: " + e.getMessage());
            LogHelper.error("Something went wrong while attempting to save the custom preset");
            e.printStackTrace();
        }
    }

    public static void deletePreset(String name) {
        File preset = new File(customPresetsFolder, name + ".json");
        if (preset.exists()) preset.delete();
    }

    //endregion

    //############################################################################
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

        public boolean isBoolean() { return this instanceof BooleanProperty; }

        public boolean isInteger() { return this instanceof IntegerProperty; }

        public boolean isColour() { return this instanceof ColourProperty; }

        public boolean canSet() { return isBoolean() || isInteger() || isColour(); }

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
            value = JsonUtils.getBoolean(jObj, type.getName(), value);
            super.load(jObj);
        }

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
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
            value = JsonUtils.getInt(jObj, type.getName(), value);
            super.load(jObj);
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
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
            return colour;
        }

        public void setColour(Colour colour) {
            this.colour = colour;
            this.setValue(alpha ? colour.argb() : colour.rgb());
        }

        @Override
        public void setValue(int value) {
            super.setValue(value);
            colour.set(alpha ? value : 0xFF000000 | value);
            unsavedChanges = true;
            saveStyle();
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
        HIDE_BUTTON("hide_button"),
        DIR_BUTTONS("dir_buttons"),

        //Markdown Window
        MD_WINDOW("md_window"),

        //Generic Colour
        COLOUR("colour"),
        HOVER("hover"),
        BACKGROUND("background"),
        BACKGROUND_HOVER("background_hover"),
        BORDER("border"),
        BORDER_HOVER("border_hover"),

        //General
        VANILLA_TEXTURE("vanilla_tex"),
        THICK_BORDERS("thick_borders"),
        SHADED_BORDERS("shaded_borders"),
        BUTTON_STYLE("button_style"),
        TEXT_COLOUR("text_colour"),
        TEXT_HOVER("text_hover"),
        TEXT_SHADOW("text_shadow"),
        HEADER("header"),
        BODY("body"),
        FOOTER("footer"),
        SCROLL_BAR("scroll_bar"),
        SCROLL_SLIDER("scroll_slider"),
        COMPACT_BAR("small_bar"),

        TEST_INT("test_int"),
        TEST_BOOL("test_bool");

        private String name;

        StyleType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
