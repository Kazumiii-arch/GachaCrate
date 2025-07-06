// ========================================================================
// File: GachaCrate.java
// Location: /src/main/java/com/kazumiii/gachacrate/GachaCrate.java
// ========================================================================
package com.kazumiii.gachacrate;

import com.kazumiii.gachacrate.animations.IdleParticleTask;
import com.kazumiii.gachacrate.commands.GachaCommand;
import com.kazumiii.gachacrate.crates.CrateManager;
import com.kazumiii.gachacrate.crates.CrateOpener;
import com.kazumiii.gachacrate.crates.PhysicalCrateManager;
import com.kazumiii.gachacrate.crates.RarityManager;
import com.kazumiii.gachacrate.guis.MenuListener;
import com.kazumiii.gachacrate.holograms.HologramManager;
import com.kazumiii.gachacrate.items.KeyManager;
import com.kazumiii.gachacrate.listeners.CrateListener;
import com.kazumiii.gachacrate.listeners.PlayerJoinListener;
import com.kazumiii.gachacrate.player.PlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GachaCrate extends JavaPlugin {

    private static GachaCrate instance;
    private CrateManager crateManager;
    private KeyManager keyManager;
    private RarityManager rarityManager;
    private PlayerManager playerManager;
    private PhysicalCrateManager physicalCrateManager;
    private HologramManager hologramManager;
    private CrateOpener crateOpener;

    @Override
    public void onEnable() {
        instance = this;

        // Load config and initialize all managers
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        
        rarityManager = new RarityManager(this);
        crateManager = new CrateManager(this);
        keyManager = new KeyManager(this);
        playerManager = new PlayerManager(this);
        hologramManager = new HologramManager(this);
        physicalCrateManager = new PhysicalCrateManager(this);
        crateOpener = new CrateOpener();

        // Register commands and event listeners
        getCommand("gacha").setExecutor(new GachaCommand(this));
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new CrateListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        
        // Load data for any players already online (e.g., on a /reload)
        getServer().getOnlinePlayers().forEach(player -> playerManager.loadPlayerData(player));

        // Start the idle particle effect task
        new IdleParticleTask(this).runTaskTimerAsynchronously(this, 0L, 2L); // Run every 2 ticks for smooth animation

        getLogger().info("========================================");
        getLogger().info("Kazumiii Gacha Crate has been enabled!");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        // Save data for any players online when the server stops
        getServer().getOnlinePlayers().forEach(player -> playerManager.savePlayerData(player));
        
        if (crateManager != null) {
            crateManager.saveCrates();
        }
        getLogger().info("Kazumiii Gacha Crate has been disabled.");
    }

    // --- Getters for Managers ---
    public CrateManager getCrateManager() { return crateManager; }
    public KeyManager getKeyManager() { return keyManager; }
    public RarityManager getRarityManager() { return rarityManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public PhysicalCrateManager getPhysicalCrateManager() { return physicalCrateManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public CrateOpener getCrateOpener() { return crateOpener; }
    public static GachaCrate getInstance() { return instance; }
}
