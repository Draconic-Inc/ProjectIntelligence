package com.brandon3055.projectintelligence.client.gui.guielementsold;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiStackIcon;
import com.brandon3055.brandonscore.lib.StackReference;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 13/09/2016.
 */
public class StackSelector extends MGuiElementBase<StackSelector> {

    public IGuiEventListener listener;
    private List<GuiStackIcon> selection = new ArrayList<GuiStackIcon>();

    public StackSelector(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0xFF707070, 0xFF000000);
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    public void setListener(IGuiEventListener listener) {
        this.listener = listener;
    }

    public void setStacks(List<ItemStack> stacks) {
        toRemove.addAll(selection);
        int cols = (int) Math.floor(xSize() / 19D);
//        LogHelper.dev("Cols "+cols);
        int index = 0;
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                int x = index % cols;
//                LogHelper.dev(index+" Col "+x+" "+cols);
                int y = index / cols;

                GuiStackIcon stackIcon = new GuiStackIcon(xPos() + 2 + (x * 19), yPos() + 2 + (y * 19), 18, 18, new StackReference(stack));
                addChild(stackIcon);
                selection.add(stackIcon);
                index++;
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (MGuiElementBase element : childElements) {
            if (element.isMouseOver(mouseX, mouseY)) {
                if (selection.contains(element)) {
                    if (listener != null) {
                        listener.onMGuiEvent(new GuiEvent(this).setEventString("SELECTOR_PICK"), element);
                    }
                    return true;
                }
                else if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }
        return isMouseOver(mouseX, mouseY);
    }
}
