// ========================================================================
// File: TimeLimitEditorMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/TimeLimitEditorMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.conversation.DatePrompt;
import com.kazumiii.gachacrate.crates.Crate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.time.LocalDate;

/**
 * Time limit editor: toggle plus start/end dates for seasonal crates.
 */
public class TimeLimitEditorMenu extends Menu {

    private final Crate crate;

    public TimeLimitEditorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Time Limit: " + crate.getDisplayName());
        updateItems();
        player.openInventory(inventory);
    }

    private void updateItems() {
        inventory.clear();
        ItemBuilder.drawBorder(inventory);

        boolean enabled = crate.isTimeLimitEnabled();

        // Header
        inventory.setItem(4, new ItemBuilder(Material.CLOCK)
                .name(ChatColor.GOLD + "Time Limit")
                .blank()
                .lore(ChatColor.GRAY + "Status: " + (enabled ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"))
                .build());

        // Toggle
        inventory.setItem(11, new ItemBuilder(enabled ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(ChatColor.GOLD + "Toggle Time Limit")
                .blank()
                .lore(enabled
                        ? ChatColor.GREEN + "Currently: ENABLED"
                        : ChatColor.RED + "Currently: DISABLED")
                .lore(ChatColor.YELLOW + "❯ Click to toggle")
                .build());

        // Start date
        String startDate = crate.getStartDate() != null ? crate.getStartDate().toString() : "Not set";
        inventory.setItem(13, new ItemBuilder(Material.OAK_SIGN)
                .name(ChatColor.AQUA + "Start Date")
                .blank()
                .lore(ChatColor.GRAY + "Current: " + ChatColor.WHITE + startDate)
                .blank()
                .lore(ChatColor.YELLOW + "❯ Click to change")
                .build());

        // End date
        String endDate = crate.getEndDate() != null ? crate.getEndDate().toString() : "Not set";
        inventory.setItem(15, new ItemBuilder(Material.DARK_OAK_SIGN)
                .name(ChatColor.AQUA + "End Date")
                .blank()
                .lore(ChatColor.GRAY + "Current: " + ChatColor.WHITE + endDate)
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
                crate.setTimeLimitEnabled(!crate.isTimeLimitEnabled());
                if (crate.isTimeLimitEnabled() && crate.getStartDate() == null) {
                    crate.setStartDate(LocalDate.now());
                    crate.setEndDate(LocalDate.now().plusDays(7));
                }
                GachaCrate.getInstance().getCrateManager().saveCrates();
                updateItems();
                break;
            case OAK_SIGN:
                player.closeInventory();
                Conversation startConvo = cf.withFirstPrompt(new DatePrompt("start-date", crate, this)).withLocalEcho(false).buildConversation(player);
                startConvo.begin();
                break;
            case DARK_OAK_SIGN:
                player.closeInventory();
                Conversation endConvo = cf.withFirstPrompt(new DatePrompt("end-date", crate, this)).withLocalEcho(false).buildConversation(player);
                endConvo.begin();
                break;
            case ARROW:
                new CrateSettingsMenu(player, crate).open();
                break;
            default:
                break;
        }
    }
}
