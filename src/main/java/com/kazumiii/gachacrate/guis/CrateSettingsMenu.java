// ========================================================================
// File: CrateSettingsMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/CrateSettingsMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.animations.AnimationSelectorMenu;
import com.kazumiii.gachacrate.animations.IdleParticleMenu;
import com.kazumiii.gachacrate.crates.Crate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Settings hub for a single crate: rewards, pity, time limit, animation,
 * opening mode and idle particles.
 */
public class CrateSettingsMenu extends Menu {

    private final Crate crate;

    public CrateSettingsMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Settings: " + crate.getDisplayName());

        ItemBuilder.drawBorder(inventory);

        // Header — crate summary
        inventory.setItem(4, new ItemBuilder(Material.BOOK)
                .name(ChatColor.AQUA + crate.getDisplayName())
                .blank()
                .lore(ChatColor.GRAY + "Rewards: " + ChatColor.WHITE + crate.getRewards().size())
                .lore(ChatColor.GRAY + "Mode: " + ChatColor.WHITE + crate.getOpeningMode().getDisplayName())
                .build());

        inventory.setItem(10, new ItemBuilder(Material.CHEST)
                .name(ChatColor.AQUA + "View & Edit Rewards")
                .lore(ChatColor.GRAY + "Add, remove, or edit the")
                .lore(ChatColor.GRAY + "rewards inside this crate.")
                .build());

        inventory.setItem(11, new ItemBuilder(Material.DIAMOND)
                .name(ChatColor.LIGHT_PURPLE + "Pity System")
                .lore(ChatColor.GRAY + "Guaranteed rare reward")
                .lore(ChatColor.GRAY + "after N opens.")
                .build());

        inventory.setItem(12, new ItemBuilder(Material.CLOCK)
                .name(ChatColor.GOLD + "Time Limit")
                .lore(ChatColor.GRAY + "Restrict when this crate")
                .lore(ChatColor.GRAY + "can be opened.")
                .build());

        inventory.setItem(14, new ItemBuilder(Material.JUKEBOX)
                .name(ChatColor.GREEN + "Animation")
                .lore(ChatColor.GRAY + "Choose the opening")
                .lore(ChatColor.GRAY + "animation style.")
                .build());

        inventory.setItem(15, new ItemBuilder(Material.ENDER_EYE)
                .name(ChatColor.YELLOW + "Opening Mode")
                .lore(ChatColor.GRAY + "Key-in-hand or")
                .lore(ChatColor.GRAY + "physical block.")
                .build());

        inventory.setItem(16, new ItemBuilder(Material.GLOWSTONE_DUST)
                .name(ChatColor.GOLD + "Idle Particles")
                .lore(ChatColor.GRAY + "Ambient particle effect")
                .lore(ChatColor.GRAY + "above the crate block.")
                .build());

        inventory.setItem(22, ItemBuilder.backButton("Crates list"));

        ItemBuilder.fillBackground(inventory);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        switch (event.getCurrentItem().getType()) {
            case CHEST:
                new CrateEditorMenu(player, crate).open();
                break;
            case DIAMOND:
                new PityEditorMenu(player, crate).open();
                break;
            case CLOCK:
                new TimeLimitEditorMenu(player, crate).open();
                break;
            case JUKEBOX:
                new AnimationSelectorMenu(player, crate).open();
                break;
            case ENDER_EYE:
                new OpeningModeMenu(player, crate).open();
                break;
            case GLOWSTONE_DUST:
                new IdleParticleMenu(player, crate).open();
                break;
            case ARROW:
                new MainMenu(player).open();
                break;
            default:
                break;
        }
    }
}
