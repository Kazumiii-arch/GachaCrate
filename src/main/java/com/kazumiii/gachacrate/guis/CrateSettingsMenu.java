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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CrateSettingsMenu extends Menu {

    private final Crate crate;

    public CrateSettingsMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 36, "§8Settings: " + crate.getName());

        // --- Decorative Panes ---
        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(" ");
        placeholder.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, placeholder);
        }

        // --- Control Buttons ---
        ItemStack viewRewards = new ItemStack(Material.CHEST);
        ItemMeta viewMeta = viewRewards.getItemMeta();
        viewMeta.setDisplayName(ChatColor.AQUA + "View & Edit Rewards");
        viewMeta.setLore(Arrays.asList(ChatColor.GRAY + "Add, remove, or edit the", ChatColor.GRAY + "rewards inside this crate."));
        viewRewards.setItemMeta(viewMeta);

        ItemStack pitySettings = new ItemStack(Material.DIAMOND);
        ItemMeta pityMeta = pitySettings.getItemMeta();
        pityMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Pity System Settings");
        pityMeta.setLore(Arrays.asList(ChatColor.GRAY + "Configure the guaranteed", ChatColor.GRAY + "reward system for this crate."));
        pitySettings.setItemMeta(pityMeta);

        ItemStack timeLimitSettings = new ItemStack(Material.CLOCK);
        ItemMeta timeMeta = timeLimitSettings.getItemMeta();
        timeMeta.setDisplayName(ChatColor.GOLD + "Time-Limit Settings");
        timeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Set a start and end date", ChatColor.GRAY + "for when this crate can be opened."));
        timeLimitSettings.setItemMeta(timeMeta);

        ItemStack animationSettings = new ItemStack(Material.JUKEBOX);
        ItemMeta animMeta = animationSettings.getItemMeta();
        animMeta.setDisplayName(ChatColor.GREEN + "Animation Settings");
        animMeta.setLore(Arrays.asList(ChatColor.GRAY + "Change the visual animation", ChatColor.GRAY + "for opening this crate."));
        animationSettings.setItemMeta(animMeta);
        
        ItemStack openingModeButton = new ItemStack(Material.ENDER_EYE);
        ItemMeta openMeta = openingModeButton.getItemMeta();
        openMeta.setDisplayName(ChatColor.YELLOW + "Opening Mode Settings");
        openMeta.setLore(Arrays.asList(ChatColor.GRAY + "Choose how players open this crate."));
        openingModeButton.setItemMeta(openMeta);

        ItemStack particleButton = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta particleMeta = particleButton.getItemMeta();
        particleMeta.setDisplayName(ChatColor.GOLD + "Idle Particle Effects");
        particleMeta.setLore(Arrays.asList(ChatColor.GRAY + "Set a constant particle effect", ChatColor.GRAY + "to display on this crate block."));
        particleButton.setItemMeta(particleMeta);

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "« Back to Crate List");
        backButton.setItemMeta(backMeta);

        inventory.setItem(10, viewRewards);
        inventory.setItem(11, pitySettings);
        inventory.setItem(12, timeLimitSettings);
        inventory.setItem(14, animationSettings);
        inventory.setItem(15, openingModeButton);
        inventory.setItem(16, particleButton);
        inventory.setItem(27, backButton);

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
