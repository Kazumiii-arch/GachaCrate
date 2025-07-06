// ========================================================================
// File: Crate.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Crate.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.animations.AnimationType;
import org.bukkit.inventory.ItemStack;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Crate {
    private final String name;
    private final List<Reward> rewards;
    
    // Advanced Feature Fields
    private boolean pityEnabled;
    private int pityThreshold;
    private String pityRarityMinimum;
    private boolean timeLimitEnabled;
    private LocalDate startDate;
    private LocalDate endDate;
    private AnimationType animationType;

    public Crate(String name) {
        this.name = name;
        this.rewards = new ArrayList<>();
        // Default values for new crates
        this.pityEnabled = false;
        this.timeLimitEnabled = false;
        this.animationType = AnimationType.REEL; // Default animation
    }
    
    // --- Getters ---
    public String getName() { return name; }
    public List<Reward> getRewards() { return rewards; }
    public boolean isPityEnabled() { return pityEnabled; }
    public int getPityThreshold() { return pityThreshold; }
    public String getPityRarityMinimum() { return pityRarityMinimum; }
    public boolean isTimeLimitEnabled() { return timeLimitEnabled; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public AnimationType getAnimationType() { return animationType; }

    // --- Setters ---
    public void setPityEnabled(boolean pityEnabled) { this.pityEnabled = pityEnabled; }
    public void setPityThreshold(int pityThreshold) { this.pityThreshold = pityThreshold; }
    public void setPityRarityMinimum(String pityRarityMinimum) { this.pityRarityMinimum = pityRarityMinimum; }
    public void setTimeLimitEnabled(boolean timeLimitEnabled) { this.timeLimitEnabled = timeLimitEnabled; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setAnimationType(AnimationType animationType) { this.animationType = animationType; }

    // --- Logic Methods ---
    public void addReward(Reward reward) { this.rewards.add(reward); }
    public void removeReward(Reward reward) { this.rewards.remove(reward); }

    /**
     * Finds a specific reward within this crate by its unique ID.
     * @param id The UUID of the reward to find.
     * @return The Reward object, or null if not found.
     */
    public Reward getRewardById(UUID id) {
        return rewards.stream()
                .filter(r -> r.getUniqueId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
