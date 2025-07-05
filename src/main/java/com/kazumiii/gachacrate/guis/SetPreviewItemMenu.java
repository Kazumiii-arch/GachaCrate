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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

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
        inventory = Bukkit.createInventory(this, 27, "Set Preview Item");

        // Instructions and placeholder slot
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Place Item Here");
        infoMeta.setLore(Arrays.asList(ChatColor.GRAY + "Place the item you want to use", ChatColor.GRAY + "as a preview from your inventory."));
        info.setItemMeta(infoMeta);
        inventory.setItem(4, info);
        inventory.setItem(13, reward.getPreviewItem()); // Show current preview item

        // Confirm Button
        ItemStack confirmButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "CONFIRM");
        confirmButton.setItemMeta(confirmMeta);

        // Cancel Button
        ItemStack cancelButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "« Cancel");
        cancelButton.setItemMeta(cancelMeta);

        inventory.setItem(21, confirmButton);
        inventory.setItem(23, cancelButton);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        // Allow player to place an item in the designated slot
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
