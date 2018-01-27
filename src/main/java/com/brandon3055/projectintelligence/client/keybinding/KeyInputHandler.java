package com.brandon3055.projectintelligence.client.keybinding;

import com.brandon3055.projectintelligence.ProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

/**
 * Created by Brandon on 14/08/2014.
 */
public class KeyInputHandler {

    public static KeyBinding openPI;

    public static void init() {
        openPI = new KeyBinding("key.openPI", Keyboard.KEY_I, ProjectIntelligence.MODNAME);
        ClientRegistry.registerKeyBinding(openPI);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openPI.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiProjectIntelligence());
//            Minecraft.getMinecraft().displayGuiScreen(new GuiContentSelect(new GuiProjectIntelligence(), MD_CONTENT, IMAGE, ENTITY, ITEM_STACK));
        }
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (openPI.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiProjectIntelligence());
        }
    }
}
