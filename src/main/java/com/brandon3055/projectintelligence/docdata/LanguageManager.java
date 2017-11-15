package com.brandon3055.projectintelligence.docdata;

import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.projectintelligence.PIHelpers;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

/**
 * Created by brandon3055 on 11/10/2017.
 */
public class LanguageManager {

    private static final Splitter EQUAL_SIGN_SPLITTER = Splitter.on('=').limit(2);
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
    public static Map<String, Map<String, Map<String, String>>> modLangPageTranslationMap = new HashMap<>();
    /**
     * This map is just for more efficient lookups.
     * Map Structure is <pageURI, <lang, translation>>
     */
    public static Map<String, Map<String, String>> pageLangTranslationMap = new HashMap<>();


    //############################################################################
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
        File[] files;

        if ((files = structureFolder.listFiles((dir, name) -> dir.isDirectory())) == null) {
            PIHelpers.displayError("Error loading documentation localization! No localization files found!");
            return;
        }

        for (File folder : files) {
            String lang = folder.getName();
            if (ALL_LANGUAGES.contains(lang)) {
                File langFile = new File(folder, lang + ".lang");
                if (!langFile.exists()) {
                    PIHelpers.displayError("En error occurred while loading localization for mod with id \"" + modid + "\" Found language folder but no lang file for language: " + LANG_NAME_MAP.get(lang));
                    continue;
                }

                Map<String, String> localizations = new HashMap<>();

                InputStream is = null;
                try {
                    is = new FileInputStream(langFile);
                    parseLangFile(is, localizations);
                    modLangPageTranslationMap.computeIfAbsent(modid, s -> new HashMap<>()).computeIfAbsent(lang, s -> new HashMap<>()).putAll(localizations);
                }
                catch (IOException e) {
                    PIHelpers.displayError("En error occurred while loading localization for mod with id \"" + modid + "\" An exception occurred while reading language file for language: " + LANG_NAME_MAP.get(lang) + "\n\n" + e.getMessage());
                    e.printStackTrace();
                }
                finally {
                    if (is != null) {
                        IOUtils.closeQuietly(is);
                    }
                }
            }
        }

        saveModLocalization(modid, "en_us");
    }

    public static void parseLangFile(InputStream inputstream, Map<String, String> langMap) {
        try {
            for (String s : IOUtils.readLines(inputstream, Charsets.UTF_8)) {
                if (!s.isEmpty() && s.charAt(0) != 35) {
                    String[] keyValue = Iterables.toArray(EQUAL_SIGN_SPLITTER.split(s), String.class);
                    if (keyValue != null && keyValue.length == 2) {
                        langMap.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }
        catch (Exception ignored) {}
    }


    /**
     * This method is used by the editor to update a mods lang file for a specific language.
     *
     * @param modid The id of the mod who's lang file is to be saved.
     * @param lang  The language that is to be saved.
     */
    private static void saveModLocalization(String modid, String lang) {
        File docFile = DocumentationManager.getDocDirectory();
        if (!PIConfig.editMode()) {
            return; //Files should no be edited unless in edit mode
        }

        ModStructurePage modPage = DocumentationManager.getModPage(modid);
        File langFolder = new File(docFile, modid + "/" + modPage.modVersion + "/structure/" + lang);
        if ((!langFolder.exists() && !langFolder.mkdirs()) || !langFolder.isDirectory()) {
            PIHelpers.displayError("En error occurred while saving localization file for mod with id \"" + modid + "The lang folder does could not be created or is invalid: " + langFolder);
            return;
        }

        File langFile = new File(langFolder, lang + ".lang");
        Map<String, Map<String, String>> langPageMap = modLangPageTranslationMap.get(modid);

        if (langPageMap != null && langPageMap.containsKey(lang)) {
            saveLangFile(modid, langPageMap.get(lang), langFile);
        }
        else {
            LogHelper.dev("Error saving lang file: " + langFile + " No translations available for this language.");
        }
    }

    public static void saveLangFile(String modid, Map<String, String> langMap, File langFile) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(langFile);
            for (Map.Entry<String, String> entry : langMap.entrySet()) {
                IOUtils.write(entry.getKey() + "=" + entry.getValue() + "\n", os, Charsets.UTF_8);
            }
        }
        catch (Exception e) {
            PIHelpers.displayError("En error occurred while saving localization file: " + langFile + " for mod with id \"" + modid + " \n\n" + e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (os != null) {
                IOUtils.closeQuietly(os);
            }
        }
    }

    /**
     * @param pageURI    The URI of the target page.
     * @param targetLang The lang to check.
     * @return true if the page has localization for the specified language.
     */
    public static boolean isPageLocalized(String pageURI, String targetLang) {
        Map<String, String> langMap = pageLangTranslationMap.get(pageURI);
        return langMap != null && langMap.containsKey(targetLang);
    }

    /**
     * @param pageURI The URI of the target page.
     * @param lang    The lang to check.
     * @return the localized name of the page for the given language or the english translation if the target lang is not available.
     * Will fall back to the unlocalized name if all else fails.
     */
    public static String getPageName(String pageURI, String lang) {
        Map<String, String> langMap = pageLangTranslationMap.get(pageURI);
        if (langMap == null) {
            return "page." + pageURI + ".name";
        }
        else if (langMap.containsKey(lang)) {
            return langMap.get(lang);
        }
        else {
            return langMap.getOrDefault(DEFAULT_LANG, "page." + pageURI + ".name");
        }
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
        modLangPageTranslationMap.computeIfAbsent(modid, s -> new HashMap<>()).computeIfAbsent(lang, s -> new HashMap<>()).put(pageURI, name);
        saveModLocalization(modid, lang);
        reloadLookupMap();
    }

    //endregion

    //############################################################################
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
//        if (true) return ALL_LANGUAGES;
        Map<String, String> langTransMap = pageLangTranslationMap.get(pageURI);
        if (langTransMap == null) {
            return Collections.emptyList();
        }
        else {
            return langTransMap.keySet();
        }
    }

    //endregion

    //############################################################################
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

    //region Old Lang Code

    private static Language findLanguage(String lang) {
        net.minecraft.client.resources.LanguageManager langManager = Minecraft.getMinecraft().getLanguageManager();
        Language match = DataUtils.firstMatch(langManager.getLanguages(), language -> language.getLanguageCode().equals(lang.toLowerCase()));
        if (match == null) {
            match = langManager.getCurrentLanguage();
            PIHelpers.displayError("Detected invalid language selected! " + lang + " Using current system language: " + match.getLanguageCode());
        }
        return match;
    }

    private static String getBestLangMatch(Language lang, Collection<String> languages) {
        String langCode = lang.getLanguageCode();

        if (languages.contains(langCode)) {
            return langCode;
        }

        String enFile = null;
        try {
            String baseLang = langCode.substring(0, langCode.indexOf("_"));
            for (String fileLang : languages) {
                if (fileLang.startsWith(baseLang)) {
                    return fileLang;
                }

                if (fileLang.equals("en_us") || (fileLang.startsWith("en_") && enFile == null)) {
                    enFile = fileLang;
                }
            }
        }
        catch (Exception ignored) {}

        return enFile;
    }

    //endregion
}
