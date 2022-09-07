package com.lilygens.gencore.gens;

import com.google.gson.Gson;
import com.lilygens.gencore.handler.PluginHandler;
import com.lilygens.gencore.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.text.NumberFormat;
import java.util.*;

public class GenTanks {

    private final HashMap<Material, Generator> gens = PluginHandler.getPlugin().Generators;
    private  HashMap<Material, Long> items = new HashMap<>();
    public static NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
    private final UUID uuid;

    public GenTanks(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    static Configuration config = PluginHandler.getPlugin().getConfig();
    public final Boolean exp_enabled = config.getBoolean("tanks.exp.enabled");
    public final String message1 = config.getString("messages.solditems");

    public void addItems(Material a, Long b) {
        items.putIfAbsent(a, 0L);
        items.replace(a, items.get(a) + b);
    }

    public String getSavingItems(Boolean d) {
        JSONObject data = new JSONObject();
        data.putAll(items);
        if(d) items.clear();
        if(data.toString() == null) return "";
        return data.toString();
    }

    public void importSavingItems(String data) {
        try {
            HashMap map = new Gson().fromJson(data, items.getClass());
            HashMap<Material, Long> map2 = new HashMap<>();
            map.forEach((key, value) -> map2.put(Material.getMaterial(key.toString().toUpperCase()), Long.parseLong(String.valueOf(value).split("\\.")[0])));
            items = map2;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Double getValue() {
        final Double[] total = {0.0};
        items.forEach((key, value) -> {
            if(gens.containsKey(key)) {
                total[0] = total[0] +(gens.get(key).getSell()*Double.parseDouble(String.valueOf(value)));
            }
        });
        return total[0];
    }

    public Long getSize() {
        final Long[] total = {0L};
        items.forEach((key, value) -> {
            total[0] += value;
        });
        return total[0];
    }

    public void sellItems(Double multi) {
        final Double[] total = {0.0};
        final Long[] total2 = {0L};
        items.forEach((key, value) -> {
            if(gens.containsKey(key)) {
                Generator gen = gens.get(key);
                total[0] += (gen.getSell()*Double.parseDouble(String.valueOf(value)))*multi;
                total2[0] += value;
            }
            items.replace(key, items.get(key)-value);
        });
        Player player = Bukkit.getPlayer(this.getUuid());
        if(exp_enabled) {
           PluginHandler.getPlugin().sendCommand(player, (Objects.requireNonNull(config.getString("tanks.exp.command"))).replaceAll("\\{amount}", String.valueOf(total[0] * config.getDouble("tanks.exp.amount"))));
        }
        assert message1 != null;
        assert player != null;
        player.sendMessage(ColorUtil.formatColor(message1.replaceAll("\\{items}", nf.format(total2[0])).replaceAll("\\{amount}", nf.format(total[0]))));
       PluginHandler.getPlugin().addMoney(player, total[0]);
    }
}
