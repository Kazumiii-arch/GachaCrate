// ========================================================================
// File: PhysicalCrateManager.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/PhysicalCrateManager.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PhysicalCrateManager {

    private final GachaCrate plugin;
    private final Map<Location, String> crateLocations = new HashMap<>();
    private File locationsFile;
    private FileConfiguration locationsConfig;

    public PhysicalCrateManager(GachaCrate plugin) {
        this.plugin = plugin;
        setupFile();
        loadLocations();
    }

    private void setupFile() {
        locationsFile = new File(plugin.getDataFolder(), "locations.yml");
        if (!locationsFile.exists()) {
            try {
                locationsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create locations.yml!");
                e.printStackTrace();
            }
        }
        locationsConfig = YamlConfiguration.loadConfiguration(locationsFile);
    }

    public void addCrate(Location location, String crateName) {
        if (crateName == null) return;
        // Normalize so stored names always match the CrateManager registry keys.
        String normalizedName = crateName.toLowerCase(Locale.ROOT);
        crateLocations.put(location, normalizedName);
        locationsConfig.set("crates." + locationToPath(location), normalizedName);
        saveFile();
        plugin.getHologramManager().createHologram(location, normalizedName);
    }

    public void removeCrate(Location location) {
        String crateName = getCrateNameAt(location);
        if (crateName != null) {
            crateLocations.remove(location);
            locationsConfig.set("crates." + locationToPath(location), null);
            saveFile();
            plugin.getHologramManager().removeHologram(location);
        }
    }

    public String getCrateNameAt(Location location) {
        return crateLocations.get(location);
    }

    public boolean isCrateLocation(Location location) {
        return crateLocations.containsKey(location);
    }

    /**
     * Read-only access to the map of crate locations, used by the
     * IdleParticleTask and the hologram cleanup on disable.
     * @return An unmodifiable map of all physical crate locations.
     */
    public Map<Location, String> getAllCrateLocations() {
        return Collections.unmodifiableMap(crateLocations);
    }

    private void loadLocations() {
        ConfigurationSection section = locationsConfig.getConfigurationSection("crates");
        if (section == null) return;
        for (String path : section.getKeys(false)) {
            Location loc = pathTolocation(path);
            String crateName = section.getString(path);
            if (loc != null && crateName != null) {
                String normalizedName = crateName.toLowerCase(Locale.ROOT);
                crateLocations.put(loc, normalizedName);
                plugin.getHologramManager().createHologram(loc, normalizedName);
            }
        }
        plugin.getLogger().info("Loaded " + crateLocations.size() + " physical crate locations.");
    }

    private void saveFile() {
        try {
            locationsConfig.save(locationsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save locations.yml!");
            e.printStackTrace();
        }
    }

    private String locationToPath(Location loc) {
        return Objects.requireNonNull(loc.getWorld()).getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    private Location pathTolocation(String path) {
        try {
            String[] parts = path.split(";");
            return new Location(plugin.getServer().getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse a location from locations.yml: " + path);
            return null;
        }
    }
}
