package com.brandon3055.projectintelligence.registry;

import com.brandon3055.projectintelligence.api.IModPlugin;
import com.brandon3055.projectintelligence.api.ModPlugin;
import com.brandon3055.projectintelligence.utils.LogHelper;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by brandon3055 on 22/09/18.
 */
public class PluginLoader {
    private static List<IModPlugin> plugins = new ArrayList<>();

    public static void preInit(ASMDataTable asmDataTable) {
        Set<ASMDataTable.ASMData> asmDataSet = asmDataTable.getAll(ModPlugin.class.getCanonicalName());
        for (ASMDataTable.ASMData asmData : asmDataSet) {
            try {
                Class<?> asmClass = Class.forName(asmData.getClassName());
                Class<? extends IModPlugin> asmInstanceClass = asmClass.asSubclass(IModPlugin.class);
                IModPlugin instance = asmInstanceClass.newInstance();
                plugins.add(instance);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | LinkageError e) {
                LogHelper.error("Failed to load: {}", asmData.getClassName(), e);
            }
        }
    }

    public static void loadComplete() {
        for (IModPlugin plugin : plugins) {
            LogHelper.dev("Loading mod plugin: " + plugin);
            plugin.registerModGUIs(GuiDocRegistry.INSTANCE);
        }
    }
}