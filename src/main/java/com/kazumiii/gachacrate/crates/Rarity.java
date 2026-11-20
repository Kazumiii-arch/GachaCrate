package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.util.Text;
import org.bukkit.ChatColor;

/**
 * A rarity tier from config.yml (COMMON, RARE, ...). Higher order = better.
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

    public String getId() {
        return id;
    }

    /** Display name with colors applied (e.g. "&6Mythic" -> "§6Mythic"). */
    public String getDisplayName() {
        return Text.color(displayName);
    }

    public ChatColor getColor() {
        return color;
    }

    public int getOrder() {
        return order;
    }

    public boolean isAtLeast(Rarity other) {
        return other == null || order >= other.order;
    }
}
