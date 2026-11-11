package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * One possible prize inside a crate.
 * <p>
 * {@code chance} is a weight: a reward with chance 10 is twice as likely as one with chance 5.
 * The real percentage is chance / (sum of all chances) and is shown in the preview.
 */
public class Reward {

    private final UUID uniqueId;
    private final RewardType type;
    private Rarity rarity;
    private double chance;

    private ItemStack item;                    // ITEM rewards
    private final List<String> commands;       // COMMAND rewards
    private ItemStack previewItem;             // COMMAND rewards: icon shown in menus

    /** New ITEM reward. */
    public Reward(ItemStack item, Rarity rarity, double chance) {
        this(UUID.randomUUID(), RewardType.ITEM, item, Collections.emptyList(), null, rarity, chance);
    }

    /** New COMMAND reward. */
    public Reward(List<String> commands, Rarity rarity, double chance) {
        this(UUID.randomUUID(), RewardType.COMMAND, null, commands, null, rarity, chance);
    }

    /** Full constructor, used when loading from crates.yml. */
    public Reward(UUID uniqueId, RewardType type, ItemStack item, List<String> commands, ItemStack previewItem,
                  Rarity rarity, double chance) {
        this.uniqueId = uniqueId;
        this.type = type;
        this.item = item;
        this.commands = new ArrayList<>();
        if (commands != null) {
            for (String command : commands) {
                String clean = cleanCommand(command);
                if (!clean.isEmpty()) this.commands.add(clean);
            }
        }
        this.previewItem = previewItem;
        this.rarity = rarity;
        this.chance = chance;
    }

    /** Removes a leading '/' and surrounding spaces. */
    public static String cleanCommand(String command) {
        if (command == null) return "";
        String clean = command.trim();
        while (clean.startsWith("/")) clean = clean.substring(1).trim();
        return clean;
    }

    // --- Getters ---
    public UUID getUniqueId() { return uniqueId; }
    public RewardType getType() { return type; }
    public Rarity getRarity() { return rarity; }
    public double getChance() { return chance; }
    public ItemStack getItem() { return item; }
    public List<String> getCommands() { return commands; }
    public ItemStack getPreviewItem() { return previewItem; }

    // --- Setters ---
    public void setRarity(Rarity rarity) { this.rarity = rarity; }
    public void setChance(double chance) { this.chance = chance; }
    public void setPreviewItem(ItemStack previewItem) { this.previewItem = previewItem; }

    /** Changes the item of an ITEM reward. Does nothing for command rewards. */
    public void setItem(ItemStack item) {
        if (type == RewardType.ITEM) this.item = item;
    }

    /** True if this reward has something to give (prevents broken rewards from being rolled). */
    public boolean isValid() {
        if (chance <= 0 || rarity == null) return false;
        if (type == RewardType.ITEM) return item != null && item.getType() != Material.AIR;
        return !commands.isEmpty();
    }

    /**
     * A copy of the icon to show in menus and animations.
     * Command rewards without a preview item get a named command block.
     */
    public ItemStack getDisplayItem() {
        if (type == RewardType.ITEM && item != null && item.getType() != Material.AIR) {
            return item.clone();
        }
        if (previewItem != null && previewItem.getType() != Material.AIR) {
            return previewItem.clone();
        }
        String color = rarity != null ? rarity.getColor().toString() : "";
        return Items.of(type == RewardType.ITEM ? Material.BARRIER : Material.COMMAND_BLOCK)
                .name(color + (type == RewardType.ITEM ? "Missing Item" : "Command Reward"))
                .build();
    }

    /** The name players see in chat, e.g. "§6Diamond Sword" or "64x Diamond". */
    public String getDisplayName() {
        ItemStack display = getDisplayItem();
        ItemMeta meta = display.getItemMeta();
        String name;
        if (meta != null && meta.hasDisplayName()) {
            name = meta.getDisplayName();
        } else {
            String color = rarity != null ? rarity.getColor().toString() : "";
            name = color + prettyMaterial(display.getType());
        }
        if (type == RewardType.ITEM && display.getAmount() > 1) {
            name = display.getAmount() + "x " + name;
        }
        return name;
    }

    private static String prettyMaterial(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    /** Short description of the commands, for admin menus. */
    public List<String> describeCommands() {
        List<String> lines = new ArrayList<>();
        for (String command : commands) lines.add("&7/" + Text.strip(command));
        return lines;
    }
}
