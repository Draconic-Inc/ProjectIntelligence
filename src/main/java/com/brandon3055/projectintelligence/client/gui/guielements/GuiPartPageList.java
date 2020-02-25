package com.brandon3055.projectintelligence.client.gui.guielements;

import codechicken.lib.colour.Colour;
import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTextField;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.projectintelligence.client.DisplayController;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.StyleHandler.PropertyGroup;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.PIConfig.SearchMode;
import com.brandon3055.projectintelligence.client.gui.PIGuiContainer;
import com.brandon3055.projectintelligence.client.gui.PIPartRenderer;
import com.brandon3055.projectintelligence.docmanagement.ContentRelation;
import com.brandon3055.projectintelligence.docmanagement.DocumentationManager;
import com.brandon3055.projectintelligence.docmanagement.DocumentationPage;
import com.brandon3055.projectintelligence.docmanagement.RootPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement.ListMode.VERT_LOCK_POS;
import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl.SliderRotation.VERTICAL;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.LEFT;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.TextRotation.NORMAL;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.TextRotation.ROT_CC;

/**
 * Created by brandon3055 on 10/07/2017.
 */
public class GuiPartPageList extends GuiElement<GuiPartPageList> {
    public static PropertyGroup dirPathProps = new PropertyGroup("page_list.dir_path");
    public static PropertyGroup dirButtonProps = new PropertyGroup("page_list.dir_path.dir_buttons");
    public static PropertyGroup headerProps = new PropertyGroup("page_list.header");
    public static PropertyGroup bodyProps = new PropertyGroup("page_list.body");
    public static PropertyGroup footerProps = new PropertyGroup("page_list.footer");
    public static PropertyGroup searchBoxProps = new PropertyGroup("page_list.search");
    public static PropertyGroup searchSettingsProps = new PropertyGroup("page_list.search.settings_button");
    public static PropertyGroup scrollProps = new PropertyGroup("page_list.scroll_bar");
    public static PropertyGroup scrollSliderProps = new PropertyGroup("page_list.scroll_bar.scroll_slider");

    public PIPartRenderer headerRender = new PIPartRenderer(headerProps).setSideTrims(true, true, false, true);
    public PIPartRenderer dirPathRender = new PIPartRenderer(dirPathProps).setSideTrims(true, true, false, true);
    public PIPartRenderer dirButtonRender = new PIPartRenderer(dirButtonProps);
    public PIPartRenderer bodyRender = new PIPartRenderer(bodyProps).setSideTrims(true, false, false, true);
    public PIPartRenderer footerRender = new PIPartRenderer(footerProps);
    public PIPartRenderer scrollRenderer = new PIPartRenderer(scrollProps);
    public PIPartRenderer scrollSlideRenderer = new PIPartRenderer(scrollSliderProps);

    public final int HIDDEN_X_SIZE = 12;

    /**
     * Nav Bar Y Size
     */
    private final int NAV_BAR_SIZE = 14;
    /**
     * Title Bar Y Size
     */
    private final int TITLE_BAR_SIZE = 0;
    /**
     * Directory Bar X Size
     */
    private final int DIR_BAR_SIZE = 12;
    /**
     * Footer Y Size
     */
    private final int FOOTER_SIZE = 16;

    private DisplayController controller;
    public boolean extended = true;
    private GuiSlideControl scrollBar;
    public GuiScrollElement scrollElement;
    private List<GuiButton> navButtons = new LinkedList<>();
    private PIGuiContainer container;
    private String buttonController = "";
    public GuiTextField searchBox;
    public GuiButton backButton;
    public GuiButton forwardButton;
    public GuiButton toggleView;
    public GuiButton searchSettings;
    private String currentPage = "";

    public GuiPartPageList(PIGuiContainer container, DisplayController controller) {
        this.container = container;
        this.controller = controller;
    }

    @Override
    public void addChildElements() {
        super.addChildElements();

        toggleView = new GuiButton().setSize(10, 10).setHoverText(I18n.format("pi.button.toggle_nav_window.info"));
        GuiTexture tex = new GuiTexture(0, 16, 6, 7, PITextures.PI_PARTS);
        tex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> StyleHandler.getColour("page_list.hide_button." + (toggleView.isMouseOver(mouseX, mouseY) ? "hover" : "colour")).glColour());
        tex.setPostDrawCallback(IDrawCallback::resetColour);
        tex.setTexXGetter(() -> extended ? 0 : 9);
        toggleView.addChild(tex);
        toggleView.addAndFireReloadCallback(guiButton -> guiButton.setYPos(yPos() + (NAV_BAR_SIZE - toggleView.ySize()) / 2));
        toggleView.onPressed(() -> extended = !extended);
        tex.setXPosMod(() -> maxXPos() - 10).translate(0, 2);
        toggleView.setXPosMod(() -> maxXPos() - 11);
        addChild(toggleView);

        backButton = new GuiButton().setSize(10, 12).setHoverText(I18n.format("pi.button.go_back"));
        tex = new GuiTexture(17, 24, 6, 8, PITextures.PI_PARTS);
        tex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> {
            Colour c = StyleHandler.getColour("page_list.hide_button." + (backButton.isMouseOver(mouseX, mouseY) && !backButton.isDisabled() ? "hover" : "colour"));
            if (backButton.isDisabled()) {
                c = c.copy().set(changeShade(c.argb(), -0.5));
            }
            c.glColour();
        });
        tex.setPostDrawCallback(IDrawCallback::resetColour);
        backButton.addChild(tex);
        backButton.addAndFireReloadCallback(guiButton -> guiButton.setYPos(yPos() + (NAV_BAR_SIZE - backButton.ySize()) / 2));
        tex.setXPosMod(() -> maxXPos() - extendedXSize() + 6).translate(0, 2);
        backButton.setXPosMod(() -> maxXPos() - extendedXSize() + 4);
        addChild(backButton);
        backButton.onPressed(() -> {
            controller.goBack();
            reloadPageButtons(true);
        });
        backButton.setEnabledCallback(() -> xSize() == extendedXSize());

        forwardButton = new GuiButton().setSize(10, 12).setHoverText(I18n.format("pi.button.go_forward"));
        tex = new GuiTexture(25, 24, 6, 8, PITextures.PI_PARTS);
        tex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> {
            Colour c = StyleHandler.getColour("page_list.hide_button." + (forwardButton.isMouseOver(mouseX, mouseY) && !forwardButton.isDisabled() ? "hover" : "colour"));
            if (forwardButton.isDisabled()) {
                c = c.copy().set(changeShade(c.argb(), -0.5));
            }
            c.glColour();
        });
        tex.setPostDrawCallback(IDrawCallback::resetColour);
        forwardButton.addChild(tex);
        forwardButton.addAndFireReloadCallback(guiButton -> guiButton.setYPos(yPos() + (NAV_BAR_SIZE - forwardButton.ySize()) / 2));
        tex.setXPosMod(() -> maxXPos() - extendedXSize() + 10 + 6).translate(0, 2);
        forwardButton.setXPosMod(() -> maxXPos() - extendedXSize() + 10 + 4);
        addChild(forwardButton);
        forwardButton.onPressed(() -> {
            controller.goForward();
            reloadPageButtons(true);
        });
        forwardButton.setEnabledCallback(() -> xSize() == extendedXSize());

        scrollBar = new GuiSlideControl(VERTICAL);
        scrollBar.setXPosMod((guiSlideControl, integer) -> maxXPos() - scrollBar.xSize() - 2);
        scrollBar.setYPosMod((guiSlideControl, integer) -> yPos() + NAV_BAR_SIZE + TITLE_BAR_SIZE + 2);
        scrollBar.setXSize(8);

        scrollBar.setBackgroundElement(scrollRenderer.asElement().setHoverStateSupplier(() -> scrollBar.isDragging()));
        scrollBar.setSliderElement(scrollSlideRenderer.asElement().setHoverStateSupplier(() -> scrollBar.isDragging()));
        scrollBar.getBackgroundElement().setXPosMod((o, integer) -> scrollBar.xPos()).setYPosMod((o, integer) -> scrollBar.yPos());
        scrollBar.getSliderElement().setXPosMod((o, integer) -> scrollBar.getInsetRect().x);
        scrollBar.setEnabledCallback(() -> extended);

        scrollElement = new GuiScrollElement();
        scrollElement.setVerticalScrollBar(scrollBar);
        scrollElement.setListMode(VERT_LOCK_POS);
        scrollElement.setStandardScrollBehavior();
        scrollElement.setXSizeMod((guiScrollElement, integer) -> xSize() - HIDDEN_X_SIZE);
        scrollElement.setAllowedScrollAxes(true, false);
        addChild(scrollElement);

        addSearchBox();
    }

    private void addSearchBox() {
        int settingsSize = FOOTER_SIZE - 4;

        searchBox = new GuiTextField();
        searchBox.setXPosMod(() -> maxXPos() - extendedXSize() + 2);
        searchBox.addAndFireReloadCallback(field -> {
            field.setSize(extendedXSize() - settingsSize - 5, FOOTER_SIZE - 4);
            field.setYPos(maxYPos() - FOOTER_SIZE + 2);
        });
        searchBox.setChangeListener(() -> reloadPageButtons(false));
        searchBox.setFocusListener(focused -> {
            if (focused) {
                resetCustomFilter();
                reloadPageButtons(false);
            }
        });
        searchBox.setEnabledCallback(() -> xSize() == extendedXSize());

        searchSettings = new GuiButton().setSize(settingsSize, settingsSize).setHoverText(I18n.format("pi.button.search_settings"));
        GuiTexture settingsTex = new GuiTexture(16, 0, settingsSize, settingsSize, PITextures.PI_PARTS);
        settingsTex.setXPosMod(() -> searchBox.maxXPos() + 1);
        settingsTex.setTexSizeOverride(16, 16);
        settingsTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> searchSettingsProps.glColour(mouseOver));
        settingsTex.setPostDrawCallback(IDrawCallback::resetColour);
        searchSettings.addChild(settingsTex);
        searchSettings.setXPosMod(() -> searchBox.maxXPos() + 2);
        searchSettings.addAndFireReloadCallback(guiButton -> guiButton.setYPos(searchBox.yPos()));
        searchSettings.onPressed(() -> {
            GuiPopUpDialogBase dialog = new GuiPopUpDialogBase(this);
            dialog.setSize(searchBox.xSize(), (SearchMode.values().length * 13) + 5);
            dialog.addChild(new StyledGuiRect("user_dialogs").setPosAndSize(dialog));
            dialog.setPos(searchBox.xPos(), searchBox.yPos() - dialog.ySize());

            int y = dialog.yPos() + 3;
            for (SearchMode mode : SearchMode.values()) {
                StyledGuiButton button = new StyledGuiButton("user_dialogs.button_style");
                button.setTrim(false);
                button.setText(I18n.format(mode.getUnlocalizedName()));
                button.setPos(dialog.xPos() + 3, y).setSize(dialog.xSize() - 6, 12);
                button.setToggleMode(true).setToggleStateSupplier(() -> PIConfig.searchMode == mode);
                button.onPressed(() -> changeSearchMode(mode));
                dialog.addChild(button);
                y += 13;
            }

            dialog.show();
        });
        searchSettings.setEnabledCallback(() -> xSize() == extendedXSize());

        addChild(searchBox);
        addChild(searchSettings);
    }

    private void changeSearchMode(SearchMode mode) {
        PIConfig.searchMode = mode;
        PIConfig.save();
        reloadPageButtons(false);
    }

    @Override
    public void reloadElement() {
        String newController = controller.getButtonController();
        double pos = scrollBar.getRawPos();

        setXSize(extended ? extendedXSize() : HIDDEN_X_SIZE);

        scrollBar.setYSize(ySize() - NAV_BAR_SIZE - TITLE_BAR_SIZE - FOOTER_SIZE - 3);
        scrollBar.getBackgroundElement().setSize(scrollBar);

        scrollElement.setPos(xPos() + DIR_BAR_SIZE, yPos() + TITLE_BAR_SIZE + NAV_BAR_SIZE + 2);
        scrollElement.setSize(xSize() - 12, ySize() - TITLE_BAR_SIZE - NAV_BAR_SIZE - FOOTER_SIZE - 3);
        scrollElement.setListSpacing(1);

        currentPage = controller.getActiveTab().pageURI;
        reloadPageButtons(true);

        super.reloadElement();
        if (buttonController.equals(newController)) {
            scrollBar.updateRawPos(pos);
        }
        buttonController = newController;
    }

    public void setPageFilter(List<String> pageURIs) {
        reloadPageButtons(true, pageURIs);
    }

    private void reloadPageButtons(boolean updateNav) {
        if (searchBox.getText().equals("[Custom Filter]")) return;
        reloadPageButtons(updateNav, null);
    }

    private void reloadPageButtons(boolean updateNav, @Nullable List<String> pageOverride) {
        scrollElement.clearElements();
        scrollElement.resetScrollPositions();
        List<DocumentationPage> pages;
        String search = searchBox.getText().toLowerCase();

        if (pageOverride != null) {
            pages = new ArrayList<>();
            for (String page : pageOverride) {
                DocumentationPage docPage = DocumentationManager.getPage(page);
                if (page != null) {
                    pages.add(docPage);
                }
            }
            searchBox.setText("[Custom Filter]");
        }
        else if (!search.isEmpty() && !updateNav) {
            pages = searchPages(search, PIConfig.searchMode);
        }
        else {
            pages = controller.getSubPages();
        }

        pages.sort(Comparator.comparingInt(page -> (page.treeDepth * 5000) + page.getSortingWeight()));

        addPageButtons(pages, pageOverride == null);
        if (updateNav) {
            updateNavButtons();
        }
    }

    private LinkedList<DocumentationPage> searchPages(String search, SearchMode mode) {
        LinkedList<DocumentationPage> candidates = new LinkedList<>();
        LinkedList<DocumentationPage> results = new LinkedList<>();
        DocumentationPage activePage = controller.getActiveTab().getDocPage();

        switch (mode) {
            case EVERYWHERE:
                candidates.addAll(DocumentationManager.getAllPages());
                break;
            case SELECTED_MOD:
                DataUtils.addIf(DocumentationManager.getAllPages(), candidates, page -> page.getModid().equals(activePage.getModid()));
                break;
            case PAGE_SUB_PAGES:
                addChildPagesRecursively(activePage, candidates);
                break;
            case PAGE_ONLY:
                candidates.addAll(activePage.getSubPages());
                break;
        }

        for (DocumentationPage page : candidates) {
            if (doesPageMatchSearch(page, search) && (!page.isHidden() || PIConfig.editMode())) {
                results.add(page);
            }
        }

        return results;
    }

    private void addChildPagesRecursively(DocumentationPage page, LinkedList<DocumentationPage> list) {
        for (DocumentationPage child : page.getSubPages()) {
            list.add(child);
            addChildPagesRecursively(child, list);
        }
    }

    private boolean doesPageMatchSearch(DocumentationPage page, String search) {
        if (page.getDisplayName().toLowerCase().contains(search)) {
            return true;
        }

        for (ContentRelation relation : page.getRelations()) {
            if (relation.getLocalizedName().toLowerCase().contains(search)) {
                return true;
            }
        }

        return false;
    }

    private void addPageButtons(List<DocumentationPage> buttons, boolean addBack) {
        DocumentationPage current = DocumentationManager.getPage(currentPage);

        if (current != null && current.getParent() != null && !currentPage.equals(RootPage.ROOT_URI)) {
            if (current.getSubPages().isEmpty()) {
                current = current.getParent();
            }
            if (current != null && current.getParent() != null) {
                PageButton back = new PageButton(current.getParent(), controller, true);
                back.setXSize(scrollElement.xSize() - (scrollBar.xSize() + 4));
                back.setXPosMod((guiButton, integer) -> scrollElement.maxXPos() - guiButton.xSize() - (scrollBar.xSize() + 3));
                scrollElement.addElement(back);
            }
        }

        for (DocumentationPage page : buttons) {
            if (page.isHidden() && !PIConfig.editMode()) continue;
            PageButton button = new PageButton(page, controller);
            button.setXSize(scrollElement.xSize() - (scrollBar.xSize() + 4));
            button.setXPosMod((guiButton, integer) -> scrollElement.maxXPos() - guiButton.xSize() - (scrollBar.xSize() + 3));
            scrollElement.addElement(button);
        }
    }

    @Override
    public boolean onUpdate() {
        int targetSize = extended ? extendedXSize() : HIDDEN_X_SIZE;
        int moveSpeed = 30;

        if (xSize() != targetSize) {
            addToXSize(MathHelper.clip(targetSize - xSize(), -moveSpeed, moveSpeed));
            container.pageListMotionUpdate();
        }

        //Need to detect when the active page is changes externally e.g. via the API
        String openPage = controller.getActiveTab().pageURI;
        if (!currentPage.equals(openPage)) {
            currentPage = openPage;
            reloadPageButtons(true);
        }

        searchBox.setTextColor(searchBoxProps.textColour());
        searchBox.fillColour = searchBoxProps.colour();
        searchBox.borderColour = searchBoxProps.border();

        return super.onUpdate();
    }

    public void updateNavButtons() {
        DisplayController.TabData activeTab = controller.getActiveTab();
        backButton.setDisabled(!activeTab.canGoBack());
        forwardButton.setDisabled(!activeTab.canGoForward());

        DocumentationPage page = controller.getActiveTab().getDocPage();
        currentPage = page.getPageURI();
        page = page.getParent();

        removeChildByGroup("TREE_BUTTON_GROUP");
        navButtons.clear();

        int currentLength = 0;

        while (page != null) {
            String name = page.getDisplayName().length() > 18 ? page.getDisplayName().substring(0, 16) + ".." : page.getDisplayName();
            int height = fontRenderer.getStringWidth(name) + 6;
            if (currentLength + height > ySize() - NAV_BAR_SIZE - FOOTER_SIZE) {
                if (currentLength == 0) {
                    name = fontRenderer.trimStringToWidth(page.getDisplayName(), ySize() - NAV_BAR_SIZE - FOOTER_SIZE - 8) + "..";
                    height = fontRenderer.getStringWidth(name) + 6;
                }
                else {
                    break;
                }
            }

            GuiButton newButton = new StyledGuiButton(dirButtonRender).setText(name).setTrim(false).setShadow(false).setRotation(ROT_CC);
            newButton.setSize(DIR_BAR_SIZE - 2, height);
            newButton.textXOffset = 1;
            newButton.setYPos(yPos() + NAV_BAR_SIZE + 1);
            newButton.setXPosMod((guiButton, integer) -> xPos() + 1);
            newButton.addToGroup("TREE_BUTTON_GROUP");
            DocumentationPage thisPage = page;
            newButton.onPressed(() -> {
                resetCustomFilter();
                controller.openPage(thisPage.getPageURI(), false);
            });
            addChild(newButton);

            for (GuiButton button : navButtons) {
                button.translate(0, height);
            }

            navButtons.add(newButton);
            currentLength += newButton.ySize();

            if (page.getParent() == page) {
                page = null;
            }
            else {
                page = page.getParent();
            }
        }
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        headerRender.render(this, xPos(), yPos(), xSize() + 1, NAV_BAR_SIZE);
        dirPathRender.render(this, xPos(), yPos() + NAV_BAR_SIZE, DIR_BAR_SIZE, ySize() - NAV_BAR_SIZE - FOOTER_SIZE);
        bodyRender.render(this, xPos() + DIR_BAR_SIZE, yPos() + NAV_BAR_SIZE + TITLE_BAR_SIZE, xSize() - DIR_BAR_SIZE + 1, ySize() - NAV_BAR_SIZE - FOOTER_SIZE - TITLE_BAR_SIZE);
        footerRender.render(this, xPos(), yPos() + ySize() - FOOTER_SIZE, xSize() + 1, FOOTER_SIZE);

        String navTitle = I18n.format("pi.gui.navigation.title");
        int width = fontRenderer.getStringWidth(navTitle);
        drawCustomString(fontRenderer, navTitle, xPos() + xSize() / 2F - width / 2F, yPos() + NAV_BAR_SIZE - fontRenderer.FONT_HEIGHT - 1, xSize() - 20, 0xFFFFFF, LEFT, NORMAL, false, true, false); //Todo style and stuff
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(false);
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    //TODO test back button
    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (keyCode == 14 && !backButton.isDisabled() && backButton.isEnabled()) {
            backButton.onPressed(backButton.xPos() + 1, backButton.yPos() + 1, 0);
        }
        return super.charTyped(typedChar, keyCode);
    }

    public void setFullyExtended() {
        extended = true;
        setXSize(extendedXSize());
        container.pageListMotionUpdate();
    }

    private int extendedXSize() {
        return container.getListMaxWidth().get();
    }

    private void resetCustomFilter() {
        if (searchBox.getText().equals("[Custom Filter]")) {
            searchBox.setText("");
        }
    }
}
