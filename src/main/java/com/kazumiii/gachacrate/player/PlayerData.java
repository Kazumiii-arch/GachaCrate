// ========================================================================
// File: PlayerData.java
// Location: /src/main/java/com/kazumiii/gachacrate/player/PlayerData.java
// ========================================================================
package com.kazumiii.gachacrate.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    private final UUID playerUUID;
    private final Map<String, Integer> cratesOpened;
    private final Map<String, Integer> pityCounts;
    private boolean hasReceivedFirstKey; // Tracks if the player has seen the welcome message

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.cratesOpened = new HashMap<>();
        this.pityCounts = new HashMap<>();
        this.hasReceivedFirstKey = false; // Default to false for new players
    }

    // --- Getters ---
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

    // --- Setters ---
    public void setHasReceivedFirstKey(boolean hasReceivedFirstKey) {
        this.hasReceivedFirstKey = hasReceivedFirstKey;
    }
    
    // --- Logic Methods ---
    public int getCratesOpened(String crateName) {
        return cratesOpened.getOrDefault(crateName.toLowerCase(), 0);
    }

    public void incrementCratesOpened(String crateName) {
        cratesOpened.put(crateName.toLowerCase(), getCratesOpened(crateName) + 1);
    }

    public int getPityCount(String crateName) {
        return pityCounts.getOrDefault(crateName.toLowerCase(), 0);
    }

    public void incrementPityCount(String crateName) {
        pityCounts.put(crateName.toLowerCase(), getPityCount(crateName) + 1);
    }

    public void resetPityCount(String crateName) {
        pityCounts.put(crateName.toLowerCase(), 0);
    }
}
