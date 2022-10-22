package com.gencore.handler;

import com.gencore.GenCore;

public class PluginHandler {
    public static GenCore getPlugin() {
        return GenCore.getPlugin(GenCore.class);
    }
}
