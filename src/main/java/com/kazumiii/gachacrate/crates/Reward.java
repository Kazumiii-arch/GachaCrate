// ========================================================================
// File: Reward.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Reward.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class Reward {
    private final UUID uniqueId;
    private final RewardType type;
    private Rarity rarity;
    private double chance;
    
    // This is now modifiable for ITEM rewards
    private ItemStack item; 
    private final String command;
    private ItemStack previewItem;

    /**
     * Constructor for creating a new ITEM reward.
     */
    public Reward(ItemStack item, Rarity rarity, double chance) {
        this.uniqueId = UUID.randomUUID();
        this.type = RewardType.ITEM;
        this.item = item;
        this.command = null;
        this.previewItem = null;
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
        this.previewItem = null;
        this.rarity = rarity;
        this.chance = chance;
    }
    
    /**
     * Full constructor for loading a reward from the config file.
     */
    public Reward(UUID uniqueId, RewardType type, ItemStack item, String command, ItemStack previewItem, Rarity rarity, double chance) {
        this.uniqueId = uniqueId;
        this.type = type;
        this.item = item;
        this.command = command;
        this.previewItem = previewItem;
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
    public ItemStack getPreviewItem() { return previewItem; }
    
    // --- Setters for Editing ---
    public void setRarity(Rarity rarity) { this.rarity = rarity; }
    public void setChance(double chance) { this.chance = chance; }
    public void setPreviewItem(ItemStack previewItem) { this.previewItem = previewItem; }
    
    /**
     * Sets the physical item for an ITEM type reward.
     * This has no effect on COMMAND rewards.
     * @param item The new ItemStack for the reward.
     */
    public void setItem(ItemStack item) {
        if (this.type == RewardType.ITEM) {
            this.item = item;
        }
    }
}
