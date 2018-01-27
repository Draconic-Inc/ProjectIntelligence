package com.brandon3055.projectintelligence.client.gui.guielements;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.StyleHandler.StyleType;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.TabManager;
import com.brandon3055.projectintelligence.docdata.DocumentationPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

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
public class GuiPartPageList extends MGuiElementBase<GuiPartPageList> {
    private final int HIDDEN_X_SIZE = 12;

    /** Nav Bar Y Size */
    private final int NAV_BAR_SIZE = 12;
    /** Title Bar Y Size */
    private final int TITLE_BAR_SIZE = 0;
    /** Directory Bar X Size */
    private final int DIR_BAR_SIZE = 12;
    /** Footer Y Size */
    private final int FOOTER_SIZE = 16;

    private int extendedXSize = 150;
    private boolean extended = true;
    private GuiSlideControl scrollBar;
    private GuiScrollElement scrollElement;
    private List<GuiButton> navButtons = new LinkedList<>();
    private GuiProjectIntelligence mainWindow;

    public GuiPartPageList(GuiProjectIntelligence mainWindow) {
        this.mainWindow = mainWindow;
    }


    @Override
    public void addChildElements() {
        super.addChildElements();

        GuiButton toggleView = new GuiButton().setSize(10, 10).setHoverText(I18n.format("pi.button.toggle_nav_window.info"));
        GuiTexture tex = new GuiTexture(0, 16, 6, 7, PITextures.PI_PARTS);
        tex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> StyleHandler.getColour("page_list.hide_button." + (toggleView.isMouseOver(mouseX, mouseY) ? "hover" : "colour")).glColour());
        tex.setPostDrawCallback(IDrawCallback::resetColour);
        tex.setTexXGetter(() -> extended ? 0 : 9);
        toggleView.addChild(tex);
        toggleView.addAndFireReloadCallback(guiButton -> guiButton.setYPos(yPos() + 1));
        toggleView.setListener((event, eventSource) -> extended = !extended);
        tex.setXPosMod((guiButton1, integer) -> maxXPos() - 10).translate(0, 2);
        toggleView.setXPosMod((guiButton1, integer) -> maxXPos() - 11);
        addChild(toggleView);

        scrollBar = new GuiSlideControl(VERTICAL);
        scrollBar.setXPosMod((guiSlideControl, integer) -> maxXPos() - (scrollBar.xSize() + (scrollBar.xSize() == 4 ? 2 : 2)));
        scrollBar.setYPosMod((guiSlideControl, integer) -> yPos() + NAV_BAR_SIZE + TITLE_BAR_SIZE);
        scrollBar.setSize(10, ySize() - (NAV_BAR_SIZE + TITLE_BAR_SIZE + FOOTER_SIZE));

        scrollBar.setBackgroundElement(new StyledGuiRect("page_list.scroll_bar"){
            @Override
            public boolean isMouseOver(int mouseX, int mouseY) {
                return super.isMouseOver(mouseX, mouseY) || scrollBar.isDragging();
            }
        });
        scrollBar.setSliderElement(new StyledGuiRect("page_list.scroll_bar.scroll_slider"){
            @Override
            public boolean isMouseOver(int mouseX, int mouseY) {
                return super.isMouseOver(mouseX, mouseY) || scrollBar.isDragging();
            }
        });
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

        scrollElement.getVerticalScrollBar().setXSize(10).updateElements();
    }

    @Override
    public void reloadElement() {
        extendedXSize = Math.min(150, mainWindow.xSize() / 3);
        setXSize(extended ? extendedXSize : HIDDEN_X_SIZE);

//        DocumentationManager.checkAndReloadDocFiles();//TODO Temp
        scrollBar.setSize(10, ySize() - (NAV_BAR_SIZE + TITLE_BAR_SIZE + FOOTER_SIZE));
        scrollBar.getBackgroundElement().setSize(10, ySize() - (NAV_BAR_SIZE + TITLE_BAR_SIZE + FOOTER_SIZE));

        scrollElement.setPos(xPos() + DIR_BAR_SIZE, yPos() + TITLE_BAR_SIZE + NAV_BAR_SIZE + 1);
        scrollElement.setSize(xSize() - 12, ySize() - TITLE_BAR_SIZE - NAV_BAR_SIZE - FOOTER_SIZE - 2);
        scrollElement.setListSpacing(1);

        reloadPageButtons();
        updatePageButtonStyle();

        super.reloadElement();
    }

    private void reloadPageButtons() {
        scrollElement.clearElements();
        LinkedList<DocumentationPage> pages = new LinkedList<>();

        //TODO create a custom button with styling and ability to highlight when selected.
        //Also icon(s) and edit mode display
        for (DocumentationPage page : TabManager.getSubPages()) {
            pages.add(page);//TODO Search Filtering
        }

        addPageButtons(pages);
        updateNavButtons();
    }

    private void addPageButtons(LinkedList<DocumentationPage> buttons) {
        for (DocumentationPage page : buttons) {
            PageButton button = new PageButton(page, mainWindow);
            button.setXSize(scrollElement.xSize() - (scrollBar.xSize() + 4));
            button.setXPosMod((guiButton, integer) -> scrollElement.maxXPos() - guiButton.xSize() - (scrollBar.xSize() + 3));
            scrollElement.addElement(button);
        }
    }

    @Override
    public boolean onUpdate() {
        int targetSize = extended ? extendedXSize : HIDDEN_X_SIZE;
        int moveSpeed = 30;

        if (xSize() != targetSize) {
            addToXSize(MathHelper.clip(targetSize - xSize(), -moveSpeed, moveSpeed));
            if (xSize() == targetSize) {
                mainWindow.contentWindow.reloadElement();
            }
        }

        if (StyleHandler.getBoolean("page_list.scroll_bar." + StyleType.COMPACT_BAR.getName())) {
            if (scrollBar.xSize() != 4) {
                scrollBar.setXSize(4);
                scrollBar.getBackgroundElement().setXSize(4);
                scrollBar.getSliderElement().setXSize(4);
                scrollBar.setInsets(1, 0, 1, 0);
                scrollElement.reloadElement();
                reloadPageButtons();
            }
        }
        else if (scrollBar.xSize() != 10) {
            scrollBar.setXSize(10);
            scrollBar.getBackgroundElement().setXSize(10);
            scrollBar.getSliderElement().setXSize(8);
            scrollBar.setInsets(1, 1, 1, 1);
            scrollElement.reloadElement();
            reloadPageButtons();
        }

        updatePageButtonStyle();

        return super.onUpdate();
    }

    public void updateNavButtons() {
        DocumentationPage page = TabManager.getActiveTab().getDocPage();
        removeChildByGroup("TREE_BUTTON_GROUP");
        navButtons.clear();

        int currentLength = 0;

        while (page != null) {
            String name = page.getDisplayName().length() > 18 ? page.getDisplayName().substring(0, 16) + ".." : page.getDisplayName();
            int height = fontRenderer.getStringWidth(name) + 6;
            if (currentLength + height > yPos() + ySize() - NAV_BAR_SIZE) {
                break;
            }

            GuiButton newButton = new StyledGuiButton("page_list.dir_buttons", true).setText(name).setTrim(false).setShadow(false).setRotation(ROT_CC);
            newButton.setSize(DIR_BAR_SIZE, height - 1);
            newButton.setYPos(yPos() + DIR_BAR_SIZE);
            newButton.setXPosMod((guiButton, integer) -> xPos());
            newButton.addToGroup("TREE_BUTTON_GROUP");
            DocumentationPage thisPage = page;
            newButton.setListener((event, eventSource) -> {
                TabManager.openPage(thisPage.getPageURI(), false);
//                DocumentationManager.setSelectedPage(thisPage);
                mainWindow.reloadGui();
            });
            addChild(newButton);

            for (GuiButton button : navButtons) {
                button.translate(0, height);
            }

            navButtons.add(newButton);
            currentLength = newButton.yPos() + newButton.ySize();

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

        //Header
        {
            boolean shadedBorders = StyleHandler.getBoolean("page_list.header." + StyleType.SHADED_BORDERS.getName());
            boolean vanillaT = StyleHandler.getBoolean("page_list.header." + StyleType.VANILLA_TEXTURE.getName());
            int border = StyleHandler.getInt("page_list.header." + StyleType.BORDER.getName());

            if (shadedBorders || !vanillaT) {
                int colour = StyleHandler.getInt("page_list.header." + StyleType.COLOUR.getName());
                int light = changeShade(border, 0.2);
                int dark = changeShade(border, -0.2);

                if (shadedBorders) {
                    boolean thickBorders = StyleHandler.getBoolean("page_list.header.shaded_borders." + StyleType.THICK_BORDERS.getName());
                    double b = thickBorders ? 1 : 0.5;

                    drawColouredRect(xPos(), yPos(), xSize(), NAV_BAR_SIZE, colour);                     //Bar
                    drawColouredRect(xPos(), yPos(), xSize(), b, light);                       //Bar Top Accent
                    drawColouredRect(xPos(), yPos() + NAV_BAR_SIZE - b, xSize(), b, dark);               //Bar Bottom Accent
                }
                else {
                    int fill = StyleHandler.getInt("page_list.header." + StyleType.COLOUR.getName());
                    drawColouredRect(xPos(), yPos(), xSize(), NAV_BAR_SIZE, border);
                    drawColouredRect(xPos() + 1, yPos() + 1, xSize() - 2, NAV_BAR_SIZE - 1, fill);
                }

            }
            else {
                StyleHandler.getColour("page_list.header." + StyleType.COLOUR.getName()).glColour();
                ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI_SQ);
                drawTiledTextureRectWithTrim(xPos(), yPos(), xSize(), NAV_BAR_SIZE, 4, 4, 0, 4, 0, 128, 256, 128);
                GlStateManager.color(1, 1, 1, 1);
            }
        }

        boolean tb = !StyleHandler.getBoolean("page_list.body." + StyleType.SHADED_BORDERS.getName()) || StyleHandler.getBoolean("page_list.body.shaded_borders." + StyleType.THICK_BORDERS.getName());
        double raWidth = tb ? 1 : 0.5; //Right Accent Width
        //Body
        {
            boolean shadedBorders = StyleHandler.getBoolean("page_list.body." + StyleType.SHADED_BORDERS.getName());
            boolean vanillaT = StyleHandler.getBoolean("page_list.body." + StyleType.VANILLA_TEXTURE.getName());
            int border = StyleHandler.getInt("page_list.body." + StyleType.BORDER.getName());
            shadedBorders = false;
//            vanillaT = true;

            if (shadedBorders || !vanillaT) {
                int colour = StyleHandler.getInt("page_list.body." + StyleType.COLOUR.getName());
                int light = changeShade(border, 0.15);
                int dark = changeShade(border, -0.15);

                if (shadedBorders) {
                    drawColouredRect(xPos(), yPos() + TITLE_BAR_SIZE, xSize(), ySize() - TITLE_BAR_SIZE, colour);           //Background
                    drawColouredRect(xPos() + DIR_BAR_SIZE - raWidth, yPos() + TITLE_BAR_SIZE, raWidth, ySize() - TITLE_BAR_SIZE, dark);                  //Left Divider
                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + TITLE_BAR_SIZE, raWidth, ySize() - TITLE_BAR_SIZE, light);                     //Left Divider

//                    drawColouredRect(xPos(), yPos() + 12, xSize(), raWidth, light);                               //Mods Divider
//                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + 24 - raWidth, xSize() - 12, raWidth, dark);                  //Mods Divider

                    drawColouredRect(xPos() + xSize() - raWidth * 2, yPos(), raWidth, ySize(), light);                  //Right Accent
                    drawColouredRect(xPos() + xSize() - raWidth, yPos(), raWidth, ySize(), dark);                       //Right Accent
                    raWidth *= 2;
                }
                else {
                    drawBorderedRect(xPos(), yPos() + NAV_BAR_SIZE, DIR_BAR_SIZE, ySize() - NAV_BAR_SIZE, 1, colour, border);

//                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + NAV_BAR_SIZE, xSize() - DIR_BAR_SIZE, NAV_BAR_SIZE, border);
//                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + NAV_BAR_SIZE + 1, xSize() - DIR_BAR_SIZE - 1, NAV_BAR_SIZE - 1, colour);

                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + NAV_BAR_SIZE + TITLE_BAR_SIZE, xSize() - DIR_BAR_SIZE, ySize() - NAV_BAR_SIZE - TITLE_BAR_SIZE, border);
                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + NAV_BAR_SIZE + TITLE_BAR_SIZE + 1, xSize() - DIR_BAR_SIZE - 1, ySize() - NAV_BAR_SIZE - TITLE_BAR_SIZE - 2, colour);
                }
            }
            else {
                StyleHandler.getColour("page_list.body." + StyleType.COLOUR.getName()).glColour();
                ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI_SQ);

                drawTiledTextureRectWithTrim(xPos(), yPos() + NAV_BAR_SIZE + TITLE_BAR_SIZE, xSize(), ySize() - NAV_BAR_SIZE - TITLE_BAR_SIZE, 4, 4, 4, 4, 0, 128, 256, 128);
                drawTiledTextureRectWithTrim(xPos(), yPos() + NAV_BAR_SIZE, DIR_BAR_SIZE, ySize() - NAV_BAR_SIZE, 4, 4, 4, 4, 0, 128, 256, 128);
//                drawTiledTextureRectWithTrim(xPos() + DIR_BAR_SIZE, yPos() + NAV_BAR_SIZE, xSize() - DIR_BAR_SIZE, TITLE_BAR_SIZE, 4, 4, 4, 4, 0, 128, 256, 128);
//                drawTexturedModalRect(xPos() + DIR_BAR_SIZE, yPos() + NAV_BAR_SIZE, 256 - (xSize() - DIR_BAR_SIZE), 128, xSize() - DIR_BAR_SIZE, TITLE_BAR_SIZE);

                GlStateManager.color(1, 1, 1, 1);
            }
        }

        //Footer
        {
            boolean shadedBorders = StyleHandler.getBoolean("page_list.footer." + StyleType.SHADED_BORDERS.getName());
            boolean vanillaT = StyleHandler.getBoolean("page_list.footer." + StyleType.VANILLA_TEXTURE.getName());
            int border = StyleHandler.getInt("page_list.footer." + StyleType.BORDER.getName());

            if (shadedBorders || !vanillaT) {
                int colour = StyleHandler.getInt("page_list.footer." + StyleType.COLOUR.getName());
                int light = changeShade(border, 0.15);
                int dark = changeShade(border, -0.15);

                if (shadedBorders) {
                    boolean thickBorders = StyleHandler.getBoolean("page_list.footer.shaded_borders." + StyleType.THICK_BORDERS.getName());
                    double b = thickBorders ? 1 : 0.5;

                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + ySize() - FOOTER_SIZE, xSize() - raWidth - DIR_BAR_SIZE, FOOTER_SIZE, colour);  //Background
                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + ySize() - FOOTER_SIZE, b, FOOTER_SIZE, light);                            //Left Accent

                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + ySize() - FOOTER_SIZE, xSize() - DIR_BAR_SIZE - raWidth, b, light);            //Search Divider
                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + ySize() - b, xSize() - DIR_BAR_SIZE - raWidth, b, dark);                      //Search Divider
                }
                else {
                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + ySize() - FOOTER_SIZE, xSize() - DIR_BAR_SIZE - raWidth, FOOTER_SIZE, border);
                    drawColouredRect(xPos() + DIR_BAR_SIZE, yPos() + ySize() - FOOTER_SIZE + 1, xSize() - DIR_BAR_SIZE - raWidth - 1, FOOTER_SIZE - 2, colour);
                }

            }
            else {
                StyleHandler.getColour("page_list.footer." + StyleHandler.StyleType.COLOUR.getName()).glColour();
                ResourceHelperBC.bindTexture(PITextures.VANILLA_GUI_SQ);
                drawTiledTextureRectWithTrim(xPos() + NAV_BAR_SIZE, yPos() + ySize() - FOOTER_SIZE, xSize() - NAV_BAR_SIZE, FOOTER_SIZE, 4, 0, 4, 4, 4, 128, 252, 128);

                GlStateManager.color(1, 1, 1, 1);
            }
        }

        DocumentationPage selected = TabManager.getActiveTab().getDocPage();
//        String page = "//" + (selected == null ? "null-Page" : selected.getDisplayName());
//        drawCustomString(fontRenderer, page, xPos() + DIR_BAR_SIZE, yPos() + NAV_BAR_SIZE + TITLE_BAR_SIZE - fontRenderer.FONT_HEIGHT, xSize() - DIR_BAR_SIZE, 0xFFFFFF, CENTER, NORMAL, false, true, false); //Todo style and stuff

        drawCustomString(fontRenderer, I18n.format("pi.gui.navigation.title"), xPos() + 4, yPos() + NAV_BAR_SIZE - fontRenderer.FONT_HEIGHT - 1, xSize() - 20, 0xFFFFFF, LEFT, NORMAL, false, true, false); //Todo style and stuff

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0xFFFFFFFF);
    }

    private void updatePageButtonStyle() {
        btnVanillaTex = StyleHandler.getBoolean("page_list.page_buttons." + StyleType.VANILLA_TEXTURE.getName());
        btnShadedBorders = StyleHandler.getBoolean("page_list.page_buttons." + StyleType.SHADED_BORDERS.getName());
        btnThickBorders = StyleHandler.getBoolean("page_list.page_buttons.shaded_borders." + StyleType.THICK_BORDERS.getName());
        btnColour = StyleHandler.getColour("page_list.page_buttons." + StyleType.COLOUR.getName());
        btnColourHover = StyleHandler.getColour("page_list.page_buttons." + StyleType.HOVER.getName());
        btnBorder = StyleHandler.getColour("page_list.page_buttons." + StyleType.BORDER.getName());
        btnBorderHover = StyleHandler.getColour("page_list.page_buttons." + StyleType.BORDER_HOVER.getName());
        btnTextColour = StyleHandler.getInt("page_list.page_buttons." + StyleType.TEXT_COLOUR.getName());
        btnTextColourHover = StyleHandler.getInt("page_list.page_buttons." + StyleType.TEXT_HOVER.getName());
        btnTextShadow = StyleHandler.getBoolean("page_list.page_buttons." + StyleType.TEXT_SHADOW.getName());
//        btnIconVanillaTex = StyleHandler.getBoolean("page_list.page_buttons.page_icon." + StyleType.VANILLA_TEXTURE.getName());
//        btnIconBackground = StyleHandler.getColour("page_list.page_buttons.page_icon." + StyleType.BACKGROUND.getName());
//        btnIconBorder = StyleHandler.getColour("page_list.page_buttons.page_icon." + StyleType.BORDER.getName());
    }

    public static boolean btnVanillaTex = false;
    public static boolean btnShadedBorders = false;
    public static boolean btnThickBorders = false;
    public static Colour btnColour = new ColourARGB(0);
    public static Colour btnColourHover = new ColourARGB(0);
    public static Colour btnBorder = new ColourARGB(0);
    public static Colour btnBorderHover = new ColourARGB(0);
    public static int btnTextColour = 0;
    public static int btnTextColourHover = 0;
//    public static boolean btnIconVanillaTex = false;
    public static boolean btnTextShadow = false;
//    public static Colour btnIconBackground = new ColourARGB(0);
//    public static Colour btnIconBorder = new ColourARGB(0);
}
