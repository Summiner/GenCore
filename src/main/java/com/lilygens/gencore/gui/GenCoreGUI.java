package com.lilygens.gencore.gui;

import com.lilygens.gencore.events.EventManager;
import com.lilygens.gencore.events.Events;
import com.lilygens.gencore.gens.GenTanks;
import com.lilygens.gencore.handler.PluginHandler;
import com.lilygens.gencore.util.ColorUtil;
import com.lilygens.gencore.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class GenCoreGUI implements Listener {
    public static ArrayList<Inventory> GensList = new ArrayList<>();
    public static boolean pagination = true;
    public static ArrayList<Integer> slots = new ArrayList<>();
    public static Configuration config = PluginHandler.getPlugin().getConfig();
    private static final String sellwand_name = config.getString("tanks.sellwands.name");
    private static final Material sellwand_item = Material.getMaterial(Objects.requireNonNull(config.getString("tanks.sellwands.item")).toUpperCase());

    public static void setupGensGuis() {
        for (int i = 10; i <= 16; ++i) {
            slots.add(i);
        }
        for (int i = 19; i <= 25; ++i) {
            slots.add(i);
        }
        for (int i = 28; i <= 34; ++i) {
            slots.add(i);
        }
        int pages = (int) Math.floor(PluginHandler.getPlugin().Generators.size() / 21f)+1;

        for(int page = 1; page < pages+1; page++) {
            int start = (page*21)-20;
            Inventory inv = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&', "&8Gen List"));
            for (int i = 0; i <= 9; ++i) {
                inv.setItem(i, ItemUtil.getItem("border2"));
            }
            for (int i = 35; i <= 44; ++i) {
                inv.setItem(i, ItemUtil.getItem("border2"));
            }
            inv.setItem(slots.get(0), ItemUtil.getItem("diamond"));
            inv.setItem(17, ItemUtil.getItem("border2"));
            inv.setItem(18, ItemUtil.getItem("border2"));
            inv.setItem(26, ItemUtil.getItem("border2"));
            inv.setItem(27, ItemUtil.getItem("border2"));
            inv.setItem(40, ItemUtil.getItem("close_btn1"));
            if(page >= 2) {
                inv.setItem(37, ItemUtil.getItem("back_btn"));
            }
            if(page < pages) {
                inv.setItem(43, ItemUtil.getItem("next_btn"));
            }
            final int[] loops = {0};
            final int[] slot = {10};
           PluginHandler.getPlugin().ListedGenerators.forEach(value -> {
                if(slot[0] < 35){
                    loops[0]++;
                    if(loops[0] >= start){
                        if(slot[0] == 17 || slot[0] == 26){
                            slot[0]+=2;
                        }
                        inv.setItem(slot[0], value.getItem());
                        slot[0]++;
                    }
                }
            });
            if(pages == 1) {
                pagination= false;
            }
            GensList.add(inv);
        }

    }

    public static void openGensGui(Player player) {
        player.openInventory(GensList.get(0));
    }

    public static void openTankGui(Player player) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.formatColor("&aSell your items!"));
        GenTanks tank = Events.tanks.get(player.getUniqueId());
        String[] lore = {ColorUtil.formatColor("&7Value: &a$&f"+nf.format(tank.getValue())), ColorUtil.formatColor("&7Items: &f"+nf.format(tank.getSize())), ColorUtil.formatColor("&8(Left-Click)")};
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        Inventory inv = Bukkit.createInventory(null, InventoryType.DROPPER, ColorUtil.formatColor("&8Storage Box"));
        inv.setItem(0, ItemUtil.getItem("border2"));
        inv.setItem(1, ItemUtil.getItem("border2"));
        inv.setItem(2, ItemUtil.getItem("border2"));
        inv.setItem(3, ItemUtil.getItem("border2"));
        inv.setItem(4, item);
        inv.setItem(5, ItemUtil.getItem("border2"));
        inv.setItem(6, ItemUtil.getItem("border2"));
        inv.setItem(7, ItemUtil.getItem("border2"));
        inv.setItem(8, ItemUtil.getItem("border2"));
        player.openInventory(inv);
    }


    @EventHandler
    public void guiClickEvent(InventoryClickEvent event) {
        InventoryView view = event.getWhoClicked().getOpenInventory();
        if(GensList.contains(event.getInventory())) {
            if (event.getRawSlot() < 0 || event.getRawSlot() >= view.countSlots()) {
                event.setCancelled(true);
                return;
            } else if (event.getInventory().getItem(event.getSlot()) == null) {
                event.setCancelled(true);
                return;
            }
            if(!pagination) {
                if (event.getInventory().equals(GensList.get(0))) {
                    event.setCancelled(true);
                    Player player = (Player) event.getWhoClicked();
                    if (event.getSlot() == 40) {
                        player.closeInventory();
                    }
                    if(player.hasPermission("gens.givegen")) {
                        if(slots.contains(event.getSlot())) {
                            player.getInventory().addItem(event.getInventory().getItem(event.getSlot()));
                        }
                    }
                }
            } else {
                if(GensList.contains(event.getInventory())) {
                    event.setCancelled(true);
                    Player player = (Player) event.getWhoClicked();
                    int page = GensList.indexOf(event.getInventory());
                    switch (event.getSlot()) {
                        case 40:
                            player.closeInventory();
                            break;
                        case 43:
                            if(page != GensList.size()-1) {
                                player.openInventory(GensList.get(page+1));
                            }
                            break;
                        case 37:
                            if(page > 0) {
                                player.openInventory(GensList.get(page-1));
                            }
                            break;
                    }
                    if(player.hasPermission("gens.givegen")) {
                        if(slots.contains(event.getSlot())) {
                            player.getInventory().addItem(event.getInventory().getItem(event.getSlot()));
                        }
                    }
                }
            }
        } else if (view.getTitle().equals(ColorUtil.formatColor("&8Storage Box"))) {
            event.setCancelled(true);
            if(event.getSlot() == 4) {
                double multiplier = 1.0;
                ItemStack item = ItemUtil.getItemInHand(event.getWhoClicked());
                ItemMeta meta = item.getItemMeta();
                if(meta != null) {
                    if(item.getType() == sellwand_item) {
                        assert sellwand_name != null;
                        if (meta.getDisplayName().contains(sellwand_name)) {
                            try {
                                multiplier = Double.parseDouble(meta.getDisplayName().replaceAll(sellwand_name, ""));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                Events.tanks.get(event.getWhoClicked().getUniqueId()).sellItems(multiplier);
                GenCoreGUI.openTankGui((Player) event.getWhoClicked());
            }
        }
    }
}
