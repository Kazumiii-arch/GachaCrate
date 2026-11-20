package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads the rarity list from config.yml.
 */
public class RarityManager {

    private final GachaCrate plugin;
    private final Map<String, Rarity> rarities = new LinkedHashMap<>();

    public RarityManager(GachaCrate plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        rarities.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("rarities");
        if (section == null || section.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("No 'rarities' found in config.yml. Using a single COMMON rarity.");
            rarities.put("COMMON", new Rarity("COMMON", "&fCommon", ChatColor.WHITE, 1));
            return;
        }

        List<Rarity> loaded = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            String id = key.toUpperCase(Locale.ROOT);
            String displayName = section.getString(key + ".display-name", id);
            String colorName = section.getString(key + ".color", "WHITE");
            int order = section.getInt(key + ".order", loaded.size() + 1);

            ChatColor color;
            try {
                color = ChatColor.valueOf(colorName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid color '" + colorName + "' for rarity '" + id + "'. Using WHITE.");
                color = ChatColor.WHITE;
            }
            loaded.add(new Rarity(id, displayName, color, order));
        }

        loaded.sort(Comparator.comparingInt(Rarity::getOrder));
        for (Rarity rarity : loaded) rarities.put(rarity.getId(), rarity);
        plugin.getLogger().info("Loaded " + rarities.size() + " rarities.");
    }

    public Rarity getRarity(String id) {
        if (id == null) return null;
        return rarities.get(id.toUpperCase(Locale.ROOT));
    }

    /** All rarities, worst first. */
    public List<Rarity> getRarities() {
        return new ArrayList<>(rarities.values());
    }

    public List<String> getRarityIds() {
        return getRarities().stream().map(Rarity::getId).collect(Collectors.toList());
    }

    public Rarity getLowest() {
        return rarities.values().iterator().next();
    }

    /** The next rarity in the list (wraps around). Used by click-to-cycle buttons. */
    public Rarity next(Rarity current, boolean forward) {
        List<Rarity> list = getRarities();
        int index = current == null ? -1 : list.indexOf(getRarity(current.getId()));
        int size = list.size();
        int nextIndex = forward ? (index + 1) % size : (index - 1 + size) % size;
        return list.get(nextIndex);
    }
}
