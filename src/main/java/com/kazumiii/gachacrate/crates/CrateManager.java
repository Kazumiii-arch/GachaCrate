// ========================================================================
// File: CrateManager.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/CrateManager.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.animations.AnimationType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrateManager {

    private final GachaCrate plugin;
    private final Map<String, Crate> crates = new HashMap<>();
    private File cratesFile;
    private FileConfiguration cratesConfig;

    public CrateManager(GachaCrate plugin) {
        this.plugin = plugin;
        setupConfigurationFile();
        loadCrates();
    }
    
    private void setupConfigurationFile() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
        cratesFile = new File(plugin.getDataFolder(), "crates.yml");
        if (!cratesFile.exists()) {
            try {
                cratesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create crates.yml file!");
                e.printStackTrace();
            }
        }
        cratesConfig = YamlConfiguration.loadConfiguration(cratesFile);
    }

    public void createCrate(String name) {
        Crate crate = new Crate(name);
        crates.put(name.toLowerCase(), crate);
        cratesConfig.createSection("crates." + name);
        saveCrates();
    }

    public Crate getCrate(String name) {
        return crates.get(name.toLowerCase());
    }
    
    public Map<String, Crate> getAllCrates() {
        return Collections.unmodifiableMap(crates);
    }

    public void deleteCrate(String crateName) {
        crates.remove(crateName.toLowerCase());
        cratesConfig.set("crates." + crateName, null);
        saveCrates();
    }

    public void saveCrates() {
        cratesConfig.set("crates", null);

        for (Crate crate : crates.values()) {
            String cratePath = "crates." + crate.getName();
            
            // Save all crate-level settings
            cratesConfig.set(cratePath + ".animation-type", crate.getAnimationType().name());
            if (crate.isPityEnabled()) {
                cratesConfig.set(cratePath + ".pity-system.enabled", true);
                cratesConfig.set(cratePath + ".pity-system.threshold", crate.getPityThreshold());
                cratesConfig.set(cratePath + ".pity-system.pity-rarity-minimum", crate.getPityRarityMinimum());
            }
            if (crate.isTimeLimitEnabled()) {
                cratesConfig.set(cratePath + ".time-limit.enabled", true);
                cratesConfig.set(cratePath + ".time-limit.start-date", crate.getStartDate().toString());
                cratesConfig.set(cratePath + ".time-limit.end-date", crate.getEndDate().toString());
            }

            // Save all rewards
            ConfigurationSection rewardsSection = cratesConfig.createSection(cratePath + ".rewards");
            for (Reward reward : crate.getRewards()) {
                String rewardId = reward.getUniqueId().toString();
                String rewardPath = rewardId + ".";
                
                rewardsSection.set(rewardPath + "type", reward.getType().name());
                rewardsSection.set(rewardPath + "rarity", reward.getRarity().getId());
                rewardsSection.set(rewardPath + "chance", reward.getChance());

                if (reward.getType() == RewardType.ITEM) {
                    rewardsSection.set(rewardPath + "item", reward.getItem());
                } else {
                    rewardsSection.set(rewardPath + "command", reward.getCommand());
                    if (reward.getPreviewItem() != null) {
                        rewardsSection.set(rewardPath + "preview-item", reward.getPreviewItem());
                    }
                }
            }
        }
        try {
            cratesConfig.save(cratesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save crates to crates.yml!");
            e.printStackTrace();
        }
    }

    public void loadCrates() {
        if (!cratesConfig.contains("crates")) return;
        ConfigurationSection cratesSection = cratesConfig.getConfigurationSection("crates");
        for (String crateName : cratesSection.getKeys(false)) {
            Crate crate = new Crate(crateName);
            String cratePath = "crates." + crateName;

            // Load animation type
            try {
                AnimationType animType = AnimationType.valueOf(cratesConfig.getString(cratePath + ".animation-type", "REEL"));
                crate.setAnimationType(animType);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid animation-type for crate '" + crateName + "'. Defaulting to REEL.");
                crate.setAnimationType(AnimationType.REEL);
            }

            // Load pity system settings
            if (cratesConfig.getBoolean(cratePath + ".pity-system.enabled", false)) {
                crate.setPityEnabled(true);
                crate.setPityThreshold(cratesConfig.getInt(cratePath + ".pity-system.threshold", 100));
                crate.setPityRarityMinimum(cratesConfig.getString(cratePath + ".pity-system.pity-rarity-minimum"));
            }

            // Load time limit settings
            if (cratesConfig.getBoolean(cratePath + ".time-limit.enabled", false)) {
                try {
                    LocalDate start = LocalDate.parse(cratesConfig.getString(cratePath + ".time-limit.start-date"));
                    LocalDate end = LocalDate.parse(cratesConfig.getString(cratePath + ".time-limit.end-date"));
                    crate.setTimeLimitEnabled(true);
                    crate.setStartDate(start);
                    crate.setEndDate(end);
                } catch (DateTimeParseException | NullPointerException e) {
                    plugin.getLogger().warning("Invalid date format for crate '" + crateName + "'. Time limit disabled. Use YYYY-MM-DD.");
                }
            }
            
            // Load rewards
            if (cratesSection.contains(crateName + ".rewards")) {
                ConfigurationSection rewardsSection = cratesSection.getConfigurationSection(crateName + ".rewards");
                for (String rewardId : rewardsSection.getKeys(false)) {
                    UUID uniqueId = UUID.fromString(rewardId);
                    String rewardPath = rewardId + ".";
                    
                    RewardType type = RewardType.valueOf(rewardsSection.getString(rewardPath + "type", "ITEM"));
                    Rarity rarity = plugin.getRarityManager().getRarity(rewardsSection.getString(rewardPath + "rarity"));
                    double chance = rewardsSection.getDouble(rewardPath + "chance");

                    if (rarity == null) {
                        plugin.getLogger().warning("Could not load a reward in crate '" + crateName + "' because its rarity is invalid. Skipping.");
                        continue;
                    }

                    Reward reward;
                    if (type == RewardType.ITEM) {
                        ItemStack item = rewardsSection.getItemStack(rewardPath + "item");
                        reward = new Reward(uniqueId, type, item, null, null, rarity, chance);
                    } else {
                        String command = rewardsSection.getString(rewardPath + "command");
                        ItemStack previewItem = rewardsSection.getItemStack(rewardPath + "preview-item");
                        reward = new Reward(uniqueId, type, null, command, previewItem, rarity, chance);
                    }
                    crate.addReward(reward);
                }
            }
            crates.put(crateName.toLowerCase(), crate);
        }
        plugin.getLogger().info("Loaded " + crates.size() + " crates from config.");
    }
                                   }
                                                           
