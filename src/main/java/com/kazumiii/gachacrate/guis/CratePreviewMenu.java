// ========================================================================
// File: CratePreviewMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/CratePreviewMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.crates.RewardType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CratePreviewMenu extends Menu {

    private final Crate crate;

    public CratePreviewMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 54, "Preview: " + crate.getName());

        for (Reward reward : crate.getRewards()) {
            ItemStack displayItem;
            ItemMeta meta;
            List<String> lore;

            if (reward.getType() == RewardType.ITEM) {
                displayItem = reward.getItem().clone();
                meta = displayItem.getItemMeta();
                lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            } else { // It's a COMMAND reward
                if (reward.getPreviewItem() != null) {
                    displayItem = reward.getPreviewItem().clone();
                } else {
                    displayItem = new ItemStack(Material.COMMAND_BLOCK);
                }
                meta = displayItem.getItemMeta();
                // Ensure display name is set if it's a default command block
                if (!meta.hasDisplayName()) {
                    meta.setDisplayName(reward.getRarity().getColor() + "Command Reward");
                }
                // Hide the actual command from players in the preview
                lore = new ArrayList<>(Collections.singletonList("§7???"));
            }
            
            lore.add("");
            lore.add("§r" + reward.getRarity().getDisplayName());
            lore.add(ChatColor.GOLD + "Chance: " + ChatColor.YELLOW + reward.getChance() + "%");
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
            inventory.addItem(displayItem);
        }

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        // This makes the GUI read-only.
        event.setCancelled(true);
    }
}
