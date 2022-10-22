package com.gencore.util;

import com.gencore.events.Events;
import com.gencore.handler.PluginHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    public Configuration config = PluginHandler.getPlugin().getConfig();

    private final String prefix = config.getString("placeholders.prefix");
    private final String option1 = config.getString("placeholders.maxgens");
    private final String option2 = config.getString("placeholders.placedgens");

    @Override
    public @NotNull String getAuthor() {
        return "Summiner, Symmettry (Optimizer)";
    }

    @Override
    public @NotNull String getIdentifier() {
        assert prefix != null;
        return prefix;
    }

    @Override
    public @NotNull String getVersion() {
        return "1.6.2-R1";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if(!player.isOnline()) {
            if(params.equalsIgnoreCase(option1) || params.equalsIgnoreCase(option2)) return "0";
            return null;
        }
        if(params.equalsIgnoreCase(option1)) return String.valueOf(Events.slots_gens.get(player));
        else if(params.equalsIgnoreCase(option2)) return String.valueOf(Events.placed_gens.get(player));
        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

}