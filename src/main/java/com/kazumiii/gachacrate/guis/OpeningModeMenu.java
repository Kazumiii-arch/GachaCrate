// ========================================================================
// File: OpeningModeMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/OpeningModeMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.OpeningMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class OpeningModeMenu extends Menu {

    private final Crate crate;

    public OpeningModeMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Opening Mode: " + crate.getName());
        updateItems();
        player.openInventory(inventory);
    }

    private void updateItems() {
        // --- Toggle Button ---
        OpeningMode currentMode = crate.getOpeningMode();
        ItemStack toggleButton = new ItemStack(currentMode == OpeningMode.PHYSICAL_BLOCKS ? Material.CHEST : Material.TRIPWIRE_HOOK);
        ItemMeta toggleMeta = toggleButton.getItemMeta();
        toggleMeta.setDisplayName(ChatColor.GOLD + "Opening Mode");
        toggleMeta.setLore(Arrays.asList(
                "§7Current: §f" + currentMode.getDisplayName(),
                "",
                "§e► Click to switch mode."
        ));
        toggleButton.setItemMeta(toggleMeta);

        // --- Back Button ---
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to Settings");
        backButton.setItemMeta(backMeta);

        inventory.setItem(13, toggleButton);
        inventory.setItem(18, backButton);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        switch (event.getCurrentItem().getType()) {
            case CHEST:
            case TRIPWIRE_HOOK:
                // Toggle the mode
                OpeningMode newMode = crate.getOpeningMode() == OpeningMode.PHYSICAL_BLOCKS ? OpeningMode.KEY_IN_HAND : OpeningMode.PHYSICAL_BLOCKS;
                crate.setOpeningMode(newMode);
                GachaCrate.getInstance().getCrateManager().saveCrates();
                player.sendMessage(ChatColor.GREEN + "Opening mode set to: " + newMode.getDisplayName());
                updateItems(); // Refresh the GUI
                break;
            case ARROW:
                new CrateSettingsMenu(player, crate).open();
                break;
            default:
                break;
        }
    }
}
