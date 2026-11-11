package com.kazumiii.gachacrate.holograms;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;

import java.util.List;

/**
 * The only class that touches the DecentHolograms API.
 * It is only created when DecentHolograms is installed.
 */
class DecentHologramsHook {

    DecentHologramsHook() {
        // Fails fast (NoClassDefFoundError) if the API isn't really there.
        DHAPI.class.getName();
    }

    void create(String name, Location location, List<String> lines) {
        remove(name);
        // 'false' = don't save to DecentHolograms' own files; GachaCrate recreates them on startup.
        DHAPI.createHologram(name, location, false, lines);
    }

    void remove(String name) {
        Hologram hologram = DHAPI.getHologram(name);
        if (hologram != null) hologram.delete();
    }
}
