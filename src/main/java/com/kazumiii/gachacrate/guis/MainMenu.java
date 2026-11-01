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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin dashboard: lists all crates, lets the admin jump into any crate's
 * settings or create a new one.
 */
public class MainMenu extends Menu {

    public MainMenu(Player player) {
        super(player);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 54, "§8GachaCrate " + ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "Crates");

        ItemBuilder.drawBorder(inventory);

        // Header item — quick stats
        int crateCount = GachaCrate.getInstance().getCrateManager().getAllCrates().size();
        ItemStack header = new ItemBuilder(Material.BOOK)
                .name(ChatColor.GOLD + "GachaCrate Dashboard")
                .blank()
                .lore(ChatColor.GRAY + "Crates: " + ChatColor.WHITE + crateCount)
                .build();
        inventory.setItem(4, header);

        // --- Crate list in the inner area ---
        int slot = 10;
        for (Crate crate : GachaCrate.getInstance().getCrateManager().getAllCrates().values()) {
            if (slot > 43) break;
            if (slot % 9 == 0 || slot % 9 == 8) slot++;

            int rewardCount = crate.getRewards().size();
            String mode = crate.getOpeningMode().getDisplayName();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Rewards: " + ChatColor.WHITE + rewardCount);
            lore.add(ChatColor.GRAY + "Mode: " + ChatColor.WHITE + mode);
            lore.add(ChatColor.GRAY + "Animation: " + ChatColor.WHITE + crate.getAnimationType().getDisplayName());
            if (crate.isPityEnabled()) {
                lore.add(ChatColor.LIGHT_PURPLE + "Pity: " + ChatColor.WHITE + crate.getPityThreshold());
            }
            if (crate.isTimeLimitEnabled()) {
                lore.add(ChatColor.GOLD + "Limited: " + ChatColor.WHITE + crate.getStartDate() + " → " + crate.getEndDate());
            }
            lore.add("");
            lore.add(ChatColor.YELLOW + "❯ Click to manage this crate");

            ItemStack crateItem = new ItemBuilder(Material.CHEST)
                    .name(ChatColor.AQUA + crate.getDisplayName())
                    .loreAll(lore)
                    .build();
            inventory.setItem(slot, crateItem);
            slot++;
        }

        // --- Control buttons ---
        inventory.setItem(48, new ItemBuilder(Material.COMPARATOR)
                .name(ChatColor.GOLD + "Plugin Settings")
                .lore(ChatColor.GRAY + "Edit global plugin settings.")
                .build());
        inventory.setItem(50, new ItemBuilder(Material.NETHER_STAR)
                .name(ChatColor.GREEN + "✚ Create New Crate")
                .lore(ChatColor.GRAY + "Click to create a new crate.")
                .build());

        ItemBuilder.fillBackground(inventory);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        switch (clickedItem.getType()) {
            case NETHER_STAR:
                player.closeInventory();
                ConversationFactory cf = new ConversationFactory(GachaCrate.getInstance());
                Conversation convo = cf.withFirstPrompt(new CrateCreatePrompt()).withLocalEcho(false).buildConversation(player);
                convo.begin();
                break;
            case CHEST:
                String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                Crate crate = GachaCrate.getInstance().getCrateManager().getCrate(itemName);
                if (crate != null) {
                    new CrateSettingsMenu(player, crate).open();
                }
                break;
            case COMPARATOR:
                player.sendMessage(ChatColor.YELLOW + "Global plugin settings coming soon!");
                break;
            default:
                break;
        }
    }
}
