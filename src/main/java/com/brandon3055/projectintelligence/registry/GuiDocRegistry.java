package com.brandon3055.projectintelligence.registry;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.projectintelligence.api.IGuiDocHandler;
import com.brandon3055.projectintelligence.api.IGuiDocRegistry;
import com.brandon3055.projectintelligence.api.IPageSupplier;
import com.brandon3055.projectintelligence.utils.LogHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 7/26/2018.
 */
public class GuiDocRegistry implements IGuiDocRegistry {

    public static final GuiDocRegistry INSTANCE = new GuiDocRegistry();

    private IGuiDocHandler<GuiScreen> DEFAULT_HANDLER = new DefaultHandlerImpl();
    private Map<Class<? extends GuiScreen>, IGuiDocHandler> guiHandlerMap = new HashMap<>();
    private Map<Class<? extends GuiScreen>, List<IPageSupplier>> pageSupplierMap = new HashMap<>();

//    //todo remove
//    public static void clear() {
//        INSTANCE.guiHandlerMap.clear();
//        INSTANCE.pageSupplierMap.clear();
//    }

    //Public registry methods exposed via the API

    /**
     * This method can be used to determine if a gui handler has already been registered for the specified
     * class before adding your own handler.
     *
     * @param guiClass the gui class.
     * @return true if there is a gui handler assigned to this class.
     * @since PI 1.0.0
     */
    @Override
    public boolean hasGuiHandler(Class<? extends GuiScreen> guiClass) {
        return guiHandlerMap.containsKey(guiClass);
    }

    /**
     * This method can be used to set the GUI handler for the specified class. This will replace any existing handler
     * registered by you or another mod for that class.
     *
     * @param guiClass The class or superclass of the target GUI.
     * @param handler The handler for this gui class.
     * @since PI 1.0.0
     */
    @Override
    public <T extends GuiScreen> void registerGuiHandler(Class<T> guiClass, IGuiDocHandler<T> handler) {
        if (guiClass == GuiContainer.class || guiClass == GuiScreen.class) {
            throw new UnsupportedOperationException("You can not assign a gui handler to any of the base gui classes!");
        }

        if (guiHandlerMap.containsKey(guiClass)) {
            LogHelper.warn("Overwriting existing gui handler for " + guiClass.getName() + ". Old: " + guiHandlerMap.get(guiClass).getClass().getName() + " -> Replaced by: " + handler.getClass().getName());
        }

        guiHandlerMap.put(guiClass, handler);
    }

    /**
     * This method allows you to assign a documentation page to a specific gui class. It should be noted
     * that this will not apply to subclasses of the target class. For that you need to register an {@link IPageSupplier}
     *
     * @param guiClass The target gui class
     * @param pageURI the page uri to assign to the target class.
     * @since PI 1.0.0
     */
    @Override
    public <T extends GuiScreen> void registerGuiDocPages(Class<T> guiClass, String... pageURI) {
        registerGuiDocPages(guiClass, Arrays.asList(pageURI));
    }

    /**
     * This method allows you to assign documentation pages to a specific gui class. It should be noted
     * that this will not apply to subclasses of the target class. For that you need to register an {@link IPageSupplier}
     *
     * @param guiClass The target gui class
     * @param pageURIs the page uri's to assign to the target class.
     * @since PI 1.0.0
     */
    @Override
    public <T extends GuiScreen> void registerGuiDocPages(Class<T> guiClass, Collection<String> pageURIs) {
        registerGuiDocPages(guiClass, new StrictPageSupplier<>(guiClass, pageURIs));
    }

    /**
     * This method can be used to assign documentation pages go a gui class using an {@link IPageSupplier}.
     * The IPageSupplier allows you to supply pages on a per instance basis.<br><br>
     *
     * Note: you can also assign an IPageSupplier to a superclass of your actual target class(es) and use the
     * page supplier to supply the correct page(s) for each gui instance.
     *
     * @param guiClass The class or superclass of the target gui.
     * @param pageSupplier A page supplier for the GUI.
     * @since PI 1.0.0
     */
    @Override
    public <T extends GuiScreen> void registerGuiDocPages(Class<T> guiClass, IPageSupplier<T> pageSupplier) {
        pageSupplierMap.computeIfAbsent(guiClass, c -> new ArrayList<>()).add(pageSupplier);
    }



    //Internal methods

    public boolean doesGuiHaveDoc(GuiScreen screen) {
        return !getPagesFor(screen).isEmpty();
    }

    public GuiDocHelper getDocHelper(GuiScreen screen) {
        IGuiDocHandler handler = getBestHandlerFor(screen);
        List<String> pages = getPagesFor(screen);
        return new GuiDocHelper(screen, pages, handler);
    }

    private IGuiDocHandler getBestHandlerFor(GuiScreen screen) {
        Class clazz = screen.getClass();

        while (clazz != null) {
            IGuiDocHandler handler = guiHandlerMap.get(clazz);
            if (handler != null) {
                return handler;
            }
            clazz = clazz.getSuperclass();
        }

        if (!(screen instanceof GuiContainer)) {
            LogHelper.warn("Using default IGuiDocHandler for GuiScreen: " + screen.getClass().getName() + ". Default handler works best with GuiContainer's please consider adding a custom IGuiDocHandler for your screen.");
        }
        return DEFAULT_HANDLER;
    }

    @SuppressWarnings("unchecked")
    private List<String> getPagesFor(GuiScreen screen) {
        List<String> pages = new ArrayList<>();
        Class clazz = screen.getClass();

        while (clazz != null) {
            List<IPageSupplier> list = pageSupplierMap.get(clazz);
            if (list != null) {
                for (IPageSupplier supplier : list) {
                    if (supplier.hasPages(screen)) {
                        DataUtils.addIf(supplier.getPageURIs(screen), pages, s -> !pages.contains(s));
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        return pages;
    }

    @SuppressWarnings("unchecked")
    public static class GuiDocHelper {
        public final IGuiDocHandler docHandler;
        private GuiScreen gui;
        private List<String> pages;
        private static Map<Class<? extends GuiScreen>, Boolean> docVisible = new HashMap<>();
        private static Map<Class<? extends GuiScreen>, Integer> selectedPage = new HashMap<>();

        public GuiDocHelper(GuiScreen gui, List<String> pages, IGuiDocHandler docHandler) {
            this.gui = gui;
            this.pages = pages;
            this.docHandler = docHandler;
        }

        public List<String> getPages() {
            return pages;
        }

        public void setSelected(int selected) {
            selectedPage.put(gui.getClass(), MathHelper.clip(selected, 0, pages.size() - 1));
        }

        public void setSelected(String page) {
            if (pages.contains(page)) {
                selectedPage.put(gui.getClass(), pages.indexOf(page));
            }
        }

        public int getSelectedIndex() {
            return selectedPage.computeIfAbsent(gui.getClass(), c -> 0);
        }

        public String getSelected() {
            int selected = selectedPage.computeIfAbsent(gui.getClass(), c -> 0);
            selected = MathHelper.clip(selected, 0, pages.size() - 1);
            return pages.get(selected);
        }

        @SuppressWarnings("unchecked")
        public void setDocVisible(boolean visible) {
            if (docHandler.docDisplayOverride(gui) == null) {
                docVisible.put(gui.getClass(), visible);
            }
        }

        @SuppressWarnings("unchecked")
        public boolean isDocVisible() {
            Supplier<Boolean> enableSupplier = docHandler.docDisplayOverride(gui);
            return enableSupplier != null ? enableSupplier.get() : docVisible.computeIfAbsent(gui.getClass(), c -> false);
        }

        //Wrapper pass through

        public Rectangle getCollapsedArea() {
            return docHandler.getCollapsedArea(gui);
        }

        public Rectangle getExpandedArea() {
            return docHandler.getExpandedArea(gui);
        }

        public boolean enableAnimation() {
            return docHandler.enableAnimation(gui);
        }

        public boolean enableButton() {
            return docHandler.enableButton(gui);
        }
    }

    private static class StrictPageSupplier<T extends GuiScreen> implements IPageSupplier<T> {
        private Class<T> guiClass;
        private Collection<String> pageURIs;

        private StrictPageSupplier(Class<T> guiClass, Collection<String> pageURIs) {
            this.guiClass = guiClass;
            this.pageURIs = pageURIs;
        }

        @Override
        public Collection<String> getPageURIs(T gui) {
            return pageURIs;
        }

        @Override
        public boolean hasPages(T gui) {
            return gui.getClass() == guiClass;
        }
    }
}
