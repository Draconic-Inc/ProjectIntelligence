package com.brandon3055.projectintelligence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.util.*;

/**
 * Created by brandon3055 on 29/9/2015.
 */
public class ModHelper {

    private static boolean initialized = false;
    private static List<String> loadedMods = null;
    private static Map<String, String> modNameMap = null;
    private static Map<String, String> modVersionMap = null;

    private static void initialize() {
        if (initialized) return;

        loadedMods = Collections.synchronizedList(new ArrayList<>());
        modNameMap = Collections.synchronizedMap(new HashMap<String, String>());
        modVersionMap = Collections.synchronizedMap(new HashMap<String, String>());

        for (ModContainer mod : Loader.instance().getModList()) {
            loadedMods.add(mod.getModId());
            modNameMap.put(mod.getModId(), mod.getName());
            modVersionMap.put(mod.getModId(), mod.getVersion());
        }

        initialized = true;
    }

    /**
     * @return a list of all loaded mod id's
     */
    public static List<String> getLoadedMods() {
        initialize();
        return ImmutableList.copyOf(loadedMods);
    }

    /**
     * @return a map of all loaded mod id's to mod names
     */
    public static Map<String, String> getModNameMap() {
        initialize();
        return ImmutableMap.copyOf(modNameMap);
    }

    /**
     * @return a map of all loaded mod id's to mod versions
     */
    public static Map<String, String> getModVersionMap() {
        initialize();
        return ImmutableMap.copyOf(modVersionMap);
    }

    /**
     * @param modid The mod id of the target mod.
     * @return Returns the human readable name for the specified mod id or nul;l if the mod is not installed.
     */
    public static String getModName(String modid) {
        return getModNameMap().get(modid);
    }

    /**
     * @param modid The mod id of the target mod.
     * @return Returns the version for the specified mod id or null if the mod is not installed.
     */
    public static String getModVersion(String modid) {
        return getModVersionMap().get(modid);
    }
}
