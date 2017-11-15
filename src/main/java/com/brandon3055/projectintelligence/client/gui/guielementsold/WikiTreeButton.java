package com.brandon3055.projectintelligence.client.gui.guielementsold;

import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiVerticalButton;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.moddata.guidoctree.TreeBranchRoot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.io.IOException;

import static com.brandon3055.projectintelligence.client.gui.PIConfig.NAV_WINDOW;

/**
 * Created by brandon3055 on 8/09/2016.
 */
public class WikiTreeButton extends MGuiVerticalButton {

    private final TreeBranchRoot branch;

    public WikiTreeButton(int xPos, int yPos, int xSize, int ySize, String buttonText, TreeBranchRoot branch) {
        super("", xPos, yPos, xSize, ySize, buttonText);
        this.branch = branch;
        setShadow(false);
        setColours(0, 0, 0);
    }

    @Override
    public int getTextColour(boolean hovered, boolean disabled) {
        return mixColours(PIConfig.TEXT_COLOUR, (hovered ? 0x202020 : 0));
    }

    @Override
    public void renderElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        int fill = isMouseOver(mouseX, mouseY) ? mixColours(NAV_WINDOW, 0x00202020, true) : 0;
        int border = isMouseOver(mouseX, mouseY) ? mixColours(NAV_WINDOW, 0x00404040, false) : mixColours(NAV_WINDOW, 0x00202020, true);
        drawBorderedRect(xPos() + 0.5, yPos() + 0.5, xSize() - 1.5, ySize() - 1, 0.5, fill, border);
        super.renderElement(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            branch.guiWiki.wikiDataTree.setActiveBranch(branch);
            return true;
        }
        return false;
    }

}
