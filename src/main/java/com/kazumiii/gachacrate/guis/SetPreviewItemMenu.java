// ========================================================================
// File: SetPreviewItemMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/SetPreviewItemMenu.java
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

/**
 * Place-an-item flow for setting a command reward's preview icon.
 */
public class SetPreviewItemMenu extends Menu {

    private final Crate crate;
    private final Reward reward;

    public SetPreviewItemMenu(Player player, Crate crate, Reward reward) {
        super(player);
        this.crate = crate;
        this.reward = reward;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Set Preview Icon");

        ItemBuilder.drawBorder(inventory);

        // Instructions
        inventory.setItem(4, new ItemBuilder(Material.PAPER)
                .name(ChatColor.YELLOW + "Place Icon Item Here")
                .blank()
                .lore(ChatColor.GRAY + "Place the item to use as this")
                .lore(ChatColor.GRAY + "reward's icon in the slot below.")
                .build());

        // Current preview item (13) — replaceable
        ItemStack current = reward.getPreviewItem();
        inventory.setItem(13, current != null ? current : null);

        // Confirm / Cancel
        inventory.setItem(21, ItemBuilder.cancelButton());
        inventory.setItem(23, ItemBuilder.confirmButton("Set Icon"));

        ItemBuilder.fillBackground(inventory);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        // Allow the player to place an item in the designated slot (13)
        if (event.getSlot() == 13 || event.getClickedInventory() == player.getInventory()) {
            return;
        }
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (clicked.getType()) {
            case LIME_STAINED_GLASS_PANE:
                ItemStack previewItem = inventory.getItem(13);
                if (previewItem == null || previewItem.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You must place an item in the slot first!");
                    return;
                }
                reward.setPreviewItem(previewItem.clone());
                GachaCrate.getInstance().getCrateManager().saveCrates();
                player.sendMessage(ChatColor.GREEN + "Preview item has been set!");
                new RewardEditorMenu(player, crate, reward).open();
                break;
            case RED_STAINED_GLASS_PANE:
                new RewardEditorMenu(player, crate, reward).open();
                break;
            default:
                break;
        }
    }
}
