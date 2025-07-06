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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDate;
import java.util.Arrays;

public class TimeLimitEditorMenu extends Menu {

    private final Crate crate;

    public TimeLimitEditorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Time-Limit Settings: " + crate.getName());
        updateItems();
        player.openInventory(inventory);
    }

    /**
     * Refreshes the items in the GUI to show the latest settings.
     */
    private void updateItems() {
        // --- Enable/Disable Button ---
        boolean enabled = crate.isTimeLimitEnabled();
        ItemStack toggleButton = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta toggleMeta = toggleButton.getItemMeta();
        toggleMeta.setDisplayName(ChatColor.GOLD + "Time Limit: " + (enabled ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
        toggleMeta.setLore(Arrays.asList(ChatColor.YELLOW + "► Click to toggle."));
        toggleButton.setItemMeta(toggleMeta);

        // --- Start Date Button ---
        ItemStack startDateButton = new ItemStack(Material.OAK_SIGN);
        ItemMeta startMeta = startDateButton.getItemMeta();
        startMeta.setDisplayName(ChatColor.AQUA + "Set Start Date");
        String startDate = crate.getStartDate() != null ? crate.getStartDate().toString() : "Not Set";
        startMeta.setLore(Arrays.asList("§7Current: §f" + startDate, "", ChatColor.YELLOW + "► Click to change."));
        startDateButton.setItemMeta(startMeta);

        // --- End Date Button ---
        ItemStack endDateButton = new ItemStack(Material.DARK_OAK_SIGN);
        ItemMeta endMeta = endDateButton.getItemMeta();
        endMeta.setDisplayName(ChatColor.AQUA + "Set End Date");
        String endDate = crate.getEndDate() != null ? crate.getEndDate().toString() : "Not Set";
        endMeta.setLore(Arrays.asList("§7Current: §f" + endDate, "", ChatColor.YELLOW + "► Click to change."));
        endDateButton.setItemMeta(endMeta);

        // --- Back Button ---
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to Settings");
        backButton.setItemMeta(backMeta);

        inventory.setItem(11, toggleButton);
        inventory.setItem(13, startDateButton);
        inventory.setItem(15, endDateButton);
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
                crate.setTimeLimitEnabled(!crate.isTimeLimitEnabled());
                // Set default dates if enabling for the first time
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
