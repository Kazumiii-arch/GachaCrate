// ========================================================================
// File: Rarity.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Rarity.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import org.bukkit.ChatColor;

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

    public String getDisplayName() {
        // Translate color codes in the display name (e.g., &6Mythic -> §6Mythic)
        return ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public ChatColor getColor() {
        return color;
    }

    public int getOrder() {
        return order;
    }
}
