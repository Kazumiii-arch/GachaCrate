package com.kazumiii.gachacrate.listeners;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.entity.Player;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.player.PlayerData;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Loads player data on join and saves it on quit.
 */
public class PlayerJoinListener implements Listener {

    private final GachaCrate plugin;
    private final CrateListener crateListener;

    public PlayerJoinListener(GachaCrate plugin, CrateListener crateListener) {
        this.plugin = plugin;
        this.crateListener = crateListener;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().loadPlayerData(player);
        deliverPendingKeys(player);

        // Remind players about free daily keys a moment after they join.
        if (plugin.getConfig().getBoolean("daily.join-reminder", true)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                int count = plugin.getShopManager().getClaimableDaily(player).size();
                if (count > 0) {
                    Text.send(player, "&aYou have free daily keys waiting! Type &e/gacha daily &ato claim them.");
                }
            }, 60L);
        }
    }

    /** Gives keys that were sent while the player was offline (e.g. from a webstore). */
    private void deliverPendingKeys(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getPendingKeys().isEmpty()) return;
        Map<String, Integer> pending = new HashMap<>(data.getPendingKeys());
        data.clearPendingKeys();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                // Left again before getting them; keep them for next time.
                pending.forEach((crate, amount) -> plugin.getPlayerManager().addPendingKeys(player.getUniqueId(), crate, amount));
                return;
            }
            for (Map.Entry<String, Integer> entry : pending.entrySet()) {
                Crate crate = plugin.getCrateManager().getCrate(entry.getKey());
                if (crate == null) continue;
                plugin.getKeyManager().giveKeys(player, crate, entry.getValue());
                Text.send(player, "&aYou received &f" + entry.getValue() + "x " + crate.getDisplayName()
                        + " &akey" + (entry.getValue() == 1 ? "" : "s") + " &awhile you were away!");
            }
        }, 40L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        // Give the reward of an unfinished animation before saving.
        plugin.getCrateOpener().finishNow(event.getPlayer());
        plugin.getCrateOpener().forget(event.getPlayer());
        plugin.getPlayerManager().unloadPlayerData(event.getPlayer());
        crateListener.forget(event.getPlayer());
    }
}
