// ========================================================================
// File: AnimationSelectorMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/animations/AnimationSelectorMenu.java
// ========================================================================
package com.kazumiii.gachacrate.animations;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.guis.CrateSettingsMenu;
import com.kazumiii.gachacrate.guis.ItemBuilder;
import com.kazumiii.gachacrate.guis.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Locale;

/**
 * Animation style picker with clear selected-state feedback.
 */
public class AnimationSelectorMenu extends Menu {

    private final Crate crate;

    public AnimationSelectorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Animation: " + crate.getDisplayName());
        ItemBuilder.drawBorder(inventory);

        // Header
        inventory.setItem(4, new ItemBuilder(Material.JUKEBOX)
                .name(ChatColor.GREEN + "Opening Animation")
                .blank()
                .lore(ChatColor.GRAY + "Choose how crates open.")
                .build());

        // The two styles
        inventory.setItem(12, new ItemBuilder(Material.PAPER)
                .name(ChatColor.AQUA + "Reel")
                .blank()
                .lore(ChatColor.GRAY + "Classic three-column slot spin.")
                .blank()
                .lore(crate.getAnimationType() == AnimationType.REEL
                        ? ChatColor.GREEN + "✔ Currently selected"
                        : ChatColor.YELLOW + "❯ Click to select")
                .glowIf(crate.getAnimationType() == AnimationType.REEL)
                .build());

        inventory.setItem(14, new ItemBuilder(Material.PAPER)
                .name(ChatColor.AQUA + "Roulette")
                .blank()
                .lore(ChatColor.GRAY + "Fast ring spin around the inventory.")
                .blank()
                .lore(crate.getAnimationType() == AnimationType.ROULETTE
                        ? ChatColor.GREEN + "✔ Currently selected"
                        : ChatColor.YELLOW + "❯ Click to select")
                .glowIf(crate.getAnimationType() == AnimationType.ROULETTE)
                .build());

        inventory.setItem(22, ItemBuilder.backButton("Settings"));

        ItemBuilder.fillBackground(inventory);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        switch (event.getCurrentItem().getType()) {
            case PAPER:
                String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName())
                        .toUpperCase(Locale.ROOT);
                try {
                    AnimationType selected = AnimationType.valueOf(displayName);
                    crate.setAnimationType(selected);
                    GachaCrate.getInstance().getCrateManager().saveCrates();
                    player.sendMessage(ChatColor.GREEN + "Animation style set to " + selected.getDisplayName() + "!");
                    open();
                } catch (IllegalArgumentException ignored) {
                    // Clicked something else — ignore
                }
                break;
            case ARROW:
                new CrateSettingsMenu(player, crate).open();
                break;
            default:
                break;
        }
    }
}
