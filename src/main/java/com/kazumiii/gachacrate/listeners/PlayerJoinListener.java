// ========================================================================
// File: PlayerJoinListener.java
// Location: /src/main/java/com/kazumiii/gachacrate/listeners/PlayerJoinListener.java
// ========================================================================
package com.kazumiii.gachacrate.listeners;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final GachaCrate plugin;

    public PlayerJoinListener(GachaCrate plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getPlayerManager().loadPlayerData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerManager().savePlayerData(event.getPlayer());
    }
}
