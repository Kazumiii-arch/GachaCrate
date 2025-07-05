// ========================================================================
// File: Crate.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/Crate.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import org.bukkit.inventory.ItemStack;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Crate {
    private final String name;
    private final List<Reward> rewards;
    
    private boolean pityEnabled;
    private int pityThreshold;
    private String pityRarityMinimum;

    private boolean timeLimitEnabled;
    private LocalDate startDate;
    private LocalDate endDate;

    public Crate(String name) {
        this.name = name;
        this.rewards = new ArrayList<>();
        this.pityEnabled = false;
        this.timeLimitEnabled = false;
    }
    
    public String getName() { return name; }
    public List<Reward> getRewards() { return rewards; }
    public boolean isPityEnabled() { return pityEnabled; }
    public int getPityThreshold() { return pityThreshold; }
    public String getPityRarityMinimum() { return pityRarityMinimum; }
    public boolean isTimeLimitEnabled() { return timeLimitEnabled; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    public void setPityEnabled(boolean pityEnabled) { this.pityEnabled = pityEnabled; }
    public void setPityThreshold(int pityThreshold) { this.pityThreshold = pityThreshold; }
    public void setPityRarityMinimum(String pityRarityMinimum) { this.pityRarityMinimum = pityRarityMinimum; }
    public void setTimeLimitEnabled(boolean timeLimitEnabled) { this.timeLimitEnabled = timeLimitEnabled; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public void addReward(Reward reward) { this.rewards.add(reward); }
    public void removeReward(Reward reward) { this.rewards.remove(reward); }
}
