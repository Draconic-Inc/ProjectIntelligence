package com.brandon3055.projectintelligence.integration;

import com.brandon3055.projectintelligence.ProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.GuiInGuiRenderer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

/**
 * Created by brandon3055 on 14/04/19.
 */
@JeiPlugin
public class PIJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ProjectIntelligence.MODID, "jei_plugin");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGlobalGuiHandler(new IGlobalGuiHandler() {
            @Override
            public Collection<Rectangle2d> getGuiExtraAreas() {
                return GuiInGuiRenderer.instance.getJeiExclusionAreas();
            }
        });
    }
}
