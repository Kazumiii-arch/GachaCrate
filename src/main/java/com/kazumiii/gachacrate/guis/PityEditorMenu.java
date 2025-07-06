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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PityEditorMenu extends Menu {

    private final Crate crate;

    public PityEditorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Pity Settings: " + crate.getName());
        updateItems();
        player.openInventory(inventory);
    }

    /**
     * Refreshes the items in the GUI to show the latest settings.
     */
    private void updateItems() {
        // --- Enable/Disable Button ---
        boolean enabled = crate.isPityEnabled();
        ItemStack toggleButton = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta toggleMeta = toggleButton.getItemMeta();
        toggleMeta.setDisplayName(ChatColor.GOLD + "Pity System: " + (enabled ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
        toggleMeta.setLore(Arrays.asList(ChatColor.YELLOW + "► Click to toggle."));
        toggleButton.setItemMeta(toggleMeta);

        // --- Threshold Button ---
        ItemStack thresholdButton = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta thresholdMeta = thresholdButton.getItemMeta();
        thresholdMeta.setDisplayName(ChatColor.AQUA + "Set Pity Threshold");
        thresholdMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Opens until pity is triggered.",
                "§7Current: §f" + crate.getPityThreshold(),
                "",
                ChatColor.YELLOW + "► Click to change."
        ));
        thresholdButton.setItemMeta(thresholdMeta);

        // --- Rarity Button ---
        ItemStack rarityButton = new ItemStack(Material.NAME_TAG);
        ItemMeta rarityMeta = rarityButton.getItemMeta();
        rarityMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Set Minimum Pity Rarity");
        String rarityName = crate.getPityRarityMinimum() != null ? crate.getPityRarityMinimum() : "Not Set";
        rarityMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "The minimum rarity guaranteed.",
                "§7Current: §f" + rarityName,
                "",
                ChatColor.YELLOW + "► Click to change."
        ));
        rarityButton.setItemMeta(rarityMeta);

        // --- Back Button ---
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to Settings");
        backButton.setItemMeta(backMeta);

        inventory.setItem(11, toggleButton);
        inventory.setItem(13, thresholdButton);
        inventory.setItem(15, rarityButton);
        inventory.setItem(18, backButton);
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
                updateItems(); // Refresh the GUI to show the change
                break;
            case EXPERIENCE_BOTTLE:
                player.closeInventory();
                Conversation thresholdConvo = cf.withFirstPrompt(new NumberPrompt("pity-threshold", crate, this)).withLocalEcho(false).buildConversation(player);
                thresholdConvo.begin();
                break;
            case NAME_TAG:
                player.closeInventory();
                // Note: The RarityPrompt needs to know which menu to re-open on completion/cancellation.
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
