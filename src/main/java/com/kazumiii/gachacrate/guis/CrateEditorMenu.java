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
            ItemMeta meta;
            List<String> lore;

            if (reward.getType() == RewardType.ITEM) {
                displayItem = reward.getItem().clone();
                meta = displayItem.getItemMeta();
                lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            } else {
                displayItem = new ItemStack(Material.COMMAND_BLOCK);
                meta = displayItem.getItemMeta();
                meta.setDisplayName(reward.getRarity().getColor() + "Command Reward");
                lore = new ArrayList<>(Arrays.asList("§7Executes: §f/" + reward.getCommand()));
            }
            
            lore.add("");
            lore.add("§eRarity: " + reward.getRarity().getDisplayName());
            lore.add("§eChance: §f" + reward.getChance() + "%");
            lore.add("");
            lore.add("§a► Left-Click to Edit");
            lore.add("§c► Right-Click to Remove");
            meta.setLore(lore);
            
            // Embed the unique ID into the item's data for reliable identification
            meta.getPersistentDataContainer().set(rewardIdKey, PersistentDataType.STRING, reward.getUniqueId().toString());
            displayItem.setItemMeta(meta);
            inventory.addItem(displayItem);
        }

        // Add the back button
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to Settings");
        backButton.setItemMeta(backMeta);
        inventory.setItem(45, backButton); // Bottom-left corner

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Handle back button click
        if (clicked.getType() == Material.BARRIER) {
            new CrateSettingsMenu(player, crate).open();
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(rewardIdKey, PersistentDataType.STRING)) return;

        // Find the reward using the unique ID stored in the item
        UUID rewardId = UUID.fromString(meta.getPersistentDataContainer().get(rewardIdKey, PersistentDataType.STRING));
        Reward targetReward = crate.getRewardById(rewardId);
        if (targetReward == null) {
            player.sendMessage(ChatColor.RED + "Error: Could not find that reward. It may have been removed.");
            open(); // Refresh the menu
            return;
        }

        if (event.isLeftClick()) {
            // Open the new editor for this specific reward
            new RewardEditorMenu(player, crate, targetReward).open();
        } else if (event.isRightClick()) {
            // Remove the reward
            crate.removeReward(targetReward);
            GachaCrate.getInstance().getCrateManager().saveCrates();
            player.sendMessage(ChatColor.GREEN + "Reward removed successfully.");
            // Re-open the menu to show the change
            open();
        }
    }
}
