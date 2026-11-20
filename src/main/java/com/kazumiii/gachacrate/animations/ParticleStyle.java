package com.kazumiii.gachacrate.animations;

import org.bukkit.Material;

/**
 * Shapes the idle particles can make around a crate block.
 */
public enum ParticleStyle {
    HELIX("Helix", Material.CHAIN, "Two streams spiral up and down."),
    RING("Halo", Material.GOLD_NUGGET, "A spinning ring floats above the crate."),
    ORBIT("Orbit", Material.ENDER_PEARL, "Three sparks orbit like planets."),
    FOUNTAIN("Fountain", Material.WATER_BUCKET, "Particles bubble up from the top."),
    PULSE("Pulse", Material.HEART_OF_THE_SEA, "Waves ripple out from the crate."),
    TORNADO("Vortex", Material.BLAZE_ROD, "A cone that swirls upward.");

    private final String displayName;
    private final Material icon;
    private final String description;

    ParticleStyle(String displayName, Material icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public String getDescription() { return description; }

    public ParticleStyle next() {
        ParticleStyle[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
