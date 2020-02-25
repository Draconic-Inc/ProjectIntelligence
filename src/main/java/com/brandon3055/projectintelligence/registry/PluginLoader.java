package com.brandon3055.projectintelligence.registry;

import com.brandon3055.projectintelligence.api.IModPlugin;
import com.brandon3055.projectintelligence.api.ModPlugin;
import com.brandon3055.projectintelligence.utils.LogHelper;
import jdk.internal.org.objectweb.asm.Type;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by brandon3055 on 22/09/18.
 */
public class PluginLoader {
    private static List<IModPlugin> plugins = new ArrayList<>();

    public static void preInit(List<ModFileScanData> asmDataTable) {
        plugins.addAll(generateClassInstances(ModPlugin.class, IModPlugin.class));
    }

    public static void loadComplete() {
        for (IModPlugin plugin : plugins) {
            LogHelper.dev("Loading mod plugin: " + plugin);
            plugin.registerModGUIs(GuiDocRegistry.INSTANCE);
        }
    }


    @SuppressWarnings("SameParameterValue")
    private static <T> List<T> generateClassInstances(Class annotationClass, Class<T> baseClass) {
        Type type = Type.getType(annotationClass);
        List<String> annotatedClassNames = new ArrayList<>();

        for (ModFileScanData scanData : ModList.get().getAllScanData()) {
            Iterable<ModFileScanData.AnnotationData> annotations = scanData.getAnnotations();
            for (ModFileScanData.AnnotationData data : annotations) {
                if (Objects.equals(data.getAnnotationType(), type)) {
                    String memberName = data.getMemberName();
                    annotatedClassNames.add(memberName);
                }
            }
        }

        List<T> instances = new ArrayList<>();
        for (String name : annotatedClassNames) {
            try {
                Class<?> asmClass = Class.forName(name);
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(baseClass);
                T instance = asmInstanceClass.newInstance();
                instances.add(instance);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | LinkageError e) {
                LogHelper.error("An error occurred while loading class: {}", name, e);
            }
        }
        return instances;
    }
}