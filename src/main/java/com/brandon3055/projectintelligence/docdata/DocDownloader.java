package com.brandon3055.projectintelligence.docdata;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.projectintelligence.PIHelpers;
import com.brandon3055.projectintelligence.docdata.ModDocManifest.RemoteFile;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.minecraft.client.Minecraft;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 4/08/2017.
 */
public class DocDownloader extends Thread {

    private int lastProgress = 0;
    private int timeSinceProgress = 0;

    private File docDirectory;
    private File manifest;
    private String lang;
    private String manifestURL;
    private boolean forceUpdate = false;
    private Runnable onComplete;
    private Map<String, ModDocManifest> modManifestMap = new HashMap<>();
    public volatile boolean finished = false;

    public DocDownloader(File docDir, String manifestURL, Runnable onComplete) {
        super("PI-Documentation-Downloader");
        this.docDirectory = docDir;
        this.manifestURL = manifestURL;
        this.onComplete = onComplete;
        this.lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
    }

    @Override
    public void run() {
        LogHelper.dev("PIDocDownloader: Start, forceUpdate: " + forceUpdate);
        //Download Manifest
        if (!downloadManifest()) {
            LogHelper.dev("PIDocDownloader: Manifest Download Failed");
            return;
        }

        //Read manifest
        JsonArray array = getManifestArray();
        if (array == null) {
            LogHelper.dev("PIDocDownloader: Get Manifest Array Failed");
            return;
        }
        readManifestArray(array);

        //Process Manifest Data
        modManifestMap.forEach((s, modManifest) -> processModManifest(modManifest));

        //Finish
        finishOnDownloadsComplete();
    }

    private void finishOnDownloadsComplete() {
//        LogHelper.dev("PIDocDownloader: Waiting for downloads...");
//        while (DocumentationManager.downloadHandler.running) {
//            timeSinceProgress++;
//
//            if (DocumentationManager.downloadHandler.filesDownloaded > lastProgress) {
//                lastProgress = DocumentationManager.downloadHandler.filesDownloaded;
//                timeSinceProgress = 0;
//            }
//
//            if (timeSinceProgress / 1000 > 30) {
//                LogHelper.error("Error downloading documentation. Download timed out after 30 seconds.");
//                PIHelpers.displayError("Failed to download documentation files. Download timed out.");
//                DocumentationManager.downloadHandler.stopDownloads();
//                DocumentationManager.downloadHandler.reset();
//                break;
//            }
//
//            try {
//                Thread.sleep(1);
//            }
//            catch (InterruptedException ignored) {}
//        }
//
//        if (!DocumentationManager.downloadHandler.failedFiles.isEmpty()) {
//            for (String url : DocumentationManager.downloadHandler.failedFiles.keySet()) {
//                LogHelper.error("Failed to download file! " + url + " -> " + DocumentationManager.downloadHandler.failedFiles.get(url));
//            }
//        }
//
//        finished = true;
//        DocumentationManager.downloadHandler.reset();
//        onComplete.run();
//        LogHelper.dev("PIDocDownloader: Finished!\n");
    }

    public DocDownloader forceUpdate() {
        this.forceUpdate = true;
        return this;
    }

    //############################################################################
    //# Acquire Manifest Data
    //region //############################################################################

    private boolean downloadManifest() {
        manifest = new File(docDirectory, "manifest.json");
        try {
            LogHelper.dev("Downloading manifest from: " + manifest);
            FileHandler.downloadFile(manifestURL, manifest);
            LogHelper.dev("Downloaded manifest to: " + manifest.getAbsolutePath());
        }
        catch (IOException e) {
            PIHelpers.displayError("Failed to download documentation manifest:\n" + e.getMessage() + "\nSee console for full error stacktrace.");
            e.printStackTrace();
            finished = true;
            onComplete.run();
            return false;
        }
        return true;
    }

    private JsonArray getManifestArray() {
        try {
            JsonParser parser = new JsonParser();
            FileReader fr = new FileReader(manifest);
            JsonElement element = parser.parse(new JsonReader(fr));
            IOUtils.closeQuietly(fr);

            if (element.isJsonArray()) {
                return element.getAsJsonArray();
            }

            PIHelpers.displayError("Failed to read documentation manifest. Manifest file is invalid.");
        }
        catch (Throwable e) {
            e.printStackTrace();
            PIHelpers.displayError("Failed to read documentation manifest:\n" + e.getMessage() + "\nSee console for full error stacktrace.");
            return null;
        }

        return null;
    }

    private void readManifestArray(JsonArray array) {
        LogHelper.dev("PIDocDownloader: Read Manifest");
        modManifestMap.clear();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                PIHelpers.displayError("Detected invalid mod manifest data! Skipping...");
                continue;
            }

            ModDocManifest manifest = ModDocManifest.fromJson(element.getAsJsonObject());
            if (manifest != null) {
                modManifestMap.put(manifest.modid, manifest);
            }
        }
    }

    //endregion

    //############################################################################
    //# Process Manifest Data
    //region //############################################################################

    private void processModManifest(ModDocManifest manifest) {
        LogHelper.dev("PIDocDownloader: Processing mod " + manifest.modid);
        Map<File, RemoteFile> manifestFiles = manifest.buildFileList(docDirectory, lang, new HashMap<>()); //TODO Properly implement language so it can be configured in gui and implement overrides

        //Check manifest files
        manifestFiles = getFilesToUpdate(manifestFiles);

        //Download Required files
//        manifestFiles.forEach((file, remoteFile) -> DocumentationManager.downloadHandler.addFileToQue(remoteFile.url, file));

        //Delete old files
        //todo Delete files that are no longer needed
    }

    private Map<File, RemoteFile> getFilesToUpdate(Map<File, RemoteFile> manifestFiles) {
        Map<File, RemoteFile> toUpdateMap = new HashMap<>();

        for (File file : manifestFiles.keySet()) {
            LogHelper.dev("PIDocDownloader: Check File: " + file);
            RemoteFile remoteFile = manifestFiles.get(file);
            if (!file.exists() || forceUpdate) {
                LogHelper.dev("PIDocDownloader: Adding File To Download (Does not exist or forceUpdate)");
                toUpdateMap.put(file, remoteFile);
            }
            else {
                try {
                    FileInputStream is = new FileInputStream(file);
                    String md5 = DigestUtils.md5Hex(is);
                    IOUtils.closeQuietly(is);
                    if (!md5.equals(remoteFile.md5)) {
                        LogHelper.dev("PIDocDownloader: Adding File To Download (MD5 changed) | MD5: " + md5);
                        toUpdateMap.put(file, remoteFile);
                    }
                    else {
                        LogHelper.dev("PIDocDownloader: File up to date!");
                    }
                }
                catch (IOException e) {
                    PIHelpers.displayError("An error occurred while validating a documentation file:\n" + e.getMessage() + "\nSee console for full stacktrace");
                    e.printStackTrace();
                }
            }
        }

        return toUpdateMap;
    }

    //endregion

}
