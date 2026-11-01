// ========================================================================
// File: CrateListener.java
// Location: /src/main/java/com/kazumiii/gachacrate/listeners/CrateListener.java
// ========================================================================
package com.kazumiii.gachacrate.listeners;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.OpeningMode;
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

    private final GachaCrate plugin;

    public CrateListener(GachaCrate plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        // --- Handle Physical Block Interactions ---
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && plugin.getPhysicalCrateManager().isCrateLocation(clickedBlock.getLocation())) {
                event.setCancelled(true); // Always cancel interaction with crate blocks
                String crateName = plugin.getPhysicalCrateManager().getCrateNameAt(clickedBlock.getLocation());
                Crate crate = plugin.getCrateManager().getCrate(crateName);
                if (crate == null) return;

                // Preview on left click, regardless of the crate's opening mode
                if (action == Action.LEFT_CLICK_BLOCK) {
                    new CratePreviewMenu(player, crate).open();
                    return;
                }

                // On right click, only proceed if the mode is set to PHYSICAL_BLOCKS
                if (crate.getOpeningMode() == OpeningMode.PHYSICAL_BLOCKS) {
                    openCrate(player, crate, player.getInventory().getItemInMainHand());
                } else {
                    player.sendMessage(ChatColor.RED + "This crate must be opened by right-clicking the key in your hand.");
                }
                return; // Stop processing to avoid double-triggering
            }
        }

        // --- Handle Key-In-Hand Interactions ---
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            String crateName = plugin.getKeyManager().getCrateFromKey(itemInHand);
            if (crateName != null) {
                Crate crate = plugin.getCrateManager().getCrate(crateName);
                if (crate != null && crate.getOpeningMode() == OpeningMode.KEY_IN_HAND) {
                    event.setCancelled(true);
                    openCrate(player, crate, itemInHand);
                }
            }
        }
    }

    /**
     * Handles the final logic for opening a crate after all initial checks.
     * Gates, in order: key match -> time limit -> permission -> has rewards.
     * Only after ALL gates pass is the key consumed.
     *
     * @param player The player opening the crate.
     * @param crate The crate being opened.
     * @param key The item stack being used as a key.
     */
    private void openCrate(Player player, Crate crate, ItemStack key) {
        String keyCrateName = plugin.getKeyManager().getCrateFromKey(key);
        if (keyCrateName == null || !keyCrateName.equalsIgnoreCase(crate.getName())) {
            if (crate.getOpeningMode() == OpeningMode.PHYSICAL_BLOCKS) {
                player.sendMessage(ChatColor.RED + "You need a " + ChatColor.YELLOW + crate.getDisplayName()
                        + " Key" + ChatColor.RED + " to open this!");
            }
            return;
        }

        // Time limit: each bound is only enforced if it is actually set.
        if (crate.isTimeLimitEnabled()) {
            LocalDate now = LocalDate.now();
            if (crate.getStartDate() != null && now.isBefore(crate.getStartDate())) {
                player.sendMessage(ChatColor.RED + "This crate opens on " + crate.getStartDate() + ".");
                return;
            }
            if (crate.getEndDate() != null && now.isAfter(crate.getEndDate())) {
                player.sendMessage(ChatColor.RED + "This crate closed on " + crate.getEndDate() + ".");
                return;
            }
        }

        String permission = "gacha.user.open." + crate.getName();
        if (!player.hasPermission(permission) && !player.hasPermission("gacha.user.open.*")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to open this crate.");
            return;
        }

        // Never consume a key for a crate that has nothing to give.
        if (crate.getRewards().isEmpty()) {
            player.sendMessage(ChatColor.RED + "This crate has no rewards yet. Your key was not used.");
            return;
        }

        // Consume one key.
        int amount = key.getAmount();
        if (amount <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            key.setAmount(amount - 1);
        }

        new CrateAnimationMenu(player, crate).open();
    }
}
