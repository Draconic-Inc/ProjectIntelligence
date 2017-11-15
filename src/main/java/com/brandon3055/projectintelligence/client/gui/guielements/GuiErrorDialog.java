package com.brandon3055.projectintelligence.client.gui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.projectintelligence.PIHelpers;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

import static com.brandon3055.projectintelligence.client.StyleHandler.StyleType.TEXT_COLOUR;

/**
 * Created by brandon3055 on 12/08/2017.
 */
public class GuiErrorDialog extends GuiPopUpDialogBase<GuiErrorDialog> {

    private GuiScrollElement errorList;


    public GuiErrorDialog(MGuiElementBase parant) {
        super(parant);
        setSize(280, 250);
        setDragBar(12);
        setCloseOnOutsideClick(false);
    }


    @Override
    public void addChildElements() {
        childElements.clear();

        //Background Rectangle
        addChild(new StyledGuiRect("user_dialogs").setPosAndSize(this));

        // Window Title
        addChild(new GuiLabel(TextFormatting.UNDERLINE + I18n.format("pi.config.pi_errors.title"))//
                .setPos(this).setSize(xSize(), 10).translate(4, 3).setTextColGetter(hovering -> StyleHandler.getInt("user_dialogs." + TEXT_COLOUR.getName())).setShadow(false).setAlignment(GuiAlign.CENTER));

        GuiButton close = new StyledGuiButton("user_dialogs.button_style").setText("OK").setSize(100, 20).setRelPos((xSize() / 2) - 50, ySize() - 25);
        close.setListener((event, eventSource) -> {
            PIHelpers.errorCache.clear();
            close();
        });
        addChild(close);

        //Config List
        errorList = new GuiScrollElement();
        errorList.setRelPos(5, 20).setSize(xSize() - 10, ySize() - 52);
        errorList.setStandardScrollBehavior();
        errorList.setListMode(GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH);
//        errorList.getVerticalScrollBar().setHidden(true);
        errorList.setListSpacing(5);

        addChild(errorList);

        if (!GuiProjectIntelligence.devMode) {
            super.addChildElements();
        }
    }

    @Override
    public void reloadElement() {
        errorList.clearElements();

        for (String error : PIHelpers.errorCache) {
            GuiLabel label = new GuiLabel(error);
            label.setTextColour(changeShade(StyleHandler.getInt("user_dialogs." + TEXT_COLOUR.getName()), 0));
            label.setWrap(true).setShadow(false);
            label.setHeightForText(errorList.xSize());
            errorList.addElement(label);
        }


        super.reloadElement();
    }

    @Override
    public boolean onUpdate() {
        return super.onUpdate();
    }

    @Override
    protected boolean keyTyped(char typedChar, int keyCode) throws IOException {
//        if (keyCode == 1) {
//            close();
//            return true;
//        }
        return super.keyTyped(typedChar, keyCode);
    }

}
