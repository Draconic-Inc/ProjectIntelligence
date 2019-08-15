package com.brandon3055.projectintelligence.client;

import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.client.gui.PIGuiContainer;
import com.brandon3055.projectintelligence.docmanagement.DocumentationManager;
import com.brandon3055.projectintelligence.docmanagement.DocumentationPage;
import com.brandon3055.projectintelligence.docmanagement.PIUpdateManager;
import com.brandon3055.projectintelligence.docmanagement.RootPage;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.WeakHashMap;

/**
 * Created by brandon3055 on 10/11/2017.
 * <p>
 * The display controller is is the underlying object that controls what is displayed in the page list as well as in the MD window including managing open tabs.
 * All control of the PI GUI can be controlled through this controller.
 * <p>
 * There is a primary static controller instance used to control the main PI UI. For non persistent UI's such as the ones i am
 * thinking of attaching mod ui's via the API controllers can be created as needed.
 * <p>
 * For use cases where a single specific page or group of pages are to be shown the display controller can be used in a restricted mode.
 * This will restrict the controller to a specific group of pages. Following links to other pages will just open that page in the main PI UI.
 */
public class DisplayController {

    public static DisplayController MASTER_CONTROLLER = new DisplayController();

    /**
     * This is a list of all open pages (tabs)
     */
    private LinkedList<TabData> openTabs = new LinkedList<>();
    /**
     * This is the current active page (tab) if null will default to the default page next time the active page is requested.
     */
    private TabData activeTab = null;
    private WeakHashMap<PIGuiContainer, Runnable> pageChangeListeners = new WeakHashMap<>();

    public LinkedList<TabData> getOpenTabs() {
        openTabs.removeIf(tabData -> !DocumentationManager.doesPageExist(tabData.pageURI));

        if (openTabs.isEmpty()) {
            openTabs.add(new TabData(RootPage.ROOT_URI));
        }

        return openTabs;
    }

    public TabData getActiveTab() {
        if (activeTab == null || !getOpenTabs().contains(activeTab)) {
            activeTab = getOpenTabs().getFirst();
        }
        return activeTab;
    }

    public boolean openPage(@Nullable String pageURI, boolean newTab) {
        if (pageURI == null) {
            pageURI = RootPage.ROOT_URI;
        }

        //Ensures active tab is not null.
        getActiveTab();

        if (newTab) {
            if (openTabs.size() < PIConfig.maxTabs) {
                activeTab = new TabData(pageURI);
                openTabs.add(activeTab);
            }
            else {
                return false;
            }
        }
        else {
            activeTab.changePage(pageURI);
        }

        onActivePageChange();
        return true;
    }

    /**
     * This is used to rearrange tabs when you drag a tab left or right.
     *
     * @param page the page who's tab is being dragged.
     * @param dir  the direction the page is being dragged -1 = left 1 = right
     */
    public void dragTab(TabData page, int dir) {
        if (!openTabs.contains(page)) {
            return;
        }

        int currentIndex = openTabs.indexOf(page);
        int newIndex = currentIndex + dir;

        if (newIndex >= openTabs.size() || newIndex < 0) {
            return;
        }

        TabData pageToReplace = openTabs.get(newIndex);
        openTabs.set(newIndex, page);
        openTabs.set(currentIndex, pageToReplace);
    }

    /**
     * @return a list of sub pages for the current active page.
     */
    public LinkedList<DocumentationPage> getSubPages() {
        LinkedList<DocumentationPage> list = new LinkedList<>();
        DocumentationPage page = getActiveTab().getDocPage();

        if (page.getSubPages().isEmpty()) {
            if (page.getParent() != null) {
                list.addAll(page.getParent().getSubPages());
            }
            else {
                if (PIConfig.downloadsAllowed && !PIUpdateManager.downloadManager.running){
                    PIGuiHelper.displayError("No documentation pages were found! This most likely means the documentation failed to download for some reason.", true);
                }
            }
        }
        else {
            list.addAll(page.getSubPages());
        }

        return list;
    }

    public String getButtonController() {
        DocumentationPage page = getActiveTab().getDocPage();
        if (page instanceof RootPage) {
            return page.getPageURI();
        }
        else {
            if (page.getSubPages().isEmpty()) {
                return page.getParent().getPageURI();
            }
            else {
                return page.getPageURI();
            }
        }
    }

    public void switchTab(TabData tab) {
        if (!getOpenTabs().contains(tab)) {
            PIGuiHelper.displayError("Attempted to open an un-tracked/invalid tab. This should not be possible! Try re opening the gui.");
            return;
        }
        activeTab = tab;
        onActivePageChange();
    }

    public void closeTab(TabData tab) {
        if (getOpenTabs().size() == 1 || !openTabs.contains(tab)) {
            return;
        }

        if (activeTab == tab) {
            int index = openTabs.indexOf(tab);

            if (index > 0) {
                index--;
            }
            else {
                index++;
            }
            switchTab(openTabs.get(index));
        }

        openTabs.remove(tab);
        onActivePageChange();
    }

    public void onActivePageChange() {
        pageChangeListeners.values().forEach(Runnable::run);
    }

    public void clear() {
        openTabs.clear();
    }

    public void goBack() {
        getActiveTab().back();
        onActivePageChange();
    }

    public void goForward() {
        getActiveTab().forward();
        onActivePageChange();
    }

    public void addChangeListener(PIGuiContainer listenerObject, Runnable changeCallback) {
        pageChangeListeners.put(listenerObject, changeCallback);
    }

    public void removeChangeListener(PIGuiContainer piGuiContainer) {
        pageChangeListeners.remove(piGuiContainer);
    }

    public static class TabData {
        public String pageURI;
        public double scrollPosition = 0;
        private LinkedList<String> pageHistory = new LinkedList<>();
        private LinkedList<String> forwardHistory = new LinkedList<>();
        public boolean requiresEditReload = false;

        public TabData(String pageURI) {
            this.pageURI = pageURI;
        }

        @Override
        public String toString() {
            return "Tab: " + pageURI;
        }

        public void changePage(String newPageURI) {
            pageHistory.add(pageURI);
            pageURI = newPageURI;

            if (pageHistory.size() > 64) {
                pageHistory.removeFirst();
            }

            forwardHistory.clear();
            scrollPosition = 0;
        }

        /**
         * Return to the previously opened page.
         * Similar to the back button in a web browser.
         */
        public void back() {
            if (!pageHistory.isEmpty()) {
                forwardHistory.add(pageURI);
                pageURI = pageHistory.removeLast();
                scrollPosition = 0;
            }
        }

        /**
         * After pressing the back button to go to a previous page this can be ued to go forward again if a new page was not opened after going back.
         * Similar to the forward button in a web browser.
         */
        public void forward() {
            if (!forwardHistory.isEmpty()) {
                pageHistory.add(pageURI);
                pageURI = forwardHistory.removeLast();
                scrollPosition = 0;
            }
        }

        public boolean canGoBack() {
            return !pageHistory.isEmpty();
        }

        public boolean canGoForward() {
            return !forwardHistory.isEmpty();
        }

        public DocumentationPage getDocPage() {
            return DocumentationManager.getPage(pageURI);
        }

        public void reloadTab() {
            requiresEditReload = true;
        }

        public void updateScroll(double scrollPosition) {
            this.scrollPosition = scrollPosition;
        }
    }
}
