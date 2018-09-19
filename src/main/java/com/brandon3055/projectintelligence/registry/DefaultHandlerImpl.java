package com.brandon3055.projectintelligence.registry;

import com.brandon3055.projectintelligence.api.IGuiDocHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.awt.*;

/**
 * Created by brandon3055 on 7/26/2018.
 */
public class DefaultHandlerImpl implements IGuiDocHandler<GuiScreen> {

    @Override
    public Rectangle getCollapsedArea(GuiScreen gui) {
        if (gui instanceof GuiContainer) {
            GuiContainer guiC = (GuiContainer) gui;
            return new Rectangle(guiC.getGuiLeft() - 25, guiC.getGuiTop() + 3, 25, 25);
        }
        else {
            return new Rectangle(0, gui.height / 2 - 12, 25, 25); //Without knowing anything about the UI best i can do is slap the PI button in a place that probably wont be in the way. (far left middle of the screen)
        }
    }

    @Override
    public Rectangle getExpandedArea(GuiScreen gui) {
        if (gui instanceof GuiContainer) {
            GuiContainer guiC = (GuiContainer) gui;
            int availWidth = guiC.getGuiLeft();
            if (availWidth < 160) {
                int width = Math.max(200, guiC.xSize);
                int height = Math.max(200, guiC.ySize);
                return new Rectangle(guiC.width / 2 - width / 2, guiC.height / 2 - height / 2, width, height);
            }
            int width = Math.max(availWidth - 25, Math.min(200, availWidth));
            int height = Math.max(guiC.ySize, 200);
            return new Rectangle(availWidth - width, guiC.height / 2 - height / 2, width, height);
        }
        return new Rectangle(gui.width / 2 - 100, gui.height / 2 - 100, 200, 200); //Again without knowing the bounds of the gui i can not put this next to the ui so i just put it on top of the ui in the middle of the screen.
    }
}
