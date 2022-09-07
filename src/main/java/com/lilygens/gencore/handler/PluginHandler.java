package com.lilygens.gencore.handler;

import com.lilygens.gencore.GenCore;
import lombok.Getter;

public class PluginHandler {
    public static GenCore getPlugin() {
        return GenCore.getPlugin(GenCore.class);
    }
}
