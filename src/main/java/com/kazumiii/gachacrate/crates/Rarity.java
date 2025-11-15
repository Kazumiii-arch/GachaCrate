package com.kazumiii.gachacrate.crate;

/**
 * Rarity levels for rewards.
 * Each rarity has:
 * - Color
 * - Weight multiplier
 * - Glow flag
 * - Display name
 */
public enum Rarity {

    COMMON("§7", "Common", 1.0, false),
    UNCOMMON("§a", "Uncommon", 1.2, false),
    RARE("§9", "Rare", 1.5, false),
    EPIC("§5", "Epic", 2.0, true),
    LEGENDARY("§6", "Legendary", 3.0, true),
    MYTHIC("§d", "Mythic", 5.0, true);

    private final String color;
    private final String displayName;
    private final double weightMultiplier;
    private final boolean glow;

    Rarity(String color, String displayName, double weightMultiplier, boolean glow) {
        this.color = color;
        this.displayName = displayName;
        this.weightMultiplier = weightMultiplier;
        this.glow = glow;
    }

    public String getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getWeightMultiplier() {
        return weightMultiplier;
    }

    public boolean isGlow() {
        return glow;
    }

    public String getStyledName() {
        return color + displayName;
    }
}
