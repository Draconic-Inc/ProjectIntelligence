package com.brandon3055.projectintelligence.client.gui.guielements;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.*;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.TabManager;
import com.brandon3055.projectintelligence.docdata.DocumentationManager;
import com.brandon3055.projectintelligence.docdata.DocumentationPage;
import com.brandon3055.projectintelligence.docdata.LanguageManager;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.LinkedList;

/**
 * Created by brandon3055 on 21/08/2017.
 */
public class PageButton extends GuiButton {
    private DocumentationPage page;
    private GuiProjectIntelligence gui;
    private LinkedList<MGuiElementBase> icons = new LinkedList<>();
    private GuiLabel label;
    private GuiButton langButton;
    private GuiTexture langButtonTexture;
    private boolean invalidIcons = false;
    private int iconIndex = 0;

    public PageButton(DocumentationPage page, GuiProjectIntelligence gui) {
        this.page = page;
        this.gui = gui;
    }

    //############################################################################
    //# Initialization
    //region //############################################################################

    @Override
    public void addChildElements() {
        loadIcons();

        boolean noIcon = icons.isEmpty();
        int textXPos = 0;//activeIcon == null ? xPos() : xPos() + 20;
        int textXSize = noIcon ? xSize() : xSize() - 20;
        label = new GuiLabel(page.getDisplayName()){
            @Override
            public boolean hasShadow() {
                return GuiPartPageList.btnTextShadow;
            }
        }.setXPos(textXPos).setXSize(textXSize).setInsets(3, noIcon ? 6 : 3, 3, 3);
        label.setHeightForText().setAlignment(GuiAlign.LEFT);
        label.setXPosMod((guiLabel, integer) -> noIcon ? xPos() : xPos() + 20);
        label.setTextColGetter(hovering -> hovering ? GuiPartPageList.btnTextColourHover : GuiPartPageList.btnTextColour);
        label.setWrap(true);

        addChild(label);

        setYSize(Math.max(label.ySize(), 22));
        label.setYPos(yPos() + (ySize() / 2) - (label.ySize() / 2));
        icons.forEach(icon -> icon.setXPosMod((o, o2) -> xPos() + 2).setYPos(yPos() + (ySize() / 2) - (icon.ySize() / 2)));

        langButton = new GuiButton().setSize(12, 12).setXPosMod((guiButton, integer) -> label.maxXPos() - 14).setYPos(label.maxYPos() - 14);
        langButton.setBorderColours(0, 0xFF004080).setFillColour(0);
        langButton.zOffset += 10;
        String[] error = {I18n.format("pi.error.page_not_localized.info"), I18n.format("pi.error.page_not_localized_click_here.info")};
        langButton.setHoverTextArray(e -> LanguageManager.isPageLangOverridden(page.getPageURI()) ? new String[] {I18n.format("pi.button.language_override_enabled.info"), TextFormatting.GOLD + LanguageManager.LANG_NAME_MAP.get(LanguageManager.getPageLanguage(page.getPageURI()))} : error);
        addChild(langButton);
        langButton.setListener((event, eventSource) -> openLanguageSelector());

        langButtonTexture = new GuiTexture(10, 10, PITextures.PI_PARTS).setXPosMod((guiButton, integer) -> label.maxXPos() - 13).setYPos(label.maxYPos() - 13);
        langButtonTexture.setTexSizeOverride(13, 14);
        langButtonTexture.zOffset += 10;
        langButton.addChild(langButtonTexture);

        super.addChildElements();
    }

    private void loadIcons() {
        LogHelper.dev("Loading icons for page: " + page.getPageId());
        if (page.getIcons().isEmpty()) {
            LogHelper.dev("No icons for page: " + page.getPageId());
            return;
        }

        //Load Icons
        for (JsonObject iconObj : page.getIcons()) {
            if (!JsonUtils.isString(iconObj, "type") || !JsonUtils.isString(iconObj, "icon_string")) {
                invalidIcons = true;
                continue;
            }
            String type = JsonUtils.getString(iconObj, "type");
            String iconString = JsonUtils.getString(iconObj, "icon_string");
            boolean drawSlot = JsonUtils.getBoolean(iconObj, "draw_slot", true);

            final MGuiElementBase icon;

            if (type.equals("stack")) {
                StackReference stack = StackReference.fromString(iconString);
                if (stack != null && !stack.createStack().isEmpty()) {
                    icon = new GuiStackIcon(stack);
                }
                else { icon = null; }
            }
            else if (type.equals("entity")) {
                GuiEntityRenderer renderer;
                if (iconString.startsWith("player:")) {
                    EntityPlayer player = GuiEntityRenderer.createRenderPlayer(mc.world, iconString.replaceFirst("player:", ""));
                    renderer = new GuiEntityRenderer().setEntity(player).setInsets(1, 1, 1, 1);
                }
                else {
                    renderer = new GuiEntityRenderer().setEntity(new ResourceLocation(iconString)).setInsets(1, 1, 1, 1);
                }

                renderer.setTrackMouse(JsonUtils.getBoolean(iconObj, "track_mouse", true));

                if (!renderer.isInvalidEntity()) {
                    icon = renderer;
                }
                else { icon = null; }
            }
            else if (type.equals("image")) {
                icon = new GuiTexture(18, 18, DLRSCache.getResource(iconString)).setTexSizeOverride(18, 18).setTexSheetSize(18);
            }
            else {
                icon = null;
            }

            if (icon == null) {
                invalidIcons = true;
            }
            else {
                icon.setSize(18, 18);
                if (drawSlot && !(icon instanceof GuiTexture)) {
                    icon.addChild(new GuiSlotRender().setYPos(icon.yPos()).setXPosMod((guiSlotRender, integer) -> icon.xPos()).setSize(18, 18));
                }
                icons.add(icon);
            }
        }

        //Apply Icon(s)
        if (icons.isEmpty() && invalidIcons) {
            icons.add(new GuiStackIcon(new StackReference("error")));
        }

        if (!icons.isEmpty()) {
            if (page.cycle_icons()) {
                icons.forEach(child -> {
                    child.setEnabled(false);
                    addChild(child);
                });
                updateIcons();
            }
            else {
                addChild(icons.getFirst());
            }
        }
    }

    @Override
    public void reloadElement() {
        boolean overridden = LanguageManager.isPageLangOverridden(page.getPageURI());
        langButtonTexture.setTexturePos(overridden ? 82 : 66, 0);
        langButton.setEnabled(!PIConfig.editMode() && (overridden || !LanguageManager.isPageLocalized(page.getPageURI(), LanguageManager.getPageLanguage(page.getPageURI()))));

//        TODO create language selection gui. Use a generified gui where i give it the list of avalible languages and a default that can be whatever.
//        That way i can use it for both the page override and setting the user prefernce.

        super.reloadElement();
    }

    //endregion

    @Override
    public void onPressed(int mouseX, int mouseY, int mouseButton) {
        LogHelper.dev(mouseButton);
        TabManager.openPage(page.getPageURI(), mouseButton == 2);
//        DocumentationManager.setSelectedPage(page);
//        gui.reloadGui();
        super.onPressed(mouseX, mouseY, mouseButton);
    }

    //############################################################################
    //# Render
    //region //############################################################################

    @Override
    public void renderElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        boolean highlighted = isMouseOver(mouseX, mouseY) || /*DocumentationManager.getSelectedPage()*/ TabManager.getActiveTab().equals(page.getPageURI());

        if (GuiPartPageList.btnShadedBorders) {
            int back = highlighted ? GuiPartPageList.btnColourHover.argb() : GuiPartPageList.btnColour.argb();
            int border = highlighted ? GuiPartPageList.btnBorderHover.argb() : GuiPartPageList.btnBorder.argb();
            int pos = changeShade(border, 0.2);
            int neg = changeShade(border, -0.1);
            double b = GuiPartPageList.btnThickBorders || highlighted ? 1 : 0.5;

            drawColouredRect(xPos(), yPos(), xSize(), ySize(), back);
            drawColouredRect(xPos(), yPos(), xSize(), b, pos);
            drawColouredRect(xPos(), yPos(), b, ySize(), pos);
            drawColouredRect(xPos(), yPos() + ySize() - b, xSize(), b, neg);
            drawColouredRect(xPos() + xSize() - b, yPos(), b, ySize(), neg);
        }
        else if (GuiPartPageList.btnVanillaTex) {
            int texV = 48 + (getRenderState(highlighted) * 20);

            if (highlighted) {
                GuiPartPageList.btnColourHover.glColour();
            }
            else {
                GuiPartPageList.btnColour.glColour();
            }

            ResourceHelperBC.bindTexture(PITextures.PI_PARTS);
            drawTiledTextureRectWithTrim(xPos(), yPos(), xSize(), ySize(), 2, 2, 2, 2, 0, texV, 200, 20);
            GlStateManager.color(1, 1, 1, 1);
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, highlighted ? GuiPartPageList.btnBorderHover.argb() : GuiPartPageList.btnBorder.argb());
        }
        else {
            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, highlighted ? GuiPartPageList.btnColourHover.argb() : GuiPartPageList.btnColour.argb(), highlighted ? GuiPartPageList.btnBorderHover.argb() : GuiPartPageList.btnBorder.argb());
        }


        super.renderElement(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onUpdate() {
        if (page.cycle_icons()) {
            updateIcons();
        }
        return super.onUpdate();
    }

    private void updateIcons() {
        int i = (iconIndex / 20) % icons.size();
        icons.forEach(elementBase -> elementBase.setEnabled(false));
        icons.get(i).setEnabled(true);
        iconIndex++;
    }

    //endregion

    public void openLanguageSelector() {
        StyledSelectDialog<String> langSelect = new StyledSelectDialog<>(langButton, "user_dialogs", "Select Language");
        String doTrans = I18n.format("pi.lang.disable_override");
        if (LanguageManager.isPageLangOverridden(page.getPageURI())) {
            langSelect.addItem(doTrans);
        }
        langSelect.setSelected(LanguageManager.getPageLanguage(page.getPageURI()));
        LanguageManager.getAvailablePageLanguages(page.getPageURI()).forEach(langSelect::addItem);
        langSelect.setSelectionListener(lang -> {
            LanguageManager.setPageLangOverride(page.getPageURI(), lang.equals(doTrans) ? null : lang);
            langButton.playClickSound();
            DocumentationManager.checkAndReloadDocFiles();
        });
        langSelect.setCloseOnSelection(true);
        langSelect.showCenter(200);
    }
}
