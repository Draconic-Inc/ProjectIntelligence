package com.brandon3055.projectintelligence.docmanagement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.JsonUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 11/08/2017.
 * This is the root page for a mod. This page is serialized to jason and saved to the mods structure.json file
 */
public class ModStructurePage extends DocumentationPage {

    /**
     * This is a list of alternate mod id's for this mod.
     * Useful if a mod changes its mod id
     */
    protected List<String> modAliases = new LinkedList<>();
    protected Map<String, DocumentationPage> URIPageMap = new HashMap<>();

    protected ModStructurePage(DocumentationPage parent, String modid, String modVersion, boolean isPackDoc) {
        super(parent, modid, modVersion, isPackDoc);
    }

    public File getMarkdownFile() {
        if (isPackDoc) {
            return new File(DocumentationManager.getPackDocDirectory(), modid + "/" + "structure" + "/"+ getLocalizationLang() + "/" + markdownFile);
        }
        return new File(DocumentationManager.getDocDirectory(), modid +"/" + modVersion + "/" + "structure" + "/"+ getLocalizationLang() + "/" + markdownFile);
    }

    @Override
    public String getPageId() {
        return getModid();
    }

    public List<String> getModAliases() {
        return modAliases;
    }

    private void generatePageURIs() {
        URIPageMap.clear();
        pageURI = modid + ":";
        URIPageMap.put(pageURI, this);
        subPages.forEach(documentationPage -> documentationPage.generatePageURIs(pageURI, URIPageMap));
    }

    public Map<String, DocumentationPage> getURIPageMap() {
        return URIPageMap;
    }

    public File getVersionDir() {
        if (isPackDoc) {
            return new File(DocumentationManager.getPackDocDirectory(), modid);
        }
        return new File(DocumentationManager.getDocDirectory(), modid + "/" + modVersion);
    }

    public File getStructureDir() {
        return new File(getVersionDir(),"structure");
    }

    //# Read and Write to JSON
    //region //############################################################################

    @Nullable
    public static ModStructurePage generateFromJson(JsonObject jObj, boolean isPackDoc) {
        if (!JsonUtils.isString(jObj, "mod_id") || !JsonUtils.isString(jObj, "mod_version")) {
            return null;
        }

        ModStructurePage page = new ModStructurePage(null, JsonUtils.getString(jObj, "mod_id"), JsonUtils.getString(jObj, "mod_version"), isPackDoc);
        page.loadFromJson(jObj);
        return page;
    }

    @Override
    public void loadFromJson(JsonObject jObj) {
        markdownFile = JsonUtils.getString(jObj, "file", "");

        if (JsonUtils.isJsonArray(jObj, "mod_aliases")) {
            modAliases.clear();
            for (JsonElement element : JsonUtils.getJsonArray(jObj, "mod_aliases")) {
                if (JsonUtils.isString(element)) {
                    modAliases.add(element.getAsJsonPrimitive().getAsString());
                }
            }
        }

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
        cycle_icons = JsonUtils.getBoolean(jObj, "cycle_icons", false);

        if (JsonUtils.isJsonArray(jObj, "pages")) {
            loadSubPages(JsonUtils.getJsonArray(jObj, "pages"));
        }

        generatePageURIs();
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject jObj = new JsonObject();
        jObj.addProperty("mod_id", modid);
        jObj.addProperty("mod_version", modVersion);
        jObj.addProperty("file", markdownFile);

        if (modAliases.size() > 0) {
            JsonArray array = new JsonArray();
            for (String id : modAliases) {
                array.add(new JsonPrimitive(id));
            }
            jObj.add("mod_aliases", array);
        }

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
        jObj.addProperty("cycle_icons", cycle_icons);

        if (getSubPages().size() > 0) {
            JsonArray subPages = new JsonArray();
            writeSubPages(subPages);
            jObj.add("pages", subPages);
        }

        return jObj;
    }

    @Override
    public void deletePage() {

    }

    //endregion
}
