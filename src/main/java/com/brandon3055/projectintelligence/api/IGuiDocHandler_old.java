package com.brandon3055.projectintelligence.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 7/26/2018.
 */
public interface IGuiDocHandler_old<T extends GuiScreen> {

    /**
     * Use this to define the size and position of the PI button within this gui. By default this will create an invisible
     * clickable button that renders a tool tip when you hover your mouse over it (similar to a JEI clickable area for recipes)
     * If you would like to render a button you can ether do that in your gui or override {@link #renderButton(boolean)}
     * and draw your button there.
     *
     * @param gui the gui screen instance
     * @return a rectangle defining the PI button location.
     */
    Rectangle getButtonArea(T gui);

    /**
     * The main use case for this is to attach the PI button to the documentation pane. E.g. this allows you to place the button
     * off to the left of the UI as a sort of "expandable doc tab" When the documentation expends out to the left of the gui the
     * button will remain fixed to the left of the doc page.
     *
     * @return true to bind the PI button pos to the documentation animation.
     */
    default boolean bindButtonToDoc() {
        return false;
    }

    /**
     * Use this to define the area in which the PI UI will be rendered. This has a default implementation for GuiContainer's
     * that places the documentation to the left of the GUI but for GuiScreen's you must override thi method and define the
     * area yourself.
     *
     * @param gui the gui screen instance
     * @return a rectangle representing the bounds of the GUI Screen
     */
    default Rectangle getDocDisplayArea(T gui) {
        if (gui instanceof GuiContainer) {
            GuiContainer guiC = (GuiContainer) gui;
            return new Rectangle(0, guiC.getGuiTop(), guiC.getGuiLeft(), guiC.getYSize());
        }
        throw new UnsupportedOperationException("The default IGuiDocHandler#getDocDisplayArea implementation only supports GuiContainer's. Please override getDocDisplayArea and return the bounds for the pi documentation.");
    }

    /**
     * This method can be used to add a custom renderer for the PI button. By default the button is not rendered other than a tool
     * tip when you mouseover the button location.
     */
    default void renderButton(Minecraft mc, boolean mouseOver) {}

    /**
     * This can be used to completely disable the pi button allowing you to substitute your own that uses {@link #docDisplayOverride()}
     * to show/hide the documentation.
     *
     * @return false if the pi button should be disabled.
     */
    default boolean enableButton() {
        return true;
    }

    /**
     * This allows you to override the default handling for weather or not the documentation is visible.
     * If defined this completely overrides and disables the built in PI button.
     * This is best used in situations where you want to disable the built in PI button and add your owm.
     *
     * @return a boolean supplier that can be used to display or hide the documentation.
     */
    default Supplier<Boolean> docDisplayOverride() {
        return null;
    }

    /**
     * Use this to define what side your gui the documentation is attached to. e.g. if the doc area you defined is
     * connected to the left edge of your gui window then you would return LEFT. However if you want to attach the
     * doc to the left edge of the actual screen (as in the left of the computer screen the user is look9ing at)
     * then you would return RIGHT.
     *
     * This is used to control the open-close animation as well as the position of the PI button.
     *
     * @return The side of your gui the documentation is attached to.
     */
    default AttachSide getAnimDirection(T gui) {
        return AttachSide.LEFT;
    }

    enum AttachSide {
        // <=|
        LEFT((anim, xSize) -> xSize - (int)(anim * xSize), (anim, ySize) -> 0),
        // |=>
        RIGHT((anim, xSize) -> -xSize + (int)(anim * xSize), (anim, ySize) -> 0),
        UP((anim, xSize) -> 0, (anim, ySize) -> ySize - (int)(anim * ySize)),
        DOWN((anim, xSize) -> 0, (anim, ySize) -> -ySize + (int)(anim * ySize));
        private final BiFunction<Double, Integer, Integer> xOffset;
        private final BiFunction<Double, Integer, Integer> yOffset;

        AttachSide(BiFunction<Double, Integer, Integer> xOffset, BiFunction<Double, Integer, Integer> yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        public int xOffset(double animPos, int xSize){
            return xOffset.apply(animPos, xSize);
        }

        public int yOffset(double animPos, int ySize){
            return yOffset.apply(animPos, ySize);
        }
    }
}
