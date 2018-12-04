package com.brandon3055.projectintelligence.client;

import com.brandon3055.brandonscore.integration.JeiHelper;
import com.brandon3055.projectintelligence.api.PiAPI;
import com.brandon3055.projectintelligence.client.gui.GuiInGuiRenderer;
import com.brandon3055.projectintelligence.client.keybinding.KeyInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

/**
 * Created by brandon3055 on 7/26/2018.
 */
public class GuiEventHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen gui = event.getGui();
        GuiInGuiRenderer.instance.guiOpened(gui);
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.getGui();
        GuiInGuiRenderer.instance.guiInit(gui);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDrawForground(GuiContainerEvent.DrawForeground event) {
        if (event.isCanceled()) return;
        GuiContainer gui = event.getGuiContainer();
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-gui.guiLeft, -gui.guiTop, 0);
        GuiInGuiRenderer.instance.drawScreen(gui);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.isCanceled()) return;
        GuiScreen gui = event.getGui();
        GlStateManager.disableLighting();
        if (!(gui instanceof GuiContainer)) {
            GuiInGuiRenderer.instance.drawScreen(gui);
        }
        GuiInGuiRenderer.instance.drawScreenPost(gui);
    }

    @SubscribeEvent()
    public void onToolTip(RenderTooltipEvent.Pre event) {
        if (GuiInGuiRenderer.instance.blockToolTip(Minecraft.getMinecraft().currentScreen)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        GuiInGuiRenderer.instance.updateScreen();
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        GuiScreen gui = event.getGui();
        try {
            if (GuiInGuiRenderer.instance.handleMouseInput(gui)) {
                event.setCanceled(true);
            }
        }
        catch (IOException ignored) {
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        GuiScreen gui = event.getGui();

        if (Keyboard.getEventKeyState() && handleKeyPress(gui, Keyboard.getEventKey())) {
            event.setCanceled(true);
            return;
        }

        try {
            if (GuiInGuiRenderer.instance.handleKeyboardInput(gui)) {
                event.setCanceled(true);
            }
        }
        catch (IOException ignored) {}
    }

    private boolean handleKeyPress(GuiScreen gui, int key) {
        if (KeyInputHandler.etGUI.isActiveAndMatches(key)) {
            if (gui instanceof GuiContainer) {
                GuiContainer container = (GuiContainer) gui;
                Slot slot = container.getSlotUnderMouse();
                ItemStack stack;
                if (slot != null) {
                    stack = slot.getStack();
                }
                else {
                    stack = JeiHelper.getPanelItemUnderMouse();
                }

                if (stack != null && !stack.isEmpty()){
                    List<String> pages = PiAPI.getRelatedPages(stack);
                    if (!pages.isEmpty()) {
                        PiAPI.openGui(gui, pages);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
