package com.kazumiii.gachacrate.stats;

import com.kazumiii.gachacrate.crate.Rarity;

import java.util.HashMap;
import java.util.Map;

/**
 * Player statistics saved in JSON through AsyncStorage.
 */
public class PlayerStats {

    private String uuid;

    private int cratesOpened = 0;
    private int totalRewards = 0;

    private double moneyEarned = 0;
    private int commandsExecuted = 0;

    // Rarity counters
    private Map<String, Integer> rarityCounts = new HashMap<>();

    // Jackpot
    private int jackpots = 0;

    // Last reward information
    private String lastReward = "None";
    private String lastCrate = "None";

    public PlayerStats(String uuid) {
        this.uuid = uuid;

        // Initialize rarity counters
        for (Rarity r : Rarity.values()) {
            rarityCounts.put(r.name(), 0);
        }
    }

    // -------------------------------------------------------
    // Update Methods
    // -------------------------------------------------------

    public void addCrateOpen() {
        cratesOpened++;
    }

    public void addReward() {
        totalRewards++;
    }

    public void addMoney(double amount) {
        moneyEarned += amount;
    }

    public void addCommandExec() {
        commandsExecuted++;
    }

    public void addRarity(Rarity rarity) {
        rarityCounts.put(rarity.name(), rarityCounts.get(rarity.name()) + 1);
    }

    public void addJackpot() {
        jackpots++;
    }

    public void setLastReward(String reward) {
        this.lastReward = reward;
    }

    public void setLastCrate(String crate) {
        this.lastCrate = crate;
    }

    // -------------------------------------------------------
    // Getters
    // -------------------------------------------------------

    public String getUuid() {
        return uuid;
    }

    public int getCratesOpened() {
        return cratesOpened;
    }

    public int getTotalRewards() {
        return totalRewards;
    }

    public double getMoneyEarned() {
        return moneyEarned;
    }

    public int getCommandsExecuted() {
        return commandsExecuted;
    }

    public int getRarityCount(Rarity rarity) {
        return rarityCounts.getOrDefault(rarity.name(), 0);
    }

    public int getJackpots() {
        return jackpots;
    }

    public String getLastReward() {
        return lastReward;
    }

    public String getLastCrate() {
        return lastCrate;
    }

    // -------------------------------------------------------
    // Luck Score Formula (Premium Feature)
    // -------------------------------------------------------
    public int getLuckScore() {
        int score = 0;

        score += getRarityCount(Rarity.MYTHIC) * 10;
        score += getRarityCount(Rarity.LEGENDARY) * 5;
        score += getRarityCount(Rarity.EPIC) * 3;
        score += getRarityCount(Rarity.RARE);

        return score;
    }
}
