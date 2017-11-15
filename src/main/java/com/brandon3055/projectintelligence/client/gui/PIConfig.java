package com.brandon3055.projectintelligence.client.gui;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.projectintelligence.PIHelpers;
import com.brandon3055.projectintelligence.client.gui.guielements.GuiPartMenu;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.JsonUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class PIConfig {

    public static File configFile;

    //Edit mode will be directly tied to the repo location. When you enable edit mode you will be asked to confirm or set the repo location.
    //If the specified repo location is invalid edit mode will be disabled.
    private static boolean editMode = false;
    public static volatile String editingRepoLoc = "";

    public static int screenMode = 0;
    public static boolean screenPosOverride = false;
    public static int screenPosX = 0;
    public static int screenPosY = 0;

    //editor Config
    public static volatile boolean editorAlwaysOnTop = false;
    public static Map<String, String> modVersionOverrides = new HashMap<>();

    //language
    public static String userLanguage = "[MINECRAFT-LANG]";
    public static Map<String, String> pageLangOverrides = new HashMap<>();
    public static Map<String, String> modLangOverrides = new HashMap<>();


    public static void initialize() {
        File piFolder = new File(FileHandler.brandon3055Folder, "ProjectIntelligence");
        configFile = new File(piFolder, "GuiConfig.json");

        if (!configFile.exists()) {
            save();
        }
        load();
    }

    public static synchronized void save() {
        JsonObject jObj = new JsonObject();

        jObj.addProperty("editMode", editMode);
        jObj.addProperty("editingRepoLoc", editingRepoLoc);
        jObj.addProperty("editorAlwaysOnTop", editorAlwaysOnTop);

        jObj.addProperty("screenMode", screenMode);
        jObj.addProperty("screenPosOverride", screenPosOverride);
        jObj.addProperty("screenPosX", screenPosX);
        jObj.addProperty("screenPosY", screenPosY);

        jObj.addProperty("userLanguage", userLanguage);

        JsonObject langOverrides = new JsonObject();
        pageLangOverrides.forEach(langOverrides::addProperty);
        jObj.add("pageLangOverrides", langOverrides);

        JsonObject modLangOverrideList = new JsonObject();
        modLangOverrides.forEach(modLangOverrideList::addProperty);
        jObj.add("modLangOverrides", modLangOverrideList);

        JsonObject modVersionOverrideList = new JsonObject();
        modVersionOverrides.forEach(modVersionOverrideList::addProperty);
        jObj.add("modVersionOverrides", modVersionOverrideList);

        //region Save JsonObject
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(configFile));
            writer.setIndent("  ");
            Streams.write(jObj, writer);
            writer.flush();
            IOUtils.closeQuietly(writer);
        }
        catch (Exception e) {
            PIHelpers.displayError("Error saving gui config: " + e.getMessage());
            LogHelper.error("Error saving gui config");
            e.printStackTrace();
        }
        //endregion
    }

    public static void load() {
        JsonObject jObj;
        //region Load JsonObject
        try {
            JsonParser parser = new JsonParser();
            FileReader reader = new FileReader(configFile);
            JsonElement element = parser.parse(reader);
            IOUtils.closeQuietly(reader);
            if (!element.isJsonObject()) {
                PIHelpers.displayError("Failed to load gui config. Detected invalid config file.");
                return;
            }
            jObj = element.getAsJsonObject();
        }
        catch (Exception e) {
            PIHelpers.displayError("Error loading gui config: " + e.getMessage());
            LogHelper.error("Error loading gui config");
            e.printStackTrace();
            return;
        }
        //endregion

        editMode = JsonUtils.getBoolean(jObj, "editMode", false);
        editingRepoLoc = JsonUtils.getString(jObj, "editingRepoLoc", "");
        editorAlwaysOnTop = JsonUtils.getBoolean(jObj, "editorAlwaysOnTop", false);

        screenMode = JsonUtils.getInt(jObj, "screenMode", screenMode);
        screenPosOverride = JsonUtils.getBoolean(jObj, "screenPosOverride", false);
        screenPosX = JsonUtils.getInt(jObj, "screenPosX", 0);
        screenPosY = JsonUtils.getInt(jObj, "screenPosY", 0);

        userLanguage = JsonUtils.getString(jObj, "userLanguage", "[MINECRAFT-LANG]");

        pageLangOverrides.clear();
        if (jObj.has("pageLangOverrides") && jObj.get("pageLangOverrides").isJsonObject()) {
            JsonObject langOverrides = JsonUtils.getJsonObject(jObj, "pageLangOverrides");
            langOverrides.entrySet().forEach(entry -> pageLangOverrides.put(entry.getKey(), entry.getValue().getAsJsonPrimitive().getAsString()));
        }

        modLangOverrides.clear();
        if (jObj.has("modLangOverrides") && jObj.get("modLangOverrides").isJsonObject()) {
            JsonObject langOverrides = JsonUtils.getJsonObject(jObj, "modLangOverrides");
            langOverrides.entrySet().forEach(entry -> modLangOverrides.put(entry.getKey(), entry.getValue().getAsJsonPrimitive().getAsString()));
        }

        modVersionOverrides.clear();
        if (jObj.has("modVersionOverrides") && jObj.get("modVersionOverrides").isJsonObject()) {
            JsonObject versionOverrides = JsonUtils.getJsonObject(jObj, "modVersionOverrides");
            versionOverrides.entrySet().forEach(entry -> modVersionOverrides.put(entry.getKey(), entry.getValue().getAsJsonPrimitive().getAsString()));
        }
    }

    public static void setEditMode(boolean editMode) {
        PIConfig.editMode = editMode;

        GuiPartMenu menu = GuiProjectIntelligence.getMenuPart();
        if (menu != null && menu.configUI != null) {
            menu.configUI.reloadConfigProperties();
        }
    }

    public static synchronized boolean editMode() {
        return editMode;
    }

    //region old

    public static boolean drawEditInfo = true;

    //Windows
    public static volatile int NAV_WINDOW = 0xFF3c3f41;
    public static volatile int CONTENT_WINDOW = 0xFF3c3f41;
    public static volatile int MENU_BAR = 0xFF3c3f41;

    //Text
    public static volatile int NAV_TEXT = 0x00FFFF;
    public static volatile int TEXT_COLOUR = 0x8c8c8c;
    //endregion
}
