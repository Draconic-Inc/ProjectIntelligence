package com.brandon3055.projectintelligence.docdata;

import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.projectintelligence.PIHelpers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.JsonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 5/08/2017.
 */
public class ModDocManifest {

    public final String modid;
    public Map<String, RemoteFile> langDescriptorMap = new HashMap<>();
    public Map<String, Map<String, RemoteFile>> pageToLangPageMaps = new HashMap<>();

    private ModDocManifest(String modid) {
        this.modid = modid;
    }

    public static ModDocManifest fromJson(JsonObject json) {
        if (!JsonUtils.isString(json, "mod_id")) {
            PIHelpers.displayError("Error checking for documentation updates\nDetected invalid manifest entry (No mod id specified)");
            return null;
        }

        ModDocManifest manifest = new ModDocManifest(JsonUtils.getString(json, "mod_id"));

        //region Read descriptors
        if (!JsonUtils.isJsonArray(json, "descriptors")) {
            PIHelpers.displayError("Error checking for documentation updates\nNo descriptors for mod: " + manifest.modid);
            return null;
        }

        JsonArray descriptors = JsonUtils.getJsonArray(json, "descriptors");
        for (JsonElement element : descriptors) {
            if (!element.isJsonObject()) {
                PIHelpers.displayError("Error checking for documentation updates\nDetected invalid descriptor entry for mod: " + manifest.modid);
                continue;
            }

            RemoteFile file = RemoteFile.fromJson(element.getAsJsonObject());
            if (file == null) {
                PIHelpers.displayError("Error checking for documentation updates\nDetected invalid descriptor entry for mod: " + manifest.modid);
            }
            else {
                manifest.langDescriptorMap.put(file.lang, file);
            }
        }
        //endregion

        //region Read pages
        if (!JsonUtils.isJsonArray(json, "pages")) {
            PIHelpers.displayError("Error checking for documentation updates\nNo pages for mod: " + manifest.modid);
            return null;
        }

        JsonArray pages = JsonUtils.getJsonArray(json, "pages");
        for (JsonElement element : pages) {
            if (!element.isJsonObject()) {
                PIHelpers.displayError("Error checking for documentation updates\nDetected invalid page entry for mod: " + manifest.modid);
                continue;
            }

            JsonObject page = element.getAsJsonObject();

            if (!JsonUtils.isString(page, "name")) {
                PIHelpers.displayError("Error checking for documentation updates\nNo page for mod: " + manifest.modid + " has no name");
                continue;
            }
            String name = JsonUtils.getString(page, "name");

            if (!JsonUtils.isJsonArray(page, "files")) {
                PIHelpers.displayError("Error checking for documentation updates\nNo page: " + name + " for mod: " + manifest.modid + " has no downloadable files listed");
                continue;
            }
            JsonArray files = JsonUtils.getJsonArray(page, "files");

            for (JsonElement pageFile : files) {
                if (!pageFile.isJsonObject()) {
                    PIHelpers.displayError("Error checking for documentation updates\nDetected invalid page file entry for mod: " + manifest.modid + " page: " + name);
                    continue;
                }

                RemoteFile file = RemoteFile.fromJson(pageFile.getAsJsonObject());
                if (file == null) {
                    PIHelpers.displayError("Error checking for documentation updates\nDetected invalid page file entry for mod: " + manifest.modid + " page: " + name);
                }
                else {
                    Map<String, RemoteFile> langPageMap = manifest.pageToLangPageMaps.computeIfAbsent(name, s -> new HashMap<>());
                    langPageMap.put(file.lang, file);
                }
            }
        }
        //endregion

        return manifest;
    }

    /**
     * This is where a bit of the file magic happens! This method assigns manifest entries to locations on disk!
     *
     * Builds and returns a list of all required files for the given language including the descriptors.
     * pageLangOverrides allows the user to select a different language for specific pages in the event the doc for a page is more
     * up to date in a different language.
     */
    public Map<File, RemoteFile> buildFileList(File docDirectory, String langCode, Map<String, String> pageLangOverrides) {
        Language lang = findLanguage(langCode);
        Map<File, RemoteFile> fileMap = new HashMap<>();

        File modDirectory = new File(docDirectory, modid);
        File descriptorDir = new File(modDirectory, "descriptors");

        //Add all descriptors
        langDescriptorMap.forEach((desLang, remoteFile) -> fileMap.put(new File(descriptorDir, desLang + ".json"), remoteFile));

        //Add language specific pages
        for (String page : pageToLangPageMaps.keySet()) {
            Map<String, RemoteFile> langPageMap = pageToLangPageMaps.get(page);

            if (page.equals("descriptor")) {
                //Will always download all descriptor md's
                for (String desLang : langPageMap.keySet()) {
                    fileMap.put(new File(modDirectory, "descriptors/" + desLang + ".md"), langPageMap.get(desLang));
                }
            }
            else if (pageLangOverrides.containsKey(page) && langPageMap.containsKey(pageLangOverrides.get(page))) {
                String overrideLang = pageLangOverrides.get(page);
                fileMap.put(new File(modDirectory, overrideLang + "/" + page), langPageMap.get(overrideLang));
            }
            else {
                RemoteFile file = getBestLangMatch(lang, langPageMap);
                if (file != null) {
                    fileMap.put(new File(modDirectory, file.lang + "/" + page), file);
                }
            }
        }

        return fileMap;
    }

    private Language findLanguage(String lang) {
        LanguageManager langManager = Minecraft.getMinecraft().getLanguageManager();
        Language match = DataUtils.firstMatch(langManager.getLanguages(), language -> language.getLanguageCode().equals(lang.toLowerCase()));
        if (match == null) {
            match = langManager.getCurrentLanguage();
            PIHelpers.displayError("Detected invalid language selected! " + lang + " Using current system language: " + match.getLanguageCode());
        }
        return match;
    }

    private RemoteFile getBestLangMatch(Language lang, Map<String, RemoteFile> langFileMap) {
        String langCode = lang.getLanguageCode();

        if (langFileMap.containsKey(langCode)) {
            return langFileMap.get(langCode);
        }

        RemoteFile enFile = null;
        try {
            String baseLang = langCode.substring(0, langCode.indexOf("_"));
            for (String fileLang : langFileMap.keySet()) {
                if (fileLang.startsWith(baseLang)) {
                    return langFileMap.get(fileLang);
                }

                if (fileLang.equals("en_us") || (fileLang.startsWith("en_") && enFile == null)) {
                    enFile = langFileMap.get(fileLang);
                }
            }
        }
        catch (Exception ignored) {}

        return enFile;
    }

    public static class RemoteFile {
        public final String url;
        public final String lang;
        public final String md5;

        private RemoteFile(String url, String lang, String md5) {
            this.url = url;
            this.lang = lang;
            this.md5 = md5;
        }

        private static RemoteFile fromJson(JsonObject json) {
            if (JsonUtils.isString(json, "url") && JsonUtils.isString(json, "lang") && JsonUtils.isString(json, "md5")) {
                return new RemoteFile(JsonUtils.getString(json, "url"), JsonUtils.getString(json, "lang"), JsonUtils.getString(json, "md5"));
            }
            return null;
        }
    }
}
