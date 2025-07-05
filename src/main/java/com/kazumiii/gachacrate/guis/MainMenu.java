// ========================================================================
// File: MainMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/MainMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainMenu extends Menu {

    public MainMenu(Player player) {
        super(player);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 54, "GachaCrate Admin Menu");

        // Add an item for each existing crate
        for (Crate crate : GachaCrate.getInstance().getCrateManager().getAllCrates().values()) {
            ItemStack crateItem = new ItemStack(Material.CHEST);
            ItemMeta meta = crateItem.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + crate.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Rewards: " + ChatColor.WHITE + crate.getRewards().size());
            lore.add("");
            lore.add(ChatColor.YELLOW + "► Click to manage this crate.");
            meta.setLore(lore);
            crateItem.setItemMeta(meta);
            inventory.addItem(crateItem);
        }

        // Add a "Create New Crate" button
        ItemStack createItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta createMeta = createItem.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "Create New Crate");
        createMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to create a new crate via chat."));
        createItem.setItemMeta(createMeta);
        inventory.setItem(53, createItem); // Place in the bottom right corner

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (clickedItem.getType() == Material.NETHER_STAR) {
            // TODO: Implement chat-based creation flow using Conversation API
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "This feature is coming soon! For now, use " + ChatColor.GOLD + "/gacha create <name>");
        } else if (clickedItem.getType() == Material.CHEST) {
            Crate crate = GachaCrate.getInstance().getCrateManager().getCrate(itemName);
            if (crate != null) {
                // Open the settings menu for the selected crate
                new CrateSettingsMenu(player, crate).open();
            }
        }
    }
}
