package com.kazumiii.gachacrate;

import com.kazumiii.gachacrate.animations.IdleParticleTask;
import com.kazumiii.gachacrate.commands.GachaCommand;
import com.kazumiii.gachacrate.crates.CrateManager;
import com.kazumiii.gachacrate.crates.CrateOpener;
import com.kazumiii.gachacrate.crates.PhysicalCrateManager;
import com.kazumiii.gachacrate.crates.RarityManager;
import com.kazumiii.gachacrate.guis.Menu;
import com.kazumiii.gachacrate.guis.MenuListener;
import com.kazumiii.gachacrate.holograms.HologramManager;
import com.kazumiii.gachacrate.holograms.PityPlaceholder;
import com.kazumiii.gachacrate.items.KeyManager;
import com.kazumiii.gachacrate.listeners.CrateListener;
import com.kazumiii.gachacrate.listeners.PlayerJoinListener;
import com.kazumiii.gachacrate.listeners.ProtectionListener;
import com.kazumiii.gachacrate.player.PlayerManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

public final class GachaCrate extends JavaPlugin {

    private static GachaCrate instance;

    private RarityManager rarityManager;
    private CrateManager crateManager;
    private KeyManager keyManager;
    private PlayerManager playerManager;
    private HologramManager hologramManager;
    private PhysicalCrateManager physicalCrateManager;
    private CrateOpener crateOpener;
    private BukkitTask particleTask;
    private boolean placeholderApiEnabled;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        rarityManager = new RarityManager(this);
        keyManager = new KeyManager(this);
        playerManager = new PlayerManager(this);
        hologramManager = new HologramManager(this);
        physicalCrateManager = new PhysicalCrateManager(this);
        crateManager = new CrateManager(this);
        crateOpener = new CrateOpener(this);

        GachaCommand command = new GachaCommand(this);
        PluginCommand gacha = getCommand("gacha");
        if (gacha != null) {
            gacha.setExecutor(command);
            gacha.setTabCompleter(command);
        }

        CrateListener crateListener = new CrateListener(this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(crateListener, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, crateListener), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                placeholderApiEnabled = new PityPlaceholder(this).register();
                if (placeholderApiEnabled) getLogger().info("Registered GachaCrate placeholders with PlaceholderAPI.");
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Could not hook into PlaceholderAPI.", t);
            }
        }

        // Players already online (e.g. after /reload).
        for (Player player : getServer().getOnlinePlayers()) playerManager.loadPlayerData(player);

        particleTask = new IdleParticleTask(this).runTaskTimer(this, 20L, 2L);
        playerManager.startAutosave();

        // Wait one tick so every world (and DecentHolograms) is ready before placing holograms.
        getServer().getScheduler().runTask(this, hologramManager::refreshAll);

        getLogger().info("GachaCrate " + getDescription().getVersion() + " enabled with "
                + crateManager.getAllCrates().size() + " crates.");
    }

    @Override
    public void onDisable() {
        Menu.closeAll();
        if (crateOpener != null) crateOpener.finishAll();
        if (playerManager != null) {
            playerManager.stopAutosave();
            playerManager.saveAll();
        }
        if (crateManager != null) crateManager.saveCrates();
        if (hologramManager != null && physicalCrateManager != null) hologramManager.removeAll();
        if (particleTask != null) particleTask.cancel();
        getLogger().info("GachaCrate disabled.");
    }

    /** /gacha reload: re-reads config.yml, crates.yml and locations.yml. */
    public void reloadAll() {
        Menu.closeAll();
        crateOpener.finishAll();
        playerManager.saveAll();
        hologramManager.removeAll();

        reloadConfig();
        rarityManager.load();
        keyManager.reload();
        crateManager.loadCrates();
        physicalCrateManager.load();
        hologramManager.refreshAll();
        playerManager.startAutosave();
    }

    public static GachaCrate getInstance() { return instance; }
    public RarityManager getRarityManager() { return rarityManager; }
    public CrateManager getCrateManager() { return crateManager; }
    public KeyManager getKeyManager() { return keyManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public PhysicalCrateManager getPhysicalCrateManager() { return physicalCrateManager; }
    public CrateOpener getCrateOpener() { return crateOpener; }
    public boolean isPlaceholderApiEnabled() { return placeholderApiEnabled; }
}
