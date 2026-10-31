// ========================================================================
// File: Crate.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Crate.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.animations.AnimationType;
import org.bukkit.Particle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A gacha crate: a named collection of rewards plus all of its configurable
 * settings (opening mode, animation, pity system, time limit, idle particles).
 * Persisted by CrateManager in crates.yml.
 */
public class Crate {

    private final String name;

    // Rewards contained in this crate
    private final List<Reward> rewards = new ArrayList<>();

    // How players open this crate
    private OpeningMode openingMode = OpeningMode.KEY_IN_HAND;

    // Which opening animation is played
    private AnimationType animationType = AnimationType.REEL;

    // Optional idle particle displayed above the physical crate block
    private Particle idleParticle;

    // Pity system (guaranteed rare reward after N opens)
    private boolean pityEnabled = false;
    private int pityThreshold = 50;
    private String pityRarityMinimum;

    // Time limit (seasonal / event crates)
    private boolean timeLimitEnabled = false;
    private LocalDate startDate;
    private LocalDate endDate;

    public Crate(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // ------------------------------------------------------------------
    // Rewards
    // ------------------------------------------------------------------
    public List<Reward> getRewards() {
        return rewards;
    }

    public void addReward(Reward reward) {
        rewards.add(reward);
    }

    public void removeReward(Reward reward) {
        rewards.remove(reward);
    }

    public Reward getRewardById(UUID id) {
        for (Reward reward : rewards) {
            if (reward.getUniqueId().equals(id)) {
                return reward;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Opening mode
    // ------------------------------------------------------------------
    public OpeningMode getOpeningMode() {
        return openingMode;
    }

    public void setOpeningMode(OpeningMode openingMode) {
        this.openingMode = openingMode;
    }

    // ------------------------------------------------------------------
    // Animation
    // ------------------------------------------------------------------
    public AnimationType getAnimationType() {
        return animationType;
    }

    public void setAnimationType(AnimationType animationType) {
        this.animationType = animationType;
    }

    // ------------------------------------------------------------------
    // Idle particle
    // ------------------------------------------------------------------
    public Particle getIdleParticle() {
        return idleParticle;
    }

    public void setIdleParticle(Particle idleParticle) {
        this.idleParticle = idleParticle;
    }

    // ------------------------------------------------------------------
    // Pity system
    // ------------------------------------------------------------------
    public boolean isPityEnabled() {
        return pityEnabled;
    }

    public void setPityEnabled(boolean pityEnabled) {
        this.pityEnabled = pityEnabled;
    }

    public int getPityThreshold() {
        return pityThreshold;
    }

    public void setPityThreshold(int pityThreshold) {
        this.pityThreshold = pityThreshold;
    }

    public String getPityRarityMinimum() {
        return pityRarityMinimum;
    }

    public void setPityRarityMinimum(String pityRarityMinimum) {
        this.pityRarityMinimum = pityRarityMinimum;
    }

    // ------------------------------------------------------------------
    // Time limit
    // ------------------------------------------------------------------
    public boolean isTimeLimitEnabled() {
        return timeLimitEnabled;
    }

    public void setTimeLimitEnabled(boolean timeLimitEnabled) {
        this.timeLimitEnabled = timeLimitEnabled;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
