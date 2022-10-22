package com.gencore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.gencore.data.DBSaver;
import com.gencore.data.DataBase;
import com.gencore.events.Events;
import com.gencore.gens.Generator;
import com.gencore.gens.ItemSpawner;
import com.gencore.gui.GenCoreGUI;
import com.gencore.util.ItemUtil;
import com.gencore.util.Placeholders;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GenCore extends JavaPlugin {
    public HashMap<Material, Generator> Generators = new HashMap<>();
    public ArrayList<Generator> ListedGenerators = new ArrayList<>();
    public ArrayList<Material> GenDrops = new ArrayList<>();
    public Economy economy = null;

    Runnable save = () -> {
        for (Player p : Bukkit.getOnlinePlayers()) {
            DataBase.savePlayerData(p, false);
        }
    };

    public void sendCommand(@Nullable Player player, String command) {
        if(player != null) Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replaceAll("\\{player}", player.getName()));
        else Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                economy = rsp.getProvider();
                return true;
            }
        }
    }

    public boolean checkRemoveMoney(Player player, Double amount) {
        double balance = economy.getBalance(player);
        if (balance >= amount) {
            economy.withdrawPlayer(player, amount);
            return true;
        } else {
            return false;
        }
    }

    public void addMoney(Player player, Double amount) {
        economy.depositPlayer(player, amount);
    }

    private void startLoop() {
        new Thread(new ItemSpawner()).start();
        new Thread(new DBSaver()).start();
    }

    public void addGenerator(String block, String drop, String next, Long worth, Long upgrade, String name, String lore) {
        Material b = Material.getMaterial(block.toUpperCase());
        Material d = Material.getMaterial(drop.toUpperCase());
        Material n = Material.getMaterial(next.toUpperCase());
        List<String> l = Arrays.asList(lore.replace("||", "@@").split("@@"));
        if(next.equals("MAX")) {
            n = null;
        }
        Generator gen = new Generator(b, name, d, worth, upgrade, l, n);
        Generators.putIfAbsent(b, gen);
        GenDrops.add(gen.getDrop());
        ListedGenerators.add(gen);
    }

    public void clearData(Player player) {
        Events.active_gens.remove(player);
        Events.placed_gens.remove(player);
        Events.slots_gens.remove(player);
    }

    public void giveGenerator(Player player, Material type) {
        player.getInventory().addItem(Generators.get(type).getItem());
    }

    private void initItems() {
        ItemUtil.createItem("close_btn1", "&c&lExit", null, Material.BARRIER, false);
        ItemUtil.createItem("border1", "&b", null, Material.GRAY_STAINED_GLASS_PANE, false);
        ItemUtil.createItem("border2", "&b", null, Material.BLACK_STAINED_GLASS_PANE, false);
        ItemUtil.createItem("back_btn", "&b&lPrevious page", null, Material.ARROW, false);
        ItemUtil.createItem("next_btn", "&b&lNext page", null, Material.ARROW, false);
        ItemUtil.createItem("tank", this.getConfig().getString("tanks.name"), Objects.requireNonNull(this.getConfig().getString("tanks.lore")).replaceAll("\\|\\|", "::"), Material.getMaterial(this.getConfig().getString("tanks.block").toUpperCase()), false);
    }

    private void setupGenerators() {
        try {
            Objects.requireNonNull(this.getConfig().getConfigurationSection("gens")).getKeys(false).forEach((key) -> {
                String block = String.valueOf(this.getConfig().getString("gens." + key + ".block"));
                String drop = String.valueOf(this.getConfig().getString("gens." + key + ".drop"));
                String next = String.valueOf(this.getConfig().getString("gens." + key + ".next"));
                Long worth = Long.valueOf(Objects.requireNonNull(this.getConfig().getString("gens." + key + ".worth")));
                Long upgrade_price = Long.valueOf(Objects.requireNonNull(this.getConfig().getString("gens." + key + ".upgrade_price")));
                String name = String.valueOf(this.getConfig().getString("gens." + key + ".name"));
                String lore = String.valueOf(this.getConfig().getString("gens." + key + ".lore"));
                addGenerator(block, drop, next, worth, upgrade_price, name, lore);
            });
        } catch (NullPointerException var2) {
            Bukkit.getLogger().severe("Core Loading Error!");
        }
    }

    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            new Placeholders().register();
            DataBase.setupDatabase();
            if (!this.setupEconomy()) {
                Bukkit.getLogger().severe(String.format("[%s] - No Vault dependency found!", this.getDescription().getName()));
                this.getServer().getPluginManager().disablePlugin(this);
            }
            this.saveDefaultConfig();
            this.getServer().getPluginManager().registerEvents(new Events(), this);
            this.getServer().getPluginManager().registerEvents(new GenCoreGUI(), this);
            this.setupGenerators();
            initItems();
            GenCoreGUI.setupGensGuis();
            this.startLoop();
            Bukkit.getOnlinePlayers().forEach(Events::runJoin);
        }
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("GenSlots")) {
            if (sender.hasPermission("gens.giveslots")) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lIncorrect Usage!"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8- &7/genslots (&6add/set/give&7) (&6Number&7) (&6Player&7)"));
                    return true;
                }
                Player p = Bukkit.getPlayer(args[2]);
                if (p == null) {
                    return true;
                }
                switch (args[0]) {
                    case "give" -> {
                        Inventory inv = p.getInventory();
                        int s = inv.firstEmpty();
                        if (s != -1) {
                            ItemStack item = new ItemStack(Material.PAPER);
                            ItemMeta meta = item.getItemMeta();
                            try {
                                Objects.requireNonNull(meta).setDisplayName(ChatColor.translateAlternateColorCodes('&', "&9Gen Slots Voucher &7(Right-Click)"));
                                int num = Integer.parseInt(args[1]);
                                meta.addEnchant(Enchantment.LURE, num, true);
                                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                List<String> lore = new ArrayList<>();
                                lore.add(ChatColor.translateAlternateColorCodes('&', "&9Slots: &f" + num));
                                lore.add(ChatColor.translateAlternateColorCodes('&', "&9Giver: &f" + p.getName()));
                                meta.setLore(lore);
                                item.setItemMeta(meta);
                                inv.setItem(s, item);
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully given a GenSlot Voucher to &6" + p.getDisplayName()));
                                return true;
                            } catch (NumberFormatException var13) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lIncorrect Usage!"));
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8- &7/genslots (&6add/set/give&7) (&6Number&7) [&6Player&7]"));
                                return true;
                            }
                        }
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis player's inventory is full!"));
                        return true;
                    }
                    case "add" -> {
                        try {
                            int num = Integer.parseInt(args[1]);
                            Events.slots_gens.replace(p, Events.slots_gens.get(p) + num);
                        } catch (NumberFormatException a) {
                            a.printStackTrace();
                        }
                        return true;
                    }
                    case "set" -> {
                        try {
                            int num = Integer.parseInt(args[1]);
                            Events.slots_gens.replace(p, num);
                        } catch (NumberFormatException a) {
                            a.printStackTrace();
                        }
                        return true;
                    }
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lIncorrect Usage!"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8- &7/genslots (&6add/set/give&7) (&6Number&7) [&6Player&7]"));
                return true;
            }
        } else if (label.equalsIgnoreCase("Genlist") || label.equalsIgnoreCase("GensList") || label.equalsIgnoreCase("GenLists") || label.equalsIgnoreCase("GensLists")) {
            if(sender instanceof Player) {
                GenCoreGUI.openGensGui((Player) sender);
            } else {
                sender.sendMessage("Please execute this command as a player!");
            }
        } else if (label.equalsIgnoreCase("GiveGenerator")) {
            if (sender.hasPermission("givegen")) {
                Player p = Bukkit.getPlayer(args[0]);
                Material m = Material.getMaterial(args[1].toUpperCase());
                if(!Bukkit.getOnlinePlayers().contains(p)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cERROR"));
                    return true;
                } else if(!Generators.containsKey(m)){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cERROR"));
                    return true;
                } else {
                    giveGenerator(p, m);
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cERROR"));
                return true;
            }
        } else if(label.equalsIgnoreCase("GiveTank")) {
            if(sender.hasPermission("*")) {
                if(args[0] != null) {
                    Player player = Bukkit.getPlayer(args[0]);
                    if(player != null) {
                        player.getInventory().addItem(ItemUtil.getItem("tank"));
                        return true;
                    }
                    return true;
                } else {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    public void onDisable() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.execute(save);
    }
}
