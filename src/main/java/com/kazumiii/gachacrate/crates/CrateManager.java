// ========================================================================
// File: CrateManager.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/CrateManager.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads, caches and persists all crates. Each crate is stored as
 * crates/<name>.yml inside the plugin data folder.
 */
public class CrateManager {

    private final GachaCrate plugin;
    private final Map<String, Crate> crates = new HashMap<>();
    private final File cratesFolder;

    public CrateManager(GachaCrate plugin) {
        this.plugin = plugin;
        this.cratesFolder = new File(plugin.getDataFolder(), "crates");
        if (!cratesFolder.exists() && !cratesFolder.mkdirs()) {
            plugin.getLogger().severe("Could not create the crates data folder!");
        }
    }

    // -------------------------------------------------------------------
    // Registry API
    // -------------------------------------------------------------------
    public Crate createCrate(String name) {
        Crate crate = new Crate(name);
        crates.put(crate.getName(), crate);
        saveCrate(crate);
        return crate;
    }

    /** Case-insensitive lookup. */
    public Crate getCrate(String name) {
        return name == null ? null : crates.get(name.toLowerCase());
    }

    public Map<String, Crate> getAllCrates() {
        return crates;
    }

    public boolean deleteCrate(String name) {
        Crate removed = name == null ? null : crates.remove(name.toLowerCase());
        if (removed == null) return false;
        File file = new File(cratesFolder, removed.getName() + ".yml");
        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning("Could not delete crate file: " + file.getName());
        }
        return true;
    }

    // -------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------
    public void loadCrates() {
        crates.clear();

        File[] files = cratesFolder.listFiles((dir, fileName) -> fileName.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                Crate crate = deserializeCrate(config);
                if (crate != null) {
                    crates.put(crate.getName(), crate);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load crate file: " + file.getName());
                e.printStackTrace();
            }
        }
        plugin.getLogger().info("Loaded " + crates.size() + " crate(s).");
    }

    public void saveCrates() {
        for (Crate crate : crates.values()) {
            saveCrate(crate);
        }
    }

    public void saveCrate(Crate crate) {
        YamlConfiguration config = new YamlConfiguration();
        serializeCrate(crate, config);
        try {
            config.save(new File(cratesFolder, crate.getName() + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save crate: " + crate.getName());
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------
    private void serializeCrate(Crate crate, YamlConfiguration config) {
        config.set("name", crate.getName());
        config.set("display-name", crate.getDisplayName());

        config.set("pity.enabled", crate.isPityEnabled());
        config.set("pity.threshold", crate.getPityThreshold());
        config.set("pity.rarity-minimum", crate.getPityRarityMinimum());

        config.set("time-limit.enabled", crate.isTimeLimitEnabled());
        if (crate.getStartDate() != null) config.set("time-limit.start-date", crate.getStartDate().toString());
        if (crate.getEndDate() != null) config.set("time-limit.end-date", crate.getEndDate().toString());

        config.set("opening-mode", crate.getOpeningMode().name());
        config.set("animation-type", crate.getAnimationType().name());
        config.set("idle-particle", crate.getIdleParticle() == null ? null : crate.getIdleParticle().name());

        int index = 0;
        for (Reward reward : crate.getRewards()) {
            String base = "rewards." + index++;
            config.set(base + ".type", reward.getType().name());
            config.set(base + ".chance", reward.getChance());
            config.set(base + ".rarity", reward.getRarityId());
            if (reward.getItem() != null) {
                config.set(base + ".item", Base64.getEncoder().encodeToString(reward.getItem().serializeAsBytes()));
            }
            if (reward.getPreviewItem() != null) {
                config.set(base + ".preview-item", Base64.getEncoder().encodeToString(reward.getPreviewItem().serializeAsBytes()));
            }
            if (reward.getCommand() != null) {
                config.set(base + ".command", reward.getCommand());
            }
        }
    }

    private Crate deserializeCrate(YamlConfiguration config) {
        String name = config.getString("name");
        if (name == null) return null;

        Crate crate = new Crate(name);
        crate.setDisplayName(config.getString("display-name", name));

        crate.setPityEnabled(config.getBoolean("pity.enabled", false));
        crate.setPityThreshold(config.getInt("pity.threshold", 50));
        crate.setPityRarityMinimum(config.getString("pity.rarity-minimum"));

        crate.setTimeLimitEnabled(config.getBoolean("time-limit.enabled", false));

        String startDate = config.getString("time-limit.start-date");
        if (startDate != null) {
            try { crate.setStartDate(LocalDate.parse(startDate)); } catch (DateTimeParseException ignored) {}
        }
        String endDate = config.getString("time-limit.end-date");
        if (endDate != null) {
            try { crate.setEndDate(LocalDate.parse(endDate)); } catch (DateTimeParseException ignored) {}
        }

        try {
            crate.setOpeningMode(OpeningMode.valueOf(config.getString("opening-mode", "KEY_IN_HAND")));
        } catch (IllegalArgumentException ignored) {}

        try {
            crate.setAnimationType(AnimationType.valueOf(config.getString("animation-type", "REEL")));
        } catch (IllegalArgumentException ignored) {}

        String particle = config.getString("idle-particle");
        if (particle != null) {
            try { crate.setIdleParticle(Particle.valueOf(particle)); } catch (IllegalArgumentException ignored) {}
        }

        ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
        if (rewardsSection != null) {
            for (String key : rewardsSection.getKeys(false)) {
                String base = "rewards." + key;
                Reward reward = new Reward();

                try {
                    reward.setType(RewardType.valueOf(config.getString(base + ".type", "ITEM")));
                } catch (IllegalArgumentException ignored) {}

                reward.setChance(config.getDouble(base + ".chance", 100.0));
                reward.setRarityId(config.getString(base + ".rarity", "COMMON"));

                String itemData = config.getString(base + ".item");
                if (itemData != null) {
                    try {
                        reward.setItem(ItemStack.deserializeBytes(Base64.getDecoder().decode(itemData)));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to decode reward item in crate " + name);
                    }
                }

                String previewData = config.getString(base + ".preview-item");
                if (previewData != null) {
                    try {
                        reward.setPreviewItem(ItemStack.deserializeBytes(Base64.getDecoder().decode(previewData)));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to decode preview item in crate " + name);
                    }
                }

                String command = config.getString(base + ".command");
                if (command != null) {
                    reward.setCommand(command);
                }

                crate.addReward(reward);
            }
        }

        return crate;
    }
}
