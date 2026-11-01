// ========================================================================
// File: CrateEditorMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/CrateEditorMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.crates.RewardType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Reward list for a crate: left-click to edit, right-click to remove,
 * star button to add new rewards.
 */
public class CrateEditorMenu extends Menu {

    private final Crate crate;
    private final NamespacedKey rewardIdKey;

    public CrateEditorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        this.rewardIdKey = new NamespacedKey(GachaCrate.getInstance(), "gacha-reward-id");
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 54, "§8Rewards" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA + crate.getDisplayName());
        ItemBuilder.drawBorder(inventory);

        for (Reward reward : crate.getRewards()) {
            ItemStack displayItem;
            if (reward.getType() == RewardType.ITEM && reward.getItem() != null) {
                displayItem = reward.getItem().clone();
            } else {
                displayItem = reward.getPreviewItem() != null
                        ? reward.getPreviewItem().clone()
                        : new ItemStack(Material.COMMAND_BLOCK);
            }

            ItemMeta meta = displayItem.hasItemMeta()
                    ? displayItem.getItemMeta()
                    : Bukkit.getItemFactory().getItemMeta(displayItem.getType());

            List<String> newLore = new ArrayList<>();
            if (meta != null && meta.hasLore() && meta.getLore() != null) {
                newLore.addAll(meta.getLore());
            }

            if (reward.getType() == RewardType.COMMAND && reward.getCommand() != null) {
                if (meta != null && !meta.hasDisplayName()) {
                    String color = reward.getRarity() != null ? reward.getRarity().getColor().toString() : "§7";
                    meta.setDisplayName(color + "Command Reward");
                }
                newLore.add(0, "§7Executes: §f/" + reward.getCommand());
            }

            // Info footer
            String rarityName = reward.getRarity() != null ? reward.getRarity().getDisplayName() : "Unknown";
            newLore.add("");
            newLore.add(ChatColor.GRAY + "Rarity: " + ChatColor.WHITE + rarityName);
            newLore.add(ChatColor.GRAY + "Chance: " + ChatColor.WHITE + reward.getChance() + "%");
            newLore.add("");
            newLore.add(ChatColor.GREEN + "❯ Left-Click to Edit");
            newLore.add(ChatColor.RED + "❯ Right-Click to Remove");

            if (meta != null) {
                meta.setLore(newLore);
                // Embed the unique ID so clicks map back to this exact reward.
                meta.getPersistentDataContainer().set(rewardIdKey, PersistentDataType.STRING, reward.getUniqueId().toString());
                displayItem.setItemMeta(meta);
            }
            inventory.addItem(displayItem);
        }

        // Controls
        inventory.setItem(45, ItemBuilder.backButton("Settings"));
        inventory.setItem(49, new ItemBuilder(Material.NETHER_STAR)
                .name(ChatColor.GREEN + "✚ Add New Reward")
                .blank()
                .lore(ChatColor.GRAY + "Add an item or command")
                .lore(ChatColor.GRAY + "reward to this crate.")
                .build());
        inventory.setItem(53, new ItemBuilder(Material.BOOK)
                .name(ChatColor.GOLD + "Preview Crate")
                .blank()
                .lore(ChatColor.GRAY + "See this crate as players see it.")
                .build());

        ItemBuilder.fillBackground(inventory);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.NETHER_STAR) {
            new RewardTypeSelectionMenu(player, crate).open();
            return;
        }
        if (clicked.getType() == Material.BOOK) {
            new CratePreviewMenu(player, crate).open();
            return;
        }
        if (clicked.getType() == Material.ARROW) {
            new CrateSettingsMenu(player, crate).open();
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(rewardIdKey, PersistentDataType.STRING)) return;

        UUID rewardId = UUID.fromString(meta.getPersistentDataContainer().get(rewardIdKey, PersistentDataType.STRING));
        Reward targetReward = crate.getRewardById(rewardId);
        if (targetReward == null) {
            player.sendMessage(ChatColor.RED + "Error: That reward could not be found.");
            return;
        }

        if (event.isRightClick()) {
            crate.removeReward(targetReward);
            GachaCrate.getInstance().getCrateManager().saveCrates();
            player.sendMessage(ChatColor.GREEN + "Reward removed successfully.");
            open();
        } else {
            new RewardEditorMenu(player, crate, targetReward).open();
        }
    }
}
