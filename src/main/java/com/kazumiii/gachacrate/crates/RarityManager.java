// ========================================================================
// File: RarityManager.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/RarityManager.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RarityManager {

    private final GachaCrate plugin;
    private final Map<String, Rarity> rarities = new HashMap<>();

    public RarityManager(GachaCrate plugin) {
        this.plugin = plugin;
        loadRarities();
    }

    private void loadRarities() {
        plugin.saveDefaultConfig();
        ConfigurationSection raritySection = plugin.getConfig().getConfigurationSection("rarities");
        if (raritySection == null) {
            plugin.getLogger().severe("'rarities' section not found in config.yml! Disabling plugin.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        for (String key : raritySection.getKeys(false)) {
            String id = key.toUpperCase();
            String displayName = raritySection.getString(key + ".display-name");
            String colorName = raritySection.getString(key + ".color");
            int order = raritySection.getInt(key + ".order");

            try {
                ChatColor color = ChatColor.valueOf(colorName.toUpperCase());
                Rarity rarity = new Rarity(id, displayName, color, order);
                rarities.put(id, rarity);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid color '" + colorName + "' for rarity '" + id + "'. Skipping.");
            }
        }
        plugin.getLogger().info("Loaded " + rarities.size() + " rarities.");
    }

    public Rarity getRarity(String id) {
        return rarities.get(id.toUpperCase());
    }

    public Set<String> getRarityIds() {
        return rarities.keySet();
    }
}
