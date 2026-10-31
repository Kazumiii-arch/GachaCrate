// ========================================================================
// File: GachaCrate.java
// Location: /src/main/java/com/kazumiii/gachacrate/GachaCrate.java
// ========================================================================
package com.kazumiii.gachacrate;

import com.kazumiii.gachacrate.animations.IdleParticleTask;
import com.kazumiii.gachacrate.commands.GachaCommand;
import com.kazumiii.gachacrate.crates.CrateManager;
import com.kazumiii.gachacrate.crates.CrateOpener;
import com.kazumiii.gachacrate.crates.RarityManager;
import com.kazumiii.gachacrate.crates.PhysicalCrateManager;
import com.kazumiii.gachacrate.guis.MenuListener;
import com.kazumiii.gachacrate.holograms.HologramManager;
import com.kazumiii.gachacrate.holograms.PityPlaceholder;
import com.kazumiii.gachacrate.items.KeyManager;
import com.kazumiii.gachacrate.listeners.CrateListener;
import com.kazumiii.gachacrate.listeners.PlayerJoinListener;
import com.kazumiii.gachacrate.player.PlayerManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for GachaCrate.
 * Registers managers, loads crates & rarities, hooks PlaceholderAPI,
 * and starts the idle particle task for physical crate blocks.
 */
public final class GachaCrate extends JavaPlugin {

    private static GachaCrate instance;

    private CrateManager crateManager;
    private RarityManager rarityManager;
    private PhysicalCrateManager physicalCrateManager;
    private CrateOpener crateOpener;
    private KeyManager keyManager;
    private PlayerManager playerManager;
    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        try {
            this.rarityManager = new RarityManager(this);
            this.keyManager = new KeyManager(this);
            this.crateManager = new CrateManager(this);
            this.playerManager = new PlayerManager(this);
            this.hologramManager = new HologramManager(this);

            // Load all saved crates from disk
            this.crateManager.loadCrates();

            // Physical crates depend on crates + holograms being loaded
            this.physicalCrateManager = new PhysicalCrateManager(this);

            // Crate opener used by animation flows
            this.crateOpener = new CrateOpener();

            // Register command executor
            PluginCommand gachaCommand = getCommand("gacha");
            if (gachaCommand != null) {
                gachaCommand.setExecutor(new GachaCommand(this));
            }

            // Register listeners
            getServer().getPluginManager().registerEvents(new MenuListener(), this);
            getServer().getPluginManager().registerEvents(new CrateListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

            // Idle particle task for physical crate blocks
            new IdleParticleTask(this).runTaskTimer(this, 40L, 2L);

            // Optional: PlaceholderAPI hook
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new PityPlaceholder(this).register();
                getLogger().info("PlaceholderAPI detected - pity placeholders registered.");
            }

            getLogger().info("GachaCrate enabled. Version: " + getDescription().getVersion());
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Failed to enable GachaCrate", t);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (crateManager != null) {
                crateManager.saveCrates();
            }
            if (playerManager != null) {
                getServer().getOnlinePlayers().forEach(p -> playerManager.savePlayerData(p));
            }
            if (hologramManager != null) {
                hologramManager.removeAllHolograms();
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error saving data on disable", e);
        }
        getLogger().info("GachaCrate disabled.");
    }

    public static GachaCrate getInstance() {
        return instance;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public RarityManager getRarityManager() {
        return rarityManager;
    }

    public PhysicalCrateManager getPhysicalCrateManager() {
        return physicalCrateManager;
    }

    public CrateOpener getCrateOpener() {
        return crateOpener;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }
}
