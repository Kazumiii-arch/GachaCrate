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
    // Key: Crate Name (lowercase), Value: Times opened
    private final Map<String, Integer> cratesOpened;
    // Key: Crate Name (lowercase), Value: Current pity count
    private final Map<String, Integer> pityCounts;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.cratesOpened = new HashMap<>();
        this.pityCounts = new HashMap<>();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

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

    public Map<String, Integer> getCratesOpenedMap() {
        return cratesOpened;
    }
    
    public Map<String, Integer> getPityCounts() {
        return pityCounts;
    }
}
