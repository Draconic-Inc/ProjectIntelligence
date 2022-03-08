package com.brandon3055.projectintelligence.client;

import com.brandon3055.brandonscore.integration.JeiHelper;
import com.brandon3055.projectintelligence.api.PiAPI;
import com.brandon3055.projectintelligence.client.gui.GuiInGuiRenderer;
import com.brandon3055.projectintelligence.client.keybinding.KeyInputHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.util.List;

/**
 * Created by brandon3055 on 7/26/2018.
 */
public class GuiEventHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        Screen gui = event.getGui();
        GuiInGuiRenderer.instance.guiOpened(gui);
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        Screen gui = event.getGui();
        GuiInGuiRenderer.instance.guiInit(gui);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDrawForground(GuiContainerEvent.DrawForeground event) {
        if (event.isCanceled()) return;
        ContainerScreen gui = event.getGuiContainer();
        RenderSystem.disableLighting();
        RenderSystem.pushMatrix();
        RenderSystem.translated(-gui.getGuiLeft(), -gui.getGuiTop(), 0);
        GuiInGuiRenderer.instance.drawScreen(gui);
        RenderSystem.popMatrix();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.isCanceled()) return;
        Screen gui = event.getGui();
        RenderSystem.disableLighting();
        if (!(gui instanceof ContainerScreen)) {
            GuiInGuiRenderer.instance.drawScreen(gui);
        }
        GuiInGuiRenderer.instance.drawScreenPost(gui);
    }

    @SubscribeEvent()
    public void onToolTip(RenderTooltipEvent.Pre event) {
        if (GuiInGuiRenderer.instance.blockToolTip(Minecraft.getInstance().screen)) {
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

    @SubscribeEvent(priority = EventPriority.HIGHEST) //Should prevent other buttons getting in the way of the close button in the event it conflicts with other mods.
    public void onMouseClicked(GuiScreenEvent.MouseClickedEvent.Pre event) {
        Screen gui = event.getGui();
        if (GuiInGuiRenderer.instance.handleMouseClicked(gui, event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseReleased(GuiScreenEvent.MouseReleasedEvent.Pre event) {
        Screen gui = event.getGui();
        if (GuiInGuiRenderer.instance.handleMouseReleased(gui, event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseDrag(GuiScreenEvent.MouseDragEvent.Pre event) {
        Screen gui = event.getGui();
        if (GuiInGuiRenderer.instance.handleMouseDrag(gui, event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onKeyboardPress(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        Screen gui = event.getGui();

        if (handleKeyPress(gui, event.getKeyCode())) {
            event.setCanceled(true);
            return;
        }

        if (GuiInGuiRenderer.instance.handleKeyboardPress(gui, event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onKeyboardReleased(GuiScreenEvent.KeyboardKeyReleasedEvent.Pre event) {
        Screen gui = event.getGui();

        if (GuiInGuiRenderer.instance.handleKeyboardRelease(gui, event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onKeyboardChar(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
        Screen gui = event.getGui();

        if (GuiInGuiRenderer.instance.handleKeyboardChar(gui, event)) {
            event.setCanceled(true);
        }
    }

    private boolean handleKeyPress(Screen gui, int keyCode) {
        if (keyCode == -1) return false;
        InputMappings.Input key = InputMappings.Type.KEYSYM.getOrCreate(keyCode);
        if (KeyInputHandler.etGUI.isActiveAndMatches(key)) {
            if (gui instanceof ContainerScreen) {
                ContainerScreen container = (ContainerScreen) gui;
                Slot slot = container.getSlotUnderMouse();
                ItemStack stack;
                if (slot != null) {
                    stack = slot.getItem();
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
