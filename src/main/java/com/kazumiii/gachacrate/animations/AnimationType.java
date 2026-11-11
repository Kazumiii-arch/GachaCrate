package com.kazumiii.gachacrate.animations;

import org.bukkit.Material;

public enum AnimationType {
    REEL("Reel", Material.HOPPER, "Items scroll past and slow down", "until one stops under the arrow."),
    ROULETTE("Roulette", Material.CLOCK, "Lights spin around the edge", "while the prize in the middle changes."),
    INSTANT("Instant", Material.LIGHTNING_ROD, "No animation. The reward is", "given right away.");

    private final String displayName;
    private final Material icon;
    private final String[] description;

    AnimationType(String displayName, Material icon, String... description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public String[] getDescription() {
        return description;
    }
}
