package com.brandon3055.projectintelligence.client.gui.guielementsold;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTextField;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.*;
import com.brandon3055.projectintelligence.client.gui.moddata.WikiDocManager;
import com.brandon3055.projectintelligence.client.gui.moddata.guidoctree.TreeBranchContent;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by brandon3055 on 10/09/2016.
 */
public class PopupEditMod extends GuiPopUpDialogBase implements IGuiEventListener {

    private final TreeBranchContent branch;
    public GuiTextField nameField;
    public MGuiButtonSolid okButton;
    public MGuiButtonSolid cancelButton;
    public MGuiButtonSolid deleteButton;
    public MGuiButtonSolid confirmYes;
    public MGuiButtonSolid confirmNo;
    public GuiLabel errorLabel;

    public PopupEditMod(int xPos, int yPos, int xSize, int ySize, MGuiList parent, TreeBranchContent branch) {
        super(xPos, yPos, xSize, ySize, parent);
        this.branch = branch;
    }

    @Override
    public void addChildElements() {
        int xPos = xPos();
        int yPos = yPos();
        int xSize = xSize();
        int ySize = ySize();
        int y = yPos + 2;
        GuiLabel label = new GuiLabel(xPos, y, xSize, 12, "Edit Name").setAlignment(GuiAlign.LEFT);
        y += 12;
        addChild(label);
        addChild(nameField = new GuiTextField(xPos + 2, y, xSize - 4, 16));
        nameField.setText(branch.branchName);
        y += 30;
        addChild(okButton = (MGuiButtonSolid) new MGuiButtonSolid("OK", xPos + 2, y, (xSize / 2) - 2, 14, "OK").setColours(0xFF00a000, 0xFF000000, 0xFFFFFFFF).setListener(this));
        addChild(cancelButton = (MGuiButtonSolid) new MGuiButtonSolid("CANCEL", xPos + 1 + (xSize / 2), y, (xSize / 2) - 2, 14, "Cancel").setColours(0xFF504040, 0xFF000000, 0xFFFFFFFF).setListener(this));
        addChild(deleteButton = (MGuiButtonSolid) new MGuiButtonSolid("DELETE", xPos + 1 + (xSize / 2), yPos + ySize - 16, (xSize / 2) - 2, 14, "Delete").setColours(0xFFFF0000, 0xFF000000, 0xFFFFFFFF).setListener(this));

        GuiLabel confirm = new GuiLabel(xPos, yPos + ySize - 30, xSize, 12, "Are You Sure?");
        confirm.setId("CONFIRM_LABEL");
        addChild(confirm);
        setChildIDEnabled("CONFIRM_LABEL", false);

        addChild(confirmYes = (MGuiButtonSolid) new MGuiButtonSolid("DELETE_YES", xPos + 2, yPos + ySize - 16, (xSize / 2) - 2, 14, "Yes").setColours(0xFFFF0000, 0xFF000000, 0xFFFFFFFF).setListener(this));
        addChild(confirmNo = (MGuiButtonSolid) new MGuiButtonSolid("DELETE_NO", xPos + 1 + (xSize / 2), yPos + ySize - 16, (xSize / 2) - 2, 14, "No").setColours(0xFFFF0000, 0xFF000000, 0xFFFFFFFF).setListener(this));
        confirmYes.setEnabled(false);
        confirmNo.setEnabled(false);

        addChild(new GuiLabel(xPos, y, xSize, 100, "Mod ID can only be edited manually because its tied to every single branch. If you need to edit it I recommend opening the xml and Find&Replace modid: with newmodid:").setWrap(true));
        y += 80;
        addChild(errorLabel = new GuiLabel(xPos, y, xSize, 100, "").setWrap(true).setAlignment(GuiAlign.LEFT).setTextColour(0xFFFF0000));
        errorLabel.setEnabled(true);

        super.addChildElements();
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0xFF222222, 0xFFAA0000);
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        errorLabel.setEnabled(false);
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase element) {
        if (element == okButton) {
            try {
                editModBranch(nameField.getText(), false);
                close();
            }
            catch (Exception e) {
                errorLabel.setEnabled(true);
                errorLabel.setLabelText("Something went wrong while saving changes...\n\n" + e.getMessage() + "\n\nSee console for stack trace");
                e.printStackTrace();
            }
        }
        else if (element == cancelButton) {
            close();
        }
        else if (element == deleteButton) {
            setChildIDEnabled("CONFIRM_LABEL", true);
            confirmYes.setEnabled(true);
            confirmNo.setEnabled(true);
            deleteButton.setEnabled(false);
        }
        else if (element == confirmYes) {
            try {
                editModBranch("", true);
                close();
            }
            catch (Exception e) {
                errorLabel.setLabelText("Something went wrong while deleting the mod...\n\n" + e.getMessage() + "\n\nSee console for stack trace");
                e.printStackTrace();
            }
        }
        else if (element == confirmNo) {
            setChildIDEnabled("CONFIRM_LABEL", false);
            confirmYes.setEnabled(false);
            confirmNo.setEnabled(false);
            deleteButton.setEnabled(true);
        }
    }

    @Override
    public void close() {
        ((MGuiList) getParent()).disableList = false;
        getParent().removeChild(this);
    }

    public void editModBranch(String newName, boolean delete) throws Exception {
        if (delete) {
            File modXML = WikiDocManager.documentToFileMap.get(branch.branchData.getOwnerDocument());
            if (modXML != null && modXML.exists()) {
                if (modXML.delete()) {
                    WikiDocManager.reload(true, true, true);
                }
                else {
                    throw new Exception("For some reason the file return false when deleting meaning it did not delete. Not sure why. Maby try deleting it manually");
                }
            }
            else {
                throw new FileNotFoundException("Could not find the file to delete... Maby hit the reload button and try again. If that fails delete the file manually.");
            }
        }
        else {
            if (!branch.branchName.equals(newName)) {
                branch.branchName = newName;
                branch.branchData.setAttribute(WikiDocManager.ATTRIB_MOD_NAME, branch.branchName);
                WikiDocManager.saveChanges(branch.branchData.getOwnerDocument());
                WikiDocManager.reload(true, true, true);
            }
        }

    }
}
