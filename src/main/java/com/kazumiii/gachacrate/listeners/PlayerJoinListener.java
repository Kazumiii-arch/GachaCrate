package com.kazumiii.gachacrate.listeners;

import com.kazumiii.gachacrate.GachaCrate;
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
        plugin.getPlayerManager().loadPlayerData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        // Give the reward of an unfinished animation before saving.
        plugin.getCrateOpener().finishNow(event.getPlayer());
        plugin.getPlayerManager().unloadPlayerData(event.getPlayer());
        crateListener.forget(event.getPlayer());
    }
}
