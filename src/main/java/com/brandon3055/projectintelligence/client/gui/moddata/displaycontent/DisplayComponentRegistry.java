package com.brandon3055.projectintelligence.client.gui.moddata.displaycontent;

import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.moddata.guidoctree.TreeBranchRoot;
import com.brandon3055.projectintelligence.utils.LogHelper;
import org.w3c.dom.Element;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 8/09/2016.
 */
public class DisplayComponentRegistry {

    public static final Map<String, IDisplayComponentFactory> REGISTRY = new LinkedHashMap<String, IDisplayComponentFactory>();

    static {
        register(new DCHeading.Factory());
        register(new DCTextArea.Factory());
        register(new DCImage.Factory());
        register(new DCLink.Factory());
        register(new DCRecipe.Factory());
        register(new DCStack.Factory());
        register(new DCVSpacer.Factory());
        register(new DCSplitContainer.Factory());
    }

    private static void register(IDisplayComponentFactory factory) {
        REGISTRY.put(factory.getID(), factory);
    }

    public static DisplayComponentBase createComponent(GuiProjectIntelligence guiWiki, String componentID, Element element, TreeBranchRoot branch) {
        if (!REGISTRY.containsKey(componentID)) {
            LogHelper.error("Found Unknown Content Type: %s in branch: %s", componentID, branch.branchID);
            return null;
        }

        DisplayComponentBase displayComponent = REGISTRY.get(componentID).createNewInstance(guiWiki, branch, guiWiki.width, guiWiki.height);
        try {
            displayComponent.loadFromXML(element);
        }
        catch (Exception e) {
            LogHelper.error("An error occurred while loading %s element for branch: %s", componentID, branch.branchID);
            e.printStackTrace();
            return null;
        }
        return displayComponent;
    }
}
