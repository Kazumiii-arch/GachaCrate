// ========================================================================
// File: CrateListener.java
// Location: /src/main/java/com/kazumiii/gachacrate/listeners/CrateListener.java
// ========================================================================
package com.kazumiii.gachacrate.listeners;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.guis.CrateAnimationMenu;
import com.kazumiii.gachacrate.guis.CratePreviewMenu;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;

public class CrateListener implements Listener {

    private final GachaCrate plugin = GachaCrate.getInstance();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        
        // --- PHYSICAL CRATE INTERACTION ---
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && plugin.getPhysicalCrateManager().isCrateLocation(clickedBlock.getLocation())) {
                event.setCancelled(true); // Always cancel interaction with crate blocks
                String crateName = plugin.getPhysicalCrateManager().getCrateNameAt(clickedBlock.getLocation());
                Crate crate = plugin.getCrateManager().getCrate(crateName);
                if (crate == null) return;

                if (action == Action.LEFT_CLICK_BLOCK) {
                    // Open preview menu on left-click
                    new CratePreviewMenu(player, crate).open();
                } else { // It's a right-click
                    // Attempt to open the crate using the item in the player's hand
                    openCrate(player, crate, player.getInventory().getItemInMainHand());
                }
                return; // Stop processing to avoid the fallback check
            }
        }

        // --- KEY IN HAND INTERACTION (fallback if not looking at a crate block) ---
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            String crateName = plugin.getKeyManager().getCrateFromKey(itemInHand);
            if (crateName != null) {
                event.setCancelled(true);
                Crate crate = plugin.getCrateManager().getCrate(crateName);
                if (crate != null) {
                    openCrate(player, crate, itemInHand);
                }
            }
        }
    }

    /**
     * Handles the logic for opening a crate after all initial checks.
     * @param player The player opening the crate.
     * @param crate The crate being opened.
     * @param key The item stack being used as a key.
     */
    private void openCrate(Player player, Crate crate, ItemStack key) {
        // Verify that the item in hand is a key for THIS specific crate
        String keyCrateName = plugin.getKeyManager().getCrateFromKey(key);
        if (keyCrateName == null || !keyCrateName.equalsIgnoreCase(crate.getName())) {
            player.sendMessage(ChatColor.RED + "You need a " + ChatColor.YELLOW + crate.getName() + " Key" + ChatColor.RED + " to open this!");
            return;
        }
        
        // Time Limit Check
        if (crate.isTimeLimitEnabled()) {
            LocalDate now = LocalDate.now();
            if (now.isBefore(crate.getStartDate()) || now.isAfter(crate.getEndDate())) {
                player.sendMessage(ChatColor.RED + "This crate is only available from " + crate.getStartDate() + " to " + crate.getEndDate() + ".");
                return;
            }
        }

        // Permission Check
        String permission = "gacha.user.open." + crate.getName();
        if (!player.hasPermission(permission) && !player.hasPermission("gacha.user.open.*")) {
            player.sendMessage("§cYou don't have permission to open this crate.");
            return;
        }

        // Consume one key and start the animation
        key.setAmount(key.getAmount() - 1);
        new CrateAnimationMenu(player, crate).open();
    }
}
