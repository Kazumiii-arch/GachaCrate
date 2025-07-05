// ========================================================================
// File: CrateEditorMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/CrateEditorMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CrateEditorMenu extends Menu {

    private final Crate crate;

    public CrateEditorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        // Create an inventory with 54 slots (a large chest)
        inventory = Bukkit.createInventory(this, 54, "Editing Crate: " + crate.getName());

        // Populate the inventory with the crate's rewards
        for (Reward reward : crate.getRewards()) {
            ItemStack item = reward.getItem().clone();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GOLD + "Rarity: " + reward.getRarity().getDisplayName());
            lore.add(ChatColor.GOLD + "Chance: " + ChatColor.YELLOW + reward.getChance() + "%");
            lore.add("");
            lore.add(ChatColor.RED + "► Right-Click to REMOVE this reward!");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        // Prevent players from taking items from the GUI
        event.setCancelled(true);

        // Check if the clicked item is a valid reward
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        // We only care about right-clicks for removing items
        if (!event.isRightClick()) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        
        // Find the reward that corresponds to the clicked item
        Reward rewardToRemove = null;
        for (Reward reward : crate.getRewards()) {
            // We check if the item is similar, ignoring the lore we added.
            if (reward.getItem().isSimilar(clickedItem)) {
                rewardToRemove = reward;
                break;
            }
        }

        if (rewardToRemove != null) {
            crate.removeReward(rewardToRemove);
            // Save the changes to the config file
            GachaCrate.getInstance().getCrateManager().saveCrates();
            player.sendMessage(ChatColor.GREEN + "Successfully removed the item from the crate.");
            // Close the inventory, the player can re-open it to see the changes.
            player.closeInventory();
        }
    }
}
