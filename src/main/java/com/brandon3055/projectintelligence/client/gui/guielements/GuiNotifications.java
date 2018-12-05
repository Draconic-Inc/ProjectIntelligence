package com.brandon3055.projectintelligence.client.gui.guielements;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTexture;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.projectintelligence.client.PITextures;
import com.brandon3055.projectintelligence.client.StyleHandler.PropertyGroup;
import com.brandon3055.projectintelligence.client.gui.PIPartRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by brandon3055 on 7/21/2018.
 */
public class GuiNotifications extends MGuiElementBase<GuiNotifications> {
    private static PropertyGroup windowProps = new PropertyGroup("user_dialogs");
    private static PropertyGroup buttonProps = new PropertyGroup("user_dialogs.button_style");
    private PIPartRenderer windowRenderer = new PIPartRenderer(windowProps);
    private static Queue<NotifyInfo> notifyQueue = new LinkedList<>();

    private IModularGui parent;
    private GuiActiveDownloads downloadsUI;
    private LinkedList<Notification> notifications = new LinkedList<>();
    private Notification lastAdded = null;
    private int tick = 0;
    private int yOffset = 0;

    public GuiNotifications(IModularGui parent, GuiActiveDownloads downloadsUI) {
        this.parent = parent;
        this.downloadsUI = downloadsUI;
    }

    public static void addNotification(String text, boolean autoClose) {
        notifyQueue.add(new NotifyInfo(text, autoClose));
    }

    public static void addNotification(String text, int displaySeconds) {
        notifyQueue.add(new NotifyInfo(text, displaySeconds * 20));
    }

    public static void addNotification(String text) {
        addNotification(text, true);
    }

    @Override
    public boolean onUpdate() {
        if (tick++ % 10 == 0) {
            DataUtils.forEachMatch(notifications, notification -> !notification.isEnabled(), this::removeChild);
            notifications.removeIf(notification -> !notification.isEnabled());
        }

        if (downloadsUI.isEnabled()) {
            int offset = screenHeight - downloadsUI.yPos();
            if (offset != yOffset) {
                notifications.forEach(notification -> notification.shiftUp(offset - yOffset));
                yOffset = offset;
            }
        }
        else if (yOffset > 0) {
            notifications.forEach(notification -> notification.shiftUp(-yOffset));
            yOffset = 0;
        }

        if (!notifyQueue.isEmpty() && (lastAdded == null || lastAdded.animComplete || !lastAdded.isEnabled())) {
            NotifyInfo info = notifyQueue.poll();
            if (info != null) { //WTF intellij this will never be null!
                Notification newNotify = new Notification(this, info, (int) (screenWidth * 0.6));
                addChild(newNotify);
                int y = screenHeight - newNotify.ySize() - 2 - yOffset;
                newNotify.setPos(screenWidth, y);
                newNotify.targetYPos = y;
                notifications.forEach(notification -> notification.shiftUp(newNotify.ySize() + 2));
                notifications.add(newNotify);
                lastAdded = newNotify;
            }
        }

        return super.onUpdate();
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        GlStateManager.color(1, 1, 1, 1);
    }

    private static class Notification extends MGuiElementBase<Notification> {
        private GuiNotifications parent;
        private final String text;
        private final boolean autoClose;
        private int maxWidth;
        private int targetYPos;
        private int age = 0;
        private int maxAge;
        private boolean animComplete = false;
        private boolean closing = false;

        private Notification(GuiNotifications parent, NotifyInfo info, int maxWidth) {
            this.parent = parent;
            this.text = info.text;
            this.autoClose = info.autoClose;
            this.maxWidth = maxWidth;
            this.maxAge = info.maxAge;
        }

        @Override
        public void addChildElements() {
            int textWidth = Math.min(maxWidth - 14, fontRenderer.getStringWidth(text));

            GuiLabel label = new GuiLabel(text).setTrim(false).setHeightForText(textWidth);
            label.setWrap(true).setShadow(false);
            label.setAlignment(GuiAlign.LEFT);
            setSize(label.xSize() + 14, label.ySize() + 6);
            label.setRelPos(this, 3, 3);
            label.setTextColour(windowProps.textColour());
            addChild(label);

            GuiButton close = new GuiButton().setSize(8, 8).setHoverText(I18n.format("pi.button.close"));
            GuiTexture closeTex = new GuiTexture(0, 0, 8, 8, PITextures.PI_PARTS);
            closeTex.setTexSizeOverride(16, 16);
            closeTex.setPreDrawCallback((minecraft, mouseX, mouseY, partialTicks, mouseOver) -> buttonProps.glColour(mouseOver));
            closeTex.setPostDrawCallback(IDrawCallback::resetColour);
            close.addChild(closeTex);
            close.setListener(() -> setEnabled(false));
            close.setPos(maxXPos() - 11, yPos() + 3);
            addChild(close);
            targetYPos = yPos();

            super.addChildElements();
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            parent.windowRenderer.render(this);
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }

        private void shiftUp(int amount) {
//            translate(0, -amount);
            targetYPos -= amount;
        }

        @Override
        public boolean onUpdate() {
            age++;
            if (age > maxAge && autoClose) {
                closing = true;
            }

            if (yPos() != targetYPos) {
                int newY = Math.round(MathHelper.approachLinear(yPos(), targetYPos, 5));
                setYPos(newY);
            }

            if (!animComplete || closing) {
                int target = closing ? screenWidth + 10 : screenWidth - xSize() - 5;
                int newX = (int) Math.round(MathHelper.approachLinear(xPos(), target, 30));
                if (newX <= target && !closing) {
                    newX = target;
                    animComplete = true;
                }
                setXPos(newX);
                if (closing && xPos() > screenWidth) {
                    setEnabled(false);
                }
            }

            return super.onUpdate();
        }
    }

    public static class NotifyInfo {
        private final String text;
        private final boolean autoClose;
        private int maxAge;

        public NotifyInfo(String text) {
            this(text, true, 30 * 20);
        }

        public NotifyInfo(String text, int maxAge) {
            this(text, true, maxAge);
        }

        public NotifyInfo(String text, boolean autoClose) {
            this(text, autoClose, 30 * 20);
        }


        public NotifyInfo(String text, boolean autoClose, int maxAge) {
            this.text = text;
            this.autoClose = autoClose;
            this.maxAge = maxAge;
        }
    }
}
