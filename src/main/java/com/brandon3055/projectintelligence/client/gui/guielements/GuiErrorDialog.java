package com.brandon3055.projectintelligence.client.gui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiBorderedRect;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.LEFT;

/**
 * Created by brandon3055 on 12/08/2017.
 */
public class GuiErrorDialog extends GuiPopUpDialogBase<GuiErrorDialog> {

    private GuiScrollElement errorList;

    public GuiErrorDialog(GuiElement parant) {
        super(parant);
        setSize(320, 250);
        setDragBar(12);
        setCloseOnOutsideClick(false);
    }

    @Override
    public void addChildElements() {
        childElements.clear();

        //Background Rectangle
        addChild(new GuiBorderedRect().setColours(0xFF000000, 0xFF909090).setPosAndSize(this));
        addChild(new GuiBorderedRect().setFillColour(0xFF909090).setPos(this).setSize(xSize(), 14));

        // Window Title
        addChild(new GuiLabel(I18n.get("pi.config.pi_errors.title")).setRelPos(this, 4, 0).setSize(xSize(), 14).setTextColour(0).setAlignment(LEFT).setShadow(false));

        GuiButton close = new GuiButton("Close").setSize(50, 12).setFillColours(0xFF900000, 0xFFFF0000).setRelPos(xSize() - 51, 1);
        close.onPressed(() -> {
            PIGuiHelper.errorCache.clear();
            close();
        });
        addChild(close);

        //Config List
        errorList = new GuiScrollElement();
        errorList.setRelPos(2, 14).setSize(xSize() - 3, ySize() - 15);
        errorList.setStandardScrollBehavior();
        errorList.setListMode(GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH);
        errorList.setListSpacing(5);

        addChild(errorList);

        super.addChildElements();
    }

    @Override
    public void reloadElement() {
        errorList.clearElements();

        for (String error : PIGuiHelper.errorCache) {
            GuiLabel label = new GuiLabel(error).setAlignment(LEFT);
            label.setTextColour(0xFF0000);
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

}
