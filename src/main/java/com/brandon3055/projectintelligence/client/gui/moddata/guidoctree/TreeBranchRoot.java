package com.brandon3055.projectintelligence.client.gui.moddata.guidoctree;

import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiListEntry;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.moddata.WikiDocManager;
import com.brandon3055.projectintelligence.client.gui.moddata.displaycontent.DisplayComponentBase;
import com.brandon3055.projectintelligence.client.gui.moddata.displaycontent.DisplayComponentRegistry;
import com.brandon3055.projectintelligence.client.gui.swing.UIAddModBranch;
import com.brandon3055.projectintelligence.utils.LogHelper;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created by brandon3055 on 7/09/2016.
 */
@Deprecated //Old Code
public class TreeBranchRoot extends MGuiListEntry {
    public static final String ATTRIB_WEIGHT = "sortingWeight";

    public Element branchData;
    public GuiProjectIntelligence guiWiki;
    public LinkedList<TreeBranchRoot> subBranches = new LinkedList<TreeBranchRoot>();
    public LinkedList<DisplayComponentBase> branchContent = new LinkedList<DisplayComponentBase>();
    public TreeBranchRoot parent;
    public String branchName = "[Unknown Branch]";
    public String branchID = "ROOT";
    public boolean isModBranch = false;
    public int sortingWeight = 0;

    public TreeBranchRoot(GuiProjectIntelligence guiWiki, TreeBranchRoot parent, String branchName) {
        this.parent = parent;
        this.branchName = branchName;
        this.guiWiki = guiWiki;
//        setSize(guiWiki.contentList != null ? guiWiki.contentList.getListEntryWidth() : 50, 20);
    }

    //region Init

    public void initBranches() {
        for (TreeBranchRoot sub : subBranches) {
            sub.initBranches();
        }
    }

    public void addSubBranch(TreeBranchRoot branch) {
        subBranches.add(branch);
    }

    //endregion

    //region List

    @Override
    public int getEntryHeight() {
        return ySize();
    }

    @Override
    public void moveEntry(int newXPos, int newYPos) {
//        xPos = newXPos;
//        yPos = newYPos;
//        for (MGuiElementBase element : childElements) {
//            element.xPos = newXPos;
//            element.yPos = newYPos;
//        }
        translate(newXPos, newYPos);
        positionElements(newXPos, newYPos);
    }

    public void positionElements(int newXPos, int newYPos) {
    }

    //endregion

    //region Misc

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY)) {
            guiWiki.wikiDataTree.setActiveBranch(this);
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }


        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Called when the user clicks the [add] button to create a new branch.
     */
    public void createNewSubBranch() {
        UIAddModBranch frame = new UIAddModBranch(this);
        frame.pack();
        frame.setVisible(true);
//        SwingHelper.centerOnMinecraftWindow(frame);
    }

    /**
     * Called by the UI. This actually creates the branch.
     */
    public void createNewSubBranch(String name, String id, String category, TreeBranchRoot branch) {
//        guiWiki.contentList.toAddList.add(new GuiPartContentListOld.ToAdd(name, id, category, branch));
    }

    public void setBranchID(String id) {
        branchID = id;
        guiWiki.wikiDataTree.idToBranchMap.put(id, this);
    }

    /**
     * Saves all changed data to disk
     */
    public void save() throws Exception {
        WikiDocManager.saveChanges(branchData.getOwnerDocument());
    }

    //endregion

    //region Content

    public void loadBranchContent() {
        branchContent.clear();

        if (branchData.hasAttribute(ATTRIB_WEIGHT)) {
            try {
                sortingWeight = Integer.parseInt(branchData.getAttribute(ATTRIB_WEIGHT));
                if (sortingWeight < 0) {
                    sortingWeight = 0;
                }
            }
            catch (Exception e) {
            }
        }

        NodeList nodeList = branchData.getElementsByTagName(WikiDocManager.ELEMENT_CONTENT);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getParentNode() != branchData) {
                continue;
            }

            if (!(node instanceof Element)) {
                LogHelper.dev("Node Is Not An Element: " + node);
                continue;
            }

            Element content = (Element) node;
            String type = content.getAttribute(WikiDocManager.ATTRIB_TYPE);

            DisplayComponentBase displayComponent = DisplayComponentRegistry.createComponent(guiWiki, type, content, this);

            if (displayComponent == null) {
                continue;
            }

            branchContent.add(displayComponent);
        }

        Collections.sort(branchContent, CONTENT_SORTER);
    }

    public static Comparator<DisplayComponentBase> CONTENT_SORTER = new Comparator<DisplayComponentBase>() {
        @Override
        public int compare(DisplayComponentBase o1, DisplayComponentBase o2) {
            return o1.posIndex < o2.posIndex ? -1 : o1.posIndex > o2.posIndex ? 1 : 0;
        }
    };

    public static Comparator<TreeBranchRoot> BRANCH_SORTER = new Comparator<TreeBranchRoot>() {
        @Override
        public int compare(TreeBranchRoot o1, TreeBranchRoot o2) {
            return o1.sortingWeight < o2.sortingWeight ? -1 : o1.sortingWeight > o2.sortingWeight ? 1 : 0;
        }
    };

    //endregion
}
