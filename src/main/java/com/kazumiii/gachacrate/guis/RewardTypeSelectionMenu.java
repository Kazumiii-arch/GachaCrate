// ========================================================================
// File: RewardTypeSelectionMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/RewardTypeSelectionMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.conversation.CommandPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Choose between adding an item reward or a command reward.
 */
public class RewardTypeSelectionMenu extends Menu {

    private final Crate crate;

    public RewardTypeSelectionMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Add Reward" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA + crate.getDisplayName());

        ItemBuilder.drawBorder(inventory);

        // Header
        inventory.setItem(4, new ItemBuilder(Material.PAPER)
                .name(ChatColor.GOLD + "Choose Reward Type")
                .blank()
                .lore(ChatColor.GRAY + "Pick what kind of reward to add.")
                .build());

        // Item reward
        inventory.setItem(12, new ItemBuilder(Material.DIAMOND_SWORD)
                .name(ChatColor.AQUA + "Item Reward")
                .blank()
                .lore(ChatColor.GRAY + "A physical item from your")
                .lore(ChatColor.GRAY + "inventory, given on win.")
                .blank()
                .lore(ChatColor.YELLOW + "❯ Click to select")
                .build());

        // Command reward
        inventory.setItem(14, new ItemBuilder(Material.COMMAND_BLOCK)
                .name(ChatColor.LIGHT_PURPLE + "Command Reward")
                .blank()
                .lore(ChatColor.GRAY + "A console command executed")
                .lore(ChatColor.GRAY + "when this reward is won.")
                .lore(ChatColor.GRAY + "Use " + ChatColor.WHITE + "{player}" + ChatColor.GRAY + " for the name.")
                .blank()
                .lore(ChatColor.YELLOW + "❯ Click to select")
                .build());

        inventory.setItem(22, ItemBuilder.backButton("Reward list"));

        ItemBuilder.fillBackground(inventory);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (clicked.getType()) {
            case DIAMOND_SWORD:
                new AddItemMenu(player, crate).open();
                break;
            case COMMAND_BLOCK:
                player.closeInventory();
                ConversationFactory cf = new ConversationFactory(GachaCrate.getInstance());
                Conversation convo = cf.withFirstPrompt(new CommandPrompt(crate)).withLocalEcho(false).buildConversation(player);
                convo.begin();
                break;
            case BARRIER:
            case ARROW:
                new CrateEditorMenu(player, crate).open();
                break;
            default:
                break;
        }
    }
}
