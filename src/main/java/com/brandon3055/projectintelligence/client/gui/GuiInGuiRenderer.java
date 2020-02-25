package com.brandon3055.projectintelligence.client.gui;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.MGuiEffectRenderer;
import com.brandon3055.projectintelligence.registry.GuiDocRegistry;
import com.brandon3055.projectintelligence.registry.GuiDocRegistry.GuiDocHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.client.event.GuiScreenEvent;

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
    private Screen activeScreen = null;
    private GuiDocHelper guiDocHelper = null;

    public void guiOpened(@Nullable Screen gui) {
        if (gui != null && GuiDocRegistry.INSTANCE.doesGuiHaveDoc(gui)) {
            activeScreen = gui;
            guiDocHelper = GuiDocRegistry.INSTANCE.getDocHelper(gui);
        } else {
            activeScreen = null;
            overlay = null;
        }
    }

    public void guiInit(Screen gui) {
        if (isActiveScreen(gui)) {
            overlay = new PIGuiOverlay(gui, guiDocHelper); //Must be created after parent gui is initialized
        }
    }

    public void drawScreen(Screen gui) {
        if (isActiveScreen(gui) && overlay != null) {
            int mouseX = (int) getMouseX(gui);
            int mouseY = (int) getMouseY(gui);
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.translated(0, 0, 500);
            overlay.renderElements(mouseX, mouseY, gui.getMinecraft().getRenderPartialTicks());
            overlay.renderOverlayLayer(mouseX, mouseY, gui.getMinecraft().getRenderPartialTicks());
            GlStateManager.translated(0, 0, -500);
        }
    }

    public void drawScreenPost(Screen gui) {
        if (isActiveScreen(gui) && overlay != null) {
            int mouseX = (int) getMouseX(gui);
            int mouseY = (int) getMouseY(gui);
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.translated(0, 0, 500);
            overlay.renderOverlayLayer(mouseX, mouseY, gui.getMinecraft().getRenderPartialTicks());
            GlStateManager.translated(0, 0, -500);
        }
    }

    public void updateScreen() {
        if (activeScreen != null && overlay != null) {
            overlay.updateScreen();
        }
    }

    public boolean handleMouseClicked(Screen gui, GuiScreenEvent.MouseClickedEvent event) {
        if (isActiveScreen(gui) && overlay != null) {
            return overlay.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
        }
        return false;
    }

    public boolean handleMouseReleased(Screen gui, GuiScreenEvent.MouseReleasedEvent event) {
        if (isActiveScreen(gui) && overlay != null) {
            overlay.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
        }
        return false;
    }

    public boolean handleMouseDrag(Screen gui, GuiScreenEvent.MouseDragEvent event) {
        if (isActiveScreen(gui) && overlay != null) {
            overlay.mouseDragged(event.getMouseX(), event.getMouseX(), event.getMouseButton(), event.getDragX(), event.getDragY());
        }
        return false;
    }

    public boolean handleKeyboardPress(Screen gui, GuiScreenEvent.KeyboardKeyPressedEvent event) {
        if (isActiveScreen(gui) && overlay != null) {
            return overlay.keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers());
        }
        return false;
    }

    public boolean handleKeyboardRelease(Screen gui, GuiScreenEvent.KeyboardKeyReleasedEvent event) {
        if (isActiveScreen(gui) && overlay != null) {
            return overlay.keyReleased(event.getKeyCode(), event.getScanCode(), event.getModifiers());
        }
        return false;
    }

    public boolean handleKeyboardChar(Screen gui, GuiScreenEvent.KeyboardCharTypedEvent event) {
        if (isActiveScreen(gui) && overlay != null) {
            return overlay.charTyped(event.getCodePoint(), event.getModifiers());//TODO I have a feeling 'getModifiers' is actually keycode but i need to confirm that
        }
        return false;
    }

    //Helpers

    public boolean isActiveScreen(Screen gui) {
        return gui != null && gui == activeScreen && guiDocHelper != null;
    }

//    private int getMouseX(Screen gui) {
//        return Mouse.getEventX() * gui.width / gui.mc.displayWidth;
//    }
//
//    private int getMouseY(Screen gui) {
//        return gui.height - Mouse.getEventY() * gui.height / gui.mc.displayHeight - 1;
//    }

    //TODO Test these getters
    public double getMouseX(Screen gui) {
        return gui.getMinecraft().mouseHelper.getMouseX() * gui.width / gui.getMinecraft().mainWindow.getWidth();
    }

    public double getMouseY(Screen gui) {
        return gui.width - gui.getMinecraft().mouseHelper.getMouseY() * gui.height / gui.getMinecraft().mainWindow.getHeight() - 1;
    }

    public boolean blockToolTip(Screen currentScreen) {
        return isActiveScreen(currentScreen) && overlay != null && overlay.isMouseOver(getMouseX(currentScreen), getMouseY(currentScreen));
    }

    public List<Rectangle> getJeiExclusionAreas() {
        Screen screen = Minecraft.getInstance().currentScreen;
        if (isActiveScreen(screen) && overlay != null && overlay.getDocBounds() != null) {
            return Collections.singletonList(new Rectangle(overlay.getDocBounds())); //TODO remove redundant copy once JEI's fix is live.
        }

        return Collections.emptyList();
    }
}
