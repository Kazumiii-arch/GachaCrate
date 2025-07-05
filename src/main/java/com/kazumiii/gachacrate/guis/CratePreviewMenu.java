// ========================================================================
// File: CratePreviewMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/CratePreviewMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CratePreviewMenu extends Menu {

    private final Crate crate;

    public CratePreviewMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        // Create an inventory with 54 slots, titled with the crate's name.
        inventory = Bukkit.createInventory(this, 54, "Preview: " + crate.getName());

        // Populate the inventory with formatted reward items.
        for (Reward reward : crate.getRewards()) {
            ItemStack displayItem = reward.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                // Get existing lore or create a new list if none exists.
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(""); // Add a blank line for spacing
                lore.add("§r" + reward.getRarity().getDisplayName());
                lore.add(ChatColor.GOLD + "Chance: " + ChatColor.YELLOW + reward.getChance() + "%");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
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
