package com.brandon3055.projectintelligence.docdata;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Created by brandon3055 on 11/08/2017.
 * This is the root page for a mod. This page is serialized to jason and saved to the mods structure.json file
 */
public class RootPage extends DocumentationPage {

    public static final String ROOT_URI = "[pi_root_page]";
//
//    /**
//     * This is a list of alternate mod id's for this mod.
//     * Useful if a mod changes its mod id
//     */
//    protected Map<String, DocumentationPage> URIPageMap = new HashMap<>();


    protected RootPage() {
        super(null, ROOT_URI, "0.0.0");
    }

    public void addModPage(ModStructurePage page) {
        subPages.add(page);
        page.parent = this;
    }

    public File getMarkdownFile() {
        return new File(DocumentationManager.getDocDirectory(), modid +"/" + modVersion + "/" + "structure" + "/"+ getLocalizationLang() + "/" + markdownFile);
    }

    @Override
    public String getPageId() {
        return getModid();
    }

    @Override
    public String getPageURI() {
        return ROOT_URI;
    }

    @Override
    public String getDisplayName() {
        return I18n.format("pi.root_page.name");
    }

    //    private void generatePageURIs() {
//        URIPageMap.clear();
//        pageURI = modid + ":";
//        URIPageMap.put(pageURI, this);
//        subPages.forEach(documentationPage -> documentationPage.generatePageURIs(pageURI, URIPageMap));
//    }

//    public Map<String, DocumentationPage> getURIPageMap() {
//        return URIPageMap;
//    }


    @Nullable
    public static RootPage generateFromJson(JsonObject jObj) {
//        if (!JsonUtils.isString(jObj, "mod_id") || !JsonUtils.isString(jObj, "mod_version")) {
//            return null;
//        }
//
//        RootPage page = new RootPage(null, JsonUtils.getString(jObj, "mod_id"), JsonUtils.getString(jObj, "mod_version"));
//        page.loadFromJson(jObj);
//        return page;
        return null;
    }

    @Override
    public void loadFromJson(JsonObject jObj) {
//        markdownFile = JsonUtils.getString(jObj, "file", "");
//
//        if (JsonUtils.isJsonArray(jObj, "mod_aliases")) {
//            modAliases.clear();
//            for (JsonElement element : JsonUtils.getJsonArray(jObj, "mod_aliases")) {
//                if (JsonUtils.isString(element)) {
//                    modAliases.add(element.getAsJsonPrimitive().getAsString());
//                }
//            }
//        }
//
////        if (JsonUtils.isJsonArray(jObj, "root")) {
////            versionMdMap.clear();
////            for (JsonElement element : JsonUtils.getJsonArray(jObj, "root")) {
////                if (element.isJsonObject()) {
////                    JsonObject jso = element.getAsJsonObject();
////                    String version = JsonUtils.getString(jso, "mod_version", "any");
////                    String file = JsonUtils.getString(jso, "file", "[errorNotFound]");
////                    versionMdMap.put(version, file);
////                }
////            }
////        }
//
//
//        if (JsonUtils.isJsonArray(jObj, "linked")) {
//            linked.clear();
//            for (JsonElement element : JsonUtils.getJsonArray(jObj, "linked")) {
//                if (JsonUtils.isString(element)) {
//                    linked.add(element.getAsJsonPrimitive().getAsString());
//                }
//            }
//        }
//
//        if (JsonUtils.isJsonArray(jObj, "icons")) {
//            icons.clear();
//            for (JsonElement element : JsonUtils.getJsonArray(jObj, "icons")) {
//                if (element.isJsonObject()) {
//                    icons.add(element.getAsJsonObject());
//                }
//            }
//        }
//        cycle_icons = JsonUtils.getBoolean(jObj, "cycle_icons", false);
//
//        if (JsonUtils.isJsonArray(jObj, "pages")) {
//            loadSubPages(JsonUtils.getJsonArray(jObj, "pages"));
//        }
//
//        generatePageURIs();
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject jObj = new JsonObject();
//        jObj.addProperty("mod_id", modid);
//        jObj.addProperty("mod_version", modVersion);
//        jObj.addProperty("file", markdownFile);
//
////        if (!modVersionRange.isEmpty()) {
////            jObj.addProperty("mod_versions", modVersionRange);
////        }
////
//        if (modAliases.size() > 0) {
//            JsonArray array = new JsonArray();
//            for (String id : modAliases) {
//                array.add(new JsonPrimitive(id));
//            }
//            jObj.add("mod_aliases", array);
//        }
//
////        if (versionMdMap.size() > 0) {
////            JsonArray array = new JsonArray();
////            for (String version : versionMdMap.keySet()) {
////                JsonObject jso = new JsonObject();
////                jso.addProperty("mod_version", version);
////                jso.addProperty("file", versionMdMap.get(version));
////                array.add(jso);
////            }
////            jObj.add("root", array);
////        }
//
//
//        if (linked.size() > 0) {
//            JsonArray array = new JsonArray();
//            for (String link : linked) {
//                array.add(new JsonPrimitive(link));
//            }
//            jObj.add("linked", array);
//        }
//
//        if (icons.size() > 0) {
//            JsonArray array = new JsonArray();
//            for (JsonObject icon : icons) {
//                array.add(icon);
//            }
//            jObj.add("icons", array);
//        }
//        jObj.addProperty("cycle_icons", cycle_icons);
//
//        if (getSubPages().size() > 0) {
//            JsonArray subPages = new JsonArray();
//            writeSubPages(subPages);
//            jObj.add("pages", subPages);
//        }

        return jObj;
    }

    @Override
    public void deletePage() {

    }

    //endregion
}
