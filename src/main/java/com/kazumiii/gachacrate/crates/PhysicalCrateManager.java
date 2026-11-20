package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Remembers which blocks are crates (locations.yml).
 * <p>
 * Locations are stored as "world;x;y;z" text keys instead of Location objects, so crates in
 * worlds that load late (e.g. Multiverse) still work once their world is loaded.
 */
public class PhysicalCrateManager {

    private final GachaCrate plugin;
    private final File file;
    private final Map<String, String> crateBlocks = new LinkedHashMap<>(); // "world;x;y;z" -> crate id

    public PhysicalCrateManager(GachaCrate plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "locations.yml");
        load();
    }

    public static String key(Location loc) {
        World world = loc.getWorld();
        return (world == null ? "?" : world.getName()) + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    public static String key(Block block) {
        return key(block.getLocation());
    }

    /** Turns a key back into a block location, or null if the world isn't loaded. */
    public static Location toLocation(String key) {
        String[] parts = key.split(";");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            return new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String worldOf(String key) {
        int index = key.indexOf(';');
        return index < 0 ? key : key.substring(0, index);
    }

    // ------------------------------------------------------------------

    public void addCrate(Location location, String crateName) {
        String key = key(location);
        crateBlocks.put(key, crateName.toLowerCase(Locale.ROOT));
        save();
        plugin.getHologramManager().create(key, crateName);
    }

    public void removeCrate(Location location) {
        String key = key(location);
        if (crateBlocks.remove(key) != null) {
            save();
            plugin.getHologramManager().remove(key);
        }
    }

    /** Removes every block of a crate (used when the crate is deleted). Returns how many were removed. */
    public int removeAllOf(String crateId) {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, String> entry : crateBlocks.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(crateId)) toRemove.add(entry.getKey());
        }
        for (String key : toRemove) {
            crateBlocks.remove(key);
            plugin.getHologramManager().remove(key);
        }
        if (!toRemove.isEmpty()) save();
        return toRemove.size();
    }

    public String getCrateNameAt(Location location) {
        return crateBlocks.get(key(location));
    }

    public boolean isCrateLocation(Location location) {
        return location != null && crateBlocks.containsKey(key(location));
    }

    public boolean isCrateBlock(Block block) {
        return block != null && crateBlocks.containsKey(key(block));
    }

    /** All crate blocks: key -> crate id. */
    public Map<String, String> getAllCrateBlocks() {
        return Collections.unmodifiableMap(crateBlocks);
    }

    public List<String> getBlocksOf(String crateId) {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, String> entry : crateBlocks.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(crateId)) keys.add(entry.getKey());
        }
        return keys;
    }

    // ------------------------------------------------------------------

    public void load() {
        crateBlocks.clear();
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("crates");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            String crateName = section.getString(key);
            if (crateName != null && key.split(";").length == 4) {
                crateBlocks.put(key, crateName.toLowerCase(Locale.ROOT));
            } else {
                plugin.getLogger().warning("Skipping a broken entry in locations.yml: " + key);
            }
        }
        plugin.getLogger().info("Loaded " + crateBlocks.size() + " physical crate blocks.");
    }

    public void save() {
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, String> entry : crateBlocks.entrySet()) {
            config.set("crates." + entry.getKey(), entry.getValue());
        }
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save locations.yml!", e);
        }
    }
}
