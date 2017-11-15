package com.brandon3055.projectintelligence.integration;

import mezz.jei.api.*;

/**
 * Created by brandon3055 on 24/07/2016.
 */
@JEIPlugin
public class PIJEIPlugin extends BlankModPlugin {

    public static IJeiHelpers jeiHelpers = null;
    public static IJeiRuntime jeiRuntime = null;

    public PIJEIPlugin() {
    }

    @Override
    public void register(IModRegistry registry) {
        jeiHelpers = registry.getJeiHelpers();
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime iJeiRuntime) {
        jeiRuntime = iJeiRuntime;
    }
}
