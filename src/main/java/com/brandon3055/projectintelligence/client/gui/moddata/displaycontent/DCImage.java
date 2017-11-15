package com.brandon3055.projectintelligence.client.gui.moddata.displaycontent;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiButtonSolid;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiHoverText;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTextField;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.lib.DLResourceLocation;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.moddata.guidoctree.TreeBranchRoot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.CENTER;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.LEFT;

/**
 * Created by brandon3055 on 8/09/2016.
 */
public class DCImage extends DisplayComponentBase {

    public static final String ATTRIB_URL = "imgURL";
    public static final String ATTRIB_SCALE = "scale";

    public String url;
    public int scale;
    public DLResourceLocation resourceLocation;
    public boolean ltComplete = false;

    public DCImage(GuiProjectIntelligence modularGui, String componentType, TreeBranchRoot branch) {
        super(modularGui, componentType, branch);
        setYSize(20);
    }

    //region Render

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);

        if (resourceLocation == null) {
            return;
        }

        double setScale = this.scale / 100D;

        if (resourceLocation.dlFailed || !resourceLocation.dlFinished) {
            setScale = 2;
        }

        int texXSize = resourceLocation.width;
        int texYSize = resourceLocation.height;

        double scaledWidth = Math.min(xSize() - 4, texXSize * setScale);
        double renderScale = scaledWidth / texXSize;
        double scaledHeight = texYSize * renderScale;


        bindTexture(resourceLocation);
        GlStateManager.color(1F, 1F, 1F, 1F);

        int texXPos = (int) (alignment == LEFT ? xPos() + 2 : alignment == CENTER ? xPos() + (xSize() / 2) - (scaledWidth / 2) : xPos() + xSize() - scaledWidth - 2);

        drawScaledCustomSizeModalRect(texXPos, yPos(), 0, 0, texXSize, texYSize, scaledWidth, scaledHeight, texXSize, texYSize);


        if (isMouseOver(mouseX, mouseY) && mouseY > list.yPos() + list.topPadding) {
            List<String> toolTip = new LinkedList<>();
            if (resourceLocation == null) {
                toolTip.add(TextFormatting.DARK_RED + "An unknown error occurred...");
            }
            else if (GuiScreen.isShiftKeyDown()) {
                toolTip.add(TextFormatting.DARK_RED + "Click to re-download image.");
            }
            else if (resourceLocation.dlFailed) {
                toolTip.add(TextFormatting.DARK_RED + "Image download failed! Click to retry.");
            }
            else if (!resourceLocation.dlFinished) {
                toolTip.add(TextFormatting.BLUE + "Downloading image...");
            }
            else {
                toolTip.add(TextFormatting.GREEN + "Right-Click to open image in browser.");
            }
            drawHoveringText(toolTip, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
        }
    }

//    @Override
//    public void renderForegroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//        super.renderForegroundLayer(minecraft, mouseX, mouseY, partialTicks);
//
//        if (isMouseOver(mouseX, mouseY) && mouseY > list.yPos + list.topPadding) {
//            List<String> toolTip = new LinkedList<String>();
//            if (resourceLocation == null) {
//                toolTip.add(TextFormatting.DARK_RED + "An unknown error occurred...");
//            }
//            else if (GuiScreen.isShiftKeyDown()) {
//                toolTip.add(TextFormatting.DARK_RED + "Click to re-download image.");
//            }
//            else if (resourceLocation.dlFailed) {
//                toolTip.add(TextFormatting.DARK_RED + "Image download failed! Click to retry.");
//            }
//            else if (!resourceLocation.dlFinished) {
//                toolTip.add(TextFormatting.BLUE + "Downloading image...");
//            }
//            else {
//                toolTip.add(TextFormatting.GREEN + "Right-Click to open image in browser.");
//            }
//            drawHoveringText(toolTip, mouseX, mouseY, fontRenderer, modularGui.screenWidth(), modularGui.screenHeight());
//        }
//    }


    //endregion

    //region Interact

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        if (isMouseOver(mouseX, mouseY)) {
            if (resourceLocation == null) {
                return false;
            }
            else if (GuiScreen.isShiftKeyDown()) {
                DLRSCache.clearFileCache(url);
                resourceLocation = DLRSCache.getResource(url);
                ltComplete = false;
                setYSize(32);
                list.schedualUpdate();
            }
            else if (resourceLocation.dlFailed) {
                DLRSCache.clearFileCache(url);
                resourceLocation = DLRSCache.getResource(url);
                ltComplete = false;
            }
            else if (resourceLocation.dlFinished && mouseButton == 1) {
                try {
                    ReflectionHelper.setPrivateValue(GuiScreen.class, branch.guiWiki, new URI(url), "clickedLinkURI", "field_175286_t");
                    this.mc.displayGuiScreen(new GuiConfirmOpenLink(branch.guiWiki, url, 31102009, false));
                }
                catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    //endregion

    //region Edit

    @Override
    public LinkedList<MGuiElementBase> getEditControls() {
        LinkedList<MGuiElementBase> list = super.getEditControls();

        list.add(new MGuiButtonSolid("TOGGLE_ALIGN", 0, 0, 26, 12, "Align") {
            @Override
            public int getBorderColour(boolean hovering, boolean disabled) {
                return hovering ? 0xFF00FF00 : 0xFFFF0000;
            }
        }.setListener(this).setHoverText("Toggle Horizontal Alignment"));

        list.add(new GuiLabel(0, 0, 22, 12, "URL:").setAlignment(GuiAlign.CENTER));
        GuiTextField urlField = new GuiTextField(0, 0, 150, 12).setListener(this).setMaxStringLength(2048).setText(url);
        urlField.addChild(new MGuiHoverText(new String[]{"Set the image URL. Note some URL's may not be supported.", TextFormatting.GREEN + "Will Auto-Save 3 seconds after you stop typing."}, urlField));
        urlField.setId("URL");
        list.add(urlField);

        list.add(new GuiLabel(0, 0, 30, 12, "Scale:").setAlignment(GuiAlign.CENTER));
        GuiTextField scaleField = new GuiTextField(0, 0, 36, 12).setListener(this).setMaxStringLength(2048).setText(String.valueOf(scale));
        scaleField.addChild(new MGuiHoverText(new String[]{"Set the image scale (As a percentage of the actual size)", TextFormatting.GOLD + "The size of the image will be limited by both this and the width of the GUI.", TextFormatting.GOLD + "Whichever value is smaller will take priority.", TextFormatting.GREEN + "Will save as you type."}, scaleField));
        scaleField.setId("SCALE");
        scaleField.setValidator(input -> {
            try {
                Integer.parseInt(input);
            }
            catch (Exception ignored) {
                return false;
            }
            return true;
        });
        list.add(scaleField);

        return list;
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventElement) {
        super.onMGuiEvent(event, eventElement);

        if (eventElement.getId().equals("SCALE") && event.isTextFiled() && event.asTextField().textChanged() && eventElement instanceof GuiTextField) {
            if (StringUtils.isNullOrEmpty(((GuiTextField) eventElement).getText())) {
                return;
            }

            int newScale = 1;
            try {
                newScale = Integer.parseInt(((GuiTextField) eventElement).getText());
            }
            catch (Exception ignored) {
            }

            if (newScale < 1) {
                newScale = 1;
            }

            element.setAttribute(ATTRIB_SCALE, String.valueOf(newScale));
            int pos = ((GuiTextField) eventElement).getCursorPosition();
            save();

//            for (MGuiElementBase element : branch.guiWiki.contentWindow.editControls) {
//                if (element instanceof GuiTextField && element.getId().equals("SCALE")) {
//                    ((GuiTextField) element).setFocused(true);
//                    ((GuiTextField) element).setCursorPosition(pos);
//                    break;
//                }
//            }
        }
        else if (eventElement.getId().equals("URL") && event.isTextFiled() && event.asTextField().textChanged() && eventElement instanceof GuiTextField) {
            element.setAttribute(ATTRIB_URL, ((GuiTextField) eventElement).getText());
            url = ((GuiTextField) eventElement).getText();
            requiresSave = true;
            saveTimer = 60;
//            int pos = ((MGuiTextField) eventElement).getCursorPosition();
//            save();
//
//            for (MGuiElementBase element : branch.guiWiki.contentWindow.editControls) {
//                if (element instanceof MGuiTextField && element.id.equals("URL")) {
//                    ((MGuiTextField) element).setFocused(true);
//                    ((MGuiTextField) element).setCursorPosition(pos);
//                    break;
//                }
//            }
        }

    }

    @Override
    public void onCreated() {
        element.setAttribute(ATTRIB_URL, "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg");
        element.setAttribute(ATTRIB_SCALE, "100");
    }

    @Override
    public DCImage setXSize(int xSize) {
        super.setXSize(xSize);

        if (resourceLocation == null) {
            ltComplete = false;
            return this;
        }

        double setScale = this.scale / 100D;
        int texXSize = resourceLocation.width;
        int texYSize = resourceLocation.height;

        double scaledWidth = Math.min(xSize - 4, texXSize * setScale);
        double renderScale = scaledWidth / texXSize;
        setYSize((int) (texYSize * renderScale));
        return this;
    }

    @Override
    public boolean onUpdate() {
        if (!ltComplete && resourceLocation != null && resourceLocation.dlFinished) {
            ltComplete = true;

            double setScale = this.scale / 100D;
            int texXSize = resourceLocation.width;
            int texYSize = resourceLocation.height;

            double scaledWidth = Math.min(xSize() - 4, texXSize * setScale);
            double renderScale = scaledWidth / texXSize;
            setYSize((int) (texYSize * renderScale));
            list.schedualUpdate();
        }

        return super.onUpdate();
    }

    //endregion

    //region XML & Factory

    @Override
    public void loadFromXML(Element element) {
        super.loadFromXML(element);
        url = element.getAttribute(ATTRIB_URL);
        try {
            scale = Integer.parseInt(element.getAttribute(ATTRIB_SCALE));
        }
        catch (Exception e) {
        }
        if (scale < 1) {
            scale = 1;
        }
        resourceLocation = DLRSCache.getResource(url);
    }

    public static class Factory implements IDisplayComponentFactory {
        @Override
        public DisplayComponentBase createNewInstance(GuiProjectIntelligence guiWiki, TreeBranchRoot branch, int screenWidth, int screenHeight) {
            DisplayComponentBase component = new DCImage(guiWiki, getID(), branch);
            component.applyGeneralElementData(guiWiki, guiWiki.mc, screenWidth, screenHeight, BCFontRenderer.convert(guiWiki.mc.fontRendererObj));
            return component;
        }

        @Override
        public String getID() {
            return "image";
        }
    }

    //endregion
}
