package com.gencore.gens;

import com.gencore.util.ColorUtil;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Generator {

    private final Material drop;
    private final Long sell;
    private final Long upgrade;
    private final Material next;
    private final ItemStack item;

    public Generator(final Material block, final String name, final Material drop, final Long sell, final Long upgrade, final List<String> loree, @Nullable final Material next) {
        this.drop = drop;
        this.sell = sell;
        this.upgrade = upgrade;
        this.next = next;
        NBTItem nbt = new NBTItem(new ItemStack(block));
        nbt.setBoolean("isGen", true);
        ItemStack item = nbt.getItem();
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.formatColor(name));
        List<String> lore = new ArrayList<>();
        loree.forEach(s -> lore.add(ColorUtil.formatColor(s)));
        meta.setLore(lore);
        item.setItemMeta(meta);
        this.item = item;
    }

    public Material getDrop() {
        return drop;
    }

    public Long getSell() {
        return sell;
    }

    public Long getUpgradeCost() {
        return upgrade;
    }

    public Material getNextBlock() {
        return next;
    }

    public ItemStack getItem() {
        return item;
    }

}
