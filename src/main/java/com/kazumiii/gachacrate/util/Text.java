package com.kazumiii.gachacrate.util;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small helpers for colors and messages.
 */
public final class Text {

    private static final DecimalFormat NUMBER = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private Text() {
    }

    private static final Pattern HEX = Pattern.compile("&#([0-9a-fA-F]{6})");

    /**
     * Translates '&' color codes and hex colors written as &amp;#RRGGBB (e.g. "&#ff8800Orange").
     * Null becomes an empty string.
     */
    public static String color(String text) {
        if (text == null) return "";
        if (text.contains("&#")) {
            Matcher matcher = HEX.matcher(text);
            StringBuffer out = new StringBuffer();
            while (matcher.find()) {
                StringBuilder hex = new StringBuilder("§x");
                for (char c : matcher.group(1).toCharArray()) hex.append('§').append(c);
                matcher.appendReplacement(out, Matcher.quoteReplacement(hex.toString()));
            }
            matcher.appendTail(out);
            text = out.toString();
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> lines) {
        List<String> out = new ArrayList<>(lines.size());
        for (String line : lines) out.add(color(line));
        return out;
    }

    public static String strip(String text) {
        return text == null ? "" : ChatColor.stripColor(text);
    }

    public static String prefix() {
        return color(GachaCrate.getInstance().getConfig().getString("prefix", "&2&lGacha&a&lCrate &8» &r"));
    }

    /** Fills PlaceholderAPI placeholders for a player, if PlaceholderAPI is installed. */
    public static String papi(org.bukkit.entity.Player player, String text) {
        GachaCrate plugin = GachaCrate.getInstance();
        if (plugin == null || !plugin.isPlaceholderApiEnabled() || text == null || text.indexOf('%') < 0) return text;
        return com.kazumiii.gachacrate.holograms.PlaceholderHook.apply(player, text);
    }

    /** Sends a message with the plugin prefix. Supports '&' and '§' colors. */
    public static void send(CommandSender to, String message) {
        to.sendMessage(prefix() + color(message));
    }

    /** Formats a number with at most two decimals (25.0 -> "25", 1.255 -> "1.26"). */
    public static String number(double value) {
        synchronized (NUMBER) {
            return NUMBER.format(value);
        }
    }
}
