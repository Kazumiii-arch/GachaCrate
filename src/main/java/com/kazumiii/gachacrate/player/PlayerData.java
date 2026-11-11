package com.kazumiii.gachacrate.player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player stats and pity counters. Crate names are stored in lower case.
 */
public class PlayerData {

    private final UUID playerUUID;
    private final Map<String, Integer> cratesOpened = new HashMap<>();
    private final Map<String, Integer> pityCounts = new HashMap<>();
    private boolean hasReceivedFirstKey;
    private boolean dirty;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    private static String id(String crateName) {
        return crateName.toLowerCase(Locale.ROOT);
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Map<String, Integer> getCratesOpenedMap() {
        return cratesOpened;
    }

    public Map<String, Integer> getPityCounts() {
        return pityCounts;
    }

    public boolean hasReceivedFirstKey() {
        return hasReceivedFirstKey;
    }

    public void setHasReceivedFirstKey(boolean value) {
        this.hasReceivedFirstKey = value;
        dirty = true;
    }

    public int getCratesOpened(String crateName) {
        return cratesOpened.getOrDefault(id(crateName), 0);
    }

    public int getTotalCratesOpened() {
        int total = 0;
        for (int count : cratesOpened.values()) total += count;
        return total;
    }

    public void incrementCratesOpened(String crateName) {
        cratesOpened.merge(id(crateName), 1, Integer::sum);
        dirty = true;
    }

    /** Opens since the last reward of the pity rarity (or better). */
    public int getPityCount(String crateName) {
        return pityCounts.getOrDefault(id(crateName), 0);
    }

    public void incrementPityCount(String crateName) {
        pityCounts.merge(id(crateName), 1, Integer::sum);
        dirty = true;
    }

    public void resetPityCount(String crateName) {
        pityCounts.put(id(crateName), 0);
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
