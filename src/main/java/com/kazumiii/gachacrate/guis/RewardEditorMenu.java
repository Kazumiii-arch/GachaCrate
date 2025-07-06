// ========================================================================
// File: RewardEditorMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/RewardEditorMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.conversation.ChancePrompt;
import com.kazumiii.gachacrate.conversation.RarityPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.crates.RewardType;
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

public class RewardEditorMenu extends Menu {

    private final Crate crate;
    private final Reward reward;

    public RewardEditorMenu(Player player, Crate crate, Reward reward) {
        super(player);
        this.crate = crate;
        this.reward = reward;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "Edit Reward");

        ItemStack displayItem;
        if (reward.getType() == RewardType.ITEM) {
            displayItem = reward.getItem().clone();
        } else {
            if (reward.getPreviewItem() != null) {
                displayItem = reward.getPreviewItem().clone();
            } else {
                displayItem = new ItemStack(Material.COMMAND_BLOCK);
            }
        }
        ItemMeta displayMeta = displayItem.getItemMeta();
        displayMeta.setDisplayName(ChatColor.YELLOW + "Editing Reward");
        displayItem.setItemMeta(displayMeta);

        ItemStack rarityButton = new ItemStack(Material.NAME_TAG);
        ItemMeta rarityMeta = rarityButton.getItemMeta();
        rarityMeta.setDisplayName(ChatColor.AQUA + "Change Rarity");
        rarityMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current: " + reward.getRarity().getDisplayName(),
                "",
                ChatColor.YELLOW + "► Click to change."
        ));
        rarityButton.setItemMeta(rarityMeta);

        ItemStack chanceButton = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta chanceMeta = chanceButton.getItemMeta();
        chanceMeta.setDisplayName(ChatColor.GREEN + "Change Chance");
        chanceMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current: " + reward.getChance() + "%",
                "",
                ChatColor.YELLOW + "► Click to change."
        ));
        chanceButton.setItemMeta(chanceMeta);
        
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to Reward List");
        backButton.setItemMeta(backMeta);

        inventory.setItem(4, displayItem);
        inventory.setItem(12, rarityButton);
        inventory.setItem(14, chanceButton);
        inventory.setItem(18, backButton);

        if (reward.getType() == RewardType.COMMAND) {
            ItemStack previewButton = new ItemStack(Material.ITEM_FRAME);
            ItemMeta previewMeta = previewButton.getItemMeta();
            previewMeta.setDisplayName(ChatColor.YELLOW + "Set Preview Item");
            previewMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Set the item that represents",
                    ChatColor.GRAY + "this command in menus.",
                    "",
                    ChatColor.YELLOW + "► Click to change."
            ));
            previewButton.setItemMeta(previewMeta);
            inventory.setItem(16, previewButton);
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ConversationFactory cf = new ConversationFactory(GachaCrate.getInstance());

        switch (clicked.getType()) {
            case NAME_TAG:
                player.closeInventory();
                Conversation rarityConvo = cf.withFirstPrompt(new RarityPrompt(crate, reward, this)).withLocalEcho(false).buildConversation(player);
                rarityConvo.begin();
                break;
            case EXPERIENCE_BOTTLE:
                player.closeInventory();
                // This now correctly passes the parent menu 'this' to the constructor
                Conversation chanceConvo = cf.withFirstPrompt(new ChancePrompt(crate, reward, this)).withLocalEcho(false).buildConversation(player);
                chanceConvo.begin();
                break;
            case ITEM_FRAME:
                new SetPreviewItemMenu(player, crate, reward).open();
                break;
            case BARRIER:
                new CrateEditorMenu(player, crate).open();
                break;
            default:
                break;
        }
    }
}
