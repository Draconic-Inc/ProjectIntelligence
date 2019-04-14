package com.brandon3055.projectintelligence.integration;

import com.brandon3055.projectintelligence.client.gui.GuiInGuiRenderer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGlobalGuiHandler;

import java.awt.*;
import java.util.Collection;

/**
 * Created by brandon3055 on 14/04/19.
 */
@JEIPlugin
public class PIJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registry.addGlobalGuiHandlers(new IGlobalGuiHandler() {
            @Override
            public Collection<Rectangle> getGuiExtraAreas() {
                return GuiInGuiRenderer.instance.getJeiExclusionAreas();
            }
        });
    }
}
