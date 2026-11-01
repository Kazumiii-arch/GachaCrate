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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Editor for a single reward: rarity, chance, item/preview, lore and glow.
 */
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
        inventory = Bukkit.createInventory(this, 45, "§8Editing Reward" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA + crate.getDisplayName());
        updateItems();
        player.openInventory(inventory);
    }

    private void updateItems() {
        inventory.clear();
        ItemBuilder.drawBorder(inventory);

        // Display the item being edited
        ItemStack displayItem;
        if (reward.getType() == RewardType.ITEM && reward.getItem() != null) {
            displayItem = reward.getItem().clone();
        } else {
            displayItem = reward.getPreviewItem() != null
                    ? reward.getPreviewItem().clone()
                    : new ItemStack(Material.COMMAND_BLOCK);
        }
        inventory.setItem(4, displayItem);

        // Rarity
        String rarityName = reward.getRarity() != null ? reward.getRarity().getDisplayName() : "Unknown";
        inventory.setItem(19, new ItemBuilder(Material.NAME_TAG)
                .name(ChatColor.AQUA + "Rarity")
                .blank()
                .lore(ChatColor.GRAY + "Current: " + ChatColor.WHITE + rarityName)
                .blank()
                .lore(ChatColor.YELLOW + "❯ Click to change")
                .build());

        // Chance
        inventory.setItem(21, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name(ChatColor.GREEN + "Drop Chance")
                .blank()
                .lore(ChatColor.GRAY + "Current: " + ChatColor.WHITE + reward.getChance() + "%")
                .blank()
                .lore(ChatColor.YELLOW + "❯ Click to change")
                .build());

        // Back
        inventory.setItem(36, ItemBuilder.backButton("Reward list"));

        // Type-specific buttons
        if (reward.getType() == RewardType.ITEM && reward.getItem() != null) {
            boolean isGlowing = reward.getItem().getItemMeta() != null
                    && reward.getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS);

            inventory.setItem(25, new ItemBuilder(Material.ANVIL)
                    .name(ChatColor.YELLOW + "Set Reward Item")
                    .blank()
                    .lore(ChatColor.GRAY + "Replace this reward's item")
                    .lore(ChatColor.GRAY + "with one from your inventory.")
                    .build());

            inventory.setItem(30, new ItemBuilder(Material.WRITABLE_BOOK)
                    .name(ChatColor.DARK_AQUA + "Edit Lore")
                    .blank()
                    .lore(ChatColor.GRAY + "Add or remove lore lines")
                    .lore(ChatColor.GRAY + "on this item.")
                    .build());

            inventory.setItem(32, new ItemBuilder(isGlowing ? Material.LIME_DYE : Material.GRAY_DYE)
                    .name(ChatColor.LIGHT_PURPLE + "Glow Effect")
                    .blank()
                    .lore(ChatColor.GRAY + "Status: " + (isGlowing
                            ? ChatColor.GREEN + "Enabled"
                            : ChatColor.RED + "Disabled"))
                    .blank()
                    .lore(ChatColor.YELLOW + "❯ Click to toggle")
                    .build());
        } else {
            inventory.setItem(25, new ItemBuilder(Material.ITEM_FRAME)
                    .name(ChatColor.YELLOW + "Set Preview Item")
                    .blank()
                    .lore(ChatColor.GRAY + "Replace this command's icon")
                    .lore(ChatColor.GRAY + "with an item of your choice.")
                    .build());
        }

        ItemBuilder.fillBackground(inventory);
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
                cf.withFirstPrompt(new RarityPrompt(crate, reward, this)).buildConversation(player).begin();
                break;
            case EXPERIENCE_BOTTLE:
                player.closeInventory();
                cf.withFirstPrompt(new ChancePrompt(crate, reward, this)).buildConversation(player).begin();
                break;
            case ANVIL:
            case ITEM_FRAME:
                new SetRewardItemMenu(player, crate, reward).open();
                break;
            case WRITABLE_BOOK:
                new LoreEditorMenu(player, crate, reward).open();
                break;
            case LIME_DYE:
            case GRAY_DYE:
                toggleGlow();
                break;
            case ARROW:
                new CrateEditorMenu(player, crate).open();
                break;
            default:
                break;
        }
    }

    private void toggleGlow() {
        if (reward.getType() != RewardType.ITEM || reward.getItem() == null) return;

        ItemStack item = reward.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Enchantment.LUCK is the 1.20.1 constant (LUCK_OF_THE_SEA only exists in 1.20.5+).
        if (meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
            meta.removeEnchant(Enchantment.LUCK);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            player.sendMessage(ChatColor.RED + "Glow effect disabled.");
        } else {
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            player.sendMessage(ChatColor.GREEN + "Glow effect enabled.");
        }
        item.setItemMeta(meta);
        GachaCrate.getInstance().getCrateManager().saveCrates();
        updateItems();
    }
}
