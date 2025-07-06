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
        inventory = Bukkit.createInventory(this, 45, "Editing Reward");
        updateItems();
        player.openInventory(inventory);
    }

    /**
     * Refreshes the GUI to show the most current reward settings.
     */
    private void updateItems() {
        inventory.clear();
        
        // Display the item being edited
        ItemStack displayItem;
        if (reward.getType() == RewardType.ITEM) {
            displayItem = reward.getItem().clone();
        } else {
            displayItem = (reward.getPreviewItem() != null) ? reward.getPreviewItem().clone() : new ItemStack(Material.COMMAND_BLOCK);
        }
        inventory.setItem(4, displayItem);

        // --- Control Buttons ---
        ItemStack rarityButton = new ItemStack(Material.NAME_TAG);
        ItemMeta rarityMeta = rarityButton.getItemMeta();
        rarityMeta.setDisplayName(ChatColor.AQUA + "Change Rarity");
        rarityMeta.setLore(Arrays.asList("§7Current: " + reward.getRarity().getDisplayName(), "§e► Click to change."));
        rarityButton.setItemMeta(rarityMeta);

        ItemStack chanceButton = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta chanceMeta = chanceButton.getItemMeta();
        chanceMeta.setDisplayName(ChatColor.GREEN + "Change Chance");
        chanceMeta.setLore(Arrays.asList("§7Current: " + reward.getChance() + "%", "§e► Click to change."));
        chanceButton.setItemMeta(chanceMeta);

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to Reward List");
        backButton.setItemMeta(backMeta);

        inventory.setItem(20, rarityButton);
        inventory.setItem(24, chanceButton);
        inventory.setItem(36, backButton);

        // Add buttons specific to the reward type
        if (reward.getType() == RewardType.ITEM) {
            ItemStack setItemButton = new ItemStack(Material.ANVIL);
            ItemMeta setItemMeta = setItemButton.getItemMeta();
            setItemMeta.setDisplayName(ChatColor.YELLOW + "Set Reward Item");
            setItemMeta.setLore(Arrays.asList("§7Replaces this reward's item", "§7with one you place in a new menu."));
            setItemButton.setItemMeta(setItemMeta);

            boolean isGlowing = reward.getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS);
            ItemStack glowButton = new ItemStack(isGlowing ? Material.LIME_DYE : Material.GRAY_DYE);
            ItemMeta glowMeta = glowButton.getItemMeta();
            glowMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Toggle Glow Effect");
            glowMeta.setLore(Arrays.asList("§7Status: " + (isGlowing ? "§aEnabled" : "§cDisabled")));
            glowButton.setItemMeta(glowMeta);
            
            ItemStack loreButton = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta loreMeta = loreButton.getItemMeta();
            loreMeta.setDisplayName(ChatColor.DARK_AQUA + "Edit Lore");
            loreMeta.setLore(Arrays.asList("§7Add, remove, or clear", "§7the lore of this item."));
            loreButton.setItemMeta(loreMeta);

            inventory.setItem(29, setItemButton);
            inventory.setItem(31, loreButton);
            inventory.setItem(33, glowButton);
        } else { // Command Reward
            ItemStack setPreviewButton = new ItemStack(Material.ITEM_FRAME);
            ItemMeta setPreviewMeta = setPreviewButton.getItemMeta();
            setPreviewMeta.setDisplayName(ChatColor.YELLOW + "Set Preview Item");
            setPreviewMeta.setLore(Arrays.asList("§7Replaces this command's icon", "§7with an item you place in a new menu."));
            setPreviewButton.setItemMeta(setPreviewMeta);
            inventory.setItem(29, setPreviewButton);
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
                cf.withFirstPrompt(new RarityPrompt(crate, reward, this)).buildConversation(player).begin();
                break;
            case EXPERIENCE_BOTTLE:
                player.closeInventory();
                cf.withFirstPrompt(new ChancePrompt(crate, reward, this)).buildConversation(player).begin();
                break;
            case ANVIL: // Set Item for Item Reward
            case ITEM_FRAME: // Set Preview Item for Command Reward
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
        }
    }

    private void toggleGlow() {
        if (reward.getType() != RewardType.ITEM) return;
        
        ItemStack item = reward.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
            meta.removeEnchant(Enchantment.LUCK);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            player.sendMessage(ChatColor.RED + "Glow effect disabled.");
        } else {
            meta.addEnchant(Enchantment.LUCK, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            player.sendMessage(ChatColor.GREEN + "Glow effect enabled.");
        }
        item.setItemMeta(meta);
        GachaCrate.getInstance().getCrateManager().saveCrates();
        updateItems(); // Refresh the menu to show the change
    }
                }
