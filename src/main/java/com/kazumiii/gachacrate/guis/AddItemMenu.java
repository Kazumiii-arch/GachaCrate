// ========================================================================
// File: AddItemMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/AddItemMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.conversation.ChancePrompt;
import com.kazumiii.gachacrate.conversation.RarityPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AddItemMenu extends Menu {

    private final Crate crate;
    // Temporary storage for the reward being built
    private Rarity tempRarity;
    private double tempChance;

    public AddItemMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        // Set default values
        this.tempRarity = GachaCrate.getInstance().getRarityManager().getRarity("COMMON");
        this.tempChance = 100.0;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 45, "Add New Item Reward");
        
        // Instructions and placeholder
        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(" ");
        placeholder.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, placeholder);
        }

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "Place Item Here");
        infoMeta.setLore(Arrays.asList(ChatColor.GRAY + "Place the item you want to add", ChatColor.GRAY + "from your inventory into the empty slot."));
        info.setItemMeta(infoMeta);
        inventory.setItem(13, info);
        inventory.setItem(22, null); // The empty slot for the item

        // Control buttons
        updateButtons();
        
        player.openInventory(inventory);
    }

    private void updateButtons() {
        // Rarity Button
        ItemStack rarityButton = new ItemStack(Material.NAME_TAG);
        ItemMeta rarityMeta = rarityButton.getItemMeta();
        rarityMeta.setDisplayName(ChatColor.AQUA + "Set Rarity");
        rarityMeta.setLore(Arrays.asList(ChatColor.GRAY + "Current: " + tempRarity.getDisplayName(), ChatColor.YELLOW + "► Click to change."));
        rarityButton.setItemMeta(rarityMeta);

        // Chance Button
        ItemStack chanceButton = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta chanceMeta = chanceButton.getItemMeta();
        chanceMeta.setDisplayName(ChatColor.GREEN + "Set Chance");
        chanceMeta.setLore(Arrays.asList(ChatColor.GRAY + "Current: " + tempChance + "%", ChatColor.YELLOW + "► Click to change."));
        chanceButton.setItemMeta(chanceMeta);

        // Confirm Button
        ItemStack confirmButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "CONFIRM & ADD REWARD");
        confirmButton.setItemMeta(confirmMeta);

        // Cancel Button
        ItemStack cancelButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "« Cancel");
        cancelButton.setItemMeta(cancelMeta);

        inventory.setItem(38, rarityButton);
        inventory.setItem(40, chanceButton);
        inventory.setItem(42, confirmButton);
        inventory.setItem(36, cancelButton);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        // Allow player to place an item in the designated slot
        if (event.getSlot() == 22 || event.getClickedInventory() == player.getInventory()) {
            return;
        }
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (clicked.getType()) {
            case NAME_TAG:
                // Logic to change rarity via chat prompt
                break;
            case EXPERIENCE_BOTTLE:
                // Logic to change chance via chat prompt
                break;
            case LIME_STAINED_GLASS_PANE:
                ItemStack rewardItem = inventory.getItem(22);
                if (rewardItem == null || rewardItem.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You must place an item in the slot first!");
                    return;
                }
                Reward newReward = new Reward(rewardItem.clone(), tempRarity, tempChance);
                crate.addReward(newReward);
                GachaCrate.getInstance().getCrateManager().saveCrates();
                player.sendMessage(ChatColor.GREEN + "Successfully added new item reward!");
                new CrateEditorMenu(player, crate).open();
                break;
            case RED_STAINED_GLASS_PANE:
                new RewardTypeSelectionMenu(player, crate).open();
                break;
            default:
                break;
        }
    }
}
