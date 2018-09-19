package com.brandon3055.projectintelligence.api.internal;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 7/24/2018.
 * <p>
 * This interface is for internal use only. All your interaction with PI should go through @{@link com.brandon3055.projectintelligence.api.PiAPI}
 */
public interface IPiAPI {

    /**
     * Opens the main project intelligence gui.
     * By default pi will open to whatever page/pages the user had open last
     * or the home page if this is the first time pi is being opened.
     *
     * @param parentScreen The screen the user will be returned to when the PI UI is closed.
     * @since PI 1.0.0
     */
    void openGui(@Nullable GuiScreen parentScreen);

    /**
     * Opens the main project intelligence gui and opens the specified documentation page.
     *
     * @param parentScreen The screen the user will be returned to when the PI UI is closed.
     * @param pageURI      This is the unique identifier for the page that is to be opened.
     *                     You can get this by opening the PI GUI right-clicking on a page in the page list.
     * @since PI 1.0.0
     */
    void openGui(@Nullable GuiScreen parentScreen, String pageURI);

    /**
     * This method allows you to open pi and display a filtered list of pages.
     *
     * @param parentScreen The screen the user will be returned to when the PI UI is closed.
     * @param pageURIs A list of pages to display in the page list. The first page will be selected.
     * @since PI 1.0.0
     */
    void openGui(@Nullable GuiScreen parentScreen, List<String> pageURIs);

    /**
     * Opens the main project intelligence gui and opens the documentation for the specified mod.
     * If there is no documentation avalible for the specified mod the home page will be opened and the user will
     * receive an error message stating that there is not documentation avalible for the specified mod.
     *
     * @param parentScreen The screen the user will be returned to when the PI UI is closed.
     * @param modid the mod id of the mod who's documentation is to be displayed.
     * @since PI 1.0.0
     */
    void openModPage(@Nullable GuiScreen parentScreen, String modid);

    /**
     * This method returns a list containing every the page uri for loaded documentation page.
     *
     * @return a list containing the page uri of every loaded documentation page.
     * @since PI 1.0.0
     */
    List<String> getPageList();

    /**
     * This method returns a list containing the page uri for every loaded documentation page for the specified mod.
     *
     * @param modid The target mod id.
     * @return a list containing the page uri of every loaded documentation page for the specified mod.
     * @since PI 1.0.0
     */
    List<String> getModPageList(String modid);

    /**
     * This method returns the page uri's for all page related to the specified stack.
     *
     * @param stack the target stack.
     * @return a list of all pages related to the specified stack.
     * @since PI 1.0.0
     */
    List<String> getRelatedPages(ItemStack stack);

    /**
     * This method returns the page uri's for all page related to the specified entity.
     *
     * @param entityName the registry name of the target entity e.g. minecraft:pig
     * @return a list of all pages related to the specified entity.
     * @since PI 1.0.0
     */
    List<String> getRelatedPages(String entityName);

    /**
     * This method returns the page uri's for all page related to the specified fluid.
     *
     * @param fluid the target fluid
     * @return a list of all pages related to the specified entity.
     * @since PI 1.0.0
     */
    List<String> getRelatedPages(Fluid fluid);

    /**
     * Displays an error message in the PI gui or if the gui is not currently open schedules an error to be displayed the
     * next time the gui is opened.
     * <p>
     * Supports multiple calls. Errors are added to a list that will be displayed the next time the PI UI is opened.
     *
     * @param error The error message to display to the user.
     * @since PI 1.0.0
     */
    default void displayError(String error) {
        displayError(error, false);
    }

    /**
     * Displays an error message in the PI gui or if the gui is not currently open schedules an error to be displayed the
     * next time the gui is opened.
     * <p>
     * Supports multiple calls. Errors are added to a list that will be displayed the next time the PI UI is opened.
     *
     * @param error    The error message to display to the user.
     * @param noRepeat If true the error will only be displayed if there is no identical error already in the list.
     * @since PI 1.0.0
     */
    void displayError(String error, boolean noRepeat);

    /**
     * @return a list of all loaded mods that currently have documentation avalible.
     * <p>
     * Note: this list may be empty during startup. It is populated once PI finishes downloading and loading documentation.
     * @since PI 1.0.0
     */
    List<String> getSupportedMods();

    /**
     * @return the keybinding used to display the main project intelligence GUI.
     * @since PI 1.0.0
     */
    KeyBinding getPIGuiKey();

    /**
     * @return the "Explain This" key binding used to display documentation for the item under mouse in a gui container.
     * @since PI 1.0.0
     */
    KeyBinding getETGuiKey();

    /**
     * @return the "Explain This" key binding used to display documentation for the block or entity the player is looking at.
     * @since PI 1.0.0
     */
    KeyBinding getETWorldKey();
}
