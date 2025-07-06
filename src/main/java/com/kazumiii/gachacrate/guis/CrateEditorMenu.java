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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
        inventory = Bukkit.createInventory(this, 54, "Edit Rewards: " + crate.getName());

        for (Reward reward : crate.getRewards()) {
            ItemStack displayItem;
            if (reward.getType() == RewardType.ITEM) {
                displayItem = reward.getItem().clone();
            } else {
                displayItem = (reward.getPreviewItem() != null) ? reward.getPreviewItem().clone() : new ItemStack(Material.COMMAND_BLOCK);
            }

            ItemMeta meta = displayItem.getItemMeta();
            // Create a fresh lore list to prevent modification issues with existing item lore.
            List<String> newLore = new ArrayList<>();
            if (meta != null && meta.hasLore()) {
                newLore.addAll(meta.getLore()); // Add the original lore first
            }

            if (reward.getType() == RewardType.COMMAND) {
                if (!meta.hasDisplayName()) {
                    meta.setDisplayName(reward.getRarity().getColor() + "Command Reward");
                }
                newLore.add("§7Executes: §f/" + reward.getCommand());
            }
            
            // Add the standardized info footer to the lore
            newLore.add("");
            newLore.add("§eRarity: " + reward.getRarity().getDisplayName());
            newLore.add("§eChance: §f" + reward.getChance() + "%");
            newLore.add("");
            newLore.add("§a► Left-Click to Edit");
            newLore.add("§c► Right-Click to Remove");
            meta.setLore(newLore);
            
            // Embed the unique ID into the item's data container.
            meta.getPersistentDataContainer().set(rewardIdKey, PersistentDataType.STRING, reward.getUniqueId().toString());
            
            // Apply the fully modified meta back to the item
            displayItem.setItemMeta(meta);
            inventory.addItem(displayItem);
        }

        // --- GUI Control Buttons ---
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to Settings");
        backButton.setItemMeta(backMeta);
        inventory.setItem(45, backButton);

        ItemStack addRewardButton = new ItemStack(Material.NETHER_STAR);
        ItemMeta addMeta = addRewardButton.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "✚ Add New Reward");
        addMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to add a new item or", ChatColor.GRAY + "command reward to this crate."));
        addRewardButton.setItemMeta(addMeta);
        inventory.setItem(53, addRewardButton);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Handle button clicks
        if (clicked.getType() == Material.BARRIER) {
            new CrateSettingsMenu(player, crate).open();
            return;
        }
        if (clicked.getType() == Material.NETHER_STAR) {
            new RewardTypeSelectionMenu(player, crate).open();
            return;
        }

        // Handle reward clicks
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(rewardIdKey, PersistentDataType.STRING)) return;

        UUID rewardId = UUID.fromString(meta.getPersistentDataContainer().get(rewardIdKey, PersistentDataType.STRING));
        Reward targetReward = crate.getRewardById(rewardId);
        if (targetReward == null) {
            player.sendMessage(ChatColor.RED + "Error: That reward could not be found.");
            return;
        }

        if (event.isLeftClick()) {
            new RewardEditorMenu(player, crate, targetReward).open();
        } else if (event.isRightClick()) {
            crate.removeReward(targetReward);
            GachaCrate.getInstance().getCrateManager().saveCrates();
            player.sendMessage(ChatColor.GREEN + "Reward removed successfully.");
            open(); // Refresh the menu
        }
    }
}
