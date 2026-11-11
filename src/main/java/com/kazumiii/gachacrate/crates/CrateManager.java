package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.animations.AnimationType;
import com.kazumiii.gachacrate.animations.ParticleStyle;
import com.kazumiii.gachacrate.util.Compat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Loads, stores and saves crates (crates.yml).
 * The file format is the same as earlier versions, so existing crates keep working.
 */
public class CrateManager {

    /** Crate names become YAML keys and permission nodes, so only safe characters are allowed. */
    public static final Pattern VALID_NAME = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");

    private final GachaCrate plugin;
    private final Map<String, Crate> crates = new LinkedHashMap<>();
    private final File cratesFile;

    public CrateManager(GachaCrate plugin) {
        this.plugin = plugin;
        this.cratesFile = new File(plugin.getDataFolder(), "crates.yml");
        loadCrates();
    }

    // ------------------------------------------------------------------
    // Access
    // ------------------------------------------------------------------

    public Crate getCrate(String name) {
        return name == null ? null : crates.get(name.toLowerCase(Locale.ROOT));
    }

    public Map<String, Crate> getAllCrates() {
        return Collections.unmodifiableMap(crates);
    }

    public List<String> getCrateNames() {
        List<String> names = new ArrayList<>();
        for (Crate crate : crates.values()) names.add(crate.getName());
        return names;
    }

    public static boolean isValidName(String name) {
        return name != null && VALID_NAME.matcher(name).matches();
    }

    public Crate createCrate(String name) {
        Crate crate = new Crate(name);
        crates.put(crate.getId(), crate);
        registerPermission(crate);
        saveCrates();
        return crate;
    }

    /** Deletes a crate, its physical blocks and its holograms. */
    public void deleteCrate(String name) {
        Crate crate = crates.remove(name.toLowerCase(Locale.ROOT));
        if (crate == null) return;
        plugin.getPhysicalCrateManager().removeAllOf(crate.getId());
        unregisterPermission(crate);
        saveCrates();
    }

    // ------------------------------------------------------------------
    // Permissions: every crate gets gacha.user.open.<crate>, given to everyone by default.
    // ------------------------------------------------------------------

    private void registerPermission(Crate crate) {
        PluginManager pm = plugin.getServer().getPluginManager();
        if (pm.getPermission(crate.getOpenPermission()) != null) return;
        Permission permission = new Permission(crate.getOpenPermission(),
                "Allows opening the " + crate.getName() + " crate.", PermissionDefault.TRUE);
        try {
            pm.addPermission(permission);
            // gacha.user.open.* also gives this crate's permission.
            permission.addParent("gacha.user.open.*", true);
        } catch (IllegalArgumentException ignored) {
            // Already registered.
        }
    }

    private void unregisterPermission(Crate crate) {
        plugin.getServer().getPluginManager().removePermission(crate.getOpenPermission());
    }

    // ------------------------------------------------------------------
    // Saving
    // ------------------------------------------------------------------

    public void saveCrates() {
        FileConfiguration config = new YamlConfiguration();

        for (Crate crate : crates.values()) {
            ConfigurationSection section = config.createSection("crates." + crate.getName());

            if (crate.getRawDisplayName() != null) section.set("display-name", crate.getRawDisplayName());
            section.set("opening-mode", crate.getOpeningMode().name());
            section.set("animation-type", crate.getAnimationType().name());
            if (crate.getIdleParticle() != null) section.set("idle-particle", crate.getIdleParticle().name());
            section.set("idle-particle-style", crate.getParticleStyle().name());

            section.set("pity-system.enabled", crate.isPityEnabled());
            section.set("pity-system.threshold", crate.getPityThreshold());
            section.set("pity-system.pity-rarity-minimum", crate.getPityRarityMinimum());

            section.set("daily-free-keys", crate.getDailyFreeKeys());
            section.set("key-price", crate.getKeyPrice());

            section.set("time-limit.enabled", crate.isTimeLimitEnabled());
            if (crate.getStartDate() != null) section.set("time-limit.start-date", crate.getStartDate().toString());
            if (crate.getEndDate() != null) section.set("time-limit.end-date", crate.getEndDate().toString());

            ConfigurationSection rewards = section.createSection("rewards");
            for (Reward reward : crate.getRewards()) {
                ConfigurationSection r = rewards.createSection(reward.getUniqueId().toString());
                r.set("type", reward.getType().name());
                r.set("rarity", reward.getRarity() != null ? reward.getRarity().getId() : null);
                r.set("chance", reward.getChance());
                if (reward.getType() == RewardType.ITEM) {
                    r.set("item", reward.getItem());
                } else {
                    r.set("commands", reward.getCommands());
                    if (reward.getPreviewItem() != null) r.set("preview-item", reward.getPreviewItem());
                }
            }
        }

        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            // Write to a temp file first, so a crash mid-save can't wipe crates.yml.
            File temp = new File(plugin.getDataFolder(), "crates.yml.tmp");
            config.save(temp);
            if (cratesFile.exists() && !cratesFile.delete()) {
                plugin.getLogger().warning("Could not replace crates.yml; saving directly.");
                config.save(cratesFile);
                temp.delete();
                return;
            }
            if (!temp.renameTo(cratesFile)) config.save(cratesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save crates.yml!", e);
        }
    }

    // ------------------------------------------------------------------
    // Loading
    // ------------------------------------------------------------------

    public void loadCrates() {
        for (Crate crate : crates.values()) unregisterPermission(crate);
        crates.clear();

        if (!cratesFile.exists()) {
            plugin.getLogger().info("No crates yet. Create one with /gacha create <name>.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(cratesFile);
        ConfigurationSection cratesSection = config.getConfigurationSection("crates");
        if (cratesSection == null) return;

        for (String crateName : cratesSection.getKeys(false)) {
            ConfigurationSection section = cratesSection.getConfigurationSection(crateName);
            if (section == null) continue;
            try {
                Crate crate = loadCrate(crateName, section);
                crates.put(crate.getId(), crate);
                registerPermission(crate);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Could not load crate '" + crateName + "'. Skipping it.", e);
            }
        }
        plugin.getLogger().info("Loaded " + crates.size() + " crates.");
    }

    private Crate loadCrate(String crateName, ConfigurationSection section) {
        Crate crate = new Crate(crateName);
        crate.setDisplayName(section.getString("display-name"));
        crate.setOpeningMode(parseEnum(OpeningMode.class, section.getString("opening-mode"), OpeningMode.PHYSICAL_BLOCKS));
        crate.setAnimationType(parseEnum(AnimationType.class, section.getString("animation-type"), AnimationType.REEL));

        crate.setParticleStyle(parseEnum(ParticleStyle.class, section.getString("idle-particle-style"), ParticleStyle.HELIX));
        String particleName = section.getString("idle-particle");
        if (particleName != null) {
            crate.setIdleParticle(Compat.particle(particleName));
            if (crate.getIdleParticle() == null) {
                plugin.getLogger().warning("Crate '" + crateName + "': idle particle '" + particleName + "' doesn't exist on this server version.");
            }
        }

        crate.setPityEnabled(section.getBoolean("pity-system.enabled", false));
        crate.setPityThreshold(section.getInt("pity-system.threshold", 50));
        crate.setPityRarityMinimum(section.getString("pity-system.pity-rarity-minimum"));
        if (crate.isPityEnabled() && plugin.getRarityManager().getRarity(crate.getPityRarityMinimum()) == null) {
            plugin.getLogger().warning("Crate '" + crateName + "': pity is on but its minimum rarity isn't set or doesn't exist. Pity won't trigger until you set it.");
        }

        crate.setDailyFreeKeys(section.getInt("daily-free-keys", 0));
        crate.setKeyPrice(section.getDouble("key-price", 0));

        crate.setTimeLimitEnabled(section.getBoolean("time-limit.enabled", false));
        crate.setStartDate(parseDate(crateName, section.getString("time-limit.start-date")));
        crate.setEndDate(parseDate(crateName, section.getString("time-limit.end-date")));

        ConfigurationSection rewards = section.getConfigurationSection("rewards");
        if (rewards != null) {
            for (String rewardId : rewards.getKeys(false)) {
                Reward reward = loadReward(crateName, rewardId, rewards.getConfigurationSection(rewardId));
                if (reward != null) crate.addReward(reward);
            }
        }
        return crate;
    }

    private Reward loadReward(String crateName, String rewardId, ConfigurationSection r) {
        if (r == null) return null;
        UUID uuid;
        try {
            uuid = UUID.fromString(rewardId);
        } catch (IllegalArgumentException e) {
            uuid = UUID.randomUUID();
        }

        RewardType type = parseEnum(RewardType.class, r.getString("type"), RewardType.ITEM);
        Rarity rarity = plugin.getRarityManager().getRarity(r.getString("rarity"));
        if (rarity == null) {
            plugin.getLogger().warning("Crate '" + crateName + "': a reward has unknown rarity '" + r.getString("rarity")
                    + "'. It was given the lowest rarity instead.");
            rarity = plugin.getRarityManager().getLowest();
        }
        double chance = r.getDouble("chance", 1.0);

        if (type == RewardType.ITEM) {
            ItemStack item = r.getItemStack("item");
            if (item == null) {
                plugin.getLogger().warning("Crate '" + crateName + "': an item reward has no item. Skipping it.");
                return null;
            }
            return new Reward(uuid, type, item, null, null, rarity, chance);
        }

        List<String> commands = new ArrayList<>(r.getStringList("commands"));
        String single = r.getString("command"); // Format used by older versions.
        if (commands.isEmpty() && single != null) commands.add(single);
        return new Reward(uuid, type, null, commands, r.getItemStack("preview-item"), rarity, chance);
    }

    private LocalDate parseDate(String crateName, String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            plugin.getLogger().warning("Crate '" + crateName + "': invalid date '" + value + "'. Use YYYY-MM-DD.");
            return null;
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, E fallback) {
        if (value == null) return fallback;
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
