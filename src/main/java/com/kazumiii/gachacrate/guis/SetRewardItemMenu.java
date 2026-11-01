// ========================================================================
// File: SetRewardItemMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/SetRewardItemMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.crates.RewardType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Place-an-item flow for replacing a reward's item (or a command reward's
 * preview icon).
 */
public class SetRewardItemMenu extends Menu {

    private final Crate crate;
    private final Reward reward;

    public SetRewardItemMenu(Player player, Crate crate, Reward reward) {
        super(player);
        this.crate = crate;
        this.reward = reward;
    }

    @Override
    public void open() {
        boolean isItemReward = reward.getType() == RewardType.ITEM;
        inventory = Bukkit.createInventory(this, 27,
                isItemReward ? "§8Set Reward Item" : "§8Set Preview Icon");

        ItemBuilder.drawBorder(inventory);

        // Instructions
        inventory.setItem(4, new ItemBuilder(Material.PAPER)
                .name(ChatColor.YELLOW + "Place New Item Here")
                .blank()
                .lore(isItemReward
                        ? ChatColor.GRAY + "Replace the reward's item"
                        : ChatColor.GRAY + "Replace the command's icon")
                .lore(ChatColor.GRAY + "in the empty slot below, then confirm.")
                .build());

        // The empty slot for the user's item (13)
        inventory.setItem(13, null);

        // Confirm / Cancel
        inventory.setItem(21, ItemBuilder.cancelButton());
        inventory.setItem(23, ItemBuilder.confirmButton("Set Item"));

        ItemBuilder.fillBackground(inventory);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        // Allow the player to place/take an item in the designated slot (13)
        if (event.getSlot() == 13 || event.getClickedInventory() == player.getInventory()) {
            return;
        }
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (clicked.getType()) {
            case LIME_STAINED_GLASS_PANE:
                ItemStack newItem = inventory.getItem(13);
                if (newItem == null || newItem.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You must place an item in the slot first!");
                    return;
                }

                if (reward.getType() == RewardType.ITEM) {
                    reward.setItem(newItem.clone());
                    player.sendMessage(ChatColor.GREEN + "Reward item updated!");
                } else {
                    reward.setPreviewItem(newItem.clone());
                    player.sendMessage(ChatColor.GREEN + "Preview item updated!");
                }

                GachaCrate.getInstance().getCrateManager().saveCrates();
                new RewardEditorMenu(player, crate, reward).open();
                break;
            case RED_STAINED_GLASS_PANE:
                // Return the placed item if cancelling
                ItemStack itemToReturn = inventory.getItem(13);
                if (itemToReturn != null && itemToReturn.getType() != Material.AIR) {
                    player.getInventory().addItem(itemToReturn);
                }
                new RewardEditorMenu(player, crate, reward).open();
                break;
            default:
                break;
        }
    }
}
