package com.brandon3055.projectintelligence;

import net.minecraft.client.gui.screen.Screen;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import javax.annotation.Nullable;
import java.util.List;

public class CommonProxy {

    public void preInit(FMLCommonSetupEvent event) {
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
    }

    public void openMainGui(Screen parentScreen, @Nullable String modid, @Nullable String page) {}

    public void openMainGui(Screen parentScreen, List<String> page) {}
}
