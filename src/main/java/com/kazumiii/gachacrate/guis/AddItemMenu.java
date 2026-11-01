// ========================================================================
// File: AddItemMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/AddItemMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.conversation.ChancePrompt;
import com.kazumiii.gachacrate.conversation.RarityPrompt;
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

/**
 * Place-an-item flow for adding a new item reward to a crate.
 */
public class AddItemMenu extends Menu {

    private final Crate crate;
    // Temporary reward that holds the rarity/chance while the item is configured.
    private final Reward tempReward;

    public AddItemMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        this.tempReward = new Reward(new ItemStack(Material.AIR),
                GachaCrate.getInstance().getRarityManager().getRarity("COMMON"), 100.0);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 45, "§8Add Item Reward" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA + crate.getDisplayName());
        updateItems();
        player.openInventory(inventory);
    }

    private void updateItems() {
        inventory.clear();
        ItemBuilder.drawBorder(inventory);

        // Instructions
        inventory.setItem(4, new ItemBuilder(Material.PAPER)
                .name(ChatColor.YELLOW + "Place Item Here")
                .blank()
                .lore(ChatColor.GRAY + "Put the item you want to add")
                .lore(ChatColor.GRAY + "into the empty slot below.")
                .build());

        // The empty slot (22) — keep whatever the player placed there
        ItemStack userItem = inventory.getItem(22);
        inventory.setItem(22, null);
        if (userItem != null && userItem.getType() != Material.AIR) {
            inventory.setItem(22, userItem);
        }

        // Rarity
        String rarityName = tempReward.getRarity() != null ? tempReward.getRarity().getDisplayName() : "Common";
        inventory.setItem(20, new ItemBuilder(Material.NAME_TAG)
                .name(ChatColor.AQUA + "Set Rarity")
                .blank()
                .lore(ChatColor.GRAY + "Current: " + ChatColor.WHITE + rarityName)
                .blank()
                .lore(ChatColor.YELLOW + "❯ Click to change")
                .build());

        // Chance
        inventory.setItem(24, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name(ChatColor.GREEN + "Set Chance")
                .blank()
                .lore(ChatColor.GRAY + "Current: " + ChatColor.WHITE + tempReward.getChance() + "%")
                .blank()
                .lore(ChatColor.YELLOW + "❯ Click to change")
                .build());

        // Confirm / Cancel
        inventory.setItem(39, ItemBuilder.cancelButton());
        inventory.setItem(41, ItemBuilder.confirmButton("Add Reward"));

        ItemBuilder.fillBackground(inventory);
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
                Reward newReward = new Reward(rewardItem.clone(), tempReward.getRarity(), tempReward.getChance());
                crate.addReward(newReward);
                GachaCrate.getInstance().getCrateManager().saveCrates();
                player.sendMessage(ChatColor.GREEN + "Successfully added new item reward!");
                new CrateEditorMenu(player, crate).open();
                break;
            case RED_STAINED_GLASS_PANE:
                // Return the placed item if cancelling
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
