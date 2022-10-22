package com.gencore.events;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

@UtilityClass
public class EventManager implements Listener {

    public String getStorageLocationString(Location loc) {
        return Objects.requireNonNull(loc.getWorld()).getName()+";"+loc.getX()+";"+loc.getY()+";"+loc.getZ();
    }
    @SuppressWarnings("all")
    public String getJson(Player player) {
        Events.active_gens.putIfAbsent(player, new HashMap<>());
        HashMap<Material, ArrayList<Location>> map = Events.active_gens.get(player);
        JSONObject json = new JSONObject();
        for (Map.Entry<Material, ArrayList<Location>> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Enumeration<? extends Location> e = Collections.enumeration((Collection<? extends Location>) entry.getValue());
            ArrayList<String> loc = new ArrayList<>();
            while (e.hasMoreElements()) {
                loc.add(getStorageLocationString(e.nextElement()));
            }
            json.put(key, loc);
        }
        return json.toString();
    }

    @SuppressWarnings("all")
    public HashMap<Material, ArrayList<Location>> getData(String str, Player player) throws ParseException {
        int actually_has = 0;
        HashMap<Material, ArrayList<Location>> map = new HashMap<>();
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(str);
        JSONObject jsonObject = (JSONObject)obj;
        for (Object stringObj : jsonObject.keySet()) {
            String key = (String)stringObj;
            JSONArray list = (JSONArray) jsonObject.get(key);
            ArrayList<Location> locs = new ArrayList<>();
            Iterator iterator = list.iterator();
            Material m = Material.getMaterial(key);
            while (iterator.hasNext()) {
                Object loc = iterator.next();
                String a = String.valueOf(loc);
                String[] arr = a.split(";");
                Location l = new Location(Bukkit.getWorld(arr[0]), Double.parseDouble(arr[1]), Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
                if (l.getBlock().getType().equals(m)) {
                    locs.add(l);
                    actually_has += 1;
                }
            }
            map.put(m, locs);
        }
        Events.placed_gens.replace(player, actually_has);
        return map;
    }

    public Integer getSlots(Player player) {
        Events.slots_gens.putIfAbsent(player, Events.defaultslots);
        return Events.slots_gens.get(player);
    }

    public Integer getPlaced(Player player) {
        Events.placed_gens.putIfAbsent(player, 0);
        return Events.placed_gens.get(player);
    }

}
