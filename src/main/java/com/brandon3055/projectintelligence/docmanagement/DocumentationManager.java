package com.brandon3055.projectintelligence.docmanagement;

import codechicken.lib.reflect.ObfMapping;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.integration.ModHelperBC;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.projectintelligence.ProjectIntelligence;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.common.primitives.Ints;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ResourceLocationUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.brandon3055.projectintelligence.docmanagement.ContentRelation.Type.*;

/**
 * Created by brandon3055 on 4/08/2017.
 * <p>
 * For reverence when referring to documentation modVersion i am referring to the minimum version of the mod that documentation applies to.
 */
public class DocumentationManager {

    public static File piConfigDirectory;
    private static File docDirectoryCache = null;
    private static File packDocDirectoryCache = null;
    private static RootPage rootPage = new RootPage();

    /**
     * This is a map of <Page URI, Doc Page> e.g. draconicevolution:fusion_crafting/core
     * This is only ever populated with active pages (meaning only the active doc version)
     */
    private static Map<String, DocumentationPage> uriPageMap = Collections.synchronizedMap(new HashMap<>());
    /**
     * This is a map of <Mod ID, Descriptor Page>
     * This is only ever populated with active pages (meaning only the active doc version)
     */
    private static Map<String, ModStructurePage> modStructureMap = Collections.synchronizedMap(new HashMap<>());
    /**
     * This map maps mod structure pages to their file on disk.
     * This is only ever populated with active pages (meaning only the active doc version)
     */
    private static Map<ModStructurePage, File> structureFileMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * This is a map of all installed documentation file revisions for each mod.
     * This is a map of <modId, <modVersion, docVersionFolder>>
     */
    private static Map<String, Map<String, File>> installedModVersionFileMap = Collections.synchronizedMap(new HashMap<>());
    /**
     * This is just for convenience. It stores a sorted list of installed versions for each mod id.
     * Sorting order is lowest version to highest version.
     */
    public static Map<String, LinkedList<String>> sortedModVersionMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * This map contains the active documentation modVersion for each mod.
     * The actual installed mod version may be higher than or equal to the doc modVersion but not lower.
     */
    private static Map<String, String> activeModVersionMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * This stores the file location for all avalible pack documentation. There will usually only ever be one set of pack documentation per
     * mod pack if any but multiple sets of local pack documentation are supported.
     */
    private static Map<String, File> packDocFileMap = Collections.synchronizedMap(new HashMap<>());

    private static Map<ContentRelation.Type, Map<ContentRelation, DocumentationPage>> contentRelationsMap = new HashMap<>();

    //# Initialization
    //region //############################################################################

    public static void initialize() {
        piConfigDirectory = new File(FileHandler.brandon3055Folder, "ProjectIntelligence");

        if (!piConfigDirectory.exists() && !piConfigDirectory.mkdirs()) {
            LogHelper.bigError("Failed to create config directory! Things are going to break! " + piConfigDirectory);
        }

//        ModContainer piContainer = ModList.get().getModContainerById(ProjectIntelligence.MODID).orElseThrow(NullPointerException::new);
        FileHandler.findFiles(ProjectIntelligence.MODID, "assets/" +ProjectIntelligence.MODID + "/default_styles", path -> true, (path2, filePath) -> {
            if (filePath.toString().endsWith(".json")) {
                try {
                    Path styleFolder = Paths.get(piConfigDirectory.getAbsolutePath(), "GuiStyle/DefaultPresets");
                    if (!Files.exists(styleFolder)) {
                        Files.createDirectories(styleFolder);
                    }
                    Path outputFile = styleFolder.resolve(filePath.getFileName().toString());
                    Files.copy(filePath, outputFile, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }, false, false);

        checkAndReloadDocFiles();
    }

    //endregion

    //# Documentation File Loading
    //region //############################################################################

    /**
     * Compares local files to remote and downloads changed files (if not in edit mode)
     * Then calls {@link #loadDocumentationFromDisk()} to actually load the files.
     */
    public static void checkAndReloadDocFiles() {
        clearDocDirCache();
        getDocDirectory(); //This is just called to validate the editing directory if edit mode is enabled.

        if (PIConfig.editMode()) {
            loadDocumentationFromDisk();
            return;
        }

        PIUpdateManager.performFullUpdateCheck();
    }

    /**
     * This method actually loads documentation from disk after it has been downloaded.
     */
    public static void loadDocumentationFromDisk() {
        LogHelper.dev("");
        LogHelper.dev("### Reloading mod documentation from disk... ###");
        clearDocDirCache();
        modStructureMap.clear();
        uriPageMap.clear();
        structureFileMap.clear();
        installedModVersionFileMap.clear();
        contentRelationsMap.clear();
        LanguageManager.clearTranslations();

        File docDir = getDocDirectory();

        //The following code including the 2 loops is responsible for iterating over the ModDocs directory and extracting all versions of each mods documentation.
        //This code will likely replaced once the download system in finished. At that point we will already have a list of all files so this will not be needed.
        //This may still be needed for some edge cases.

        //First create a list off all directories in the ModDocs folder. These should all be mod documentation root directories
        List<File> modFolders = new ArrayList<>();  //e.g. draconicevolution, thermalexpansion
        parseFilesInDirectory(docDir, File::isDirectory, modFolders::add);
        LogHelper.dev("Found " + modFolders.size() + " potential mod root folders.");

        for (File modFolder : modFolders) {
            LogHelper.dev("Checking potential mod folder: " + modFolder);
            //Next iterate over all folders in each mods root folder. Each of these folders should be a documentation version folder.
            List<File> modVersionFolders = new ArrayList<>(); //e.g. 1.0.0, 1.1.0, 1.2.0 (Remember this is mod version not doc revision (The mod never deals with revisions directly))
            parseFilesInDirectory(modFolder, File::isDirectory, modVersionFolders::add);
            LogHelper.dev("Found " + modVersionFolders.size() + " potential document version folders.");

            for (File verFolder : modVersionFolders) {
                LogHelper.dev("Checking potential version folder: " + verFolder);
                //Next iterate over the list of suspected version folders and check if it contains a structure file.
                //If it does then it is a valid version folder and the structure file and version folder are sent to be parsed.
                File structure = new File(verFolder, "structure/structure.json");
                if (structure.exists()) {
                    LogHelper.dev("Found structure file! Structure file will be parsed and added to version list.");
                    parseStructureFile(structure, verFolder, false);
                }
                else {
                    LogHelper.dev("No structure file found. This is not a valid version folder.");
                }
            }
        }

        //Load pack documentation
        File packDocDir = getPackDocDirectory();
        List<File> packFolders = new ArrayList<>();  //e.g. procras2craft, tolkiencraft, dw20
        parseFilesInDirectory(packDocDir, File::isDirectory, packFolders::add);
        LogHelper.dev("");
        LogHelper.dev("Found " + packFolders.size() + " potential pack documentation folders.");
        for (File docFolder : packFolders) {
            LogHelper.dev("Checking potential pack documentation folder: " + docFolder);
            File structure = new File(docFolder, "structure/structure.json");
            if (structure.exists()) {
                LogHelper.dev("Found structure file! Structure file will be parsed and added to pack doc list.");
                parseStructureFile(structure, docFolder, true);
            }
            else {
                LogHelper.dev("No structure file found. This is not a valid pack doc folder.");
            }
        }

        LogHelper.dev("");

        //At this point we should now have a map of all available documentation and all available versions of each mods documentation.
        //This is all stored in installedModVersionFileMap

        //Last thing to do is sort documentation versions (this also loads the appropriate versions)
        sortDocVersions();
        loadRootPage();
        LogHelper.startTimer("LanguageManager.reloadLookupMap");
        LanguageManager.reloadLookupMap();
        LogHelper.stopTimer();

        GuiProjectIntelligence.requiresEditReload = true;
        if (PIGuiHelper.editor != null) {
            PIGuiHelper.editor.reload();
        }
    }

    /**
     * This creates the root page and adds all of the mod pages to it as child pages.
     * Also sets the root page as the parent of each mod page.
     * Must be called after mod pages have been loaded and sorted.
     */
    private static void loadRootPage() {
        rootPage = new RootPage();
        DataUtils.forEachMatch(modStructureMap.values(), DocumentationPage::isPackDoc, rootPage::addModPage);
        DataUtils.forEachMatch(modStructureMap.values(), page -> !page.isPackDoc(), rootPage::addModPage);
    }

    /**
     * This method is responsible for loading each structure file.
     * The file is converted to json then passed into ModStructurePage.generateFromJson
     * If the file was a valid structure file a {@link ModStructurePage} will be generated and
     * added along with the versionFolder for this file to {@link #installedModVersionFileMap}
     *
     * @param structureFile The mod structure file to be parsed.
     * @param versionFolder The version folder for this structure file. (~/modid/x.x.x)
     * @return true if the structure file was successful parsed.
     */
    private static void parseStructureFile(File structureFile, File versionFolder, boolean isPackDoc) {
        try (JsonReader jsonReader = new JsonReader(new InputStreamReader(new FileInputStream(structureFile), StandardCharsets.UTF_8))) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(jsonReader);
            if (element.isJsonObject()) {
                JsonObject jObj = element.getAsJsonObject();
                ModStructurePage structurePage = ModStructurePage.generateFromJson(jObj, isPackDoc);

                //Make sure the structure file was valid (If it is not the generate method will return null)
                if (structurePage == null) {
                    PIGuiHelper.displayError("Found invalid doc structure file: " + structureFile + " No modid or lang detected.");
                    return;
                }

                if (isPackDoc) {
                    packDocFileMap.put(structurePage.modid, versionFolder);
                    return;
                }

                //Check that the structure file is in the correct directory for its modid and version. This is essential because mod id and version are used to find page md files
                //for example: ModDocs/<modid>/<modVersion>/<language>/md_file_path.md

                //Make sure the this structure file is in the correct mod root folder
                if (!versionFolder.getParentFile().getName().equals(structurePage.getModid())) {
                    PIGuiHelper.displayError("Found a mod documentation structure file in the wrong rood mod folder!\n\nThe name of the mod folder must match the mod's mod id!\n\nFound file for modid: " + structurePage.modid + " In folder:" + versionFolder.getParentFile());
                    return;
                }

                //Make sure the structure version matches the version folder name. This is important because this is used to find the correct path to the md files
                if (!versionFolder.getName().equals(structurePage.modVersion)) {
                    PIGuiHelper.displayError("Found a mod documentation structure file in the wrong version folder!\n\nThe name of the version folder must match the version!\n\nFound file for version: " + structurePage.modVersion + " In folder:" + versionFolder);
                    return;
                }

                //Finally assuming all checks passed the validated version folder is added to the list of all installed documentation versions
                installedModVersionFileMap.computeIfAbsent(structurePage.getModid(), s -> new HashMap<>()).put(structurePage.modVersion, versionFolder);
            }
            else {
            }
        }
        catch (Exception e) {
            PIGuiHelper.displayError("Error reading mod descriptor file: " + e.getMessage() + "\n\nError occurred while reading file: " + structureFile);
            LogHelper.error("Error reading mod descriptor file");
            e.printStackTrace();
        }

    }

    public static void saveDocToDisk(ModStructurePage modPage) {
        if (!PIConfig.editMode()) {
            PIGuiHelper.displayError("Can not save documentation when not in edit mode!");
            return;
        }
        if (!structureFileMap.containsKey(modPage)) {
            PIGuiHelper.displayError("Something went wrong... Attempted to save mod descriptor but could not find cached save file");
            return;
        }


        JsonObject jObj = modPage.writeToJson();

        //region Save JsonObject
        try (JsonWriter writer = new JsonWriter(new FileWriterWithEncoding(structureFileMap.get(modPage), StandardCharsets.UTF_8))) {
            writer.setIndent("  ");
            Streams.write(jObj, writer);
            writer.flush();
        }
        catch (Exception e) {
            PIGuiHelper.displayError("Error saving mod Descriptor " + e.getMessage());
            LogHelper.error("Error saving mod Descriptor");
            e.printStackTrace();
        }
    }

    public static void parseFilesInDirectory(File directory, Predicate<File> fileValidator, Consumer<File> fileProcessor) {
        if (!directory.exists() || !directory.isDirectory()) {
            PIGuiHelper.displayError("An error occurred while parsing documentation files.\nThe pacified file does not exist or is not a directory: " + directory);
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            PIGuiHelper.displayError("An error occurred while parsing documentation files.\nAn error occurred while trying to read files ib the following directory: " + directory);
            return;
        }

        for (File file : files) {
            if (fileValidator.test(file)) {
                fileProcessor.accept(file);
            }
        }
    }

    //endregion

    //# Documentation version handling
    //region //############################################################################

    /**
     * This method is responsible for iterating over all available doc versions for each mod and selecting the version that
     * best matches the version of the mod that is installed. It then loads the selected version
     * <p>
     * If a mod is not installed but its documentation is its documentation will be hidden unless edit mode is enabled.
     * Edit mode allows version overrides so you can edit any version of a mods documentation.
     */
    private static void sortDocVersions() {
        LogHelper.startTimer("Sorting documentation versions");
        LogHelper.dev("Sorting documentation versions...");
        activeModVersionMap.clear();
        sortedModVersionMap.clear();

        for (String modid : installedModVersionFileMap.keySet()) {
            Map<String, File> versionFolderMap = installedModVersionFileMap.get(modid);

            //Sort the available versions
            LinkedList<String> versions = new LinkedList<>(versionFolderMap.keySet());
            versions.sort(VERSION_COMPARATOR);
            sortedModVersionMap.put(modid, versions);

            //Check if a version override is enabled for this mod.
            String versionOverride = PIConfig.modVersionOverrides.get(modid);
            if (versionOverride != null && versionFolderMap.containsKey(versionOverride)) {
                if (loadDocVersion(versionFolderMap.get(versionOverride), false)) {
                    LogHelper.dev("Version override enabled for mod: " + modid + " version " + versionOverride + " will be loaded");
                    activeModVersionMap.put(modid, versionOverride);
                    continue;
                }
            }
            else if (versionOverride != null) {
                PIConfig.modVersionOverrides.remove(modid);
                PIConfig.save();
            }

            //If no version override is enabled then find the best possible match for the version installed.
            String installedVersion = ModHelperBC.getModVersion(modid);
            LogHelper.dev("Checking Mod: " + modid + " Installed: " + installedVersion);
            boolean versionLoaded = false;

            if (installedVersion != null) {
                //We want to iterate over the list backwards so we start at the latest version of the documentation.
                //The first version we find that is targeted at the same or a lower version than the version of the mod installed is the one we go with.
                //This way we end up loading the latest doc version that is not higher than the mod version.
                Iterator<String> i = versions.descendingIterator();
                while (i.hasNext()) {
                    String version = i.next();
                    if (compareVersion(installedVersion, version) >= 0) {
                        LogHelper.dev("Found best version match for mod: " + modid + " loading version " + version);
                        loadDocVersion(versionFolderMap.get(version), false);
                        versionLoaded = true;
                        break;
                    }
                }
                if (!versionLoaded && BrandonsCore.inDev) {
                    String version = versions.getLast();
                    LogHelper.dev("No version match found for " + modid + " but mod is running in dev so loading the latest version: " + version);
                    loadDocVersion(versionFolderMap.get(version), false);
                }
            }

            if (!versionLoaded && PIConfig.editMode()) {
                LogHelper.dev("Mod " + modid + " is not installed or its version is not supporter but edit mode is enabled. Loading latest version: " + versions.getLast());
                loadDocVersion(versionFolderMap.get(versions.getLast()), false);
            }
        }

        //Load pack docks
        for (String pack : packDocFileMap.keySet()) {
            loadDocVersion(packDocFileMap.get(pack), true);
        }
        LogHelper.stopTimer();
    }

    public static Comparator<String> VERSION_COMPARATOR = DocumentationManager::compareVersion;

    //This comparator code is pulled directly from ForgeGradle

    /**
     * Compares 2 versions. Returns -1 if version1 is outdated, +1 if version2 is outdated, and 0 if they are the same.
     */
    public static int compareVersion(String version1, String version2) {
        if (version2 == null || version2.equals(version1)) {
            return 0;
        }

        int[] v1 = stringToInt(version1.split("\\."));
        int[] v2 = stringToInt(version2.split("\\."));

        return Ints.lexicographicalComparator().compare(v1, v2);
    }

    public static int[] stringToInt(String[] strings) {
        int[] ints = new int[strings.length];
        for (int i = 0; i < ints.length; i++) {
            Integer v = Ints.tryParse(strings[i]);
            ints[i] = v == null ? 0 : v;
        }
        return ints;
    }

    /**
     * Once the a document version has been selected for a mods documentation this method parses its structure file.
     *
     * @param versionFolder the version folder for the target version.
     */
    //TODO There's a bit of redundant code here. I could just cache the structure page when its first parsed but if there are a lot of
    //versions installed this could be very inefficient. This needs further consideration.
    private static boolean loadDocVersion(File versionFolder, boolean isPackDock) {
        LogHelper.dev("Loading Documentation from: " + versionFolder);
        File structureFile = new File(versionFolder, "structure/structure.json");
        try {
            JsonParser parser = new JsonParser();
            JsonReader jsonReader = new JsonReader(new FileReader(structureFile));
            JsonElement element = parser.parse(jsonReader);
            IOUtils.closeQuietly(jsonReader);
            if (element.isJsonObject()) {
                JsonObject jObj = element.getAsJsonObject();
                ModStructurePage structurePage = ModStructurePage.generateFromJson(jObj, isPackDock);

                //This should never be null at this point we should have already successfully generated a page using this same file but just in case...
                if (structurePage == null) {
                    PIGuiHelper.displayError("Found invalid mod structure file. No modid or lang detected.");
                    return false;
                }

                uriPageMap.putAll(structurePage.getURIPageMap());
                modStructureMap.put(structurePage.getModid(), structurePage);
                structureFileMap.put(structurePage, structureFile);
                LanguageManager.loadModLocalization(structurePage.getModid(), structureFile.getParentFile());
                return true;
            }
        }
        catch (FileNotFoundException e) {
            //Again this file should have already been generated successfully at this point so there should never be an exception here.
            e.printStackTrace();
        }
        return false;
    }

    public static void addNewDocVersion(String modid, String newVersion, @Nullable String copyFrom) throws IOException {
        Map<String, File> versionFileMap = installedModVersionFileMap.get(modid);
        ModStructurePage existingPage = getModPage(modid);

        if (versionFileMap == null || versionFileMap.containsKey(newVersion) || existingPage == null) {
            return;
        }

        //Create the new version directory
        File modFolder = new File(getDocDirectory(), modid + "/" + newVersion + "/structure");
        if (!modFolder.exists() && !modFolder.mkdirs()) {
            throw new IOException("Failed to create mod directory! " + modFolder);
        }

        //Make sure this version does not already exist
        File structure = new File(modFolder, "structure.json");
        if (structure.exists()) {
            throw new IOException("Mod structure file already exists! Perhaps it is invalid? Structure file:" + structure);
        }

        //If not copying then just save a blank constructor.
        if (copyFrom == null) {
            ModStructurePage modPage = new ModStructurePage(null, modid, newVersion, false);
            structureFileMap.put(modPage, structure);
            modStructureMap.put(modid, modPage);
            saveDocToDisk(modPage);
            LanguageManager.setPageName(modid, modid + ":", existingPage.getModPageName(), LanguageManager.getUserLanguage());
        }
        else {
            if (!versionFileMap.containsKey(copyFrom)) {
                throw new IOException("It seems the version you are trying to copy does not exist?!?!?");
            }

            File copyFromVersion = versionFileMap.get(copyFrom);
            File newVersionFolder = new File(getDocDirectory(), modid + "/" + newVersion);
            FileUtils.copyDirectory(copyFromVersion, newVersionFolder);

            if (!structure.exists()) {
                throw new IOException("Attempted to copy version but could not find copied structure file. " + structure);
            }

            try {
                JsonParser parser = new JsonParser();
                JsonReader jsonReader = new JsonReader(new FileReader(structure));
                JsonElement element = parser.parse(jsonReader);
                IOUtils.closeQuietly(jsonReader);
                if (element.isJsonObject()) {
                    JsonObject jObj = element.getAsJsonObject();
                    ModStructurePage structurePage = ModStructurePage.generateFromJson(jObj, false);

                    if (structurePage == null) {
                        throw new IOException("Error reading copied structure file. " + structure);
                    }
                    structurePage.modVersion = newVersion;
                    LogHelper.dev("NewVersion: " + newVersion);
                    structureFileMap.put(structurePage, structure);
                    modStructureMap.put(modid, structurePage);
                    saveDocToDisk(structurePage);
                }
                else {
                    throw new IOException("Error reading copied structure file. (File is not a valid json)" + structure);
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        checkAndReloadDocFiles();
    }
    //    Its loading things from the ModDocd need think when not tired

    //endregion

    //# Misc getters/setters/stuffs
    //region //############################################################################

    public static void clearDocDirCache() {
        docDirectoryCache = null;
        packDocDirectoryCache = null;
    }

    public static File getDocDirectory() {
        if (docDirectoryCache != null) {
            return docDirectoryCache;
        }

        if (PIConfig.editMode()) {
            File file = new File(PIConfig.editingRepoLoc);
            if (file.exists() && file.isDirectory()) {
                docDirectoryCache = file;
                return docDirectoryCache;
            }


            PIConfig.setEditMode(false);
            PIConfig.save();
            PIGuiHelper.displayError("Specified editing directory does not exist or is not a directory! Edit mode disabled.");
            PIGuiHelper.displayError("Please clone or download the Project Intelligence documentation repo from https://github.com/brandon3055/Project-Intelligence-Docs then specify the location of the ModDocs repo.");
            PIGuiHelper.displayError("e.g. C:\\Users\\<your-name>\\Desktop\\Project-Intelligence-Docs\\ModDocs");
        }

        docDirectoryCache = new File(piConfigDirectory, "ModDocs");
        if (!docDirectoryCache.exists() && !docDirectoryCache.mkdirs()) {
            LogHelper.bigError("Failed to create document directory! Things are going to break! " + docDirectoryCache);
        }

        return docDirectoryCache;
    }

    public static File getDlDocDirectory() {
        File dir = new File(piConfigDirectory, "ModDocs");
        if (!dir.exists() && !dir.mkdirs()) {
            LogHelper.bigError("Failed to create document directory! Things are going to break! " + dir);
        }
        return dir;
    }

    public static File getPackDocDirectory() {
        if (packDocDirectoryCache != null) {
            return packDocDirectoryCache;
        }

        packDocDirectoryCache = new File(piConfigDirectory, "PackDocs");
        if (!packDocDirectoryCache.exists() && !packDocDirectoryCache.mkdirs()) {
            LogHelper.bigError("Failed to create pack document directory! Things are going to break! " + packDocDirectoryCache);
        }

        return packDocDirectoryCache;
    }

    public static boolean hasModPage(String modid) {
        return modStructureMap.containsKey(modid);
    }

    /**
     * @param modid The mod id
     * @return the closest matching ModDescriptorPage for the specified mod and the current language.
     */
    public static ModStructurePage getModPage(String modid) {
        return modStructureMap.get(modid);
    }

    public static boolean hasPage(String pagePath) {
        return uriPageMap.containsKey(pagePath);
    }

    /**
     * @param pageURI The uri of the page to retrieve or null to retrieve the root page.
     * @return the page
     */
    @Nullable
    public static synchronized DocumentationPage getPage(@Nullable String pageURI) {
        return pageURI == null || pageURI.equals(RootPage.ROOT_URI) ? rootPage : uriPageMap.get(pageURI);
    }

    public static Collection<DocumentationPage> getAllPages() {
        return uriPageMap.values();
    }

    public static Collection<String> getAllPageURIs() {
        return uriPageMap.keySet();
    }

    public static synchronized Map<String, ModStructurePage> getModStructureMap() {
        return modStructureMap;
    }

    public static Collection<String> getDocumentedMods() {
        return installedModVersionFileMap.keySet();
    }

    public static void setModVersionOverride(String modid, @Nullable String version) {
        if (version == null) {
            PIConfig.modVersionOverrides.remove(modid);
        }
        else {
            List<String> versions = sortedModVersionMap.get(modid);
            if (versions != null && versions.contains(version)) {
                PIConfig.modVersionOverrides.put(modid, version);
            }
            else {
                PIConfig.modVersionOverrides.remove(modid);
            }
        }
        PIConfig.save();
        checkAndReloadDocFiles();
    }

    //endregion

    public static void clear() {
        uriPageMap.clear();
        modStructureMap.clear();
        structureFileMap.clear();
        installedModVersionFileMap.clear();
        sortedModVersionMap.clear();
        activeModVersionMap.clear();
        packDocFileMap.clear();
        contentRelationsMap.clear();
        loadRootPage();
        LanguageManager.clearTranslations();
    }

    //# Content Relations
    //region //############################################################################

    public static void clearRelationCache() {
        contentRelationsMap.clear();
    }

    private static void checkInitRelationMap() {
        if (contentRelationsMap.isEmpty()) {
            for (DocumentationPage page : uriPageMap.values()) {
                for (ContentRelation relation : page.relations) {
                    contentRelationsMap.computeIfAbsent(relation.type, type -> new HashMap<>()).put(relation, page);
                }
            }
        }
    }

    public static List<DocumentationPage> getRelatedPages(ItemStack stack) {
        checkInitRelationMap();
        List<DocumentationPage> results = new ArrayList<>();

        Map<ContentRelation, DocumentationPage> candidates = contentRelationsMap.get(STACK);
        if (candidates != null) {
            for (ContentRelation relation : candidates.keySet()) {
                DocumentationPage page = candidates.get(relation);
                if (relation.isMatch(stack) && !results.contains(page)) {
                    results.add(page);
                }
            }
        }

        return results;
    }

    public static List<DocumentationPage> getRelatedPages(Fluid stack) {
        checkInitRelationMap();
        List<DocumentationPage> results = new ArrayList<>();

        Map<ContentRelation, DocumentationPage> candidates = contentRelationsMap.get(FLUID);
        if (candidates != null) {
            for (ContentRelation relation : candidates.keySet()) {
                DocumentationPage page = candidates.get(relation);
                if (relation.isMatch(stack) && !results.contains(page)) {
                    results.add(page);
                }
            }
        }

        return results;
    }

    public static List<DocumentationPage> getRelatedPages(String entityRegName) {
        checkInitRelationMap();
        List<DocumentationPage> results = new ArrayList<>();

        Map<ContentRelation, DocumentationPage> candidates = contentRelationsMap.get(ENTITY);
        if (candidates != null) {
            for (ContentRelation relation : candidates.keySet()) {
                DocumentationPage page = candidates.get(relation);
                if (relation.contentString.equals(entityRegName) && !results.contains(page)) {
                    results.add(page);
                }
            }
        }

        return results;
    }

    //endregion

    //# Add / Remove Doc
    //region //############################################################################

    public static void addMod(String modid, String modName, String version) throws IOException {
        if (!PIConfig.editMode()) {
            return;
        }

        ModStructurePage modPage = new ModStructurePage(null, modid, version, false);
        File modFolder = new File(getDocDirectory(), modid + "/" + version + "/structure");

        if (!modFolder.exists() && !modFolder.mkdirs()) {
            throw new IOException("Failed to create mod directory! " + modFolder);
        }

        File structure = new File(modFolder, "structure.json");

        if (structure.exists()) {
            throw new IOException("Mod structure file already exists! Perhaps it is invalid? Structure file:" + structure);
        }

        structureFileMap.put(modPage, structure);
        modStructureMap.put(modid, modPage);
        saveDocToDisk(modPage);


        LanguageManager.setPageName(modid, modid + ":", modName, LanguageManager.getUserLanguage());
        checkAndReloadDocFiles();
    }

    public static void addLocalDoc(String docID, String docName) throws IOException {
        if (!PIConfig.editMode()) {
            return;
        }

        ModStructurePage docPage = new ModStructurePage(null, docID, "", true);
        File docFolder = new File(getPackDocDirectory(), docID + "/structure");

        if (!docFolder.exists() && !docFolder.mkdirs()) {
            throw new IOException("Failed to create doc directory! " + docFolder);
        }

        File structure = new File(docFolder, "structure.json");

        if (structure.exists()) {
            throw new IOException("Mod structure file already exists! Perhaps it is invalid? Structure file:" + structure);
        }

        structureFileMap.put(docPage, structure);
        modStructureMap.put(docID, docPage);
        saveDocToDisk(docPage);

        LanguageManager.setPageName(docID, docID + ":", docName, LanguageManager.getUserLanguage());
        checkAndReloadDocFiles();
    }

    public static void deleteDoc(DocumentationPage page) {
        getDocDirectory();
        if (!PIConfig.editMode()) {
            return;
        }

        File docFolder = new File(page.isPackDoc() ? getPackDocDirectory() : getDocDirectory(), page.getModid());
        if (docFolder.exists() && docFolder.isDirectory()) {
            try {
                FileUtils.deleteDirectory(docFolder);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (page instanceof ModStructurePage){
            loadDocumentationFromDisk();
        }else {
            uriPageMap.remove(page.pageURI);
        }
    }

    //endregion

    public static boolean doesPageExist(String pageURI) {
        return pageURI.equals(RootPage.ROOT_URI) || uriPageMap.containsKey(pageURI);
    }
}
