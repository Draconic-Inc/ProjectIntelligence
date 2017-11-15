package com.brandon3055.projectintelligence.client.gui.moddata.displaycontent;

import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.moddata.guidoctree.TreeBranchRoot;

/**
 * Created by brandon3055 on 8/09/2016.
 */
public interface IDisplayComponentFactory {

    DisplayComponentBase createNewInstance(GuiProjectIntelligence guiWiki, TreeBranchRoot branch, int screenWidth, int screenHeight);

    String getID();
}

