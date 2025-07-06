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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

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
        inventory = Bukkit.createInventory(this, 27, "Set New Reward Item");
        
        // --- Instructions and Placeholder ---
        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta pMeta = placeholder.getItemMeta();
        pMeta.setDisplayName(" ");
        placeholder.setItemMeta(pMeta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, placeholder);
        }
        
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Place New Item Here");
        infoMeta.setLore(Arrays.asList("§7Place the item you want to use", "§7from your inventory into the empty slot."));
        info.setItemMeta(infoMeta);
        inventory.setItem(11, info);
        inventory.setItem(15, null); // The empty slot for the user's item

        // --- Control Buttons ---
        ItemStack confirmButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "§lCONFIRM");
        confirmButton.setItemMeta(confirmMeta);

        ItemStack cancelButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "§lCANCEL");
        cancelButton.setItemMeta(cancelMeta);

        inventory.setItem(21, confirmButton);
        inventory.setItem(23, cancelButton);
        
        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        // Allow the player to place/take an item in the designated slot (15)
        if (event.getSlot() == 15 || event.getClickedInventory() == player.getInventory()) {
            return;
        }
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (clicked.getType()) {
            case LIME_STAINED_GLASS_PANE:
                ItemStack newItem = inventory.getItem(15);
                if (newItem == null || newItem.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You must place an item in the slot first!");
                    return;
                }
                
                if (reward.getType() == RewardType.ITEM) {
                    reward.setItem(newItem.clone());
                    player.sendMessage(ChatColor.GREEN + "Reward item updated!");
                } else { // It's a Command reward, so we're setting the preview item
                    reward.setPreviewItem(newItem.clone());
                    player.sendMessage(ChatColor.GREEN + "Preview item updated!");
                }
                
                GachaCrate.getInstance().getCrateManager().saveCrates();
                new RewardEditorMenu(player, crate, reward).open();
                break;
            case RED_STAINED_GLASS_PANE:
                // Return the item to the player if they cancel
                ItemStack itemToReturn = inventory.getItem(15);
                if (itemToReturn != null && itemToReturn.getType() != Material.AIR) {
                    player.getInventory().addItem(itemToReturn);
                }
                new RewardEditorMenu(player, crate, reward).open();
                break;
        }
    }
                }
