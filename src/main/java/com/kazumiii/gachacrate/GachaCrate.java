package com.kazumiii.gachacrate;

import com.kazumiii.gachacrate.commands.CommandGacha;
import com.kazumiii.gachacrate.config.ConfigManager;
import com.kazumiii.gachacrate.crate.CrateManager;
import com.kazumiii.gachacrate.storage.AsyncStorage;
import com.kazumiii.gachacrate.stats.PlayerStatsManager;
import com.kazumiii.gachacrate.ui.GUIManager;
import com.kazumiii.gachacrate.utils.BackupTask;
import com.kazumiii.gachacrate.utils.PlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for GachaCrate (Premium upgrade).
 * - Registers managers
 * - Loads configs
 * - Hooks PlaceholderAPI if available
 * - Starts async save/backup tasks
 */
public final class GachaCrate extends JavaPlugin {

    private static GachaCrate instance;

    private ConfigManager configManager;
    private CrateManager crateManager;
    private AsyncStorage asyncStorage;
    private GUIManager guiManager;
    private PlayerStatsManager statsManager;

    @Override
    public void onLoad() {
        instance = this;
        // We set up logging early if needed
        getLogger().info("GachaCrate is loading (premium systems prepared)...");
    }

    @Override
    public void onEnable() {
        try {
            // Ensure data folder exists
            saveDefaultConfig();

            // Managers
            this.configManager = new ConfigManager(this);
            this.asyncStorage  = new AsyncStorage(this);
            this.crateManager  = new CrateManager(this);
            this.guiManager    = new GUIManager(this);
            this.statsManager  = new PlayerStatsManager(this);

            // Load config-driven defaults
            configManager.loadAll();

            // Load crates from storage (async)
            asyncStorage.loadAllCrates(result -> {
                // result: success callback
                if (!result) {
                    getLogger().warning("Crate load reported issues — falling back to defaults.");
                } else {
                    getLogger().info("Crates loaded successfully.");
                }
                // After crates load, register commands and listeners to avoid race conditions
                registerCommands();
                registerListeners();
            });

            // Hook PlaceholderAPI (optional)
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new PlaceholderHook(this).register();
                getLogger().info("PlaceholderAPI detected — placeholders registered.");
            }

            // Schedule periodic async saves (config-driven)
            int saveInterval = configManager.getSaveIntervalSeconds();
            if (saveInterval > 0) {
                Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                    getLogger().fine("Running scheduled async save of crates & stats...");
                    asyncStorage.saveAllCrates();
                    asyncStorage.saveAllPlayerStats();
                }, saveInterval * 20L, saveInterval * 20L);
            }

            // Schedule backups
            int backupIntervalMinutes = configManager.getBackupIntervalMinutes();
            if (backupIntervalMinutes > 0) {
                new BackupTask(this).start(backupIntervalMinutes);
            }

            // Register a shutdown save hook just in case
            getLogger().info("GachaCrate (premium) enabled. Version: " + getDescription().getVersion());
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Failed to enable GachaCrate", t);
            // If serious issue, disable plugin gracefully
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("GachaCrate is disabling — saving data synchronously...");
        try {
            // Perform a final synchronous save on disable to avoid data loss
            asyncStorage.saveAllCratesSync();
            asyncStorage.saveAllPlayerStatsSync();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error saving data on disable", e);
        }
        getLogger().info("GachaCrate disabled.");
    }

    private void registerCommands() {
        getCommand("gacha").setExecutor(new CommandGacha(this));
        // optionally set tab completer if implemented
    }

    private void registerListeners() {
        // Register listeners (GUI click, crate block interaction, player join/quit)
        Bukkit.getPluginManager().registerEvents(new com.kazumiii.gachacrate.listeners.PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new com.kazumiii.gachacrate.listeners.CrateBlockListener(this), this);
        Bukkit.getPluginManager().registerEvents(guiManager, this); // GUIManager implements Listener
    }

    // Getters for managers so other classes can access them
    public static GachaCrate getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public AsyncStorage getAsyncStorage() {
        return asyncStorage;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public PlayerStatsManager getStatsManager() {
        return statsManager;
    }
}
