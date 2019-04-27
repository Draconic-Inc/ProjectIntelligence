package com.brandon3055.projectintelligence.client.gui;

import com.brandon3055.projectintelligence.registry.GuiDocRegistry;
import com.brandon3055.projectintelligence.registry.GuiDocRegistry.GuiDocHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by brandon3055 on 7/26/2018.
 */
public class GuiInGuiRenderer {

    public static GuiInGuiRenderer instance = new GuiInGuiRenderer();

    private PIGuiOverlay overlay = null;
    private GuiScreen activeScreen = null;
    private GuiDocHelper guiDocHelper = null;

    public void guiOpened(@Nullable GuiScreen gui) {
        if (gui != null && GuiDocRegistry.INSTANCE.doesGuiHaveDoc(gui)) {
            activeScreen = gui;
            guiDocHelper = GuiDocRegistry.INSTANCE.getDocHelper(gui);
        }
        else {
            activeScreen = null;
            overlay = null;
        }
    }

    public void guiInit(GuiScreen gui) {
        if (isActiveScreen(gui)) {
            overlay = new PIGuiOverlay(gui, guiDocHelper); //Must be created after parent gui is initialized
        }
    }

    public void drawScreen(GuiScreen gui) {
        if (isActiveScreen(gui) && overlay != null) {
            int mouseX = getMouseX(gui);
            int mouseY = getMouseY(gui);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.translate(0, 0, 500);
            overlay.renderElements(mouseX, mouseY, gui.mc.getRenderPartialTicks());
            overlay.renderOverlayLayer(mouseX, mouseY, gui.mc.getRenderPartialTicks());
            GlStateManager.translate(0, 0, -500);
        }
    }

    public void drawScreenPost(GuiScreen gui) {
        if (isActiveScreen(gui) && overlay != null) {
            int mouseX = getMouseX(gui);
            int mouseY = getMouseY(gui);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.translate(0, 0, 500);
            overlay.renderOverlayLayer(mouseX, mouseY, gui.mc.getRenderPartialTicks());
            GlStateManager.translate(0, 0, -500);
        }
    }

    public void updateScreen() {
        if (activeScreen != null && overlay != null) {
            overlay.updateScreen();
        }
    }

    private int eventButton = -1;
    private long lastMouseEvent = 0;

    public boolean handleMouseInput(GuiScreen gui) throws IOException {
        if (isActiveScreen(gui) && overlay != null) {
            int mouseX = getMouseX(gui);
            int mouseY = getMouseY(gui);
            int button = Mouse.getEventButton();

            if (overlay.handleMouseInput()) {
                return true;
            }

            if (Mouse.getEventButtonState()) {
                eventButton = button;
                lastMouseEvent = Minecraft.getSystemTime();
                return overlay.mouseClicked(mouseX, mouseY, button);
            }
            else if (button != -1) {
                eventButton = -1;
                overlay.mouseReleased(mouseX, mouseY, button);
            }
            else if (eventButton != -1 && lastMouseEvent > 0L) {
                long timeClicked = Minecraft.getSystemTime() - gui.lastMouseEvent;
                overlay.mouseClickMove(mouseX, mouseY, eventButton, timeClicked);
            }
        }
        return false;
    }

    public boolean handleKeyboardInput(GuiScreen gui) throws IOException {
        if (isActiveScreen(gui) && overlay != null) {
            char c0 = Keyboard.getEventCharacter();
            if (Keyboard.getEventKey() == 0 && c0 >= ' ' || Keyboard.getEventKeyState()) {
                return overlay.keyTyped(c0, Keyboard.getEventKey());
            }
        }
        return false;
    }

    //Helpers

    public boolean isActiveScreen(GuiScreen gui) {
        return gui != null && gui == activeScreen && guiDocHelper != null;
    }

    private int getMouseX(GuiScreen gui) {
        return Mouse.getEventX() * gui.width / gui.mc.displayWidth;
    }

    private int getMouseY(GuiScreen gui) {
        return gui.height - Mouse.getEventY() * gui.height / gui.mc.displayHeight - 1;
    }

    public boolean blockToolTip(GuiScreen currentScreen) {
        return isActiveScreen(currentScreen) && overlay != null && overlay.isMouseOver(getMouseX(currentScreen), getMouseY(currentScreen));
    }

    public List<Rectangle> getJeiExclusionAreas() {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (isActiveScreen(screen) && overlay != null && overlay.getDocBounds() != null) {
            return Collections.singletonList(new Rectangle(overlay.getDocBounds())); //TODO remove redundant copy once JEI's fix is live.
        }

        return Collections.emptyList();
    }
}
