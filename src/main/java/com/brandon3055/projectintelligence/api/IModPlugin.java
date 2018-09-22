package com.brandon3055.projectintelligence.api;

/**
 * Created by brandon3055 on 22/09/18.
 *
 * This plugin allows you to access PI's registries. This has the advantage of not requiring the PI API in the class path
 * at runtime because PI will call your plugin as opposed to your mod needing to call PI.
 */
public interface IModPlugin {

    /**
     * Use this method to register Gui documentation.
     * What this does is allows you to bind a documentation page(s) to a mod gui. For example if your mod adds
     * a say a custom furnace, this allows you to display the PI documentation page for that furnace in in the furnace
     * GUI as a sort of "Help/Info tab"
     *
     * For an example implementation take a look at Draconic Evolution's PIPlugin.
     *
     * @param registry the IGuiDocRegistry instance.
     * @since PI 1.0.1
     */
    default void registerModGUIs(IGuiDocRegistry registry) {}
}
