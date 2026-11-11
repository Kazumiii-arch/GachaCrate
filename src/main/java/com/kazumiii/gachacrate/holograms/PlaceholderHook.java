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

    public static String apply(Player player, String text) {
        try {
            return PlaceholderAPI.setPlaceholders((OfflinePlayer) player, text);
        } catch (Throwable t) {
            return text;
        }
    }
}
