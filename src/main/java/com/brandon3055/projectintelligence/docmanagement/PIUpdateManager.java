package com.brandon3055.projectintelligence.docmanagement;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.integration.ModHelperBC;
import com.brandon3055.brandonscore.lib.FileDownloadManager;
import com.brandon3055.brandonscore.lib.PairKV;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.brandon3055.projectintelligence.docmanagement.PIUpdateManager.UpdateStage.*;

/**
 * Created by brandon3055 on 5/27/2018.
 * This class is responsible for checking for and downloading documentation updates.
 * This takes into account the user's language and language overrides only downloading the relevant docs for
 * the selected language.
 */
public class PIUpdateManager {

    public static FileDownloadManager downloadManager = new FileDownloadManager("PI-Update-Manager", 6, false);

    private static String modManifestURL = "https://pi.brandon3055.com/manifest.json";
    private static File modManifest;
    private static File updaterFolder;
    private static File updaterModsFolder;
    public static UpdateStage updateStage = INACTIVE;

    /**
     * Stores all of the per mod manifests
     */
    private static Map<String, File> perModManifestMap = new LinkedHashMap<>();

    /**
     * A mod file manifest contains all of the files for a specific mod as well as the language for each file.
     */
    private static Map<String, File> modDLManifestMap = new LinkedHashMap<>();

    private static Map<String, String> downloadingVersionMap = new LinkedHashMap<>();

    public static void performFullUpdateCheck() {
        if (PIConfig.editMode()) {
            LogHelper.warn("Canceling documentation update as edit mode is now enabled!");
            return;
        }

        if (!PIConfig.downloadsAllowed) {
            return;
        }

        updateStage = DL_MASTER_MANIFEST;
        LogHelper.dev("### Performing full update check ###");
        downloadManager.reset();

        updaterFolder = new File(DocumentationManager.piConfigDirectory, "DocUpdater");
        if (!updaterFolder.exists() && !updaterFolder.mkdirs()) {
            PIGuiHelper.displayError("Failed to create folder - " + updaterFolder + " Doc update check can not be completed!");
            updateStage = INACTIVE;
            return;
        }
        updaterModsFolder = new File(updaterFolder, "Mods");
        if (!updaterModsFolder.exists() && !updaterModsFolder.mkdirs()) {
            PIGuiHelper.displayError("Failed to create folder - " + updaterModsFolder + " Doc update check can not be completed!");
            updateStage = INACTIVE;
            return;
        }

        modManifest = new File(updaterFolder, "manifest.json");

        downloadManager.addFileToQue(modManifestURL, modManifest);
        downloadManager.setQueCompeteCallback(PIUpdateManager::downloadPerModManifests);
        downloadManager.startDownload();
    }

    private static void downloadPerModManifests() {
        if (PIConfig.editMode()) {
            LogHelper.warn("Canceling documentation update as edit mode is now enabled!");
            return;
        }
        updateStage = DL_MOD_MANIFESTS;
        LogHelper.dev("### Downloading per mod manifests ###");

        if (downloadManager.failedFiles.containsValue(modManifest)) {
            PIGuiHelper.displayError("Failed to download the PI mod manifest from " + modManifestURL);
            DocumentationManager.loadDocumentationFromDisk();
            updateStage = INACTIVE;
            return;
        }

        downloadManager.reset();

        perModManifestMap.clear();
        JsonObject modsJson;
        try {
            modsJson = FileHandler.readObj(modManifest);
        }
        catch (IOException e) {
            e.printStackTrace();
            PIGuiHelper.displayError("Failed to read mod manifest! See console for stacktrace.");
            updateStage = INACTIVE;
            return;
        }

        for (String modid : ModHelperBC.getLoadedMods()) {
            if (modsJson.has(modid)) {
                LogHelper.dev("Mod has dock avalible: " + modid);
                File perModManifest = new File(updaterModsFolder, modid + ".json");
                downloadManager.addFileToQue(modsJson.get(modid).getAsString(), perModManifest);
                perModManifestMap.put(modid, perModManifest);
            }
        }

        downloadManager.setQueCompeteCallback(PIUpdateManager::readPerModManifests);
        downloadManager.startDownload();
    }

    private static void readPerModManifests() {
        if (PIConfig.editMode()) {
            LogHelper.warn("Canceling documentation update as edit mode is now enabled!");
            return;
        }
        updateStage = DL_MOD_DOCUMENTATION;
        LogHelper.dev("### Reading per mod manifests ###");

        if (downloadManager.failedFiles.size() > 0) {
            PIGuiHelper.displayError("Failed to download one or more per-mod manifest files");
        }

        downloadManager.reset();
        modDLManifestMap.clear();

        for (String mod : perModManifestMap.keySet()) {
            try {
                JsonObject modJson;
                try {
                    modJson = FileHandler.readObj(perModManifestMap.get(mod));
                }
                catch (IOException e) {
                    e.printStackTrace();
                    PIGuiHelper.displayError("Failed to read manifest jason for mod " + mod + " See console for stacktrace.");
                    continue;
                }

                String installedVersion = ModHelperBC.getModVersion(mod);
                String latestMatch = "";
                String latestMatchURL = "";
                int latestBuild = -1;

                for (Map.Entry<String, JsonElement> entry : modJson.entrySet()) {
                    if (entry.getKey().equals("mod_id")) continue;
                    int build = Utils.parseInt(entry.getKey());
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    String version = obj.get("mod_version").getAsString();
                    String file = obj.get("file").getAsString();

                    //If this version is lower or equal (but not greater than) the installed version
                    if (DocumentationManager.compareVersion(version, installedVersion) <= 0) {
                        int versCheck = DocumentationManager.compareVersion(version, latestMatch);
                        //And there is no previous match or this version is greater than the last matching version or the same as the last match but a newer build.
                        if (latestMatch.isEmpty() || versCheck > 0 || (versCheck == 0 && build > latestBuild)) {
                            latestBuild = build;
                            latestMatch = version;
                            latestMatchURL = file;
                        }
                    }
                }

                if (!latestMatchURL.isEmpty()) {
                    LogHelper.dev("Found documentation for version " + latestMatch + " of " + mod + ". Installed mod version is " + installedVersion);
                    File file = new File(updaterModsFolder, mod + "-downloadManifest.json");
                    downloadManager.addFileToQue(latestMatchURL, file);
                    modDLManifestMap.put(mod, file);
                    downloadingVersionMap.put(mod, latestMatch);
                }
                else {
                    LogHelper.dev("Did not find documentation for mod " + mod + " that matches the installed mod version " + installedVersion);
                }
            }
            catch (Exception e) {
                PIGuiHelper.displayError("An error occurred while reading manifest for mod " + mod + " See console for stacktrace.");
                e.printStackTrace();
            }
        }

        downloadManager.setQueCompeteCallback(PIUpdateManager::performLocalUpdateCheck);
        downloadManager.startDownload();
    }

    public static Map<File, PairKV<String, File>> tempFileToFileMap = new LinkedHashMap<>();

    /**
     * The only time this is fired outside of this class should be when a page or mod language override state changes.
     * This checks the the existing language files against the cached mod manifest files from the last full update check.
     */
    public static void performLocalUpdateCheck() {
        if (PIConfig.editMode()) {
            LogHelper.warn("Canceling documentation update as edit mode is now enabled!");
            return;
        }
        updateStage = DL_MOD_DOCUMENTATION;
        LogHelper.dev("### Performing local update check ###");

        if (downloadManager.failedFiles.size() > 0) {
            PIGuiHelper.displayError("Failed to download one or more mod download manifests");
        }

        downloadManager.reset();

        tempFileToFileMap.clear();
        for (String mod : modDLManifestMap.keySet()) {
            try {
                JsonObject dlManifest;
                try {
                    dlManifest = FileHandler.readObj(modDLManifestMap.get(mod));
                }
                catch (IOException e) {
                    e.printStackTrace();
                    PIGuiHelper.displayError("Failed to read download manifest jason for mod " + mod + " See console for stacktrace.");
                    continue;
                }

                JsonArray baseFiles = dlManifest.getAsJsonArray("base_files");
                JsonArray langFiles = dlManifest.getAsJsonArray("lang_files");

                for (JsonElement element : baseFiles) {
                    JsonObject obj = element.getAsJsonObject();
                    File file = new File(DocumentationManager.getDlDocDirectory(), mod + "/" + downloadingVersionMap.get(mod) + "/" + obj.get("file_path").getAsString());
                    String hash = obj.get("sha1").getAsString();

                    if (file.exists() && getHash(file).equals(hash)) {
                        LogHelper.dev("Existing file is valid! Skipping file: " + file);
                        continue;
                    }

                    File tempFile = new File(updaterFolder, hash + ".temp");
                    tempFileToFileMap.put(tempFile, new PairKV<>(hash, file));
                    downloadManager.addFileToQue(obj.get("url").getAsString(), tempFile);
                }

                for (JsonElement element : langFiles) {
                    JsonObject obj = element.getAsJsonObject();
                    File file = new File(DocumentationManager.getDlDocDirectory(), mod + "/" + downloadingVersionMap.get(mod) + "/" + obj.get("file_path").getAsString());
                    String hash = obj.get("sha1").getAsString();
                    String lang = obj.get("lang").getAsString();

                    if (!LanguageManager.isLangUsedByMod(lang, mod)) {
                        continue;
                    }

                    if (file.exists() && getHash(file).equals(hash)) {
                        LogHelper.dev("Existing file is valid! Skipping file: " + file);
                        continue;
                    }

                    File tempFile = new File(updaterFolder, hash + ".temp");
                    tempFileToFileMap.put(tempFile, new PairKV<>(hash, file));
                    downloadManager.addFileToQue(obj.get("url").getAsString(), tempFile);
                }
            }
            catch (Exception e) {
                PIGuiHelper.displayError("An error occurred while reading download list for mod " + mod + " See console for stacktrace.");
                e.printStackTrace();
            }
        }
        downloadManager.setQueCompeteCallback(() -> completeDownloads(tempFileToFileMap));
        downloadManager.startDownload();
    }

    private static void completeDownloads(Map<File, PairKV<String, File>> tempFileToFileMap) {
        updateStage = RELOAD_DOCUMENTATION;
        LogHelper.dev("### Transferring downloaded files ###");
        if (downloadManager.failedFiles.size() > 0) {
            PIGuiHelper.displayError("Failed to download one or more mod page files!");
            tempFileToFileMap.entrySet().removeIf(entry -> downloadManager.failedFiles.containsValue(entry.getKey()));
        }

        for (File tempFile : tempFileToFileMap.keySet()) {
            PairKV<String, File> hashFilePair = tempFileToFileMap.get(tempFile);
            try {
                String hash = getHash(tempFile);

                if (hash.equals(hashFilePair.getKey())) {
                    hashFilePair.getValue().delete();
                    LogHelper.dev(tempFile + " -> " + hashFilePair.getValue());
                    FileUtils.moveFile(tempFile, hashFilePair.getValue());
                }
                else {
                    PIGuiHelper.displayError("An error occurred while transferring downloaded file. The hash of the downloaded file does not match the expected hash. " + tempFile);
                }
            }
            catch (Exception e) {
                PIGuiHelper.displayError("An error occurred while transferring downloaded file to final location file: " + tempFile + " -> " + hashFilePair.getValue() + " See console for stacktrace.");
                e.printStackTrace();
            }
        }

        DocumentationManager.loadDocumentationFromDisk();
        updateStage = INACTIVE;
    }

    private static String getHash(File file) throws IOException {
        return Hashing.sha1().hashBytes(FileUtils.readFileToByteArray(file)).toString();
    }

    public enum UpdateStage {
        INACTIVE,
        DL_MASTER_MANIFEST,
        DL_MOD_MANIFESTS,
        DL_MOD_DOCUMENTATION,
        RELOAD_DOCUMENTATION;

        public String getUnlocalizedName() {
            return "pi.update.status." + name().toLowerCase();
        }
    }
}
