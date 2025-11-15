package com.kazumiii.gachacrate.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crate.Crate;
import com.kazumiii.gachacrate.stats.PlayerStats;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AsyncStorage {

    private final GachaCrate plugin;
    private final Gson gson;

    private final File cratesFolder;
    private final File statsFolder;

    public AsyncStorage(GachaCrate plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        this.cratesFolder = new File(plugin.getDataFolder(), "crates");
        this.statsFolder  = new File(plugin.getDataFolder(), "stats");

        if (!cratesFolder.exists()) cratesFolder.mkdirs();
        if (!statsFolder.exists()) statsFolder.mkdirs();
    }

    // ----------------------------------------------------------------------
    // Load crates asynchronously
    // ----------------------------------------------------------------------
    public void loadAllCrates(Consumer<Boolean> done) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Map<String, Crate> loaded = new HashMap<>();

                for (File file : cratesFolder.listFiles()) {
                    if (!file.getName().endsWith(".json")) continue;

                    try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                        Crate crate = gson.fromJson(reader, Crate.class);
                        if (crate != null) {
                            loaded.put(crate.getId(), crate);
                        }
                    }
                }

                // Push results to CrateManager (main thread)
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getCrateManager().setLoadedCrates(loaded);
                    done.accept(true);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                done.accept(false);
            }
        });
    }

    // ----------------------------------------------------------------------
    // Save ALL crates async
    // ----------------------------------------------------------------------
    public void saveAllCrates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                for (Crate crate : plugin.getCrateManager().getCrates().values()) {
                    saveCrateFile(crate);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ----------------------------------------------------------------------
    // Sync version (used on disable)
    // ----------------------------------------------------------------------
    public void saveAllCratesSync() {
        try {
            for (Crate crate : plugin.getCrateManager().getCrates().values()) {
                saveCrateFile(crate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCrateFile(Crate crate) throws IOException {
        File file = new File(cratesFolder, crate.getId() + ".json");

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(crate, writer);
        }
    }

    // ======================================================================
    // PLAYER STATS STORAGE
    // ======================================================================

    public void loadPlayerStats(String uuid, Consumer<PlayerStats> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File file = new File(statsFolder, uuid + ".json");

            if (!file.exists()) {
                PlayerStats newStats = new PlayerStats(uuid);
                callback.accept(newStats);
                return;
            }

            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                PlayerStats stats = gson.fromJson(reader, PlayerStats.class);
                if (stats == null) stats = new PlayerStats(uuid);

                PlayerStats finalStats = stats;
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(finalStats));

            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(new PlayerStats(uuid)));
            }
        });
    }

    public void savePlayerStats(PlayerStats stats) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> savePlayerStatsSync(stats));
    }

    public void saveAllPlayerStats() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (PlayerStats stats : plugin.getStatsManager().getAllStats().values()) {
                savePlayerStatsSync(stats);
            }
        });
    }

    public void saveAllPlayerStatsSync() {
        for (PlayerStats stats : plugin.getStatsManager().getAllStats().values()) {
            savePlayerStatsSync(stats);
        }
    }

    private void savePlayerStatsSync(PlayerStats stats) {
        try {
            File file = new File(statsFolder, stats.getUuid() + ".json");

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                gson.toJson(stats, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
