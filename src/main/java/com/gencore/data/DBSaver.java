package com.gencore.data;

import com.gencore.handler.PluginHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DBSaver implements Runnable {

    public void run() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&aSaving data!")));
                    DataBase.savePlayerData(p, false);
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&aData saved!")));
                }
            }
        }.runTaskTimerAsynchronously(PluginHandler.getPlugin(), 6000L, 6000L);
    }

}
