package com.gencore.util;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ColorUtil {

    private final Pattern pattern = Pattern.compile("&#[a-fA-f0-9]{6}");

    public String formatColor(String str) {
        Matcher match = pattern.matcher(str);
        while (match.find()) {
            String color = str.substring(match.start(), match.end());
            str = str.replace(color, ChatColor.of(color.replace("&", "")) + "");
            match = pattern.matcher(str);
        }
        return ChatColor.translateAlternateColorCodes('&', str);
    }

}
