package com.kazumiii.gachacrate.stats;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crate.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles player statistics loading, saving, caching, and leaderboards.
 */
public class PlayerStatsManager {

    private final GachaCrate plugin;

    // Cached stats by player UUID
    private final Map<String, PlayerStats> statsMap = new ConcurrentHashMap<>();

    public PlayerStatsManager(GachaCrate plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------
    // GET & LOAD
    // ---------------------------------------------------------
    public PlayerStats getStats(String uuid) {
        return statsMap.get(uuid);
    }

    public PlayerStats getOrLoadStats(String uuid) {
        return statsMap.computeIfAbsent(uuid, id -> new PlayerStats(id));
    }

    // Async load
    public void loadStats(Player player) {
        String uuid = player.getUniqueId().toString();

        plugin.getAsyncStorage().loadPlayerStats(uuid, loaded -> {
            statsMap.put(uuid, loaded);
        });
    }

    // ---------------------------------------------------------
    // SAVE
    // ---------------------------------------------------------
    public void saveStats(String uuid) {
        if (!statsMap.containsKey(uuid)) return;
        plugin.getAsyncStorage().savePlayerStats(statsMap.get(uuid));
    }

    public void saveAll() {
        plugin.getAsyncStorage().saveAllPlayerStats();
    }

    // ---------------------------------------------------------
    // UPDATE STATS WHEN PLAYER WINS A REWARD
    // ---------------------------------------------------------
    public void rewardReceived(Player player, String crateId, String rewardName, Rarity rarity,
                               double money, int commandCount, boolean isJackpot) {

        String uuid = player.getUniqueId().toString();
        PlayerStats ps = getOrLoadStats(uuid);

        ps.addCrateOpen();
        ps.addReward();

        if (money > 0) ps.addMoney(money);
        for (int i = 0; i < commandCount; i++) ps.addCommandExec();

        ps.addRarity(rarity);

        if (isJackpot) {
            ps.addJackpot();
        }

        ps.setLastCrate(crateId);
        ps.setLastReward(rewardName);

        // Save async
        saveStats(uuid);
    }

    // ---------------------------------------------------------
    // LEADERBOARDS
    // ---------------------------------------------------------

    public List<PlayerStats> getTopByCrates(int limit) {
        return statsMap.values().stream()
                .sorted((a, b) -> Integer.compare(b.getCratesOpened(), a.getCratesOpened()))
                .limit(limit)
                .toList();
    }

    public List<PlayerStats> getTopByLuck(int limit) {
        return statsMap.values().stream()
                .sorted((a, b) -> Integer.compare(b.getLuckScore(), a.getLuckScore()))
                .limit(limit)
                .toList();
    }

    public List<PlayerStats> getTopByJackpots(int limit) {
        return statsMap.values().stream()
                .sorted((a, b) -> Integer.compare(b.getJackpots(), a.getJackpots()))
                .limit(limit)
                .toList();
    }

    public List<PlayerStats> getTopByRarity(Rarity rarity, int limit) {
        return statsMap.values().stream()
                .sorted((a, b) -> Integer.compare(
                        b.getRarityCount(rarity),
                        a.getRarityCount(rarity)
                ))
                .limit(limit)
                .toList();
    }

    // ---------------------------------------------------------
    // CLEAR / REMOVE STATS
    // ---------------------------------------------------------
    public void removeCache(String uuid) {
        statsMap.remove(uuid);
    }

    public Map<String, PlayerStats> getAllStats() {
        return statsMap;
    }
  }
