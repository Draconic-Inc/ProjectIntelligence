package com.brandon3055.projectintelligence.client;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.ProcessHandlerClient;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiPopUpDialogBase;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.lib.DelayedExecutor;
import com.brandon3055.brandonscore.lib.DelayedTask;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.client.gui.ContentInfo;
import com.brandon3055.projectintelligence.client.gui.ContentInfo.ContentType;
import com.brandon3055.projectintelligence.client.gui.GuiContentSelect;
import com.brandon3055.projectintelligence.client.gui.GuiContentSelect.SelectMode;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;

import com.brandon3055.projectintelligence.client.gui.guielements.*;
import com.brandon3055.projectintelligence.client.gui.swing.PIEditor;
import com.brandon3055.projectintelligence.docmanagement.DocumentationManager;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Monitor;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.datafix.fixes.SpawnEggNames;


import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

/**
 * Created by brandon3055 on 4/08/2017.
 */
public class PIGuiHelper {

    public static PIEditor editor = null;
    public static ConcurrentLinkedDeque<String> errorCache = new ConcurrentLinkedDeque<>();
    private static LinkedList<String> entitySelectionList = new LinkedList<>();
    private static LinkedList<String> playerInventory = new LinkedList<>();

    /**
     * Displays an error message in the PI gui or id the gui is not currently open schedules an error to be displayed the
     * next time the gui is opened.
     * <p>
     * Supports multiple calls. Errors will be added to a list to be displayed.
     */
    public static void displayError(String error) {
        displayError(error, false);
    }

    /**
     * Displays an error message in the PI gui or id the gui is not currently open schedules an error to be displayed the
     * next time the gui is opened.
     * <p>
     * Supports multiple calls. Errors will be added to a list to be displayed.
     *
     * @param error    The error message to display to the user
     * @param noRepeat If true the error will only be displayed if there is no identical error already in the list.
     */
    public static void displayError(String error, boolean noRepeat) {
        if (noRepeat && errorCache.contains(error)) {
            return;
        }
        LogHelperBC.error("[Pi Reported Error]: " + error);
        errorCache.add(error);
        GuiProjectIntelligence.updateErrorDialog = true;
    }

    public static void displayLinkConfirmDialog(GuiElement parent, String link) {
        URI uri;
        try {
            uri = new URI(link);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            PIGuiHelper.displayError("Failed to open link due to unknown error!\n" + e.getMessage());
            return;
        }
        GuiPopUpDialogBase dialog = new GuiPopUpDialogBase(parent);
        dialog.setXSize(300);
        dialog.setDragBar(12);
        dialog.setCloseOnCapturedClick(true);
        GuiElement background;
        dialog.addChild(background = new StyledGuiRect("user_dialogs"));

        GuiLabel infoLabel = new GuiLabel(I18n.get("pi.md.link_confirmation.txt"));
        infoLabel.setWrap(true).setShadow(false);
        infoLabel.setTextColour(StyleHandler.getInt("user_dialogs." + StyleHandler.StyleType.TEXT_COLOUR.getName()));
        infoLabel.setXSize(dialog.xSize() - 30);
        infoLabel.setHeightForText();
        infoLabel.setRelPos(15, 10);
        dialog.addChild(infoLabel);

        GuiLabel urlLabel = new GuiLabel("\"" + link + "\"");
        urlLabel.setWrap(true).setShadow(false);
        urlLabel.setTextColour(StyleHandler.getInt("user_dialogs." + StyleHandler.StyleType.TEXT_COLOUR.getName()));
        urlLabel.setXSize(dialog.xSize() - 30);
        urlLabel.setHeightForText();
        urlLabel.setPos(infoLabel.xPos(), infoLabel.maxYPos() + 10);
        dialog.addChild(urlLabel);

        GuiButton yesButton = new StyledGuiButton("user_dialogs." + StyleHandler.StyleType.BUTTON_STYLE.getName());
        yesButton.setText(I18n.get("pi.button.yes"));
        yesButton.setSize(80, 15);
        yesButton.setPos(dialog.xPos() + 15, urlLabel.maxYPos() + 10);
        yesButton.onPressed(() -> Utils.openWebLink(uri));
        dialog.addChild(yesButton);

        GuiButton copyButton = new StyledGuiButton("user_dialogs." + StyleHandler.StyleType.BUTTON_STYLE.getName());
        copyButton.setText(I18n.get("pi.button.copy_to_clipboard"));
        copyButton.setSize(108, 15);
        copyButton.setRelPos(yesButton, 81, 0);
        copyButton.onPressed(() -> Utils.setClipboardString(link));
        dialog.addChild(copyButton);

        GuiButton cancelButton = new StyledGuiButton("user_dialogs." + StyleHandler.StyleType.BUTTON_STYLE.getName());
        cancelButton.setText(I18n.get("pi.button.cancel"));
        cancelButton.setSize(80, 15);
        cancelButton.setRelPos(copyButton, 109, 0);
        dialog.addChild(cancelButton);

        dialog.setYSize((cancelButton.maxYPos() + 10) - dialog.yPos());
        background.setPosAndSize(dialog);
        dialog.showCenter(parent.displayZLevel + 50);
    }

    /**
     * @return a list of all mods listed in the doc manifest or on disk if the manifest failed to downland.
     */
    public static synchronized List<String> getSupportedMods() {
        return ImmutableList.copyOf(DocumentationManager.getDocumentedMods());
    }

    //region Editor Helpers
    public static void displayEditor() {
        new Thread(() -> {
            if (editor == null) {
                editor = new PIEditor();
            } else {
                editor.dispose();
            }
            editor.reload();
            editor.setExtendedState(JFrame.NORMAL);
            editor.setVisible(true);
            editor.toFront();
            centerWindowOnMC(editor);
        }).start();
    }

    public static void closeEditor() {
        if (editor != null && editor.isVisible()) {
            editor.dispose();
        }
    }

    //TODO Test This
    public static void centerWindowOnMC(Component window) {
        Monitor monitor = Minecraft.getInstance().getWindow().findBestMonitor();
        if (monitor == null) return;
        int centerX = monitor.getX() + (monitor.getCurrentMode().getWidth() / 2);
        int centerY = monitor.getY() + (monitor.getCurrentMode().getHeight() / 2);
        window.setLocation(centerX - (window.getWidth() / 2), Math.max(0, centerY - (window.getHeight() / 2)));
    }

    public static void centerWindowScreen(Component window) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = ge.getCenterPoint();

        int centerX = (int) centerPoint.getX();
        int centerY = (int) centerPoint.getY();
        window.setLocation(centerX - (window.getWidth() / 2), Math.max(0, centerY - (window.getHeight() / 2)));
    }

    public static void centerWindowOn(Component window, Component centerOn) {
        int centerX = (int) centerOn.getX() + (centerOn.getWidth() / 2);
        int centerY = (int) centerOn.getY() + (centerOn.getHeight() / 2);
        window.setLocation(centerX - (window.getWidth() / 2), Math.max(0, centerY - (window.getHeight() / 2)));
    }

    public static synchronized LinkedList<String> getEntitySelectionList() {
        if (entitySelectionList.isEmpty()) {
            for (SpawnEggItem type : SpawnEggItem.eggs()) {
                entitySelectionList.add(type.getType(null).getRegistryName().toString());
            }
            Collections.sort(entitySelectionList);
        }

        return entitySelectionList;
    }

    public static synchronized LinkedList<String> getPlayerInventory() {
        return playerInventory;
    }

//    public static void updatePlayerInventory(PlayerEntity player) {
//        playerInventory.clear();
//        player.inventory.mainInventory.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> playerInventory.add(new StackReference(stack).toString()));
//        player.inventory.armorInventory.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> playerInventory.add(new StackReference(stack).toString()));
//        player.inventory.offHandInventory.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> playerInventory.add(new StackReference(stack).toString()));
//    }

    public static void openContentChooser(@Nullable ContentInfo contentInfo, SelectMode mode, Consumer<ContentInfo> action, ContentType... types) {
        ProcessHandlerClient.syncTask(() -> {
            if (Minecraft.getInstance().screen instanceof GuiContentSelect) {
                return;
            }
            GuiContentSelect gui = new GuiContentSelect(Minecraft.getInstance().screen, mode, contentInfo, types);
            gui.setSelectCallBack(action);
            Minecraft.getInstance().setScreen(gui);
        });
    }

    //endregion
}
