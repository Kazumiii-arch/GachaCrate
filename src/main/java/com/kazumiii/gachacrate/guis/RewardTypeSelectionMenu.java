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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class RewardTypeSelectionMenu extends Menu {

    private final Crate crate;

    public RewardTypeSelectionMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "Select Reward Type");

        ItemStack itemReward = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta itemMeta = itemReward.getItemMeta();
        itemMeta.setDisplayName(ChatColor.AQUA + "Add Item Reward");
        itemMeta.setLore(Arrays.asList(ChatColor.GRAY + "Add a physical item from", ChatColor.GRAY + "your inventory as a reward."));
        itemReward.setItemMeta(itemMeta);

        ItemStack commandReward = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta commandMeta = commandReward.getItemMeta();
        commandMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Add Command Reward");
        commandMeta.setLore(Arrays.asList(ChatColor.GRAY + "Add a console command", ChatColor.GRAY + "as a reward."));
        commandReward.setItemMeta(commandMeta);

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back");
        backButton.setItemMeta(backMeta);

        inventory.setItem(12, itemReward);
        inventory.setItem(14, commandReward);
        inventory.setItem(18, backButton);

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
                new CrateEditorMenu(player, crate).open();
                break;
            default:
                break;
        }
    }
}
