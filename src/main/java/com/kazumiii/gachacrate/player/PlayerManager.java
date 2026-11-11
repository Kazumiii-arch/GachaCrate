package com.kazumiii.gachacrate.player;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.history.WinRecord;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * Loads and saves player data (playerdata/&lt;uuid&gt;.yml).
 * Data is loaded on join, saved on quit, and auto-saved in the background.
 */
public class PlayerManager {

    private final GachaCrate plugin;
    private final Map<UUID, PlayerData> online = new ConcurrentHashMap<>();
    // Each snapshot gets a number, so an old background save can never overwrite newer data.
    private final Map<UUID, Long> latestWritten = new ConcurrentHashMap<>();
    private final AtomicLong snapshotCounter = new AtomicLong();
    private final File dataFolder;
    private BukkitTask autosaveTask;

    public PlayerManager(GachaCrate plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) dataFolder.mkdirs();
    }

    /** Data of an online player. Loads it if needed, so this never returns null for online players. */
    public PlayerData getPlayerData(UUID uuid) {
        PlayerData data = online.get(uuid);
        if (data == null && Bukkit.getPlayer(uuid) != null) {
            data = readFromDisk(uuid);
            online.put(uuid, data);
        }
        return data;
    }

    /** Reads an offline player's data without keeping it in memory (for /gacha stats). */
    public PlayerData getOfflineData(UUID uuid) {
        PlayerData data = online.get(uuid);
        if (data != null) return data;
        File file = fileOf(uuid);
        return file.exists() ? readFromDisk(uuid) : null;
    }

    public void loadPlayerData(Player player) {
        PlayerData data = readFromDisk(player.getUniqueId());
        data.setLastKnownName(player.getName());
        online.put(player.getUniqueId(), data);
    }

    /** The folder with one file per player (used to build the leaderboard). */
    public File getDataFolder() {
        return dataFolder;
    }

    /** Reads a player file without touching the online cache (safe to call off the main thread). */
    public PlayerData readFile(UUID uuid) {
        return readFromDisk(uuid);
    }

    /** Saves and unloads a player who is leaving. */
    public void unloadPlayerData(Player player) {
        PlayerData data = online.remove(player.getUniqueId());
        if (data != null) writeToDisk(data);
    }

    public void saveAll() {
        for (PlayerData data : online.values()) writeToDisk(data);
    }

    /** Edits an offline player's file directly (used by /gacha resetpity). */
    public boolean resetPityOffline(UUID uuid, String crateName) {
        PlayerData data = getOfflineData(uuid);
        if (data == null) return false;
        data.resetPityCount(crateName);
        writeToDisk(data);
        return true;
    }

    // ------------------------------------------------------------------
    // Auto-save
    // ------------------------------------------------------------------

    public void startAutosave() {
        stopAutosave();
        int minutes = plugin.getConfig().getInt("autosave-minutes", 5);
        if (minutes <= 0) return;
        long ticks = minutes * 60L * 20L;
        autosaveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::autosave, ticks, ticks);
    }

    public void stopAutosave() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
    }

    /** Builds the files on the main thread, then writes them in the background. */
    private void autosave() {
        for (PlayerData data : online.values()) {
            if (!data.isDirty()) continue;
            final UUID uuid = data.getPlayerUUID();
            final String yaml = toYaml(data);
            final long snapshot = snapshotCounter.incrementAndGet();
            data.setDirty(false);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> writeString(uuid, yaml, snapshot));
        }
    }

    // ------------------------------------------------------------------
    // File format (same as earlier versions)
    // ------------------------------------------------------------------

    private File fileOf(UUID uuid) {
        return new File(dataFolder, uuid + ".yml");
    }

    private PlayerData readFromDisk(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        File file = fileOf(uuid);
        if (!file.exists()) return data;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection opened = config.getConfigurationSection("crates-opened");
        if (opened != null) {
            for (String crate : opened.getKeys(false)) {
                data.getCratesOpenedMap().merge(crate.toLowerCase(Locale.ROOT), opened.getInt(crate), Integer::sum);
            }
        }
        ConfigurationSection pity = config.getConfigurationSection("pity-counts");
        if (pity != null) {
            for (String crate : pity.getKeys(false)) {
                data.getPityCounts().put(crate.toLowerCase(Locale.ROOT), pity.getInt(crate));
            }
        }
        data.setHasReceivedFirstKey(config.getBoolean("has-received-first-key", false));
        data.setLastKnownName(config.getString("name"));
        ConfigurationSection daily = config.getConfigurationSection("daily-claims");
        if (daily != null) {
            for (String crate : daily.getKeys(false)) {
                try {
                    data.getDailyClaims().put(crate.toLowerCase(Locale.ROOT), LocalDate.parse(daily.getString(crate, "")));
                } catch (DateTimeParseException ignored) {
                    // Skip a broken date.
                }
            }
        }
        for (String line : config.getStringList("history")) {
            WinRecord record = WinRecord.parse(line);
            if (record != null) data.getHistory().add(record);
        }
        data.setDirty(false);
        return data;
    }

    private String toYaml(PlayerData data) {
        FileConfiguration config = new YamlConfiguration();
        data.getCratesOpenedMap().forEach((crate, count) -> config.set("crates-opened." + crate, count));
        data.getPityCounts().forEach((crate, count) -> config.set("pity-counts." + crate, count));
        config.set("has-received-first-key", data.hasReceivedFirstKey());
        if (data.getLastKnownName() != null) config.set("name", data.getLastKnownName());
        data.getDailyClaims().forEach((crate, day) -> config.set("daily-claims." + crate, day.toString()));
        List<String> history = new ArrayList<>();
        for (WinRecord record : data.getHistory()) history.add(record.serialize());
        if (!history.isEmpty()) config.set("history", history);
        return config.saveToString();
    }

    private void writeToDisk(PlayerData data) {
        writeString(data.getPlayerUUID(), toYaml(data), snapshotCounter.incrementAndGet());
        data.setDirty(false);
    }

    private synchronized void writeString(UUID uuid, String yaml, long snapshot) {
        Long newest = latestWritten.get(uuid);
        if (newest != null && newest > snapshot) return; // A newer save already happened.
        latestWritten.put(uuid, snapshot);
        File file = fileOf(uuid);
        File temp = new File(dataFolder, uuid + ".yml.tmp");
        try {
            if (!dataFolder.exists()) dataFolder.mkdirs();
            Files.write(temp.toPath(), yaml.getBytes(StandardCharsets.UTF_8));
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player data for " + uuid, e);
        }
    }
}
