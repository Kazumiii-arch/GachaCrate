package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.animations.AnimationType;
import com.kazumiii.gachacrate.animations.ParticleStyle;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Particle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * A crate and all its settings.
 * {@link #getName()} is the id used in commands and files; {@link #getDisplayName()} is what players see.
 */
public class Crate {

    private final String name;
    private String displayName;
    private final List<Reward> rewards = new ArrayList<>();

    private boolean pityEnabled = false;
    private int pityThreshold = 50;
    private String pityRarityMinimum;

    private boolean timeLimitEnabled = false;
    private LocalDate startDate;
    private LocalDate endDate;

    private int dailyFreeKeys = 0;
    private double keyPrice = 0;

    private AnimationType animationType = AnimationType.REEL;
    private OpeningMode openingMode = OpeningMode.PHYSICAL_BLOCKS;
    private Particle idleParticle;
    private ParticleStyle particleStyle = ParticleStyle.HELIX;

    public Crate(String name) {
        this.name = name;
    }

    // --- Identity ---
    public String getName() { return name; }

    /** Lower-case id used for keys, permissions and lookups. */
    public String getId() { return name.toLowerCase(Locale.ROOT); }

    /** The colored name players see. Falls back to the crate id. */
    public String getDisplayName() {
        return displayName == null || displayName.isEmpty() ? "§b" + name : Text.color(displayName);
    }

    /** The raw display name with '&' codes, or null if not set. */
    public String getRawDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    // --- Rewards ---
    public List<Reward> getRewards() { return rewards; }
    public void addReward(Reward reward) { rewards.add(reward); }
    public void removeReward(Reward reward) { rewards.remove(reward); }

    public Reward getRewardById(UUID id) {
        for (Reward reward : rewards) {
            if (reward.getUniqueId().equals(id)) return reward;
        }
        return null;
    }

    /** Rewards that can actually be won. */
    public List<Reward> getValidRewards() {
        List<Reward> valid = new ArrayList<>();
        for (Reward reward : rewards) {
            if (reward.isValid()) valid.add(reward);
        }
        return valid;
    }

    public double getTotalChance() {
        double total = 0;
        for (Reward reward : rewards) {
            if (reward.isValid()) total += reward.getChance();
        }
        return total;
    }

    /** Real chance of winning a reward, from 0 to 100. */
    public double getPercent(Reward reward) {
        double total = getTotalChance();
        if (total <= 0 || !reward.isValid()) return 0;
        return reward.getChance() / total * 100.0;
    }

    // --- Pity ---
    public boolean isPityEnabled() { return pityEnabled; }
    public int getPityThreshold() { return pityThreshold; }
    public String getPityRarityMinimum() { return pityRarityMinimum; }
    public void setPityEnabled(boolean pityEnabled) { this.pityEnabled = pityEnabled; }
    public void setPityThreshold(int pityThreshold) { this.pityThreshold = Math.max(1, pityThreshold); }
    public void setPityRarityMinimum(String pityRarityMinimum) { this.pityRarityMinimum = pityRarityMinimum; }

    // --- Time limit ---
    public boolean isTimeLimitEnabled() { return timeLimitEnabled; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setTimeLimitEnabled(boolean timeLimitEnabled) { this.timeLimitEnabled = timeLimitEnabled; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    /** True if the crate can be opened today (or has no time limit). */
    public boolean isAvailable(LocalDate today) {
        if (!timeLimitEnabled) return true;
        if (startDate != null && today.isBefore(startDate)) return false;
        return endDate == null || !today.isAfter(endDate);
    }

    // --- Daily keys & shop ---
    /** Free keys each player can claim once per day with /gacha daily. 0 = off. */
    public int getDailyFreeKeys() { return dailyFreeKeys; }
    public void setDailyFreeKeys(int dailyFreeKeys) { this.dailyFreeKeys = Math.max(0, dailyFreeKeys); }

    /** Price of one key in the shop (needs Vault). 0 = not for sale. */
    public double getKeyPrice() { return keyPrice; }
    public void setKeyPrice(double keyPrice) { this.keyPrice = Math.max(0, keyPrice); }

    // --- Looks ---
    public AnimationType getAnimationType() { return animationType; }
    public OpeningMode getOpeningMode() { return openingMode; }
    public Particle getIdleParticle() { return idleParticle; }
    public void setAnimationType(AnimationType animationType) { this.animationType = animationType; }
    public void setOpeningMode(OpeningMode openingMode) { this.openingMode = openingMode; }
    public void setIdleParticle(Particle idleParticle) { this.idleParticle = idleParticle; }
    public ParticleStyle getParticleStyle() { return particleStyle; }
    public void setParticleStyle(ParticleStyle particleStyle) { this.particleStyle = particleStyle == null ? ParticleStyle.HELIX : particleStyle; }

    public String getOpenPermission() {
        return "gacha.user.open." + getId();
    }
}
