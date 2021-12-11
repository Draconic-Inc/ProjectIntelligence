package com.brandon3055.projectintelligence.client.gui.guielements;

import codechicken.lib.reflect.ObfMapping;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.*;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.client.DisplayController;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.gui.ContentInfo;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.PIPartRenderer;
import com.brandon3055.projectintelligence.docmanagement.DocumentationManager;
import com.brandon3055.projectintelligence.docmanagement.DocumentationPage;
import com.brandon3055.projectintelligence.docmanagement.LanguageManager;
import com.brandon3055.projectintelligence.docmanagement.LanguageManager.PageLangData;
import com.brandon3055.projectintelligence.docmanagement.ModStructurePage;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.gson.JsonObject;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;

import net.minecraft.entity.EntityType;
import net.minecraft.inventory.EquipmentSlotType;

import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.LinkedList;

import static com.brandon3055.projectintelligence.client.StyleHandler.StyleType.*;

/**
 * Created by brandon3055 on 21/08/2017.
 */
@SuppressWarnings("ALL")
public class PageButton extends GuiButton {
    public static StyleHandler.PropertyGroup pageButtonProps = new StyleHandler.PropertyGroup("page_list.page_buttons");
    public static StyleHandler.PropertyGroup pageBackButtonProps = new StyleHandler.PropertyGroup("page_list.page_buttons.page_back_button");
    public PIPartRenderer buttonRender = new PIPartRenderer(pageButtonProps).setButtonRender(true);
    public PIPartRenderer pageBackButtonRender = new PIPartRenderer(pageBackButtonProps).setButtonRender(true);

    private DocumentationPage page;
    private DisplayController controller;
    private LinkedList<GuiElement> icons = new LinkedList<>();
    private GuiLabel label;
    private GuiTexture versMissMatch;
    private GuiButton langButton;
    private GuiTexture langButtonTexture;
    private boolean invalidIcons = false;
    private int iconIndex = 0;
    private boolean backPage = false;

    public PageButton(DocumentationPage page, DisplayController controller) {
        this.page = page;
        this.controller = controller;
    }

    public PageButton(DocumentationPage page, DisplayController controller, boolean backPage) {
        this.page = page;
        this.controller = controller;
        this.backPage = backPage;
    }

    //# Initialization
    //region //############################################################################

//    todo fix issue where buton position in list is proken because reasons.

    @Override
    public void addChildElements() {
        loadIcons();

        boolean noIcon = icons.isEmpty() || backPage;
        int textXPos = 0;
        int textXSize = noIcon ? backPage ? xSize() - 10 : xSize() : xSize() - 20;
        label = new GuiLabel(page.getDisplayName()) {
            @Override
            public boolean hasShadow() {
                return backPage ? pageBackButtonProps.textShadow() : pageButtonProps.textShadow();
            }
        }.setXPos(textXPos).setXSize(textXSize).setInsets(3, noIcon ? 6 : 3, 3, 3);
        label.setHeightForText().setAlignment(GuiAlign.LEFT);
        label.setXPosMod((guiLabel, integer) -> noIcon ? backPage ? xPos() + 5 : xPos() : xPos() + 20);
        label.setHoverableTextCol(hovering -> backPage ? pageBackButtonProps.textColour(hovering) : pageButtonProps.textColour(hovering));
        label.setWrap(true);

        addChild(label);

        setYSize(Math.max(label.ySize(), backPage ? 16 : 22));
        label.setYPos(yPos() + (ySize() / 2) - (label.ySize() / 2));
        icons.forEach(icon -> icon.setXPosMod((o, o2) -> xPos() + 2).setYPos(yPos() + (ySize() / 2) - (icon.ySize() / 2)));

        if (backPage) {
            GuiTexture backIcon = new GuiTexture(17, 24, 6, 8, PITextures.PI_PARTS);
            backIcon.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> {
                StyleHandler.getColour(PAGE_LIST.pre() + PAGE_BUTTONS.pre() + PAGE_BACK_BUTTON.pre() + ICON.pre() + (isMouseOver(mouseX, mouseY) ? "hover" : "colour")).glColour();
            });
//            backIcon.setPostDrawCallback(IDrawCallback::resetColour);
            addChild(backIcon);
//          backButton.addAndFireReloadCallback(guiButton -> guiButton.setYPos(yPos() + (NAV_BAR_SIZE - backButton.ySize()) / 2));
            backIcon.setXPosMod(() -> xPos() + 3).setYPos(yPos() + (ySize() / 2) - (backIcon.ySize() / 2));
            ;
        } else {
            boolean showVerified = page instanceof ModStructurePage && ((ModStructurePage) page).verified;

            langButton = new GuiButton().setSize(12, 12).setXPosMod((guiButton, integer) -> label.maxXPos() - 14).setYPos(showVerified ? maxYPos() - 14 : yPos() + 1);
            langButton.setBorderColours(0, 0xFF004080).setFillColour(0);
            langButton.zOffset += 10;
            String[] error = {I18n.get("pi.error.page_not_localized.info"), I18n.get("pi.error.page_not_localized_click_here.info"), TextFormatting.GRAY + I18n.get("pi.error.page_not_localized_alt_version.info")};
            langButton.setHoverText(e -> {
                boolean pageLangOverriden = LanguageManager.isPageLangOverridden(page.getPageURI());
                boolean modLangOverriden = LanguageManager.isModLangOverridden(page.getModid());
                if (pageLangOverriden) {
                    String lang = LanguageManager.getPageLanguage(page.getPageURI());
                    return new String[]{I18n.get("pi.button.language_override_page.info"), TextFormatting.GOLD + LanguageManager.LANG_NAME_MAP.get(lang) + " [" + lang + "]"};
                } else if (modLangOverriden) {
                    String lang = LanguageManager.getModLanguage(page.getModid());
                    return new String[]{I18n.get("pi.button.language_override_mod.info"), TextFormatting.GOLD + LanguageManager.LANG_NAME_MAP.get(lang) + " [" + lang + "]"};
                } else {
                    return error;
                }
            });
            addChild(langButton);
            langButton.onPressed(() -> openLanguageSelector(LanguageManager.isModLangOverridden(page.getModid())));

            langButtonTexture = new GuiTexture(10, 10, PITextures.PI_PARTS).setXPosMod((guiButton, integer) -> label.maxXPos() - 13).setYPos(langButton.yPos() + 1/*yPos() + 2*/);
            langButtonTexture.setTexSizeOverride(13, 14);
            langButtonTexture.zOffset += 10;
            langButton.addChild(langButtonTexture);

            versMissMatch = new GuiTexture(8, 8, PITextures.PI_PARTS).setXPosMod((guiButton, integer) -> label.maxXPos() - 13).setYPos(yPos() + 12);
            versMissMatch.setTexSizeOverride(8, 8);
            versMissMatch.setTexturePos(0, 24);
            versMissMatch.zOffset += 10;
            addChild(versMissMatch);

            if (showVerified) {
                GuiTexture verified = new GuiTexture(72, 16, 5, 5, PITextures.PI_PARTS);
                verified.setYPos(yPos() + 2);
                verified.setXPosMod(() -> maxXPos() - 8);
                verified.setHoverText(TextFormatting.GREEN + I18n.get("pi.pagebtn.verified"), TextFormatting.BLUE + I18n.get("pi.pagebtn.verified.info"));
                addChild(verified);
            }

            if ((page instanceof ModStructurePage) && PIConfig.modVersionOverrides.get(page.getModid()) != null) {
                String text = "v" + PIConfig.modVersionOverrides.get(page.getModid());
                GuiButton versOverLabel = new GuiButton() {
                    @Override
                    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
                        RenderSystem.pushMatrix();
                        RenderSystem.translated(label.xPos() + 1, PageButton.this.yPos() + 1, 0);
                        RenderSystem.scaled(0.5, 0.5, 1);
                        drawString(fontRenderer, text, 0, 0, 0xFF5050, pageButtonProps.textShadow());
                        RenderSystem.popMatrix();
                    }
                };
                versOverLabel.setYPos(yPos() + 1);
                versOverLabel.setXPosMod(() -> label.xPos() + 1);
                versOverLabel.setSize((int) (fontRenderer.getSplitter().stringWidth(text) / 2), 4);
                versOverLabel.setHoverText(TextFormatting.RED + I18n.get("pi.pagebtn.version_override"), TextFormatting.BLUE + I18n.get("pi.pagebtn.version_override.info"));
                versOverLabel.onPressed(() -> openVersionSelector());
                addChild(versOverLabel);
            }
        }

        super.addChildElements();
    }

    private void loadIcons() {
        if (page.getIcons().isEmpty() || backPage) {
            return;
        }

        //Load Icons
        for (JsonObject iconObj : page.getIcons()) {
            if (!JSONUtils.isStringValue(iconObj, "type") || !JSONUtils.isStringValue(iconObj, "icon_string")) {
                invalidIcons = true;
                continue;
            }

            ContentInfo ci = ContentInfo.fromIconObj(iconObj);
            String type = JSONUtils.getAsString(iconObj, "type");

            final GuiElement icon;

            switch (type) {
                case "stack":
                    StackReference stack = ci.stack;
                    if (stack != null && !stack.createStack().isEmpty()) {
                        icon = new GuiStackIcon(stack);
                        ((GuiStackIcon) icon).setToolTip(ci.drawHover);
                    } else {
                        icon = null;
                    }
                    break;
                case "entity":
                    Entity entity = null;

                    if (ci.entity.startsWith("player:")) {
                        entity = GuiEntityRenderer.createRenderPlayer(mc.level, ci.entity.replaceFirst("player:", ""));
                    } else {
                        EntityType eType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(ci.entity));
                        entity = eType == null ? null : eType.create(mc.level);
                    }

                    if (entity != null) {
                        for (int i = 0; i < 6; i++) {
                            entity.setItemSlot(EquipmentSlotType.values()[i], ci.entityInventory[i > 1 ? 7 - i : i]);
                        }
                    }

                    GuiEntityRenderer renderer = new GuiEntityRenderer().setEntity(entity).setInsets(1, 1, 1, 1);
                    renderer.setTrackMouse(ci.trackMouse);
                    renderer.setRotationSpeedMultiplier((float) ci.rotationSpeed);
                    if (ci.rotationSpeed == 0) {
                        renderer.setLockedRotation(ci.rotation);
                    }
                    if (!ci.hover_text.isEmpty()) {
                        renderer.setHoverText(ci.hover_text.split("\n"));
                    }

                    if (!renderer.isInvalidEntity()) {
                        icon = renderer;
                    } else {
                        icon = null;
                    }
                    break;
                case "image":
                    icon = new GuiTexture(18, 18, DLRSCache.getResource(ci.imageURL)).setTexSizeOverride(18, 18).setTexSheetSize(18);
                    break;
                default:
                    icon = null;
                    break;
            }

            if (icon == null) {
                invalidIcons = true;
            } else {
                icon.setSize(18, 18);
                if (ci.drawSlot && !(icon instanceof GuiTexture)) {
                    icon.addChild(new GuiSlotRender().setYPos(icon.yPos()).setXPosMod((guiSlotRender, integer) -> icon.xPos()).setSize(18, 18));
                }
                icons.add(icon);
            }
        }

        //Apply Icon(s)
        if (icons.isEmpty() && invalidIcons) {
            icons.add(new GuiStackIcon(new StackReference("error")).setToolTipOverride(Collections.singletonList("Invalid or missing icon.")));
        }

        if (!icons.isEmpty()) {
            if (page.cycle_icons()) {
                icons.forEach(child -> {
                    child.setEnabled(false);
                    addChild(child);
                });
                updateIcons();
            } else {
                addChild(icons.getFirst());
            }
        }
    }

    @Override
    public void reloadElement() {
        if (!backPage) {
            boolean overridden = LanguageManager.isPageLangOverridden(page.getPageURI()) || LanguageManager.isModLangOverridden(page.getModid());
            if (isElementInitialized() && langButtonTexture != null && langButton != null) {
                langButtonTexture.setTexturePos(overridden ? 82 : 66, 0);
                langButton.setEnabled(!PIConfig.editMode() && (overridden || !LanguageManager.isPageLocalized(page.getPageURI(), LanguageManager.getPageLanguage(page.getPageURI()))));
            }

            PageLangData data = LanguageManager.getLangData(page.getPageURI(), LanguageManager.getPageLanguage(page.getPageURI()));
            if (data != null && data.matchLang != null) {
                PageLangData matches = LanguageManager.getLangData(page.getPageURI(), data.matchLang);
                if (matches != null && matches.pageRev > data.matchRev) {
                    versMissMatch.setHoverText(I18n.get("pi.error.page_lang_outdated", matches.lang));
                } else {
                    versMissMatch.setEnabled(false);
                }
            } else {
                versMissMatch.setEnabled(false);
            }
        }

        super.reloadElement();
    }

    //endregion

    @Override
    public void onPressed(double mouseX, double mouseY, int mouseButton) {
        //Open Context Menu
        if (mouseButton == 1) {

            StyledSelectDialog<ContextMenuItem> context = new StyledSelectDialog<>(langButton, "user_dialogs", I18n.get("pi.page.cm.page_options"));
            context.setSelectionListener(ContextMenuItem::onClicked);

            ContextMenuItem menuItem = new ContextMenuItem(I18n.get("pi.page.cm.open_new_tab"));
            menuItem.setAction(() -> controller.openPage(page.getPageURI(), true));
            context.addItem(menuItem);

            menuItem = new ContextMenuItem(I18n.get("pi.page.cm.override_lang"));
            menuItem.setAction(() -> openLanguageSelector(false));
            context.addItem(menuItem);

            menuItem = new ContextMenuItem(I18n.get("pi.page.cm.override_mod_lang"));
            menuItem.setAction(() -> openLanguageSelector(true));
            context.addItem(menuItem);

            menuItem = new ContextMenuItem(I18n.get("pi.page.cm.set_home_page"));
            menuItem.setAction(() -> {
                PIConfig.setHomePage(page.getPageURI());
                controller.onActivePageChange();
            });
            context.addItem(menuItem);

            menuItem = new ContextMenuItem(I18n.get("pi.page.cm.override_mod_version"));
            menuItem.setAction(() -> openVersionSelector());
            context.addItem(menuItem);

            if (PIConfig.editMode() || BrandonsCore.inDev) {
                menuItem = new ContextMenuItem(I18n.get("Copy page URI"));
                menuItem.setAction(() -> Utils.setClipboardString(page.getPageURI()));
                context.addItem(menuItem);
            }

            context.setYSize(16 + (context.getItems().size() * 16));
            context.setCloseOnSelection(true);
            context.show(200);
            context.setPos((int) mouseX, (int) mouseY).normalizePosition();
        } else {
            boolean newTab = mouseButton == 2;

            if (!newTab && controller.getActiveTab().pageURI.equals(page.getPageURI())) {
                //Go back if the page is already selected
                controller.openPage(page.getParent().getPageURI(), false);
            } else {
                //Open Page
                controller.openPage(page.getPageURI(), newTab);
            }
        }

        super.onPressed(mouseX, mouseY, mouseButton);
    }

    //# Render
    //region //############################################################################

    @Override
    public void renderElement(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        boolean highlighted = isMouseOver(mouseX, mouseY) || controller.getActiveTab().pageURI.equals(page.getPageURI());

        if (backPage) {
            pageBackButtonRender.render(this, highlighted);
        } else {
            buttonRender.render(this, highlighted);
        }

        int texV = 48 + (getRenderState(highlighted) * 20);

        if (page.isHidden() && PIConfig.editMode()) {
            drawColouredRect(xPos() + 1, yPos() + 1, xSize() - 2, ySize() - 2, 0xA0000000);
            RenderSystem.pushMatrix();
            RenderSystem.translated(xPos() + 1, yPos() + 1, 0);
            RenderSystem.scaled(0.62, 0.62, 0.5);
            drawString(fontRenderer, "Hidden button. (only visible in edit mode)", 0, 0, 0xFF5050, false);
            RenderSystem.popMatrix();
        }

        super.renderElement(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onUpdate() {
        if (page.cycle_icons() && !Screen.hasShiftDown()) {
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

    public void openLanguageSelector(boolean mod) {
        LogHelper.dev("Lang Selector");

        StyledSelectDialog<String> langSelect = new StyledSelectDialog<>(langButton, "user_dialogs", I18n.get("pi.popup.select_language"));
        String doTrans = I18n.get("pi.lang.disable_override");

        //Add Search Box
        GuiTextField filter = new GuiTextField();
        langSelect.addChild(filter);
        filter.setSize(langSelect.xSize() - 4, 14).setPos(langSelect.xPos() + 2, langSelect.maxYPos() - 16);
//        filter.setListener((event, eventSource) -> langSelect.reloadElement());
        filter.setChangeListener(() -> langSelect.reloadElement());
        langSelect.setSelectionFilter(item -> {
            String ft = filter.getText().toLowerCase();
            return ft.isEmpty() || item.toLowerCase().contains(ft) || LanguageManager.LANG_NAME_MAP.getOrDefault(item, "").toLowerCase().contains(ft);
        });

        //Add languages
        if (mod) {
            ModStructurePage modPage = DocumentationManager.getModPage(page.getModid());
            if (modPage == null) return;

            if (LanguageManager.isModLangOverridden(modPage.getModid())) {
                langSelect.addItem(doTrans);
            }
            langSelect.setSelected(LanguageManager.getModLanguage(modPage.getModid()));
            LanguageManager.getAvailablePageLanguages(modPage.getPageURI()).forEach(langSelect::addItem);
        } else {
            if (LanguageManager.isPageLangOverridden(page.getPageURI())) {
                langSelect.addItem(doTrans);
            }
            langSelect.setSelected(LanguageManager.getPageLanguage(page.getPageURI()));
            LanguageManager.getAvailablePageLanguages(page.getPageURI()).forEach(langSelect::addItem);
        }


        langSelect.setSelectionListener(lang -> {
            langButton.playClickEvent(false);
            String newLang = lang.equals(doTrans) ? null : lang;
            if (mod) {
                LanguageManager.setModLangOverride(page.getModid(), newLang);
            } else {
                LanguageManager.setPageLangOverride(page.getPageURI(), newLang);
            }
        });
        langSelect.setCloseOnSelection(true);
        langSelect.showCenter(200);
    }

    public void openVersionSelector() {
        try {
            LogHelper.dev("Version Selector");

            StyledSelectDialog<String> versionSelect = new StyledSelectDialog<>(langButton, "user_dialogs", I18n.get("pi.popup.select_version"));
            String doTrans = I18n.get("pi.lang.disable_override");

            //Add Search Box
            GuiTextField filter = new GuiTextField();
            versionSelect.addChild(filter);
            filter.setSize(versionSelect.xSize() - 4, 14).setPos(versionSelect.xPos() + 2, versionSelect.maxYPos() - 16);
//            filter.setListener((event, eventSource) -> versionSelect.reloadElement());
            filter.setChangeListener(() -> versionSelect.reloadElement());
            versionSelect.setSelectionFilter(item -> {
                String ft = filter.getText().toLowerCase();
                return ft.isEmpty() || item.toLowerCase().contains(ft);
            });

            //Add Versions
            String currentOverride = PIConfig.modVersionOverrides.get(page.getModid());
            if (currentOverride != null) {
                versionSelect.addItem(doTrans);
            }

            versionSelect.setSelected(currentOverride);
            DocumentationManager.sortedModVersionMap.get(page.getModid()).forEach(versionSelect::addItem);

            versionSelect.setSelectionListener(version -> {
                langButton.playClickEvent(false);
                String newVersiob = version.equals(doTrans) ? null : version;
                DocumentationManager.setModVersionOverride(page.getModid(), newVersiob);
            });
            versionSelect.setCloseOnSelection(true);
            versionSelect.showCenter(200);
        }
        catch (Throwable e) {
        }
    }

    private static class ContextMenuItem {
        private String name;
        private Runnable action;

        private ContextMenuItem(String name) {
            this.name = name;
        }

        public void setAction(Runnable action) {
            this.action = action;
        }

        public void onClicked() {
            if (action != null) {
                action.run();
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
