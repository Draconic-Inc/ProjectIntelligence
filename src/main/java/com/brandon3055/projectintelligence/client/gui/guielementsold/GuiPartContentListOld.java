package com.brandon3055.projectintelligence.client.gui.guielementsold;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiButtonSolid;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiList;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiListEntry;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiScrollBar;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.moddata.WikiDocManager;
import com.brandon3055.projectintelligence.client.gui.moddata.guidoctree.TreeBranchRoot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.brandon3055.projectintelligence.client.gui.PIConfig.NAV_WINDOW;

/**
 * Created by brandon3055 on 31/08/2016.
 */
@Deprecated
public class GuiPartContentListOld extends MGuiList implements IGuiEventListener {
    private GuiProjectIntelligence guiMain;
    private GuiLabel navLabel;
    private GuiLabel branchLabel;
    private MGuiButtonSolid buttonToggleNav;
    private LinkedList<WikiTreeButton> treeButtons = new LinkedList<WikiTreeButton>();
    public List<ToAdd> toAddList = Collections.synchronizedList(new ArrayList<ToAdd>());
    private MGuiButtonSolid buttonAddBranch;
    private boolean navUpdateRequired = true;
    public boolean isHidden = false;
    private int maxXSize = 150;

    public GuiPartContentListOld(GuiProjectIntelligence parentGui) {
        guiMain = parentGui;
        topPadding = 24;
        leftPadding = 12;
        bottomPadding = 2;
    }

    //region Init

    @Override
    public void addChildElements() {
        toRemove.addAll(childElements);

        addChild(buttonToggleNav = ((MGuiButtonSolid) new MGuiButtonSolid("TOGGLE_PANEL", xSize() - 11, yPos() + 1, 10, 10, "<") {
            @Override
            public int getTextColour(boolean hovered, boolean disabled) {
                return mixColours(PIConfig.NAV_WINDOW, (hovered ? 0x606060 : 0x404040));
            }
        }.setListener(this).setHoverText("Toggle Navigation Window")).setColours(0, 0, 0));
        addChild(navLabel = new GuiLabel(xPos(), yPos(), xSize(), 12, I18n.format("guiwiki.label.navigation")) {
//            @Override
//            public int getTextColour() {
//                return GuiPartConfig.TEXT_COLOUR;
//            }
        }.setAlignment(GuiAlign.LEFT).setShadow(false));
        addChild(branchLabel = new GuiLabel(xPos() + 12, yPos() + 12, maxXSize - 12, 12, I18n.format("guiwiki.label.mods")) {
//            @Override
//            public int getTextColour() {
//                return GuiPartConfig.TEXT_COLOUR;
//            }
        }.setShadow(false));
        addChild(buttonAddBranch = (MGuiButtonSolid) new MGuiButtonSolid("ADD_BRANCH", xPos() + maxXSize - 40, yPos(), 28, 12, "[§4add§r]") {
            @Override
            public int getTextColour(boolean hovered, boolean disabled) {
                return hovered ? 0xFFFFFF : 0xFF000000;
            }
        }.setColours(0, 0, 0).setShadow(false));
        buttonAddBranch.setListener(this).setHoverText(new String[]{"Add new sub-branch to the selected branch", "Adds a new mod branch if on the Mods branch", "Otherwise adds a new sub branch."}).setEnabled(false);
        branchLabel.setTrim(true);

        super.addChildElements();
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
//        updateHiddenState();
//        updateNavButtons();
    }

    @Override
    protected void initScrollBar() {
        if (scrollBar != null) {
            removeChild(scrollBar);
        }
        scrollBar = new MGuiScrollBar(xPos() + xSize() - 5, yPos() + 23, 6, ySize() - 24) {
            @Override
            public int getScrollColour() {
                return mixColours(NAV_WINDOW, 0x00505050);
            }
        };
        addChild(scrollBar);
        scrollBar.setListener(this);
        scrollBar.parentScrollable = this;
        scrollBar.borderColour = 0x00000000;
        scrollBar.backColour = 0x00000000;
    }

    //endregion

    //region Render

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        int xPos = xPos();
        int yPos = yPos();
        int xSize = xSize();
        int ySize = ySize();
        int navWindowColour = NAV_WINDOW;
//        NAV_WINDOW = 0xFF300060;
//        //NAV_TEXT = 0x23FFFFFF;
//        NAV_WINDOW = 0xFF3c3f41;

//        int neg10 = mixColours(navWindowColour, 0x00101010, true);
//        int pos10 = mixColours(navWindowColour, 0x00101010);
//        int pos15 = mixColours(navWindowColour, 0x00151515);
//        int neg15 = mixColours(navWindowColour, 0x00151515, true);
//        int pos20 = mixColours(navWindowColour, 0x00202020);
//
//        drawColouredRect(xPos, yPos, xSize, ySize, navWindowColour);         //Main Window
//        drawColouredRect(xPos + xSize, yPos, 0.5, ySize, neg10);             //Window Right Accent
//        drawColouredRect(xPos, yPos, xSize, 12, pos10);                      //Bar
//        drawColouredRect(xPos, yPos, xSize, 0.5, pos20);                     //Bar Top Accent
//        drawColouredRect(xPos, yPos + 11.5, xSize, 0.5, neg10);              //Bar Bottom Accent
//        drawColouredRect(xPos + 11.5, yPos + 12, 0.5, ySize - 12, neg15);    //Left Divider
//        drawColouredRect(xPos + 12, yPos + 12, 0.5, ySize - 12, pos15);      //Left Divider
//
//        drawColouredRect(xPos, yPos + 12, xSize, 0.5, pos20);                //Mods Divider
//        drawColouredRect(xPos + 12, yPos + 23.5, xSize - 12, 0.5, neg10);    //Mods Divider
//
//        drawColouredRect(xPos + 12, yPos + ySize - 2, xSize - 12, 2, neg15);              //Window Bottom Accent

        //Draw Scroll Highlight
//        if (!isHidden && scrollBar.isEnabled() && GuiHelper.isInRect(scrollBar.xPos(), scrollBar.yPos(), scrollBar.xSize(), scrollBar.ySize(), mouseX, mouseY)) {
//            drawColouredRect(xPos + xSize - 4, yPos + 24, 4, ySize - 24, mixColours(scrollBar.scrollColour, 0xCC000000, true));
//        }

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    //endregion

    //region Interact

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase element) {
        if (element == buttonToggleNav) {
            isHidden = !isHidden;
            updateHiddenState();
        }
        else if (element == buttonAddBranch) {
            guiMain.wikiDataTree.getActiveBranch().createNewSubBranch();
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        for (MGuiElementBase element : nonListEntries) {
            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }

        for (MGuiElementBase element : listEntries) {
            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                navUpdateRequired = true;
                return true;
            }
        }

        return false;
    }

    //endregion

    //region Update

    @Override
    public boolean onUpdate() {
        checkAddQue();
        if (navUpdateRequired) {
            navUpdateRequired = false;
            updateNavButtons();

        }

        return super.onUpdate();
    }

    private void updateHiddenState() {
//        scrollBar.setEnabled(!isHidden && scrollBarEnabled);
//        scrollBar.setXPos(xPos() + maxXSize - 5);
//        navLabel.setEnabled(!isHidden);
//        branchLabel.setEnabled(!isHidden);
//        buttonAddBranch.setEnabled(!isHidden && GuiPartConfig.editMode);
//        setXSize(isHidden ? 12 : maxXSize);
//        buttonToggleNav.setXPos(xSize() - 11);
//        buttonToggleNav.setText(isHidden ? ">" : "<");
//        disableList = isHidden;
//
//        guiMain.updateWindowPositions();
//        guiMain.contentWindow.setActiveBranch(guiMain.wikiDataTree.getActiveBranch());
    }

    public void updateNavButtons() {
        TreeBranchRoot branch = guiMain.wikiDataTree.getActiveBranch().parent;
        removeChildByGroup("TREE_BUTTON_GROUP");
        treeButtons.clear();

        int currentLength = 0;

        while (branch != null) {
            int height = fontRenderer.getStringWidth(branch.branchName) + 6;
            if (currentLength + height > yPos() + ySize() - bottomPadding) {
                break;
            }

            WikiTreeButton newButton = new WikiTreeButton(xPos(), yPos() + 12, 12, height, branch.branchName, branch);
            newButton.addToGroup("TREE_BUTTON_GROUP");
            addChild(newButton);

            for (WikiTreeButton button : treeButtons) {
                button.translate(0, height);
                if (button.yPos() + button.ySize() > currentLength) {
                    currentLength = button.yPos() + button.ySize();
                }
            }

            treeButtons.add(newButton);
            branch = branch.parent;
        }
    }

    //endregion

    //region List

    public void reloadList() {
        clear();

        for (TreeBranchRoot branch : guiMain.wikiDataTree.getActiveList()) {
            addEntry(branch);
        }

        branchLabel.setLabelText(guiMain.wikiDataTree.getActiveBranch().branchName);
        updateNavButtons();
    }

    @Override
    public MGuiList addEntry(MGuiListEntry entry) {
        entry.setXSize(maxXSize - leftPadding - rightPadding - 4);
        return super.addEntry(entry);
    }

    //endregion

    //region misc

    public int getListEntryWidth() {
        return maxXSize - rightPadding - leftPadding;
    }

    //endregion

    //region Thread Sync TODO think of a better way to handle this

    private void checkAddQue() {
        if (toAddList.isEmpty()) {
            return;
        }

        for (ToAdd toAdd : toAddList) {
            Document doc = toAdd.branch.branchData.getOwnerDocument();
            Element subBranch = doc.createElement(WikiDocManager.ELEMENT_ENTRY);
            subBranch.setAttribute(WikiDocManager.ATTRIB_BRANCH_NAME, toAdd.name);
            subBranch.setAttribute(WikiDocManager.ATTRIB_BRANCH_ID, toAdd.branch.branchID + (toAdd.branch.isModBranch ? ":" : "/") + toAdd.id);
            subBranch.setAttribute(WikiDocManager.ATTRIB_ICON_TYPE, WikiDocManager.ICON_TYPE_OFF);
            if (!StringUtils.isNullOrEmpty(toAdd.category)) {
                subBranch.setAttribute(WikiDocManager.ATTRIB_BRANCH_CATEGORY, toAdd.category);
            }
            toAdd.branch.branchData.appendChild(subBranch);

            try {
                WikiDocManager.writeXMLToFile(doc, WikiDocManager.documentToFileMap.get(doc));

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            WikiDocManager.reload(true, true, true);
            if (guiMain.wikiDataTree.idToBranchMap.containsKey(toAdd.branch.branchID)) {
                guiMain.wikiDataTree.setActiveBranch(guiMain.wikiDataTree.idToBranchMap.get(toAdd.branch.branchID));
            }
        }

        toAddList.clear();
    }

    public static class ToAdd {
        public final String name;
        public final String id;
        public final String category;
        public final TreeBranchRoot branch;

        public ToAdd(String name, String id, String category, TreeBranchRoot branch) {
            this.name = name;
            this.id = id;
            this.category = category;
            this.branch = branch;
        }
    }

    //endregion
}
