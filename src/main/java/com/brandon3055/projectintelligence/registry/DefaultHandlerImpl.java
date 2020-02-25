package com.brandon3055.projectintelligence.registry;

import com.brandon3055.projectintelligence.api.IGuiDocHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

import java.awt.*;

/**
 * Created by brandon3055 on 7/26/2018.
 */
public class DefaultHandlerImpl implements IGuiDocHandler<Screen> {

    @Override
    public Rectangle getCollapsedArea(Screen gui) {
        if (gui instanceof ContainerScreen) {
            ContainerScreen guiC = (ContainerScreen) gui;
            return new Rectangle(guiC.getGuiLeft() - 25, guiC.getGuiTop() + 3, 25, 25);
        }
        else {
            return new Rectangle(0, gui.height / 2 - 12, 25, 25); //Without knowing anything about the UI best i can do is slap the PI button in a place that probably wont be in the way. (far left middle of the screen)
        }
    }

    @Override
    public Rectangle getExpandedArea(Screen gui) {
        if (gui instanceof ContainerScreen) {
            ContainerScreen guiC = (ContainerScreen) gui;
            int availWidth = guiC.getGuiLeft();
            if (availWidth < 160) {
                int width = Math.max(200, guiC.getXSize());
                int height = Math.max(200, guiC.getYSize());
                return new Rectangle(guiC.width / 2 - width / 2, guiC.height / 2 - height / 2, width, height);
            }
            int width = Math.max(availWidth - 25, Math.min(200, availWidth));
            int height = Math.max(guiC.getYSize(), 200);
            return new Rectangle(availWidth - width, guiC.height / 2 - height / 2, width, height);
        }
        return new Rectangle(gui.width / 2 - 100, gui.height / 2 - 100, 200, 200); //Again without knowing the bounds of the gui i can not put this next to the ui so i just put it on top of the ui in the middle of the screen.
    }
}
