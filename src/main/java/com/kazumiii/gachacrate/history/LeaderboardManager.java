package com.kazumiii.gachacrate.history;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.player.PlayerData;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Who opened the most crates. Built from every player file in the background on startup,
 * then kept up to date as players open crates.
 */
public class LeaderboardManager {

    /** One row of the leaderboard. */
    public static final class Entry {
        public final UUID uuid;
        public final String name;
        public final int value;

        Entry(UUID uuid, String name, int value) {
            this.uuid = uuid;
            this.name = name;
            this.value = value;
        }
    }

    private static final class Totals {
        volatile String name;
        final Map<String, Integer> opened = new ConcurrentHashMap<>();

        int total() {
            int sum = 0;
            for (int count : opened.values()) sum += count;
            return sum;
        }
    }

    private static final long CACHE_MILLIS = 30_000;

    private final GachaCrate plugin;
    private final Map<UUID, Totals> totals = new ConcurrentHashMap<>();
    private final Map<String, List<Entry>> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTime = new ConcurrentHashMap<>();
    private volatile boolean loaded;

    public LeaderboardManager(GachaCrate plugin) {
        this.plugin = plugin;
    }

    /** Reads every player file in the background. */
    public void loadAsync() {
        loaded = false;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File[] files = plugin.getPlayerManager().getDataFolder().listFiles((dir, name) -> name.endsWith(".yml"));
            Map<UUID, Totals> fresh = new HashMap<>();
            if (files != null) {
                for (File file : files) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(file.getName().substring(0, file.getName().length() - 4));
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    PlayerData data = plugin.getPlayerManager().readFile(uuid);
                    fresh.put(uuid, toTotals(data));
                }
            }
            // Online players' in-memory data is newer than their files.
            fresh.putAll(totals);
            totals.clear();
            totals.putAll(fresh);
            cache.clear();
            loaded = true;
        });
    }

    private static Totals toTotals(PlayerData data) {
        Totals t = new Totals();
        t.name = data.getLastKnownName();
        t.opened.putAll(data.getCratesOpenedMap());
        return t;
    }

    /** Called after a player opens a crate. */
    public void update(PlayerData data) {
        if (data == null) return;
        totals.put(data.getPlayerUUID(), toTotals(data));
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * The top players, best first.
     *
     * @param crateId a crate id, or null for all crates together
     */
    public List<Entry> top(String crateId, int limit) {
        String key = crateId == null ? "*" : crateId.toLowerCase(Locale.ROOT);
        Long time = cacheTime.get(key);
        List<Entry> cached = cache.get(key);
        if (cached == null || time == null || System.currentTimeMillis() - time > CACHE_MILLIS) {
            List<Entry> all = new ArrayList<>();
            for (Map.Entry<UUID, Totals> e : totals.entrySet()) {
                int value = crateId == null ? e.getValue().total() : e.getValue().opened.getOrDefault(key, 0);
                if (value <= 0) continue;
                String name = e.getValue().name == null ? "Unknown" : e.getValue().name;
                all.add(new Entry(e.getKey(), name, value));
            }
            all.sort((a, b) -> b.value != a.value ? Integer.compare(b.value, a.value) : a.name.compareToIgnoreCase(b.name));
            cached = all.size() > 100 ? new ArrayList<>(all.subList(0, 100)) : all;
            cache.put(key, cached);
            cacheTime.put(key, System.currentTimeMillis());
        }
        return cached.size() > limit ? cached.subList(0, limit) : cached;
    }

    /** A player's rank (1 = best), or 0 if they haven't opened anything. */
    public int rankOf(UUID uuid, String crateId) {
        List<Entry> list = top(crateId, 100);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).uuid.equals(uuid)) return i + 1;
        }
        return 0;
    }
}
