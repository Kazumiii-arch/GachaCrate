// ========================================================================
// File: Crate.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Crate.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A gacha crate. Serialized to crates/<id>.yml by CrateManager.
 */
public class Crate {

    private String name;
    private String displayName;
    private ItemStack icon;

    // Pity system
    private boolean pityEnabled = false;
    private int pityThreshold = 50;
    private String pityRarityMinimum; // rarity id, e.g. "EPIC"

    // Time limits
    private boolean timeLimitEnabled = false;
    private LocalDate startDate;
    private LocalDate endDate;

    // Opening mode
    private OpeningMode openingMode = OpeningMode.KEY_IN_HAND;

    // Animation
    private AnimationType animationType = AnimationType.REEL;
    private Particle idleParticle;

    // Rewards
    private List<Reward> rewards = new ArrayList<>();

    public Crate() {} // Used by config serializers

    public Crate(String name) {
        this.name = name.toLowerCase();
        this.displayName = name;
    }

    // -------------------------------------------------------------------
    // Name / Display
    // -------------------------------------------------------------------
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    // -------------------------------------------------------------------
    // Pity System
    // -------------------------------------------------------------------
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

    // -------------------------------------------------------------------
    // Time Limits
    // -------------------------------------------------------------------
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

    // -------------------------------------------------------------------
    // Opening Mode
    // -------------------------------------------------------------------
    public OpeningMode getOpeningMode() {
        return openingMode;
    }

    public void setOpeningMode(OpeningMode openingMode) {
        this.openingMode = openingMode;
    }

    // -------------------------------------------------------------------
    // Animation
    // -------------------------------------------------------------------
    public AnimationType getAnimationType() {
        return animationType;
    }

    public void setAnimationType(AnimationType animationType) {
        this.animationType = animationType;
    }

    public Particle getIdleParticle() {
        return idleParticle;
    }

    public void setIdleParticle(Particle idleParticle) {
        this.idleParticle = idleParticle;
    }

    // -------------------------------------------------------------------
    // Rewards
    // -------------------------------------------------------------------
    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
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
}
