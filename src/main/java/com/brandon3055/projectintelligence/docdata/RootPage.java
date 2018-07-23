package com.brandon3055.projectintelligence.docdata;

import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.I18n;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by brandon3055 on 11/08/2017.
 * This is the root page for a mod. This page is serialized to jason and saved to the mods structure.json file
 */
public class RootPage extends DocumentationPage {

    public static final String ROOT_URI = "[pi_root_page]";

    protected RootPage() {
        super(null, ROOT_URI, "0.0.0", false);
    }

    public void addModPage(ModStructurePage page) {
        subPages.add(page);
        page.parent = this;
    }

    public DocumentationPage getHomePage() {
        DocumentationPage page = DocumentationManager.getPage(PIConfig.homePage);
        return page instanceof RootPage ? null : page;
    }

    @Override
    public File getMarkdownFile() {
        DocumentationPage homePage = getHomePage();
        if (homePage != null) {
            return homePage.getMarkdownFile();
        }
        return new File(DocumentationManager.getDocDirectory(), "Invalid-Home-Page");
    }

    @Override
    public LinkedList<String> getMarkdownLines() {
        DocumentationPage homePage = getHomePage();
        if (homePage == null) {
            return new LinkedList<>(Lists.newArrayList("The specified home page \""+ PIConfig.homePage+"\" does not exist!", //
                    "To set a new home page right-click on a page in the navigation pane and select \"Set as home page\""));
        }
        return homePage.getMarkdownLines();
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

    @Override
    public void loadFromJson(JsonObject jObj) {}

    @Override
    public JsonObject writeToJson() { return new JsonObject(); }

    @Override
    public void deletePage() {}

    //endregion
}
