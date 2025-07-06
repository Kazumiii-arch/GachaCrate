// ========================================================================
// File: HologramManager.java
// Location: /src/main/java/com/kazumiii/gachacrate/holograms/HologramManager.java
// ========================================================================
package com.kazumiii.gachacrate.holograms;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

public class HologramManager {

    private final GachaCrate plugin;
    private boolean isHologramsEnabled = false;

    public HologramManager(GachaCrate plugin) {
        this.plugin = plugin;
        // Check if DecentHolograms is installed and enabled on the server
        if (plugin.getServer().getPluginManager().getPlugin("DecentHolograms") != null) {
            this.isHologramsEnabled = true;
            plugin.getLogger().info("DecentHolograms found! Physical crate holograms enabled.");
        } else {
            plugin.getLogger().warning("DecentHolograms not found. Physical crate holograms will be disabled.");
        }
    }

    /**
     * Creates a unique name for a hologram based on its block's location.
     * This is used to easily find and delete it later.
     */
    private String getHologramName(Location loc) {
        return "gachacrate-" + loc.getWorld().getName() + "-" + loc.getBlockX() + "-" + loc.getBlockY() + "-" + loc.getBlockZ();
    }

    public void createHologram(Location blockLocation, String crateName) {
        if (!isHologramsEnabled) return;

        // First, remove any existing hologram at this location to prevent duplicates.
        removeHologram(blockLocation);

        Crate crate = plugin.getCrateManager().getCrate(crateName);
        if (crate == null) return;
        
        String hologramName = getHologramName(blockLocation);
        // Position the hologram nicely above the block
        Location hologramLocation = blockLocation.clone().add(0.5, 2.0, 0.5);
        
        List<String> lines = Arrays.asList(
            "&b&l" + crate.getName() + " Crate",
            "&eLeft-Click to Preview",
            "&aRight-Click with Key to Open"
        );

        // Use the DecentHolograms API to create the hologram.
        // The third argument (false) means it is not persistent through DecentHolograms' own config,
        // as we are managing persistence ourselves in locations.yml.
        DHAPI.createHologram(hologramName, hologramLocation, false, lines);
    }

    public void removeHologram(Location blockLocation) {
        if (!isHologramsEnabled) return;

        String hologramName = getHologramName(blockLocation);
        Hologram hologram = DHAPI.getHologram(hologramName);

        // If a hologram with our unique name exists, delete it.
        if (hologram != null) {
            hologram.delete();
        }
    }
}
