package com.brandon3055.projectintelligence.client.gui.moddata.guidoctree;

import com.brandon3055.projectintelligence.client.gui.moddata.WikiDocManager;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.moddata.ModDocContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StringUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by brandon3055 on 7/09/2016.
 */
@Deprecated //Old Code
public class GuiDocTree {

    private TreeBranchRoot dataRootBranch;
    private GuiProjectIntelligence guiWiki;
    private TreeBranchRoot activeBranch;
    public String filterCategory = null;
    public String filterText = null;
    public Map<String, TreeBranchRoot> idToBranchMap = new HashMap<String, TreeBranchRoot>();

    public GuiDocTree(GuiProjectIntelligence guiWiki) {
        this.guiWiki = guiWiki;
        this.dataRootBranch = new TreeBranchRoot(guiWiki, null, "[Not Loaded]");
        this.activeBranch = this.dataRootBranch;
    }

    //region Load / Reload

    /**
     * Reloads the data tree from the WikiDocManager
     */
    public void reloadData() {
        idToBranchMap.clear();
        dataRootBranch = new TreeBranchRoot(guiWiki, null, I18n.format("guiwiki.label.mods"));
        activeBranch = dataRootBranch;

        if (WikiDocManager.projectIntelContainer != null) {
            dataRootBranch.branchData = WikiDocManager.projectIntelContainer.getElement("en_US");
            if (dataRootBranch.branchData != null) {
                dataRootBranch.loadBranchContent();
            }
        }

        String lang = guiWiki.mc.getLanguageManager().getCurrentLanguage().getLanguageCode();

        for (ModDocContainer modDoc : WikiDocManager.modDocMap.values()) {
            generateModBranch(modDoc.getElement(lang), modDoc.modid);
        }
    }

    private void generateModBranch(Element modData, String modid) {
        String modName = modData.getAttribute(WikiDocManager.ATTRIB_MOD_NAME);
        TreeBranchContent modBranch = new TreeBranchContent(guiWiki, dataRootBranch, modData, modName);
        modBranch.isModBranch = true;
        modBranch.setBranchID(modid);
        modBranch.loadBranchesXML();
        modBranch.initBranches();
        dataRootBranch.addSubBranch(modBranch);
    }

    //endregion

    //region Active Branch

    public TreeBranchRoot getActiveBranch() {
        return activeBranch;
    }

    public List<TreeBranchRoot> getActiveList() {
        if (activeBranch.subBranches.size() > 0) {
            return activeBranch.subBranches;
        }
        else if (activeBranch.subBranches.size() == 0 && activeBranch.parent != null) {
            return activeBranch.parent.subBranches;
        }
        else return new ArrayList<TreeBranchRoot>();
    }

    public void setActiveBranch(TreeBranchRoot activeBranch) {
        this.activeBranch = activeBranch;
//        guiWiki.contentList.reloadList();
        GuiProjectIntelligence.activePath = activeBranch.branchID;
//        guiWiki.contentWindow.setActiveBranch(activeBranch);
    }

    public void reOpenLast() {
        if (!StringUtils.isNullOrEmpty(GuiProjectIntelligence.activePath) && idToBranchMap.containsKey(GuiProjectIntelligence.activePath)) {
            setActiveBranch(idToBranchMap.get(GuiProjectIntelligence.activePath));
        }
    }

    //endregion

    //region Filter

    public void updateFilter() {
    }

    //endregion

}
