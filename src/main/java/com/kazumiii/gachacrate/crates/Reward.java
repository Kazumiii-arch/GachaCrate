// ========================================================================
// File: Reward.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Reward.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import org.bukkit.inventory.ItemStack;

public class Reward {
    private final ItemStack item;
    private final Rarity rarity;
    private final double chance;

    public Reward(ItemStack item, Rarity rarity, double chance) {
        this.item = item;
        this.rarity = rarity;
        this.chance = chance;
    }

    public ItemStack getItem() {
        return item;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public double getChance() {
        return chance;
    }
}
