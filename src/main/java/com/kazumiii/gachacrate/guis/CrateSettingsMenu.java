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
import java.util.Collections;

public class CrateSettingsMenu extends Menu {

    private final Crate crate;

    public CrateSettingsMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "Settings: " + crate.getName());

        // Create the control items
        ItemStack viewRewards = new ItemStack(Material.CHEST);
        ItemMeta viewMeta = viewRewards.getItemMeta();
        viewMeta.setDisplayName(ChatColor.AQUA + "View & Edit Rewards");
        viewMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to see all rewards.", ChatColor.GRAY + "Right-click rewards to remove them."));
        viewRewards.setItemMeta(viewMeta);

        ItemStack addItem = new ItemStack(Material.DIAMOND);
        ItemMeta addMeta = addItem.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "Add Item Reward");
        addMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Use /gacha additem to add the item in your hand."));
        addItem.setItemMeta(addMeta);
        
        ItemStack addCmd = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta cmdMeta = addCmd.getItemMeta();
        cmdMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Add Command Reward");
        cmdMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Use /gacha addcmd to add a command reward."));
        addCmd.setItemMeta(cmdMeta);

        ItemStack deleteCrate = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteCrate.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "DELETE CRATE");
        deleteMeta.setLore(Arrays.asList(ChatColor.DARK_RED + "This action is permanent", ChatColor.DARK_RED + "and cannot be undone."));
        deleteCrate.setItemMeta(deleteMeta);

        inventory.setItem(10, viewRewards);
        inventory.setItem(12, addItem);
        inventory.setItem(14, addCmd);
        inventory.setItem(16, deleteCrate);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        
        Material clickedType = event.getCurrentItem().getType();

        switch (clickedType) {
            case CHEST:
                // Open the existing editor menu
                new CrateEditorMenu(player, crate).open();
                break;
            case DIAMOND:
            case COMMAND_BLOCK:
                // For now, just close and send a message
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Please use the respective add command for now.");
                break;
            case BARRIER:
                // TODO: Open a dedicated confirmation menu before deleting
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Deletion confirmation coming soon! For now, use " + ChatColor.GOLD + "/gacha delete <name>");
                break;
            default:
                break;
        }
    }
}
