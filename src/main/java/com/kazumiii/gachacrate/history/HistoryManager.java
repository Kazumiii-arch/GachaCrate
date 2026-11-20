package com.kazumiii.gachacrate.history;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Keeps each player's recent wins (in their player file) and the server-wide
 * "recent big wins" feed (wins.yml).
 */
public class HistoryManager {

    private final GachaCrate plugin;
    private final File file;
    private final List<WinRecord> serverWins = new ArrayList<>(); // Newest first.

    public HistoryManager(GachaCrate plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "wins.yml");
        load();
    }

    public void load() {
        serverWins.clear();
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String line : config.getStringList("wins")) {
            WinRecord record = WinRecord.parse(line);
            if (record != null) serverWins.add(record);
        }
    }

    private int perPlayerLimit() {
        return Math.max(0, plugin.getConfig().getInt("history.per-player", 15));
    }

    private int serverLimit() {
        return Math.max(0, plugin.getConfig().getInt("history.server-wide", 30));
    }

    /** Records a win for the player, and in the server feed if the rarity is high enough. */
    public void record(Player player, PlayerData data, Crate crate, Reward reward) {
        WinRecord record = new WinRecord(System.currentTimeMillis(), player.getUniqueId(), player.getName(),
                crate.getId(), reward.getUniqueId(), reward.getRarity().getId(), reward.getDisplayName());
        if (data != null && perPlayerLimit() > 0) data.addWin(record, perPlayerLimit());

        Rarity minimum = plugin.getRarityManager().getRarity(plugin.getConfig().getString("history.server-wide-minimum-rarity", "EPIC"));
        if (serverLimit() > 0 && (minimum == null || reward.getRarity().isAtLeast(minimum))) {
            serverWins.add(0, record);
            while (serverWins.size() > serverLimit()) serverWins.remove(serverWins.size() - 1);
            saveAsync();
        }
    }

    public List<WinRecord> getServerWins() {
        return Collections.unmodifiableList(serverWins);
    }

    private List<String> snapshot() {
        List<String> lines = new ArrayList<>();
        for (WinRecord record : serverWins) lines.add(record.serialize());
        return lines;
    }

    private void saveAsync() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("wins", snapshot());
        String yaml = config.saveToString();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> write(yaml));
    }

    public void saveNow() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("wins", snapshot());
        write(config.saveToString());
    }

    private synchronized void write(String yaml) {
        File temp = new File(plugin.getDataFolder(), "wins.yml.tmp");
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            Files.write(temp.toPath(), yaml.getBytes(StandardCharsets.UTF_8));
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save wins.yml", e);
        }
    }
}
