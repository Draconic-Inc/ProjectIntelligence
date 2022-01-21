package com.brandon3055.projectintelligence.client.gui;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.projectintelligence.api.PiAPI;
import com.brandon3055.projectintelligence.client.DisplayController;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.gui.guielements.GuiPartMenu;
import com.brandon3055.projectintelligence.docmanagement.DocumentationManager;
import com.brandon3055.projectintelligence.docmanagement.PIUpdateManager;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.JSONUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by brandon3055 on 3/09/2016.
 */
//TODO switch to instance based config
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

    //Misc config
    public static boolean etCheckFluid = true;

    //editor Config
    public static volatile boolean editorAlwaysOnTop = false;
    public static volatile boolean editorLineWrap = false;
    public static Map<String, String> modVersionOverrides = new HashMap<>();
    public static int maxTabs = 16;
    public static volatile String editorLAF = "";

    //language
    public static String userLanguage = "[MINECRAFT-LANG]";
    public static Map<String, String> pageLangOverrides = new HashMap<>();
    public static Map<String, String> modLangOverrides = new HashMap<>();

    //Search Config
    public static SearchMode searchMode = SearchMode.EVERYWHERE;

    public static boolean tutorialDisplayed = false;
    public static boolean downloadsAllowed = false;
    public static boolean showTutorialLater = false;


    public static String homePage = "projectintelligence:";

    public static void initialize() {
        File piFolder = new File(FileHandler.brandon3055Folder, "ProjectIntelligence");
        configFile = new File(piFolder, "GuiConfig.json");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            save();
        }
        load();
    }

    public static synchronized void save() {
        JsonObject jObj = new JsonObject();

        jObj.addProperty("downloadsAllowed", downloadsAllowed);
        jObj.addProperty("tutorialDisplayed", tutorialDisplayed);
        jObj.addProperty("editMode", editMode);
        jObj.addProperty("etCheckFluid", etCheckFluid);
        jObj.addProperty("editingRepoLoc", editingRepoLoc);
        jObj.addProperty("editorAlwaysOnTop", editorAlwaysOnTop);
        jObj.addProperty("editorLineWrap", editorLineWrap);
        jObj.addProperty("maxTabs", maxTabs);
        jObj.addProperty("editorLAF", editorLAF);

        jObj.addProperty("screenMode", screenMode);
        jObj.addProperty("screenPosOverride", screenPosOverride);
        jObj.addProperty("screenPosX", screenPosX);
        jObj.addProperty("screenPosY", screenPosY);

        jObj.addProperty("userLanguage", userLanguage);

        jObj.addProperty("homePage", homePage);

        jObj.addProperty("searchMode", searchMode.name());

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
            PIGuiHelper.displayError("Error saving gui config: " + e.getMessage());
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
                PIGuiHelper.displayError("Failed to load gui config. Detected invalid config file.");
                return;
            }
            jObj = element.getAsJsonObject();
        }
        catch (Exception e) {
            PIGuiHelper.displayError("Error loading gui config: " + e.getMessage());
            LogHelper.error("Error loading gui config");
            e.printStackTrace();
            return;
        }
        //endregion

        downloadsAllowed = JSONUtils.getAsBoolean(jObj, "downloadsAllowed", false);
        tutorialDisplayed = JSONUtils.getAsBoolean(jObj, "tutorialDisplayed", false);
        editMode = JSONUtils.getAsBoolean(jObj, "editMode", false);
        etCheckFluid = JSONUtils.getAsBoolean(jObj, "etCheckFluid", true);
        editingRepoLoc = JSONUtils.getAsString(jObj, "editingRepoLoc", "");
        editorAlwaysOnTop = JSONUtils.getAsBoolean(jObj, "editorAlwaysOnTop", false);
        editorLineWrap = JSONUtils.getAsBoolean(jObj, "editorLineWrap", true);
        maxTabs = JSONUtils.getAsInt(jObj, "maxTabs", 16);
        editorLAF = JSONUtils.getAsString(jObj, "editorLAF", "");

        screenMode = JSONUtils.getAsInt(jObj, "screenMode", screenMode);
        screenPosOverride = JSONUtils.getAsBoolean(jObj, "screenPosOverride", false);
        screenPosX = JSONUtils.getAsInt(jObj, "screenPosX", 0);
        screenPosY = JSONUtils.getAsInt(jObj, "screenPosY", 0);

        userLanguage = JSONUtils.getAsString(jObj, "userLanguage", "[MINECRAFT-LANG]");

        homePage = JSONUtils.getAsString(jObj, "homePage", "projectintelligence:");

        try {
            searchMode = SearchMode.valueOf(JSONUtils.getAsString(jObj, "searchMode", SearchMode.EVERYWHERE.name()));
        }
        catch (Exception e) {
            LogHelper.error("Detected invalid search mode in PI Config! Default mode will be used.");
            e.printStackTrace();
        }

        pageLangOverrides.clear();
        if (jObj.has("pageLangOverrides") && jObj.get("pageLangOverrides").isJsonObject()) {
            JsonObject langOverrides = JSONUtils.getAsJsonObject(jObj, "pageLangOverrides");
            langOverrides.entrySet().forEach(entry -> pageLangOverrides.put(entry.getKey(), entry.getValue().getAsJsonPrimitive().getAsString()));
        }

        modLangOverrides.clear();
        if (jObj.has("modLangOverrides") && jObj.get("modLangOverrides").isJsonObject()) {
            JsonObject langOverrides = JSONUtils.getAsJsonObject(jObj, "modLangOverrides");
            langOverrides.entrySet().forEach(entry -> modLangOverrides.put(entry.getKey(), entry.getValue().getAsJsonPrimitive().getAsString()));
        }

        modVersionOverrides.clear();
        if (jObj.has("modVersionOverrides") && jObj.get("modVersionOverrides").isJsonObject()) {
            JsonObject versionOverrides = JSONUtils.getAsJsonObject(jObj, "modVersionOverrides");
            versionOverrides.entrySet().forEach(entry -> modVersionOverrides.put(entry.getKey(), entry.getValue().getAsJsonPrimitive().getAsString()));
        }
    }

    public static void setHomePage(String homePage) {
        PIConfig.homePage = homePage;
        save();
    }

    public static void setEditMode(boolean editMode) {
        PIConfig.editMode = editMode;

        if (editMode && PIUpdateManager.updateStage != PIUpdateManager.UpdateStage.INACTIVE) {
            PIUpdateManager.updateStage = PIUpdateManager.UpdateStage.INACTIVE; //Prevents update ui getting stuck on the screen.
        }

        GuiPartMenu menu = GuiProjectIntelligence.getMenuPart();
        if (menu != null && menu.configUI != null) {
            menu.configUI.reloadConfigProperties();
        }
    }

    public static synchronized boolean editMode() {
        return editMode;
    }

    public static void deleteConfigAndReload() {
        File piFolder = new File(FileHandler.brandon3055Folder, "ProjectIntelligence");
        try {
            FileUtils.deleteDirectory(piFolder);
            DocumentationManager.clear();

            //So this is why i want to switch to an instance based config...
            editMode = false;
            editingRepoLoc = "";
            screenMode = 0;
            screenPosOverride = false;
            screenPosX = 0;
            screenPosY = 0;
            etCheckFluid = true;
            editorAlwaysOnTop = false;
            editorLineWrap = false;
            modVersionOverrides = new HashMap<>();
            maxTabs = 16;
            editorLAF = "";
            userLanguage = "[MINECRAFT-LANG]";
            pageLangOverrides = new HashMap<>();
            modLangOverrides = new HashMap<>();
            searchMode = SearchMode.EVERYWHERE;
            tutorialDisplayed = false;
            downloadsAllowed = false;
            showTutorialLater = false;
            initialize();
            DocumentationManager.initialize();
            DisplayController.MASTER_CONTROLLER.clear();
            DLRSCache.clearFileCache();
            PiAPI.openGui(null);
        }
        catch (IOException e) {
            e.printStackTrace();
            PIGuiHelper.displayError(e.getMessage());
        }
    }

    //endregion

    public static enum SearchMode {
        EVERYWHERE,
        SELECTED_MOD,
        PAGE_SUB_PAGES,
        PAGE_ONLY;

        public String getUnlocalizedName() {
            return "pi.search.mode." + name().toLowerCase(Locale.ENGLISH);
        }
    }
}
