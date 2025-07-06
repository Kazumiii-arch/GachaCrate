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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AddItemMenu extends Menu {

    private final Crate crate;
    // We create a temporary reward object to hold the settings as we build it.
    private final Reward tempReward;

    public AddItemMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        // Create a dummy reward to be configured. The item will be added later.
        // We initialize it with default values.
        this.tempReward = new Reward(new ItemStack(Material.AIR), GachaCrate.getInstance().getRarityManager().getRarity("COMMON"), 100.0);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 45, "Add New Item Reward");
        updateItems();
        player.openInventory(inventory);
    }

    /**
     * Refreshes the items in the GUI, especially the buttons with dynamic lore.
     */
    private void updateItems() {
        // --- Instructions and Placeholder ---
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
        // Only clear the slot if it's not already holding the user's item
        if (inventory.getItem(22) == null) {
            inventory.setItem(22, null);
        }

        // --- Control Buttons ---
        ItemStack rarityButton = new ItemStack(Material.NAME_TAG);
        ItemMeta rarityMeta = rarityButton.getItemMeta();
        rarityMeta.setDisplayName(ChatColor.AQUA + "Set Rarity");
        rarityMeta.setLore(Arrays.asList(ChatColor.GRAY + "Current: " + tempReward.getRarity().getDisplayName(), ChatColor.YELLOW + "► Click to change."));
        rarityButton.setItemMeta(rarityMeta);

        ItemStack chanceButton = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta chanceMeta = chanceButton.getItemMeta();
        chanceMeta.setDisplayName(ChatColor.GREEN + "Set Chance");
        chanceMeta.setLore(Arrays.asList(ChatColor.GRAY + "Current: " + tempReward.getChance() + "%", ChatColor.YELLOW + "► Click to change."));
        chanceButton.setItemMeta(chanceMeta);

        ItemStack confirmButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "CONFIRM & ADD REWARD");
        confirmButton.setItemMeta(confirmMeta);

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
        // Allow the player to place/take an item in the designated slot (22)
        if (event.getSlot() == 22 || event.getClickedInventory() == player.getInventory()) {
            return;
        }
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ConversationFactory cf = new ConversationFactory(GachaCrate.getInstance());

        switch (clicked.getType()) {
            case NAME_TAG:
                player.closeInventory();
                Conversation rarityConvo = cf.withFirstPrompt(new RarityPrompt(crate, tempReward, this)).withLocalEcho(false).buildConversation(player);
                rarityConvo.begin();
                break;
            case EXPERIENCE_BOTTLE:
                player.closeInventory();
                Conversation chanceConvo = cf.withFirstPrompt(new ChancePrompt(crate, tempReward, this)).withLocalEcho(false).buildConversation(player);
                chanceConvo.begin();
                break;
            case LIME_STAINED_GLASS_PANE:
                ItemStack rewardItem = inventory.getItem(22);
                if (rewardItem == null || rewardItem.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You must place an item in the slot first!");
                    return;
                }
                // Create the final reward with the item from the slot and the configured settings
                Reward newReward = new Reward(rewardItem.clone(), tempReward.getRarity(), tempReward.getChance());
                crate.addReward(newReward);
                GachaCrate.getInstance().getCrateManager().saveCrates();
                player.sendMessage(ChatColor.GREEN + "Successfully added new item reward!");
                new CrateEditorMenu(player, crate).open();
                break;
            case RED_STAINED_GLASS_PANE:
                // Return the item to the player if they cancel
                ItemStack itemToReturn = inventory.getItem(22);
                if (itemToReturn != null && itemToReturn.getType() != Material.AIR) {
                    player.getInventory().addItem(itemToReturn);
                }
                new RewardTypeSelectionMenu(player, crate).open();
                break;
            default:
                break;
        }
    }
                                                           }
