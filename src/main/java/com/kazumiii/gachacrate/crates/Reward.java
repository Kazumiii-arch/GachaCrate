// ========================================================================
// File: Reward.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Reward.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import org.bukkit.inventory.ItemStack;

public class Reward {
    private final RewardType type;
    private final Rarity rarity;
    private final double chance;
    
    // These can be null depending on the reward type
    private final ItemStack item;
    private final String command;

    /**
     * Constructor for an ITEM reward.
     */
    public Reward(ItemStack item, Rarity rarity, double chance) {
        this.type = RewardType.ITEM;
        this.item = item;
        this.command = null;
        this.rarity = rarity;
        this.chance = chance;
    }

    /**
     * Constructor for a COMMAND reward.
     */
    public Reward(String command, Rarity rarity, double chance) {
        this.type = RewardType.COMMAND;
        this.command = command;
        this.item = null;
        this.rarity = rarity;
        this.chance = chance;
    }

    public RewardType getType() { return type; }
    public Rarity getRarity() { return rarity; }
    public double getChance() { return chance; }
    public ItemStack getItem() { return item; }
    public String getCommand() { return command; }
}
