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
import java.util.Arrays;
import java.util.List;

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
        inventory = Bukkit.createInventory(this, 27, "Edit Item Lore");
        updateItems();
        player.openInventory(inventory);
    }

    /**
     * Refreshes the items in the GUI, especially the item being edited.
     */
    private void updateItems() {
        inventory.clear(); // Clear the menu before redrawing to reflect changes

        // Display the item being edited in the center
        ItemStack displayItem = reward.getItem().clone();
        inventory.setItem(4, displayItem);

        // --- Control Buttons ---
        ItemStack addLineButton = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta addMeta = addLineButton.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "Add Lore Line");
        addMeta.setLore(Arrays.asList(ChatColor.GRAY + "Adds a new line to the", ChatColor.GRAY + "bottom of the item's lore."));
        addLineButton.setItemMeta(addMeta);

        ItemStack removeLineButton = new ItemStack(Material.TNT);
        ItemMeta removeMeta = removeLineButton.getItemMeta();
        removeMeta.setDisplayName(ChatColor.YELLOW + "Remove Last Line");
        removeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Removes the last line", ChatColor.GRAY + "from the item's lore."));
        removeLineButton.setItemMeta(removeMeta);

        ItemStack clearLoreButton = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta clearMeta = clearLoreButton.getItemMeta();
        clearMeta.setDisplayName(ChatColor.RED + "Clear All Lore");
        clearMeta.setLore(Arrays.asList(ChatColor.DARK_RED + "Warning: This removes all", ChatColor.DARK_RED + "lines of lore from the item."));
        clearLoreButton.setItemMeta(clearMeta);

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "« Back to Reward Editor");
        backButton.setItemMeta(backMeta);

        inventory.setItem(12, addLineButton);
        inventory.setItem(13, removeLineButton);
        inventory.setItem(14, clearLoreButton);
        inventory.setItem(18, backButton);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemStack rewardItem = reward.getItem();
        ItemMeta meta = rewardItem.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

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
                    updateItems(); // Refresh the GUI to show the change
                } else {
                    player.sendMessage(ChatColor.RED + "This item has no lore to remove.");
                }
                break;
            case LAVA_BUCKET:
                if (!lore.isEmpty()) {
                    meta.setLore(new ArrayList<>());
                    rewardItem.setItemMeta(meta);
                    GachaCrate.getInstance().getCrateManager().saveCrates();
                    player.sendMessage(ChatColor.GREEN + "All lore has been cleared from the item.");
                    updateItems(); // Refresh the GUI
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
