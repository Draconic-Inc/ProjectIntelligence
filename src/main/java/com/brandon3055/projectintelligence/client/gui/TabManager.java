package com.brandon3055.projectintelligence.client.gui;

import com.brandon3055.projectintelligence.PIHelpers;
import com.brandon3055.projectintelligence.client.gui.guielements.GuiPartMDWindow;
import com.brandon3055.projectintelligence.client.gui.guielements.GuiPartPageList;
import com.brandon3055.projectintelligence.docdata.DocumentationManager;
import com.brandon3055.projectintelligence.docdata.DocumentationPage;
import com.brandon3055.projectintelligence.docdata.RootPage;

import javax.annotation.Nullable;
import java.util.LinkedList;

import static com.brandon3055.projectintelligence.docdata.DocumentationManager.getModPage;

/**
 * Created by brandon3055 on 10/11/2017.
 * This class is used to manage all active pages (tabs) and everything related to them.
 * Including saving and loading the active tabs from disc.
 * <p>
 * Everything in this class will be static and the page list and MD window will be driven by this class.
 * Page list will load its pages from this based on the active tab which will also be stored in this class.
 * <p>
 * This will also handle filtering in the page list.
 * <p>
 * TLDR this handles everything related to pages displayed in the page list and the MD window.
 */
public class TabManager {

    //There must always be ablest 1 open page and the active page must always be one of the open page.
    //This is enforced by the getter methods for open/active pages
    /**
     * This is a list of all open pages (tabs)
     */
    private static LinkedList<TabData> openTabs = new LinkedList<>();
    /**
     * This is the current active page (tab) if null will default to the default page next time the active page is requested.
     */
    private static TabData activeTab = null;

    public static LinkedList<TabData> getOpenTabs() {
        openTabs.removeIf(tabData -> !DocumentationManager.doesPageExist(tabData.pageURI));

        if (openTabs.isEmpty()) {
            openTabs.add(new TabData(RootPage.ROOT_URI));
        }

        return openTabs;
    }

    public static TabData getActiveTab() {
        if (activeTab == null || !getOpenTabs().contains(activeTab)) {
            activeTab = getOpenTabs().getFirst();
        }
        return activeTab;
    }

    public static void openPage(@Nullable String pageURI, boolean newTab) {
        if (pageURI == null) {
            pageURI = RootPage.ROOT_URI;
        }

        if (newTab) {
            activeTab = new TabData(pageURI);
            openTabs.add(activeTab);
        }
        else {
            activeTab.changePage(pageURI);
        }

        reloadGuiForTabChange();
    }

    /**
     * This is used to rearrange tabs when you drag a tab left or right.
     *
     * @param page the page who's tab is being dragged.
     * @param dir  the direction the page is being dragged -1 = left 1 = right
     */
    public static void dragTab(TabData page, int dir) {
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
    public static LinkedList<DocumentationPage> getSubPages() {
        LinkedList<DocumentationPage> list = new LinkedList<>();
        DocumentationPage page = getActiveTab().getDocPage();

        if (page instanceof RootPage/*page == null || (page.getSubPages().isEmpty() && page.getParent() == null)*/) {
            for (String modid : DocumentationManager.getModStructureMap().keySet()) {
                list.add(getModPage(modid));
            }
        }
        else {
            if (page.getSubPages().isEmpty()) {
                list.addAll(page.getParent().getSubPages());
            }
            else {
                list.addAll(page.getSubPages());
            }
        }

        return list;
    }

    public static void switchTab(TabData tab) {
        if (!getOpenTabs().contains(tab)) {
            PIHelpers.displayError("Attempted to open an un-tracked/invalid tab. This should not be possible! Try re opening the gui.");
            return;
        }
        activeTab = tab;
//        reloadGuiForTabChange();
        GuiPartPageList pageList = GuiProjectIntelligence.getListPart();
        if (pageList != null) {
            pageList.reloadElement();
        }
    }

    public static void closeTab(TabData tab) {
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
        reloadGuiForTabChange();
    }

    public static void reloadGuiForTabChange() {
        GuiPartMDWindow mdWindow = GuiProjectIntelligence.getMDPart();
        GuiPartPageList pageList = GuiProjectIntelligence.getListPart();
        if (mdWindow != null && pageList != null) {
            pageList.reloadElement();
            mdWindow.reloadElement();
        }
    }

    public static class TabData {
        public String pageURI;
        public double scrollPosition = 0;
        private LinkedList<String> pageHistory = new LinkedList<>();
        private LinkedList<String> forwardHistory = new LinkedList<>();

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
            GuiPartMDWindow mdWindow = GuiProjectIntelligence.getMDPart();
            if (mdWindow != null) {
                mdWindow.reloadElement();
            }
        }

        public void updateScroll(double scrollPosition) {
            this.scrollPosition = scrollPosition;
        }
    }
}
