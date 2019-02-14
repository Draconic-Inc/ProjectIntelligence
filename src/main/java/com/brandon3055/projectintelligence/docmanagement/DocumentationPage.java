package com.brandon3055.projectintelligence.docmanagement;

import com.brandon3055.brandonscore.integration.ModHelperBC;
import com.brandon3055.projectintelligence.client.DisplayController;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.docmanagement.LanguageManager.PageLangData;
import com.google.common.base.Objects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * Created by brandon3055 on 11/08/2017.
 * This page represents a documentation page as it exists in json.
 */
public class DocumentationPage {
    public static Random random = new Random();

    protected LinkedList<DocumentationPage> subPages = new LinkedList<>();

    protected DocumentationPage parent = null;
    protected LinkedList<JsonObject> icons = new LinkedList<>();
    protected LinkedList<ContentRelation> relations = new LinkedList<>();

    protected int sortingWeight;

    protected String modid;
    protected String pageId;
    protected String pageURI;

    protected boolean isPackDoc;

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

    protected boolean hidden;
    protected boolean cycle_icons;

    public final int treeDepth;

    private LinkedList<String> mdLineCache = null;

    public DocumentationPage(DocumentationPage parent, String modid, String modVersion, boolean isPackDoc) {
        this.parent = parent;
        this.modid = modid;
        this.modVersion = modVersion;
        this.isPackDoc = isPackDoc;
        this.treeDepth = parent == null ? 0 : parent.treeDepth + 1;
    }

    public LinkedList<DocumentationPage> getSubPages() {
        return subPages;
    }

    public void generatePageURIs(String parentURI, Map<String, DocumentationPage> URIPageMap) {
        pageURI = (parentURI.endsWith(":") ? parentURI : parentURI + "/") + pageId;

        if (URIPageMap.containsKey(pageURI)) {
            PIGuiHelper.displayError("Detected duplicate page! " + modid + ":" + pageURI);
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

    public String getModName() {
        return ModHelperBC.getModName(modid);
    }

    public String getModPageName() {
        return LanguageManager.getPageName(getModid() + ":", getLocalizationLang());
    }

    /**This is the page simple id e.g. "draconium_ore"*/
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

    public LinkedList<ContentRelation> getRelations() {
        return relations;
    }

    public String getDisplayName() {
        return LanguageManager.getPageName(pageURI, getLocalizationLang());
    }

    public File getMarkdownFile() {
        if (isPackDoc) {
            return new File(DocumentationManager.getPackDocDirectory(), modid + "/" + getLocalizationLang() + "/" + markdownFile);
        }
        return new File(DocumentationManager.getDocDirectory(), modid +"/" + modVersion + "/" + getLocalizationLang() + "/" + markdownFile);
    }

    public boolean isPackDoc() {
        return isPackDoc;
    }

    private File lastFile = null;

    public LinkedList<String> getMarkdownLines() {
        if (PIConfig.editMode() || mdLineCache == null || !Objects.equal(lastFile, getMarkdownFile())) {
            mdLineCache = new LinkedList<>();

            File mdFile = getMarkdownFile();
            PageLangData data = LanguageManager.getLangData(pageURI, getLocalizationLang());
            if (data == null && !PIConfig.editMode()) {
                mdLineCache.add("This page has not yet been translated to your language!");
                mdLineCache.add("Click the \"Broken Language\" icon on the page button to select an alternate language.");
                mdLineCache.add("Or change your selected language in PI settings (PI language can be set independently from Minecraft language)");
            }
            else if (!mdFile.exists() || markdownFile.isEmpty()) {
                mdLineCache.add("Error Loading Page!");
                if (markdownFile.isEmpty()) {
                    mdLineCache.add("There is no document file listed for this page. This most likely means the documentation for this page has not been written yet.");
                }
                else {
                    mdLineCache.add("Could not find page md file: " + mdFile);
                }
            }
            else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mdFile), StandardCharsets.UTF_8))) {
                    String s;
                    while ((s = reader.readLine()) != null) {
                        mdLineCache.add(s);
                    }
                }
                catch (IOException e) {
                    mdLineCache.add("Error Loading Page!");
                    mdLineCache.add("An error occurred while reading the md file! ");
                    mdLineCache.add("File: " + mdFile);
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
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(getMarkdownFile()), StandardCharsets.UTF_8);
            writer.write(rawMarkdownString);
            IOUtils.closeQuietly(writer);

//            FileUtils.writeStringToFile(getMarkdownFile(), rawMarkdownString, StandardCharsets.UTF_8);
            saveToDisk();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new MDException("An error occurred while saving markdown to disk. " + e.getMessage() + " See console for full stack trace");
        }

        DisplayController.MASTER_CONTROLLER.getActiveTab().reloadTab();
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
        saveToDisk();
    }

    public void setSortingWeight(int sortingWeight) {
        this.sortingWeight = sortingWeight;
        saveToDisk();
    }

    public void setRevision(int revision) {
        PageLangData data = LanguageManager.getLangData(pageURI, LanguageManager.getUserLanguage());
        if (data != null) {
            data.pageRev = revision;
        }
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
        markdownFile = JsonUtils.getString(jObj, "file", "");
        sortingWeight = JsonUtils.getInt(jObj, "sorting_weight", 0);
        cycle_icons = JsonUtils.getBoolean(jObj, "cycle_icons", false);
        hidden = JsonUtils.getBoolean(jObj, "hidden", false);

        if (JsonUtils.isJsonArray(jObj, "relations")) {
            relations.clear();
            for (JsonElement element : JsonUtils.getJsonArray(jObj, "relations")) {
                if (element.isJsonObject()) {
                    ContentRelation relation = ContentRelation.fromJson(element.getAsJsonObject());
                    if (relation != null) {
                        relations.add(relation);
                    }
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
                DocumentationPage subPage = new DocumentationPage(this, modid, modVersion, isPackDoc);
                subPage.loadFromJson(element.getAsJsonObject());
                subPages.add(subPage);
            }
        }
    }

    public JsonObject writeToJson() {
        JsonObject jObj = new JsonObject();

        jObj.addProperty("id", pageId);
        jObj.addProperty("file", markdownFile);
        jObj.addProperty("cycle_icons", cycle_icons);
        jObj.addProperty("hidden", hidden);

        if (sortingWeight > 0) jObj.addProperty("sorting_weight", sortingWeight);

        if (relations.size() > 0) {
            JsonArray array = new JsonArray();
            for (ContentRelation relation : relations) {
                array.add(relation.toJson());
            }
            jObj.add("relations", array);
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
            DocumentationManager.saveDocToDisk((ModStructurePage) this);
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
