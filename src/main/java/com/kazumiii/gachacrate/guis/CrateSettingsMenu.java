// ========================================================================
// File: CrateSettingsMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/CrateSettingsMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

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
        inventory = Bukkit.createInventory(this, 27, "Settings: " + crate.getName());

        ItemStack viewRewards = new ItemStack(Material.CHEST);
        ItemMeta viewMeta = viewRewards.getItemMeta();
        viewMeta.setDisplayName(ChatColor.AQUA + "View & Edit Rewards");
        viewMeta.setLore(Arrays.asList(ChatColor.GRAY + "Add, remove, or edit the", ChatColor.GRAY + "rewards inside this crate."));
        viewRewards.setItemMeta(viewMeta);

        // We can add buttons for pity/time-limit settings here in the future
        
        ItemStack deleteCrate = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteCrate.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "DELETE CRATE");
        deleteMeta.setLore(Arrays.asList(ChatColor.DARK_RED + "This action is permanent."));
        deleteCrate.setItemMeta(deleteMeta);
        
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "« Back to Crate List");
        backButton.setItemMeta(backMeta);

        inventory.setItem(13, viewRewards);
        inventory.setItem(22, deleteCrate);
        inventory.setItem(18, backButton);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        
        switch (event.getCurrentItem().getType()) {
            case CHEST:
                new CrateEditorMenu(player, crate).open();
                break;
            case BARRIER:
                player.closeInventory();
                player.sendMessage("§cFor safety, please use §e/gacha delete " + crate.getName());
                break;
            case ARROW:
                new MainMenu(player).open();
                break;
            default:
                break;
        }
    }
}
