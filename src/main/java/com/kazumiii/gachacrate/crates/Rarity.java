// ========================================================================
// File: Rarity.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Rarity.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import org.bukkit.ChatColor;

/**
 * A rarity tier loaded from config.yml ("rarities" section).
 * Config-driven so server owners can define their own tiers,
 * matching what RarityManager already expects.
 */
public class Rarity {

    private final String id;
    private final String displayName;
    private final ChatColor color;
    private final int order;

    public Rarity(String id, String displayName, ChatColor color, int order) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.order = order;
    }

    /** The config key / ID of this rarity (e.g. "MYTHIC"). */
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    /** Higher is better. Used for pity checks, sorting and broadcast thresholds. */
    public int getOrder() {
        return order;
    }

    public String getStyledName() {
        return color + displayName;
    }
}
