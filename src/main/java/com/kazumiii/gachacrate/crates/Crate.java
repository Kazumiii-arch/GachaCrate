package com.kazumiii.gachacrate.crate;

import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Crate {

    private String id;
    private String displayName;
    private ItemStack icon;

    private String description = "A special gacha crate.";

    private List<Reward> rewards = new ArrayList<>();

    // Crate type (NORMAL, EVENT, SEASONAL, LIMITED)
    private CrateType type = CrateType.NORMAL;

    // Optional expiration date (for event/seasonal crates)
    private LocalDate expirationDate;

    // Color theme for GUI menus
    private String color = "§b";

    // Animation type (WHEEL, SLOT, ROLLBAR, SIMPLE)
    private SpinAnimation animation = SpinAnimation.ROLLBAR;

    // Sound keys from sounds.yml
    private String soundOpen = "crate-open";
    private String soundSpin = "crate-spin";
    private String soundWin = "crate-win";

    // Hologram displayed above physical crate block
    private String hologramText = "§e§lGacha Crate";

    // Optional permission needed to open
    private String permission = null;

    // Tags for sorting / seasonal filtering
    private List<String> tags = new ArrayList<>();


    // -------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------
    public Crate() {}

    public Crate(String id, String displayName, ItemStack icon) {
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.icon = icon;
    }

    // -------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id.toLowerCase();
    }

    public String getDisplayName() {
        return displayName != null ? displayName : id;
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

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public void addReward(Reward reward) {
        rewards.add(reward);
    }

    public CrateType getType() {
        return type;
    }

    public void setType(CrateType type) {
        this.type = type;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getColor() {
        return color == null ? "§f" : color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public SpinAnimation getAnimation() {
        return animation;
    }

    public void setAnimation(SpinAnimation animation) {
        this.animation = animation;
    }

    public String getSoundOpen() {
        return soundOpen;
    }

    public void setSoundOpen(String soundOpen) {
        this.soundOpen = soundOpen;
    }

    public String getSoundSpin() {
        return soundSpin;
    }

    public void setSoundSpin(String soundSpin) {
        this.soundSpin = soundSpin;
    }

    public String getSoundWin() {
        return soundWin;
    }

    public void setSoundWin(String soundWin) {
        this.soundWin = soundWin;
    }

    public String getHologramText() {
        return hologramText;
    }

    public void setHologramText(String hologramText) {
        this.hologramText = hologramText;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<String> getTags() {
        return tags == null ? new ArrayList<>() : tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }


    // -------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------

    public boolean isLimited() {
        return type == CrateType.EVENT || type == CrateType.SEASONAL || type == CrateType.LIMITED;
    }

    public boolean hasExpired() {
        if (expirationDate == null) return false;
        return LocalDate.now().isAfter(expirationDate);
    }
}
