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

/**
 * Opening mode editor: key-in-hand vs physical block.
 */
public class OpeningModeMenu extends Menu {

    private final Crate crate;

    public OpeningModeMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Opening Mode: " + crate.getDisplayName());
        updateItems();
        player.openInventory(inventory);
    }

    private void updateItems() {
        inventory.clear();
        ItemBuilder.drawBorder(inventory);

        OpeningMode currentMode = crate.getOpeningMode();

        // Header
        inventory.setItem(4, new ItemBuilder(Material.ENDER_EYE)
                .name(ChatColor.YELLOW + "Opening Mode")
                .blank()
                .lore(ChatColor.GRAY + "Current: " + ChatColor.WHITE + currentMode.getDisplayName())
                .build());

        // The two options, with glow on the active one
        inventory.setItem(11, new ItemBuilder(Material.TRIPWIRE_HOOK)
                .name(ChatColor.AQUA + "Key in Hand")
                .blank()
                .lore(ChatColor.GRAY + "Players right-click a key")
                .lore(ChatColor.GRAY + "item to open the crate.")
                .blank()
                .lore(currentMode == OpeningMode.KEY_IN_HAND
                        ? ChatColor.GREEN + "✔ Currently selected"
                        : ChatColor.YELLOW + "❯ Click to select")
                .glowIf(currentMode == OpeningMode.KEY_IN_HAND)
                .build());

        inventory.setItem(15, new ItemBuilder(Material.CHEST)
                .name(ChatColor.AQUA + "Physical Block")
                .blank()
                .lore(ChatColor.GRAY + "Players right-click a placed")
                .lore(ChatColor.GRAY + "crate block with a key.")
                .blank()
                .lore(currentMode == OpeningMode.PHYSICAL_BLOCKS
                        ? ChatColor.GREEN + "✔ Currently selected"
                        : ChatColor.YELLOW + "❯ Click to select")
                .glowIf(currentMode == OpeningMode.PHYSICAL_BLOCKS)
                .build());

        inventory.setItem(22, ItemBuilder.backButton("Settings"));

        ItemBuilder.fillBackground(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        switch (event.getCurrentItem().getType()) {
            case TRIPWIRE_HOOK:
                selectMode(OpeningMode.KEY_IN_HAND);
                break;
            case CHEST:
                selectMode(OpeningMode.PHYSICAL_BLOCKS);
                break;
            case ARROW:
                new CrateSettingsMenu(player, crate).open();
                break;
            default:
                break;
        }
    }

    private void selectMode(OpeningMode mode) {
        if (crate.getOpeningMode() == mode) {
            updateItems();
            return;
    }
        crate.setOpeningMode(mode);
        GachaCrate.getInstance().getCrateManager().saveCrates();
        player.sendMessage(ChatColor.GREEN + "Opening mode set to: " + mode.getDisplayName());
        updateItems();
    }
}
