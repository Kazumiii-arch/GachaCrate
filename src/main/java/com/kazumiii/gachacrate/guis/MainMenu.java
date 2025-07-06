// ========================================================================
// File: MainMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/MainMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.conversation.CrateCreatePrompt;
import com.kazumiii.gachacrate.crates.Crate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
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
        inventory = Bukkit.createInventory(this, 54, "§8GachaCrate Admin Menu");

        // --- Decorative Panes ---
        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(" ");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        placeholder.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, placeholder);
        }
        for (int i = 10; i < 44; i++) { // Clear the middle area for crate items
             if (i % 9 != 0 && i % 9 != 8) inventory.setItem(i, null);
        }

        // --- Crate Items ---
        int slot = 10;
        for (Crate crate : GachaCrate.getInstance().getCrateManager().getAllCrates().values()) {
            if (slot > 43) break; // Prevent overflow if there are too many crates
            if (slot % 9 == 0 || slot % 9 == 8) slot++; // Skip border slots

            ItemStack crateItem = new ItemStack(Material.CHEST);
            ItemMeta crateMeta = crateItem.getItemMeta();
            crateMeta.setDisplayName(ChatColor.AQUA + crate.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Rewards: " + ChatColor.WHITE + crate.getRewards().size());
            lore.add("");
            lore.add(ChatColor.YELLOW + "► Click to manage this crate.");
            crateMeta.setLore(lore);
            crateItem.setItemMeta(crateMeta);
            inventory.setItem(slot, crateItem);
            slot++;
        }

        // --- Control Button ---
        ItemStack createItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta createMeta = createItem.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "✚ Create New Crate");
        createMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to create a new crate."));
        createItem.setItemMeta(createMeta);
        inventory.setItem(49, createItem); // Center bottom slot

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (clickedItem.getType() == Material.NETHER_STAR) {
            // Start the crate creation conversation
            player.closeInventory();
            ConversationFactory cf = new ConversationFactory(GachaCrate.getInstance());
            Conversation convo = cf.withFirstPrompt(new CrateCreatePrompt()).withLocalEcho(false).buildConversation(player);
            convo.begin();
        } else if (clickedItem.getType() == Material.CHEST) {
            Crate crate = GachaCrate.getInstance().getCrateManager().getCrate(itemName);
            if (crate != null) {
                // Open the settings menu for the selected crate
                new CrateSettingsMenu(player, crate).open();
            }
        }
    }
}
