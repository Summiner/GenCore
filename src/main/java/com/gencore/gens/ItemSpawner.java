package com.gencore.gens;

import java.util.ArrayList;
import java.util.HashMap;

import com.gencore.events.Events;
import com.gencore.handler.PluginHandler;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemSpawner implements Runnable {

    public void run() {
        Configuration config = PluginHandler.getPlugin().getConfig();
        start(config.getBoolean("tanks.enabled"), config.getLong("gentime"));
    }

    public void start(Boolean tanks, Long time) {
        if(tanks) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        HashMap<Material, ArrayList<Location>> a = Events.active_gens.get(p);
                        if (a == null) {
                            return;
                        }
                        a.forEach((key, value) -> {
                            int size = value.size();
                            Events.tanks.get(p.getUniqueId()).addItems(key, (long) size);
                        });
                    }
                }
            }.runTaskTimerAsynchronously(PluginHandler.getPlugin(), time, time);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        HashMap<Material, ArrayList<Location>> a = Events.active_gens.get(p);
                        if (a == null) {
                            return;
                        }
                        a.forEach((key, value) -> {
                            int size = value.size();
                            if(size > 0) {
                                Location c = value.get(size-1);
                                if(c.getChunk().isLoaded()) {
                                    Location loc = new Location(c.getWorld(), c.getX() + 0.5D, c.getY() + 1.0D, c.getZ() + 0.5D);
                                    ItemStack item = new ItemStack(PluginHandler.getPlugin().Generators.get(key).getDrop());
                                    NBTItem nbt = new NBTItem(item);
                                    var compound = nbt.getOrCreateCompound("GenCore");
                                    compound.setBoolean("gen_Item", true);
                                    compound.setString("gen_Type", key.toString());
                                    item.setAmount(size);
                                    Bukkit.getScheduler().runTask(PluginHandler.getPlugin(), () -> {
                                        Entity e = loc.getWorld().dropItem(loc, item);
                                        e.setVelocity(e.getVelocity().zero());
                                    });
                                }
                            }
                        });
                    }
                }
            }.runTaskTimerAsynchronously(PluginHandler.getPlugin(), time, time);
        }
    }
}
