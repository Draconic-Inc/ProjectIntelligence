package com.brandon3055.projectintelligence;

import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.projectintelligence.client.gui.swing.PIEditor;
import com.brandon3055.projectintelligence.docdata.DocumentationManager;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;
import org.lwjgl.opengl.Display;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 4/08/2017.
 */
public class PIHelpers {

    public static PIEditor editor = null;
    public static LinkedList<String> errorCache = new LinkedList<>();
    private static LinkedList<String> entitySelectionList = new LinkedList<>();
    private static LinkedList<String> playerInventory = new LinkedList<>();

    /**
     * Displays an error message in the PI gui or id the gui is not currently open schedules an error to be displayed the
     * next time the gui is opened.
     * <p>
     * Supports multiple calls. Errors will be added to a list to be displayed.
     */
    public static void displayError(String error) {
        LogHelperBC.error("[Pi Reported Error]: " + error);
        errorCache.add(error);
    }

    /**
     * By default this will only return true if the specified mod is installed however the user has an option do override this
     * functionality and force mods to be displayed even if they are not installed.
     *
     * @return true if documentation for the specifies mod should be downloaded and displayed.
     */
    public static boolean displayModDoc(String modid) {
        if (Loader.isModLoaded(modid)) {
            return true;
        }

        if (DocumentationManager.hasModPage(modid)) {
            for (String mod : DocumentationManager.getModPage(modid).getModAliases()) {
                if (Loader.isModLoaded(mod)) {
                    return true;
                }
            }
        }

        return false;//TODO or mod is forced to display regardless by user config
    }

    public static boolean doesModVersionMatch(String modid, String targetVersion) {
        //If the mod is not loaded we return true because it means the default behavior of not showing a mod that is not installed has been overridden.
        if (targetVersion.isEmpty() || !Loader.isModLoaded(modid)) return true;

        VersionRange target = VersionParser.parseRange(targetVersion);
        ModContainer mod = Loader.instance().getIndexedModList().get(modid);
        DefaultArtifactVersion version = new DefaultArtifactVersion(modid, mod.getVersion());

        return target.containsVersion(version);
    }

    /**
     * @return a list of all mods listed in the doc manifest or on disk if the manifest failed to downland.
     */
    public static synchronized List<String> getSupportedMods() {
        return ImmutableList.copyOf(DocumentationManager.getDocumentedMods());
    }


    //region Editor Helpers
    public static void displayEditor() {
        try {
            UIManager.setLookAndFeel("com.bulenkov.darcula.DarculaLaf");
        }
        catch (Throwable ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (Throwable ignored2) {}
        }


//        if (editor == null) {
            editor = new PIEditor();
//        }

        editor.setVisible(true);
        editor.reload();
        centerWindowOnMC(editor);
    }

    public static void centerWindowOnMC(Component window) {
        int centerX = Display.getX() + (Display.getWidth() / 2);
        int centerY = Display.getY() + (Display.getHeight() / 2);
        window.setLocation(centerX - (window.getWidth() / 2), Math.max(0, centerY - (window.getHeight() / 2)));
    }

    public static void centerWindowScreen(Component window) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = ge.getCenterPoint();

        int centerX = (int) centerPoint.getX();
        int centerY = (int) centerPoint.getY();
        window.setLocation(centerX - (window.getWidth() / 2), Math.max(0, centerY - (window.getHeight() / 2)));
    }

    public static synchronized LinkedList<String> getEntitySelectionList() {
        if (entitySelectionList.isEmpty()) {
            for (EntityList.EntityEggInfo info : EntityList.ENTITY_EGGS.values()) {
                entitySelectionList.add(info.spawnedID.toString());
            }
            Collections.sort(entitySelectionList);
        }

        return entitySelectionList;
    }

    public static synchronized LinkedList<String> getPlayerInventory() {
        return playerInventory;
    }

    public static void updatePlayerInventory(EntityPlayer player) {
        playerInventory.clear();
        player.inventory.mainInventory.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> playerInventory.add(new StackReference(stack).toString()));
        player.inventory.armorInventory.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> playerInventory.add(new StackReference(stack).toString()));
        player.inventory.offHandInventory.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> playerInventory.add(new StackReference(stack).toString()));
    }

    //endregion
}
