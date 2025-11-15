package com.kazumiii.gachacrate.crate;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.*;

public class CrateManager {

    private final GachaCrate plugin;

    // Loaded crates
    private final Map<String, Crate> crates = new HashMap<>();

    // Physical crate blocks mapped by location
    private final Map<Location, String> crateBlocks = new HashMap<>();

    public CrateManager(GachaCrate plugin) {
        this.plugin = plugin;
    }

    // Called by AsyncStorage after loading
    public void setLoadedCrates(Map<String, Crate> loaded) {
        crates.clear();
        crates.putAll(loaded);
    }

    // ------------------------------------------------------------
    // CRATE REGISTRATION
    // ------------------------------------------------------------
    public void registerCrate(Crate crate) {
        crates.put(crate.getId(), crate);
    }

    public boolean crateExists(String id) {
        return crates.containsKey(id);
    }

    public Crate getCrate(String id) {
        return crates.get(id);
    }

    public Map<String, Crate> getCrates() {
        return crates;
    }

    // ------------------------------------------------------------
    // CRATE REMOVAL
    // ------------------------------------------------------------
    public boolean deleteCrate(String id) {
        return crates.remove(id) != null;
    }

    // ------------------------------------------------------------
    // REWARD ROLLING ENGINE
    // ------------------------------------------------------------
    public Reward rollReward(Crate crate) {
        List<Reward> rewards = crate.getRewards();
        if (rewards.isEmpty()) return null;

        // Weighted random
        double total = 0;
        for (Reward r : rewards) total += r.getChance();

        double random = Math.random() * total;
        double current = 0;

        for (Reward r : rewards) {
            current += r.getChance();
            if (current >= random) return r;
        }

        return rewards.get(rewards.size() - 1); // fallback
    }

    // ------------------------------------------------------------
    // PHYSICAL CRATE BLOCKS
    // ------------------------------------------------------------
    public void registerCrateBlock(Location loc, String crateId) {
        crateBlocks.put(loc, crateId);
    }

    public void removeCrateBlock(Location loc) {
        crateBlocks.remove(loc);
    }

    public boolean isCrateBlock(Block block) {
        return block != null && crateBlocks.containsKey(block.getLocation());
    }

    public String getCrateIdAtBlock(Block block) {
        return crateBlocks.get(block.getLocation());
    }

    public Map<Location, String> getCrateBlocks() {
        return crateBlocks;
    }

    // ------------------------------------------------------------
    // EXPIRATION CHECK (SEASONAL CRATES)
    // ------------------------------------------------------------
    public boolean isCrateExpired(Crate crate) {
        if (crate.getExpirationDate() == null) return false;
        return LocalDate.now().isAfter(crate.getExpirationDate());
    }

    // ------------------------------------------------------------
    // OPEN CRATE (Handles animations elsewhere)
    // ------------------------------------------------------------
    public void openCrate(Player player, String crateId) {
        Crate crate = getCrate(crateId);
        if (crate == null) {
            player.sendMessage("§cThis crate no longer exists!");
            return;
        }

        if (isCrateExpired(crate)) {
            player.sendMessage("§eThis event crate has expired!");
            return;
        }

        // Delegate opening to animation system / GUI system
        plugin.getGuiManager().openCrateAnimation(player, crate);
    }

    // ------------------------------------------------------------
    // SAVE WRAPPERS
    // ------------------------------------------------------------
    public void saveAll() {
        plugin.getAsyncStorage().saveAllCrates();
    }

    public void saveAllSync() {
        plugin.getAsyncStorage().saveAllCratesSync();
    }
}
