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
    
    private final ItemStack item; // The actual item for ITEM rewards
    private final String command; // The actual command for COMMAND rewards
    private ItemStack previewItem; // The visual representation for COMMAND rewards

    // Constructor for an ITEM reward
    public Reward(ItemStack item, Rarity rarity, double chance) {
        this.uniqueId = UUID.randomUUID();
        this.type = RewardType.ITEM;
        this.item = item;
        this.command = null;
        this.previewItem = null; // Not used for item rewards
        this.rarity = rarity;
        this.chance = chance;
    }

    // Constructor for a COMMAND reward
    public Reward(String command, Rarity rarity, double chance) {
        this.uniqueId = UUID.randomUUID();
        this.type = RewardType.COMMAND;
        this.command = command;
        this.item = null;
        this.previewItem = null; // Can be set later
        this.rarity = rarity;
        this.chance = chance;
    }
    
    // Full constructor for loading from file
    public Reward(UUID uniqueId, RewardType type, ItemStack item, String command, ItemStack previewItem, Rarity rarity, double chance) {
        this.uniqueId = uniqueId;
        this.type = type;
        this.item = item;
        this.command = command;
        this.previewItem = previewItem;
        this.rarity = rarity;
        this.chance = chance;
    }

    // Getters
    public UUID getUniqueId() { return uniqueId; }
    public RewardType getType() { return type; }
    public Rarity getRarity() { return rarity; }
    public double getChance() { return chance; }
    public ItemStack getItem() { return item; }
    public String getCommand() { return command; }
    public ItemStack getPreviewItem() { return previewItem; }
    
    // Setters
    public void setRarity(Rarity rarity) { this.rarity = rarity; }
    public void setChance(double chance) { this.chance = chance; }
    public void setPreviewItem(ItemStack previewItem) { this.previewItem = previewItem; }
}
