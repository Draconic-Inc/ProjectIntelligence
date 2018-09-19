//package com.brandon3055.projectintelligence.registry;
//
//import com.brandon3055.brandonscore.client.ResourceHelperBC;
//import com.brandon3055.brandonscore.client.utils.GuiHelper;
//import com.brandon3055.projectintelligence.api.IGuiDocHandler;
//import com.brandon3055.projectintelligence.client.PITextures;
//import com.brandon3055.projectintelligence.client.StyleHandler;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiScreen;
//import net.minecraft.client.gui.inventory.GuiContainer;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.client.resources.I18n;
//
//import java.awt.*;
//
//import static com.brandon3055.projectintelligence.client.StyleHandler.StyleType.*;
//
///**
// * Created by brandon3055 on 7/26/2018.
// */
//public class DefaultHandlerImpl_old implements IGuiDocHandler<GuiScreen> {
//
//    @Override
//    public Rectangle getButtonArea(GuiScreen gui) {
//        if (gui instanceof GuiContainer) {
//            return new Rectangle(5, ((GuiContainer) gui).getGuiTop(), 20, 20);
//        }
//        else {
//            return new Rectangle(0, 30, 20, 20);
//        }
//    }
//
//    @Override
//    public boolean bindButtonToDoc() {
//        return true;
//    }
//
//    @Override
//    public Rectangle getDocDisplayArea(GuiScreen gui) {
//        if (gui instanceof GuiContainer) {
//            GuiContainer guiC = (GuiContainer) gui;
//            return new Rectangle(25, guiC.getGuiTop(), guiC.getGuiLeft() - 25, guiC.getYSize());
//        }
//        return new Rectangle(0, 50, 200, 300); //TODO figure out a reasonable area
//    }
//
//    @Override
//    public AttachSide getAnimDirection(GuiScreen gui) {
//        return gui instanceof GuiContainer ? AttachSide.LEFT : AttachSide.RIGHT;
//    }
//
//    @Override
//    public void renderButton(Minecraft mc, boolean mouseOver) {
//        int border = StyleHandler.getInt("user_dialogs.button_style." + BORDER.getName());
//        int borderHover = StyleHandler.getInt("user_dialogs.button_style." + BORDER_HOVER.getName());
//        if (StyleHandler.getBoolean("user_dialogs.button_style." + VANILLA_TEXTURE.getName())) {
//            int texV = 48 + ((mouseOver ? 2 : 1) * 20);
//
//            if (mouseOver) {
//                StyleHandler.getColour("user_dialogs.button_style." + HOVER.getName()).glColour();
//            }
//            else {
//                StyleHandler.getColour("user_dialogs.button_style." + COLOUR.getName()).glColour();
//            }
//
//            ResourceHelperBC.bindTexture(PITextures.PI_PARTS);
//            GuiHelper.drawTiledTextureRectWithTrim(0, 0, 20, 20, 2, 2, 2, 0, 0, texV, 200, 20, 0);
//            GlStateManager.color(1, 1, 1, 1);
//            GuiHelper.drawBorderedRect(0, 0, 20, 20, 1, 0, mouseOver ? borderHover : border);
//        }
//        else {
//            int fill = StyleHandler.getInt("user_dialogs.button_style." + COLOUR.getName());
//            int fillHover = StyleHandler.getInt("user_dialogs.button_style." + HOVER.getName());
//            GuiHelper.drawBorderedRect(0, 0, 20, 20, 1, mouseOver ? fillHover : fill, mouseOver ? borderHover : border);
//        }
//
//        int text = StyleHandler.getInt("user_dialogs.button_style." + TEXT_COLOUR.getName());
//        GuiHelper.drawCenteredString(mc.fontRenderer, I18n.format("pi.gui_in_gui.display_doc"), 11, 7, text, false);
//    }
//}
