package com.kazumiii.gachacrate.holograms;

import org.bukkit.Location;

import java.util.List;

/**
 * A hologram plugin GachaCrate can use (DecentHolograms, FancyHolograms, or any plugin via commands).
 */
interface HologramProvider {

    String getName();

    /** Creates or replaces a hologram. Lines use '&' color codes. */
    void create(String name, Location location, List<String> lines);

    void remove(String name);
}
