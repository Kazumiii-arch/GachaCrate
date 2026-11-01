// ========================================================================
// File: PityEditorMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/PityEditorMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.conversation.NumberPrompt;
import com.kazumiii.gachacrate.conversation.RarityPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Pity system editor: toggle, threshold, and minimum guaranteed rarity.
 */
public class PityEditorMenu extends Menu {

    private final Crate crate;

    public PityEditorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Pity: " + crate.getDisplayName());
        updateItems();
        player.openInventory(inventory);
    }

    private void updateItems() {
        inventory.clear();
        ItemBuilder.drawBorder(inventory);

        boolean enabled = crate.isPityEnabled();

        // Header
        inventory.setItem(4, new ItemBuilder(Material.DIAMOND)
                .name(ChatColor.LIGHT_PURPLE + "Pity System")
                .blank()
                .lore(ChatColor.GRAY + "Status: " + (enabled ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"))
                .build());

        // Toggle
        inventory.setItem(11, new ItemBuilder(enabled ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(ChatColor.GOLD + "Toggle Pity")
                .blank()
                .lore(enabled
                        ? ChatColor.GREEN + "Currently: ENABLED"
                        : ChatColor.RED + "Currently: DISABLED")
                .lore(ChatColor.YELLOW + "❯ Click to toggle")
                .build());

        // Threshold
        inventory.setItem(13, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name(ChatColor.AQUA + "Pity Threshold")
                .blank()
                .lore(ChatColor.GRAY + "Opens until a guaranteed win: " + ChatColor.WHITE + crate.getPityThreshold())
                .blank()
                .lore(ChatColor.YELLOW + "❯ Click to change")
                .build());

        // Rarity
        String rarityName = crate.getPityRarityMinimum() != null
                ? crate.getPityRarityMinimum()
                : "Not set";
        inventory.setItem(15, new ItemBuilder(Material.NAME_TAG)
                .name(ChatColor.LIGHT_PURPLE + "Minimum Rarity")
                .blank()
                .lore(ChatColor.GRAY + "Guaranteed at or above: " + ChatColor.WHITE + rarityName)
                .blank()
                .lore(ChatColor.YELLOW + "❯ Click to change")
                .build());

        inventory.setItem(22, ItemBuilder.backButton("Settings"));

        ItemBuilder.fillBackground(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        ConversationFactory cf = new ConversationFactory(GachaCrate.getInstance());

        switch (event.getCurrentItem().getType()) {
            case LIME_DYE:
            case GRAY_DYE:
                crate.setPityEnabled(!crate.isPityEnabled());
                GachaCrate.getInstance().getCrateManager().saveCrates();
                updateItems();
                break;
            case EXPERIENCE_BOTTLE:
                player.closeInventory();
                Conversation thresholdConvo = cf.withFirstPrompt(new NumberPrompt("pity-threshold", crate, this)).withLocalEcho(false).buildConversation(player);
                thresholdConvo.begin();
                break;
            case NAME_TAG:
                player.closeInventory();
                Conversation rarityConvo = cf.withFirstPrompt(new RarityPrompt("pity-rarity", crate, this)).withLocalEcho(false).buildConversation(player);
                rarityConvo.begin();
                break;
            case ARROW:
                new CrateSettingsMenu(player, crate).open();
                break;
            default:
                break;
        }
    }
}
