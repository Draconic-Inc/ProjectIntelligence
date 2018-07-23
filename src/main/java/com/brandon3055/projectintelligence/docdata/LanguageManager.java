package com.brandon3055.projectintelligence.docdata;

import com.brandon3055.projectintelligence.PIHelpers;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by brandon3055 on 11/10/2017.
 */
public class LanguageManager {

    public static final String DEFAULT_LANG = "en_us";
    public static final HashSet<String> ALL_LANGUAGES;
    /**
     * Maps all languages to their human readable names
     */
    public static final Map<String, String> LANG_NAME_MAP = new HashMap<>();

    static {
        List<String> langs = new LinkedList<>();
        net.minecraft.client.resources.LanguageManager langManager = Minecraft.getMinecraft().getLanguageManager();
        langManager.getLanguages().forEach(language -> {
            langs.add(language.getLanguageCode());
            LANG_NAME_MAP.put(language.getLanguageCode(), language.toString());
        });
        Collections.sort(langs);
        ALL_LANGUAGES = new LinkedHashSet<>(langs);
    }

    /**
     * This map is used to store all name localization for doc pages.
     * Map Structure is <modid, <lang, <pageURI, translation>>>
     */
    public static Map<String, Map<String, Map<String, PageLangData>>> modLangPageTranslationMap = new HashMap<>();
    /**
     * This map is just for more efficient lookups.
     * Map Structure is <pageURI, <lang, translation>>
     */
    public static Map<String, Map<String, PageLangData>> pageLangTranslationMap = new HashMap<>();

    public static boolean isLangUsedByMod(String lang, String mod) {
        if (lang.equals(DEFAULT_LANG) || lang.equals(getUserLanguage())) {
            return true;
        }

        if (PIConfig.modLangOverrides.containsKey(mod) && PIConfig.modLangOverrides.get(mod).equals(lang)) {
            return true;
        }

        for (Map.Entry<String, String> entry : PIConfig.pageLangOverrides.entrySet()) {
            if (entry.getKey().startsWith(mod + ":") && entry.getValue().equals(lang)) {
                return true;
            }
        }

        return false;
    }

    //# Page Localization
    //region //############################################################################

    /**
     * Clears the translation map (Called before doc files are reloaded)
     */
    public static void clearTranslations() {
        modLangPageTranslationMap.clear();
    }

    /**
     * Reloads the language lookup map (Called after all language files have been read)
     */
    public static void reloadLookupMap() {
        pageLangTranslationMap.clear();
        modLangPageTranslationMap.forEach((mod, langPageTransMap) -> langPageTransMap.forEach((lang, pageTransMap) -> pageTransMap.forEach((page, trans) -> pageLangTranslationMap.computeIfAbsent(page, s -> new HashMap<>()).put(lang, trans))));
    }

    /**
     * Loads the language files for the specified mod.
     *
     * @param modid           The mod id of the mod who's localization is to be loaded
     * @param structureFolder The structure folder for the specified mod (Must be a directory)
     */
    public static void loadModLocalization(String modid, File structureFolder) {
        File langFolder = new File(structureFolder, "lang");
        File[] files;

        if ((files = langFolder.listFiles((dir, name) -> dir.isDirectory())) == null) {
            PIHelpers.displayError("Error loading documentation localization for mod: " + modid + ", No localization files found!");
            return;
        }

        //Iterate over all files in the structure folder
        for (File langFile : files) {
            String lang = langFile.getName().replace(".json", "");
            if (langFile.getName().endsWith(".json") && ALL_LANGUAGES.contains(lang)) {
                if (!langFile.exists()) {
                    PIHelpers.displayError("En error occurred while loading localization for mod with id \"" + modid + "\" Found language folder but no lang file for language: " + LANG_NAME_MAP.get(lang));
                    continue;
                }
                loadLangFile(modid, langFile, lang);
            }
        }

        saveModLocalization(modid, "en_us");
    }

    public static void loadLangFile(String modid, File langFile, String lang) {
        JsonArray translations;
        //region Load JsonObject
        try {
            JsonParser parser = new JsonParser();
            FileReader reader = new FileReader(langFile);
            JsonElement element = parser.parse(reader);
            IOUtils.closeQuietly(reader);
            if (!element.isJsonArray()) {
                PIHelpers.displayError("Failed to load lang file. Detected invalid json file: " + langFile);
                return;
            }
            translations = element.getAsJsonArray();
        }
        catch (Exception e) {
            PIHelpers.displayError("Error loading lang file: " + e.getMessage());
            PIHelpers.displayError("File: " + langFile);
            LogHelper.error("Error loading lang file: " + langFile);
            e.printStackTrace();
            return;
        }

        Map<String, PageLangData> localizations = new HashMap<>();
        translations.forEach(element -> {
            JsonObject obj = element.getAsJsonObject();
            PageLangData data = PageLangData.fromJson(obj, lang, modid);
            localizations.put(data.pageURI, data);
        });
        modLangPageTranslationMap.computeIfAbsent(modid, s -> new HashMap<>()).computeIfAbsent(lang, s -> new HashMap<>()).putAll(localizations);
    }

    /**
     * This method is used by the editor to update a mods lang file for a specific language.
     *
     * @param modid The id of the mod who's lang file is to be saved.
     * @param lang  The language that is to be saved.
     */
    private static void saveModLocalization(String modid, String lang) {
        if (!PIConfig.editMode()) {
            return; //Files should no be edited unless in edit mode
        }

        ModStructurePage modPage = DocumentationManager.getModPage(modid);
        File langFolder = new File(modPage.getStructureDir(), "lang");
        if ((!langFolder.exists() && !langFolder.mkdirs()) || !langFolder.isDirectory()) {
            PIHelpers.displayError("En error occurred while saving localization file for mod with id \"" + modid + "The lang folder could not be created or is invalid: " + langFolder);
            return;
        }

        File langFile = new File(langFolder, lang + ".json");
        Map<String, Map<String, PageLangData>> langPageMap = modLangPageTranslationMap.get(modid);

        if (langPageMap != null && langPageMap.containsKey(lang)) {
            saveLangFile(modid, langPageMap.get(lang), langFile);
        }
        else {
            LogHelper.dev("Error saving lang file: " + langFile + " No translations available for this language.");
        }
    }

    public static void saveLangFile(String modid, Map<String, PageLangData> langMap, File langFile) {
        JsonArray translations = new JsonArray();
        langMap.forEach((uri, langData) -> translations.add(langData.toObj()));

        //region Save JsonObject
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(langFile));
            writer.setIndent("  ");
            Streams.write(translations, writer);
            writer.flush();
            IOUtils.closeQuietly(writer);
        }
        catch (Exception e) {
            PIHelpers.displayError("Error saving lang file: " + e.getMessage());
            PIHelpers.displayError("File: " + langFile);
            LogHelper.error("Error saving lang file: " + langFile);
            e.printStackTrace();
        }
        //endregion
    }

    /**
     * @param pageURI    The URI of the target page.
     * @param targetLang The lang to check.
     * @return true if the page has localization for the specified language.
     */
    public static boolean isPageLocalized(String pageURI, String targetLang) {
        Map<String, PageLangData> langMap = pageLangTranslationMap.get(pageURI);
        return langMap != null && langMap.containsKey(targetLang);
    }

    /**
     * @param pageURI The URI of the target page.
     * @param lang    The lang to check.
     * @return the localized name of the page for the given language or the english translation if the target lang is not available.
     * Will fall back to the unlocalized name if all else fails.
     */
    public static String getPageName(String pageURI, String lang) {
        Map<String, PageLangData> langMap = pageLangTranslationMap.get(pageURI);
        if (langMap == null) {
            return "page." + pageURI + ".name";
        }
        else if (langMap.containsKey(lang)) {
            return langMap.get(lang).translation;
        }
        else {
            return langMap.containsKey(DEFAULT_LANG) ? langMap.get(DEFAULT_LANG).translation : "page." + pageURI + ".name";
        }
    }

    /**
     * Similar to get getPageName except wont fall back to english will instead return the unlocalized name
     */
    public static String getPageLangName(String pageURI, String lang) {
        Map<String, PageLangData> langMap = pageLangTranslationMap.get(pageURI);

        if (langMap != null && langMap.containsKey(lang)) {
            return langMap.get(lang).translation;
        }

        return pageURI;
    }

    public static String getPageLangName(DocumentationPage page) {
        return getPageLangName(page.pageURI, page.getLocalizationLang());
    }

    /**
     * This sets the localized name for a page in the specified language and then writes the associated lang file to disk.
     *
     * @param modid   The mod the page belongs to.
     * @param pageURI The page URI.
     * @param name    The new name for the page.
     * @param lang    The language of the new name.
     */
    public static void setPageName(String modid, String pageURI, String name, String lang) {
        Map<String, PageLangData> langMap = modLangPageTranslationMap.computeIfAbsent(modid, s -> new HashMap<>()).computeIfAbsent(lang, s -> new HashMap<>());
        langMap.computeIfAbsent(pageURI, uri -> new PageLangData(modid, uri, lang, name, 0)).translation = name;
        saveModLocalization(modid, lang);
        reloadLookupMap();
    }

    //endregion

    //# Page Language Management
    //region //############################################################################

    /**
     * @param pageURI the uri of the page who's language we are checking.
     * @return the set language wor the specified page. May be overridden by the user
     */
    public static String getPageLanguage(String pageURI) {
        if (PIConfig.editMode()) {
            return getUserLanguage();
        }

        String defaultLang;
        if (!pageURI.contains(":")) { //The page uri should always start with the mod id but just in case something breaks...
            defaultLang = getUserLanguage();
        }
        else {
            defaultLang = getModLanguage(pageURI.substring(0, pageURI.indexOf(":")));
        }

        return PIConfig.pageLangOverrides.getOrDefault(pageURI, defaultLang);
    }

    public static boolean isPageLangOverridden(String pageURI) {
        return PIConfig.pageLangOverrides.containsKey(pageURI) && !PIConfig.editMode();
    }

    /**
     * @param modid the mod id of the mod who's language we are checking.
     * @return the set language wor the specified page. May be overridden by the user
     */
    public static String getModLanguage(String modid) {
        if (PIConfig.editMode()) {
            return getUserLanguage();
        }

        return PIConfig.modLangOverrides.getOrDefault(modid, getUserLanguage());
    }

    public static boolean isModLangOverridden(String modid) {
        return PIConfig.modLangOverrides.containsKey(modid) && !PIConfig.editMode();
    }

    /**
     * Sets the language override for the specified page.
     *
     * @param pageURI the uri of the page who's language is to be overridden.
     * @param lang    The language to set or null to disable override for this page.
     */
    public static void setPageLangOverride(String pageURI, @Nullable String lang) {
        if (lang == null) {
            PIConfig.pageLangOverrides.remove(pageURI);
        }
        else {
            PIConfig.pageLangOverrides.put(pageURI, lang);
        }
        PIConfig.save();
        DocumentationManager.checkAndReloadDocFiles();
    }

    /**
     * Sets the language override for the specified mod.
     *
     * @param modid the uri of the mod's language is to be overridden.
     * @param lang  The language to set or null to disable override for this mod.
     */
    public static void setModLangOverride(String modid, @Nullable String lang) {
        if (lang == null) {
            PIConfig.modLangOverrides.remove(modid);
        }
        else {
            PIConfig.modLangOverrides.put(modid, lang);
        }
        PIConfig.save();
        DocumentationManager.checkAndReloadDocFiles();
    }

    public static Collection<String> getAvailablePageLanguages(String pageURI) {
        Map<String, PageLangData> langTransMap = pageLangTranslationMap.get(pageURI);
        if (langTransMap == null) {
            return Collections.emptyList();
        }
        else {
            return langTransMap.keySet();
        }
    }

    @Nullable
    public static PageLangData getLangData(String pageURI, String lang) {
        Map<String, PageLangData> langTransMap = pageLangTranslationMap.get(pageURI);
        return langTransMap == null ? null : langTransMap.get(lang);
    }

    //endregion

    //# General User Language Handling Code
    //region //############################################################################

    /**
     * @return the desired documentation language. This defaults to MC language but can be overridden by the user.
     */
    public static String getUserLanguage() {
        if (!PIConfig.userLanguage.equals("[MINECRAFT-LANG]")) {
            return PIConfig.userLanguage;
        }
        return mcLanguage();
    }

    public static boolean isCustomUserLanguageSet() {
        return !PIConfig.userLanguage.equals("[MINECRAFT-LANG]");
    }

    public static void setCustomUserLanguage(@Nullable String lang) {
        if (lang == null) {
            PIConfig.userLanguage = "[MINECRAFT-LANG]";
            PIConfig.save();
        }
        else {
            PIConfig.userLanguage = lang;
        }

        DocumentationManager.checkAndReloadDocFiles();
    }

    /**
     * @return the current minecraft language.
     */
    private static String mcLanguage() {
        net.minecraft.client.resources.LanguageManager langManager = Minecraft.getMinecraft().getLanguageManager();
        return langManager.getCurrentLanguage().getLanguageCode();
    }

    //endregion

    public static class PageLangData {
        public String pageURI;
        public String lang;
        public String translation;
        public int pageRev;
        public String matchLang = null;
        public int matchRev = 0;
        public String modid;

        private PageLangData() {}

        public PageLangData(String modid, String pageURI, String lang, String translation, int pageRev) {
            this.modid = modid;
            this.pageURI = pageURI;
            this.lang = lang;
            this.translation = translation;
            this.pageRev = pageRev;
        }

        public JsonObject toObj() {
            JsonObject obj = new JsonObject();
            obj.addProperty("page_uri", pageURI);
            obj.addProperty("translation", translation);
            obj.addProperty("page_rev", pageRev);
            if (matchLang != null) {
                JsonObject ml = new JsonObject();
                ml.addProperty("lang", matchLang);
                ml.addProperty("rev", matchRev);
                obj.add("matches", ml);
            }
            return obj;
        }

        public PageLangData readObj(JsonObject obj) {
            pageURI = obj.get("page_uri").getAsString();
            translation = obj.get("translation").getAsString();
            pageRev = obj.get("page_rev").getAsInt();
            if (obj.has("matches")) {
                JsonObject ml = obj.get("matches").getAsJsonObject();
                matchLang = ml.get("lang").getAsString();
                matchRev = ml.get("rev").getAsInt();
            }
            return this;
        }

        //For use by the editor
        public void setMatchLang(String matchLang, int matchRev) {
            this.matchLang = matchLang;
            this.matchRev = matchRev;
            LanguageManager.saveModLocalization(modid, lang);
        }

        public static PageLangData fromJson(JsonObject obj, String lang, String modid) {
            PageLangData data = new PageLangData().readObj(obj);
            data.lang = lang;
            data.modid = modid;
            return data;
        }
    }
}
