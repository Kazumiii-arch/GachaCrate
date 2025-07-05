// ========================================================================
// File: CrateListener.java
// Location: /src/main/java/com/kazumiii/gachacrate/listeners/CrateListener.java
// ========================================================================
package com.kazumiii.gachacrate.listeners;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.guis.CrateAnimationMenu;
import org.bukkit.ChatColor;
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
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        String crateName = plugin.getKeyManager().getCrateFromKey(itemInHand);
        if (crateName == null) return;
        event.setCancelled(true);

        Crate crate = plugin.getCrateManager().getCrate(crateName);
        if (crate == null) {
            player.sendMessage("§cThis key is for a crate that no longer exists!");
            return;
        }
        
        if (crate.isTimeLimitEnabled()) {
            LocalDate now = LocalDate.now();
            if (now.isBefore(crate.getStartDate()) || now.isAfter(crate.getEndDate())) {
                player.sendMessage(ChatColor.RED + "This crate is only available from " + crate.getStartDate() + " to " + crate.getEndDate() + ".");
                return;
            }
        }

        String permission = "gacha.user.open." + crateName;
        if (!player.hasPermission(permission) && !player.hasPermission("gacha.user.open.*")) {
            player.sendMessage("§cYou don't have permission to open this crate.");
            return;
        }

        itemInHand.setAmount(itemInHand.getAmount() - 1);
        new CrateAnimationMenu(player, crate).open();
    }
}
