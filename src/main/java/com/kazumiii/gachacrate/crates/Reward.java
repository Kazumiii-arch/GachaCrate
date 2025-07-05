// ========================================================================
// File: Reward.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Reward.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class Reward {
    private final UUID uniqueId; // Unique identifier for every reward
    private final RewardType type;
    private Rarity rarity; // Now modifiable
    private double chance; // Now modifiable
    
    private final ItemStack item;
    private final String command;

    /**
     * Constructor for creating a new ITEM reward.
     */
    public Reward(ItemStack item, Rarity rarity, double chance) {
        this.uniqueId = UUID.randomUUID();
        this.type = RewardType.ITEM;
        this.item = item;
        this.command = null;
        this.rarity = rarity;
        this.chance = chance;
    }

    /**
     * Constructor for creating a new COMMAND reward.
     */
    public Reward(String command, Rarity rarity, double chance) {
        this.uniqueId = UUID.randomUUID();
        this.type = RewardType.COMMAND;
        this.command = command;
        this.item = null;
        this.rarity = rarity;
        this.chance = chance;
    }
    
    /**
     * Constructor for loading a reward from the config file, preserving its ID.
     */
    public Reward(UUID uniqueId, RewardType type, ItemStack item, String command, Rarity rarity, double chance) {
        this.uniqueId = uniqueId;
        this.type = type;
        this.item = item;
        this.command = command;
        this.rarity = rarity;
        this.chance = chance;
    }

    // --- Getters ---
    public UUID getUniqueId() { return uniqueId; }
    public RewardType getType() { return type; }
    public Rarity getRarity() { return rarity; }
    public double getChance() { return chance; }
    public ItemStack getItem() { return item; }
    public String getCommand() { return command; }
    
    // --- Setters for Editing ---
    public void setRarity(Rarity rarity) { this.rarity = rarity; }
    public void setChance(double chance) { this.chance = chance; }
}
