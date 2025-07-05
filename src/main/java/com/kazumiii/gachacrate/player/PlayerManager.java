// ========================================================================
// File: PlayerManager.java
// Location: /src/main/java/com/kazumiii/gachacrate/player/PlayerManager.java
// ========================================================================
package com.kazumiii.gachacrate.player;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final GachaCrate plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final File dataFolder;

    public PlayerManager(GachaCrate plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        PlayerData data = new PlayerData(uuid);

        if (playerFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            if (config.contains("crates-opened")) {
                config.getConfigurationSection("crates-opened").getKeys(false).forEach(crateName ->
                    data.getCratesOpenedMap().put(crateName, config.getInt("crates-opened." + crateName))
                );
            }
            if (config.contains("pity-counts")) {
                config.getConfigurationSection("pity-counts").getKeys(false).forEach(crateName ->
                    data.getPityCounts().put(crateName.toLowerCase(), config.getInt("pity-counts." + crateName))
                );
            }
            // Load the new flag, defaulting to false if it doesn't exist
            data.setHasReceivedFirstKey(config.getBoolean("has-received-first-key", false));
        }
        playerDataMap.put(uuid, data);
    }

    public void savePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        data.getCratesOpenedMap().forEach((crateName, count) -> config.set("crates-opened." + crateName, count));
        data.getPityCounts().forEach((crateName, count) -> config.set("pity-counts." + crateName, count));

        // Save the new flag
        config.set("has-received-first-key", data.hasReceivedFirstKey());

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data for " + player.getName());
            e.printStackTrace();
        }
        playerDataMap.remove(uuid); // Unload data for offline player
    }
}
