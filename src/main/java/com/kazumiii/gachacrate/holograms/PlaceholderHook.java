package com.kazumiii.gachacrate.holograms;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Fills PlaceholderAPI placeholders in reward commands.
 * Only called when PlaceholderAPI is installed.
 */
public final class PlaceholderHook {

    private PlaceholderHook() {
    }

    /** Fills placeholders that don't depend on a player (e.g. leaderboards). */
    public static String applyGlobal(String text) {
        try {
            return PlaceholderAPI.setPlaceholders((OfflinePlayer) null, text);
        } catch (Throwable t) {
            return text;
        }
    }

    public static String apply(Player player, String text) {
        try {
            return PlaceholderAPI.setPlaceholders((OfflinePlayer) player, text);
        } catch (Throwable t) {
            return text;
        }
    }
}
