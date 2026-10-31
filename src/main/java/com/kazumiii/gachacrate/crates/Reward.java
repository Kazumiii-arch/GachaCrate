// ========================================================================
// File: Reward.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Reward.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * A reward inside a crate. Supports ITEM and COMMAND reward types,
 * matching the GUI editor flows (AddItemMenu, CommandPrompt, RewardEditorMenu).
 */
public class Reward {

    private final UUID uniqueId = UUID.randomUUID();

    private RewardType type = RewardType.ITEM;

    // Item reward
    private ItemStack item;

    // Command reward (single command string, as entered via CommandPrompt)
    private String command;

    // Icon shown in GUIs when the reward is not an ITEM type
    private ItemStack previewItem;

    // Drop chance (percentage weight)
    private double chance = 100.0;

    // Rarity tier id, e.g. "COMMON". Resolved via RarityManager when needed.
    private String rarityId;

    // -------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------
    public Reward() {}

    /** Item reward. */
    public Reward(ItemStack item, Rarity rarity, double chance) {
        this.type = RewardType.ITEM;
        this.item = item;
        this.rarityId = rarity != null ? rarity.getId() : "COMMON";
        this.chance = chance;
    }

    /** Command reward. The command is run from console with {player} replaced. */
    public Reward(String command, Rarity rarity, double chance) {
        this.type = RewardType.COMMAND;
        this.command = command;
        this.rarityId = rarity != null ? rarity.getId() : "COMMON";
        this.chance = chance;
    }

    // -------------------------------------------------------------------
    // Type
    // -------------------------------------------------------------------
    public RewardType getType() {
        return type;
    }

    public void setType(RewardType type) {
        this.type = type;
    }

    // -------------------------------------------------------------------
    // Unique ID (used by CrateEditorMenu to identify rewards in the GUI)
    // -------------------------------------------------------------------
    public UUID getUniqueId() {
        return uniqueId;
    }

    // -------------------------------------------------------------------
    // Item
    // -------------------------------------------------------------------
    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    // -------------------------------------------------------------------
    // Command
    // -------------------------------------------------------------------
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    // -------------------------------------------------------------------
    // Preview item (icon for non-ITEM rewards in GUIs)
    // -------------------------------------------------------------------
    public ItemStack getPreviewItem() {
        return previewItem;
    }

    public void setPreviewItem(ItemStack previewItem) {
        this.previewItem = previewItem;
    }

    // -------------------------------------------------------------------
    // Chance
    // -------------------------------------------------------------------
    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    // -------------------------------------------------------------------
    // Rarity
    // -------------------------------------------------------------------
    public String getRarityId() {
        return rarityId;
    }

    public void setRarityId(String rarityId) {
        this.rarityId = rarityId;
    }

    /**
     * Resolves the live Rarity object for this reward via the plugin's
     * RarityManager. Returns null if the rarity id no longer exists.
     */
    public Rarity getRarity() {
        return com.kazumiii.gachacrate.GachaCrate.getInstance()
                .getRarityManager().getRarity(rarityId);
    }

    public void setRarity(Rarity rarity) {
        this.rarityId = rarity != null ? rarity.getId() : null;
    }
}
