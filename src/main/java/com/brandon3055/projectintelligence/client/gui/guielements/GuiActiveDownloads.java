package com.brandon3055.projectintelligence.client.gui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.lib.FileDownloadManager;
import com.brandon3055.brandonscore.lib.Pair;
import com.brandon3055.brandonscore.utils.MathUtils;
import com.brandon3055.projectintelligence.client.StyleHandler;
import com.brandon3055.projectintelligence.docmanagement.PIUpdateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.brandon3055.projectintelligence.docmanagement.PIUpdateManager.UpdateStage.INACTIVE;
import static com.brandon3055.projectintelligence.docmanagement.PIUpdateManager.UpdateStage.RELOAD_DOCUMENTATION;

/**
 * Created by brandon3055 on 7/21/2018.
 */
public class GuiActiveDownloads extends GuiElement<GuiActiveDownloads> {

    private int completionTimeOut = 0;
    private IModularGui parent;
    private Map<File, DLInfo> downloads = new HashMap<>();
    private double overallProgress = 0;
    private int textColour = 0;

    public GuiActiveDownloads(IModularGui parent) {
        this.parent = parent;
        this.setEnabled(false);
    }

    @Override
    public void addChildElements() {
        super.addChildElements();
        addChild(new StyledGuiRect("user_dialogs").bindSize(this, false).setXPosMod((s, i) -> xPos()).setYPosMod((s, i) -> yPos()));
        this.setXSize(200);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        this.setXSize(200);
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);

        drawCenteredString(fontRenderer, I18n.get("pi.update_manager.title"), xPos() + (xSize() / 2F), yPos() + 2, textColour, false);

        String status = I18n.get(PIUpdateManager.updateStage.getUnlocalizedName());
        if (PIUpdateManager.updateStage != INACTIVE && PIUpdateManager.updateStage != RELOAD_DOCUMENTATION) {
            status += " " + MathUtils.round(overallProgress * 100D, 100) + "%";
        }
        drawCenteredString(fontRenderer, status, xPos() + (xSize() / 2F), yPos() + 12, textColour, false);

        if (!downloads.isEmpty()){
            drawColouredRect(xPos() + 3, yPos() + 22, xSize() - 6, ySize() - 25, 0xFF505050);

            int y = yPos() + 23;
            for (DLInfo info : downloads.values()) {
                int colour = info.failed ? 0xAA0000 : info.progress == 1 ? 0x55FF55 : 0xFFFF55;

                String progress = MathUtils.round(info.progress * 100, 100) + "%";
                int pw = (int) fontRenderer.getSplitter().stringWidth(progress) + 3;
                drawString(fontRenderer, progress, maxXPos() - pw, y, colour);

                File file = PIUpdateManager.tempFileToFileMap.getOrDefault(info.file, new Pair<>("", info.file)).value();
                String fileName = file.getName();
                int fileWidth = xSize() - pw - 6;
                if (fontRenderer.getSplitter().stringWidth(fileName) > fileWidth){
                    fileName = fontRenderer.plainSubstrByWidth(fileName, fileWidth - 4) + "..";
                }
                drawString(fontRenderer, fileName, xPos() + 4, y, colour);

                y += 9;
            }
        }
        RenderSystem.color4f(1, 1, 1, 1);
    }

    @Override
    public boolean onUpdate() {
        FileDownloadManager manager = PIUpdateManager.downloadManager;
        if (PIUpdateManager.updateStage == INACTIVE && downloads.isEmpty()) {
            if (completionTimeOut < 50) {
                completionTimeOut++;
            }
            else {
                setEnabled(false);
            }
        }
        else {
            completionTimeOut = 0;
            setEnabled(true);
            overallProgress = manager.getDownloadProgressTotal();

            Map<File, Double> progressMap = manager.getActiveProgress();

            for (Map.Entry<File, Double> entry : progressMap.entrySet()) {
                if (!downloads.containsKey(entry.getKey())) {
                    downloads.put(entry.getKey(), new DLInfo(entry.getValue(), entry.getKey()));
                }
                else {
                    downloads.get(entry.getKey()).progress = entry.getValue();
                }
            }

            downloads.values().forEach(dlInfo -> {
                if (manager.failedFiles.containsValue(dlInfo.file)) {
                    dlInfo.failed = true;
                }
                if (!progressMap.containsKey(dlInfo.file)) {
                    dlInfo.finishedTime++;
                    dlInfo.progress = 1;
                }
            });

            if (downloads.size() > 20) {
                DLInfo oldest = null;
                for (DLInfo info : downloads.values()) {
                    if (oldest == null || (info.finishedTime > oldest.finishedTime && !info.failed)) {
                        oldest = info;
                    }
                }
                if (oldest != null) {
                    downloads.remove(oldest.file);
                }
            }

            downloads.entrySet().removeIf(entry -> entry.getValue().finishedTime > (entry.getValue().failed ? 200 : 10));

            int newY = (downloads.size() * 9) + (downloads.isEmpty() ? 22 : 25);
            setYSize(Math.min(PIUpdateManager.updateStage == INACTIVE || PIUpdateManager.updateStage == RELOAD_DOCUMENTATION ? newY : Math.max(ySize(), newY), parent.ySize() - 50));
            setPos((parent.guiLeft() + parent.xSize()) - xSize() - 2, (parent.guiTop() + parent.ySize()) - ySize() - 2);
            textColour = StyleHandler.getInt("user_dialogs.text_colour");
        }

        return super.onUpdate();
    }

    private static class DLInfo {
        public double progress;
        public File file;
        public int finishedTime = 0;
        public boolean failed = false;

        public DLInfo(double progress, File file) {
            this.progress = progress;
            this.file = file;
        }

        @Override
        public String toString() {
            return progress + "";
        }
    }
}
