package com.brandon3055.projectintelligence.docdata;

import com.brandon3055.projectintelligence.PIHelpers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by brandon3055 on 11/08/2017.
 * This page represents a documentation page as it exists in json.
 */
public class DocumentationPage {
    public static Random random = new Random();

    protected LinkedList<DocumentationPage> subPages = new LinkedList<>();

    protected DocumentationPage parent = null;
    protected LinkedList<JsonObject> icons = new LinkedList<>();
    protected LinkedList<String> linked = new LinkedList<>();

    protected int revision;
    protected int targetEnRev;
    protected int sortingWeight;

    //    protected String lang;
    protected String modid;
    protected String pageId;
    protected String pageURI;

    /**
     * This is the earliest version of the mod to which this documentation applies.
     * This is a single version not a version range.
     * If there are multiple versions available e.g 2.0.0, 2.1.0, 2.2.0
     * and the version of the mod installed is say 2.1.4 then the doc for version 2.1.0 will be loaded.
     * And if the mod version is lower than the minimum e.g. 1.5.0 then no documentation will be loaded.
     * This string can only contain numbers and period's
     * This must should be the same as the name of the version folder it corresponds to.
     */
    protected String modVersion;
    protected String markdownFile = "";
    protected String rawMarkdownString;
//    protected Map<String, String> versionMdMap = new HashMap<>();

    protected boolean hidden;
    protected boolean cycle_icons;

    private LinkedList<String> mdLineCache = null;

    public DocumentationPage(DocumentationPage parent, String modid, String modVersion) {
        this.parent = parent;
        this.modid = modid;
        this.modVersion = modVersion;
    }

    public LinkedList<DocumentationPage> getSubPages() {
        return subPages;
    }

    public void generatePageURIs(String parentURI, Map<String, DocumentationPage> URIPageMap) {
        pageURI = (parentURI.endsWith(":") ? parentURI : parentURI + "/") + pageId;

        if (URIPageMap.containsKey(pageURI)) {
            PIHelpers.displayError("Detected duplicate page! " + modid + ":" + pageURI);
            return;
        }

        URIPageMap.put(pageURI, this);
        subPages.forEach(documentationPage -> documentationPage.generatePageURIs(pageURI, URIPageMap));
    }

    //############################################################################
    //# Getters
    //region //############################################################################

    public String getModid() {
        return modid;
    }

    public String getModPageName() {
        return LanguageManager.getPageName(getModid() + ":", getLocalizationLang());
    }

    public String getPageId() {
        return pageId;
    }

    public String getLocalizationLang() {
        return LanguageManager.getPageLanguage(getPageURI());
    }

    public boolean isHidden() {
        return hidden;
    }

    public LinkedList<JsonObject> getIcons() {
        return icons;
    }

    public boolean cycle_icons() {
        return cycle_icons;
    }

    public LinkedList<String> getLinked() {
        return linked;
    }

//    public boolean checkModVersion() {
//        return modVersion.isEmpty() || PIHelpers.doesModVersionMatch(modid, modVersion);
//    }

    public String getDisplayName() {
        return LanguageManager.getPageName(pageURI, getLocalizationLang());
    }

    public File getMarkdownFile() {
        return new File(DocumentationManager.getDocDirectory(), modid +"/" + modVersion + "/" + getLocalizationLang() + "/" + markdownFile);
    }

    public String getRawMarkdownString() {
        if (rawMarkdownString == null) {
            File mdFile = getMarkdownFile();

            try {
                rawMarkdownString = new String(Files.readAllBytes(mdFile.toPath()));
            }
            catch (IOException e) {
                rawMarkdownString = "";
            }
        }

        return rawMarkdownString;
    }

    public LinkedList<String> getMarkdownLines() {
        if (mdLineCache == null) {
            mdLineCache = new LinkedList<>();

            File mdFile = getMarkdownFile();
            if (!mdFile.exists()) {
                mdLineCache.add("Error Loading Page!");
                mdLineCache.add("Could not find page md file: " + mdFile);
            }
            else {
                try {
                    FileInputStream is = new FileInputStream(mdFile);
                    mdLineCache.addAll(IOUtils.readLines(is));
                    IOUtils.closeQuietly(is);
                }
                catch (IOException e) {
                    mdLineCache.add("Error Loading Page!");
                    mdLineCache.add("An error occurred while reading the md file!");
                    for (StackTraceElement el : e.getStackTrace()) {
                        mdLineCache.add(el.toString());
                    }
                }
            }
        }

        return mdLineCache;
    }

    public DocumentationPage getParent() {
        return parent;
    }

    public int getSortingWeight() {
        return sortingWeight;
    }

//    public String getModVersions() {
//        return modVersion;
//    }

    public int getRevision() {
        return revision;
    }

    /**
     * This is the page file path only used for locating the pages md file. {@link #getPageURI()} should be used for everything else.
     *
     * @return the page id with the parent page path prefixed. e.g. fusionCrafting/structureSetup
     */
    protected String getPageFilePath() {
        return parent == null || parent.getClass() != DocumentationPage.class ? getPageId() : parent.getPageId() + "/" + getPageId();
    }

    /**
     * @return the "Uniform Resource Identifier" for this page. This can be used to identity or find a specific page. An example URI may be "draconicevolution:fusionCrafting/fusionInjector"
     */
    public String getPageURI() {
        return pageURI;
    }

    public String getModVersion() {
        return modVersion;
    }

    //endregion

    //############################################################################
    //# Setters (These write to disk)
    //region //############################################################################

    public void setRawMarkdown(String rawMarkdownString) throws MDException {
        if (getPageId().isEmpty()) {
            throw new MDException("Please set page id before writing content. Page id is used to save content to disk.");
        }

        this.rawMarkdownString = rawMarkdownString;
        mdLineCache = null;

        if (markdownFile.isEmpty()) {
            do {
                markdownFile = getPageFilePath() + "_" + genHex() + ".md";
            } while (getMarkdownFile().exists());
        }

        try {
            FileUtils.writeStringToFile(getMarkdownFile(), rawMarkdownString);
            saveToDisk();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new MDException("An error occurred while saving markdown to disk. " + e.getMessage() + " See console for full stack trace");
        }
    }

//    public void setDisplayName(String displayName) {
//        this.displayName = displayName;
//        saveToDisk();
//    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
        saveToDisk();
    }

    public void setSortingWeight(int sortingWeight) {
        this.sortingWeight = sortingWeight;
        saveToDisk();
    }

//    public void setModVersion(String modVersion) {
//        this.modVersion = modVersion;
//        saveToDisk();
//    }

    public void setRevision(int revision) {
        this.revision = revision;
        saveToDisk();
    }

    public void setTargetEnRev(int targetEnRev) {
        this.targetEnRev = targetEnRev;
        saveToDisk();
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        saveToDisk();
    }

    public void setCycle_icons(boolean cycle_icons) {
        this.cycle_icons = cycle_icons;
        saveToDisk();
    }

    //endregion

    //############################################################################
    //# Read and Write to JSON
    //region //############################################################################

    public void loadFromJson(JsonObject jObj) {
        pageId = JsonUtils.getString(jObj, "id", "<invalid-id>");
//        modVersion = JsonUtils.getString(jObj, "mod_versions", "");
//        displayName = JsonUtils.getString(jObj, "name", "[Unnamed]");
        markdownFile = JsonUtils.getString(jObj, "file", "");

        revision = JsonUtils.getInt(jObj, "revision", 0);
//        targetEnRev = JsonUtils.getInt(jObj, "target_en_rev", -1);
        sortingWeight = JsonUtils.getInt(jObj, "sorting_weight", 0);
        cycle_icons = JsonUtils.getBoolean(jObj, "cycle_icons", false);
        hidden = JsonUtils.getBoolean(jObj, "hidden", false);

        if (JsonUtils.isJsonArray(jObj, "linked")) {
            linked.clear();
            for (JsonElement element : JsonUtils.getJsonArray(jObj, "linked")) {
                if (JsonUtils.isString(element)) {
                    linked.add(element.getAsJsonPrimitive().getAsString());
                }
            }
        }

        if (JsonUtils.isJsonArray(jObj, "icons")) {
            icons.clear();
            for (JsonElement element : JsonUtils.getJsonArray(jObj, "icons")) {
                if (element.isJsonObject()) {
                    icons.add(element.getAsJsonObject());
                }
            }
        }

        if (JsonUtils.isJsonArray(jObj, "pages")) {
            loadSubPages(JsonUtils.getJsonArray(jObj, "pages"));
        }
    }

    public void loadSubPages(JsonArray pageArray) {
        subPages.clear();

        for (JsonElement element : pageArray) {
            if (element.isJsonObject()) {
                DocumentationPage subPage = new DocumentationPage(this, modid, modVersion);
                subPage.loadFromJson(element.getAsJsonObject());
                subPages.add(subPage);
            }
        }
    }

    public JsonObject writeToJson() {
        JsonObject jObj = new JsonObject();

        jObj.addProperty("id", pageId);
//        jObj.addProperty("name", displayName);
        jObj.addProperty("file", markdownFile);
//        jObj.addProperty("mod_versions", modVersion);
        jObj.addProperty("cycle_icons", cycle_icons);
        jObj.addProperty("hidden", hidden);

        if (revision > 0) jObj.addProperty("revision", revision);
//        if (targetEnRev != -1) jObj.addProperty("target_en_rev", targetEnRev);
        if (sortingWeight > 0) jObj.addProperty("sorting_weight", sortingWeight);

        if (linked.size() > 0) {
            JsonArray array = new JsonArray();
            for (String link : linked) {
                array.add(new JsonPrimitive(link));
            }
            jObj.add("linked", array);
        }

        if (icons.size() > 0) {
            JsonArray array = new JsonArray();
            for (JsonObject icon : icons) {
                array.add(icon);
            }
            jObj.add("icons", array);
        }

        if (getSubPages().size() > 0) {
            JsonArray subPages = new JsonArray();
            writeSubPages(subPages);
            jObj.add("pages", subPages);
        }

        return jObj;
    }

    public void writeSubPages(JsonArray pageArray) {
        for (DocumentationPage page : getSubPages()) {
            pageArray.add(page.writeToJson());
        }
    }

    public void saveToDisk() {
        if (!(this instanceof ModStructurePage) && parent != null) {
            parent.saveToDisk();
        }
        else if (this instanceof ModStructurePage) {
            DocumentationManager.saveModToDisk((ModStructurePage) this);
        }
    }

    //endregion

    @Override
    public String toString() {
        return getDisplayName();
    }

    private static String genHex() {
        StringBuffer sb = new StringBuffer();
        while (sb.length() < 5) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.toString().substring(0, 5);
    }

    public void deletePage() {
        getSubPages().forEach(DocumentationPage::deletePage);
        if (getMarkdownFile().exists()) {
            getMarkdownFile().delete();
        }

        if (parent != null) {
            parent.getSubPages().remove(this);
            parent.saveToDisk();
        }
    }

    public static class MDException extends Exception {
        public MDException(String message) {
            super(message);
        }
    }
}
