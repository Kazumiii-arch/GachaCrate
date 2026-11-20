package com.kazumiii.gachacrate.util;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Locale;

/**
 * Number-based permissions, e.g. gacha.daily.multiplier.2 or gacha.massopen.128.
 * Works with LuckPerms and any other permission plugin.
 */
public final class Perms {

    private Perms() {
    }

    /**
     * The highest number in a permission the player has, like {@code prefix + "<number>"}.
     * Returns {@code fallback} if they have none.
     */
    public static int highest(Player player, String prefix, int fallback) {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        int best = -1;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) continue;
            String permission = info.getPermission().toLowerCase(Locale.ROOT);
            if (!permission.startsWith(lowerPrefix)) continue;
            try {
                best = Math.max(best, Integer.parseInt(permission.substring(lowerPrefix.length())));
            } catch (NumberFormatException ignored) {
                // Not a number (e.g. a wildcard); skip.
            }
        }
        return best < 0 ? fallback : best;
    }
}
