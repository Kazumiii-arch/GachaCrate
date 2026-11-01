// ========================================================================
// File: LoreEditorMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/LoreEditorMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.conversation.LoreEditPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Lore editor for item rewards: add, remove last, clear.
 */
public class LoreEditorMenu extends Menu {

    private final Crate crate;
    private final Reward reward;

    public LoreEditorMenu(Player player, Crate crate, Reward reward) {
        super(player);
        this.crate = crate;
        this.reward = reward;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Edit Lore" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA + crate.getDisplayName());
        updateItems();
        player.openInventory(inventory);
    }

    private void updateItems() {
        inventory.clear();
        ItemBuilder.drawBorder(inventory);

        // Display the item being edited
        ItemStack displayItem = reward.getItem() != null ? reward.getItem().clone() : new ItemStack(Material.BARRIER);
        inventory.setItem(4, displayItem);

        // Add line
        inventory.setItem(12, new ItemBuilder(Material.WRITABLE_BOOK)
                .name(ChatColor.GREEN + "Add Lore Line")
                .blank()
                .lore(ChatColor.GRAY + "Appends a line to the bottom")
                .lore(ChatColor.GRAY + "of the item's lore.")
                .build());

        // Remove last line
        inventory.setItem(13, new ItemBuilder(Material.TNT)
                .name(ChatColor.YELLOW + "Remove Last Line")
                .blank()
                .lore(ChatColor.GRAY + "Removes the last line of lore.")
                .build());

        // Clear all
        inventory.setItem(14, new ItemBuilder(Material.LAVA_BUCKET)
                .name(ChatColor.RED + "Clear All Lore")
                .blank()
                .lore(ChatColor.DARK_RED + "Warning: removes all lore lines.")
                .build());

        inventory.setItem(22, ItemBuilder.backButton("Reward editor"));

        ItemBuilder.fillBackground(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (reward.getItem() == null) {
            player.sendMessage(ChatColor.RED + "This reward has no item to edit.");
            return;
        }

        ItemStack rewardItem = reward.getItem();
        ItemMeta meta = rewardItem.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() && meta.getLore() != null
                ? new ArrayList<>(meta.getLore())
                : new ArrayList<>();

        switch (clicked.getType()) {
            case WRITABLE_BOOK:
                player.closeInventory();
                ConversationFactory cf = new ConversationFactory(GachaCrate.getInstance());
                Conversation convo = cf.withFirstPrompt(new LoreEditPrompt(crate, reward, this)).withLocalEcho(false).buildConversation(player);
                convo.begin();
                break;
            case TNT:
                if (!lore.isEmpty()) {
                    lore.remove(lore.size() - 1);
                    meta.setLore(lore);
                    rewardItem.setItemMeta(meta);
                    GachaCrate.getInstance().getCrateManager().saveCrates();
                    player.sendMessage(ChatColor.GREEN + "Last line of lore removed.");
                    updateItems();
                } else {
                    player.sendMessage(ChatColor.RED + "This item has no lore to remove.");
                }
                break;
            case LAVA_BUCKET:
                if (!lore.isEmpty()) {
                    meta.setLore(new ArrayList<>());
                    rewardItem.setItemMeta(meta);
                    GachaCrate.getInstance().getCrateManager().saveCrates();
                    player.sendMessage(ChatColor.GREEN + "All lore has been cleared.");
                    updateItems();
                } else {
                    player.sendMessage(ChatColor.RED + "This item has no lore to clear.");
                }
                break;
            case ARROW:
                new RewardEditorMenu(player, crate, reward).open();
                break;
            default:
                break;
        }
    }
}
