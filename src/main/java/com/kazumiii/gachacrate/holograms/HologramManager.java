package com.kazumiii.gachacrate.holograms;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.PhysicalCrateManager;
import com.kazumiii.gachacrate.crates.Rarity;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Shows a hologram above every physical crate using a hologram plugin.
 * <p>
 * {@code hologram.provider} in config.yml:
 * <ul>
 *   <li>{@code auto}: DecentHolograms if installed, otherwise FancyHolograms, otherwise none.</li>
 *   <li>{@code decentholograms} / {@code fancyholograms}: use that plugin.</li>
 *   <li>{@code commands}: any hologram plugin, through console commands set in config.yml.</li>
 *   <li>{@code none}: no holograms.</li>
 * </ul>
 */
public class HologramManager {

    private final GachaCrate plugin;
    private HologramProvider provider;

    public HologramManager(GachaCrate plugin) {
        this.plugin = plugin;
        setup();
    }

    /** Picks the hologram plugin from config.yml. Called on startup and reload. */
    public void setup() {
        provider = null;
        String choice = plugin.getConfig().getString("hologram.provider", "auto").toLowerCase(Locale.ROOT);
        boolean hasDecent = isInstalled("DecentHolograms");
        boolean hasFancy = isInstalled("FancyHolograms");

        switch (choice) {
            case "none": case "off": case "false":
                plugin.getLogger().info("Crate holograms are turned off.");
                return;
            case "commands": case "custom":
                provider = new CommandHologramProvider(plugin);
                break;
            case "decentholograms": case "decent":
                if (hasDecent) provider = tryDecent();
                else plugin.getLogger().warning("hologram.provider is 'decentholograms' but DecentHolograms isn't installed.");
                break;
            case "fancyholograms": case "fancy":
                if (hasFancy) provider = tryFancy();
                else plugin.getLogger().warning("hologram.provider is 'fancyholograms' but FancyHolograms isn't installed.");
                break;
            default: // auto
                if (hasDecent) provider = tryDecent();
                if (provider == null && hasFancy) provider = tryFancy();
                break;
        }

        if (provider != null) {
            plugin.getLogger().info("Crate holograms: using " + provider.getName() + ".");
        } else {
            plugin.getLogger().info("Crate holograms: no hologram plugin found (DecentHolograms or FancyHolograms). Holograms are off.");
        }
    }

    private boolean isInstalled(String name) {
        return plugin.getServer().getPluginManager().getPlugin(name) != null;
    }

    private HologramProvider tryDecent() {
        try {
            return new DecentHologramsHook();
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "DecentHolograms is installed but couldn't be used.", t);
            return null;
        }
    }

    private HologramProvider tryFancy() {
        try {
            return new FancyHologramsHook();
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "FancyHolograms is installed but couldn't be used (GachaCrate supports FancyHolograms 2.x).", t);
            return null;
        }
    }

    public boolean isEnabled() {
        return provider != null;
    }

    public String getProviderName() {
        return provider == null ? "none" : provider.getName();
    }

    private static String hologramName(String blockKey) {
        return "gachacrate-" + blockKey.replace(';', '-').replaceAll("[^A-Za-z0-9_-]", "_");
    }

    /** The hologram lines for a crate, with '&' color codes (hologram plugins color them). */
    private List<String> linesFor(Crate crate) {
        List<String> lines = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("hologram.lines")) lines.add(fill(line, crate));
        if (crate.isPityEnabled()) {
            boolean papi = isInstalled("PlaceholderAPI");
            String pityLine = papi
                    ? plugin.getConfig().getString("hologram.pity-line", "%gachacrate_pity_text_{id}%")
                    : plugin.getConfig().getString("hologram.pity-line-no-papi", "&dGuaranteed {rarity} &dwithin {threshold} opens");
            if (pityLine != null && !pityLine.isEmpty()) lines.add(fill(pityLine, crate));
        }
        return lines;
    }

    private String fill(String line, Crate crate) {
        Rarity rarity = plugin.getRarityManager().getRarity(crate.getPityRarityMinimum());
        return line.replace("{crate}", crate.getDisplayName().replace('§', '&'))
                .replace("{id}", crate.getId())
                .replace("{rewards}", String.valueOf(crate.getValidRewards().size()))
                .replace("{rarity}", rarity == null ? "?" : rarity.getDisplayName().replace('§', '&'))
                .replace("{threshold}", String.valueOf(crate.getPityThreshold()))
                .replace("{price}", crate.getKeyPrice() > 0 ? plugin.getShopManager().format(crate.getKeyPrice()) : "-");
    }

    /** Creates (or replaces) the hologram for one crate block. Does nothing if the world isn't loaded. */
    public void create(String blockKey, String crateName) {
        if (provider == null) return;
        Crate crate = plugin.getCrateManager().getCrate(crateName);
        Location block = PhysicalCrateManager.toLocation(blockKey);
        if (crate == null || block == null) return;
        double height = plugin.getConfig().getDouble("hologram.height", 2.2);
        try {
            provider.create(hologramName(blockKey), block.clone().add(0.5, height, 0.5), linesFor(crate));
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Could not create hologram for crate block " + blockKey, t);
        }
    }

    public void remove(String blockKey) {
        if (provider == null) return;
        try {
            provider.remove(hologramName(blockKey));
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Could not remove hologram for crate block " + blockKey, t);
        }
    }

    /** Hides a crate's hologram for a while (so it doesn't cover the in-world animation), then brings it back. */
    public void hide(Location crateBlock, long millis) {
        if (provider == null || crateBlock == null) return;
        String key = PhysicalCrateManager.key(crateBlock);
        String crateId = plugin.getPhysicalCrateManager().getCrateNameAt(crateBlock);
        if (crateId == null) return;
        remove(key);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getPhysicalCrateManager().getAllCrateBlocks().containsKey(key)) create(key, crateId);
        }, Math.max(1, millis / 50));
    }

    /** Rebuilds the holograms of one crate, e.g. after its name or pity settings changed. */
    public void refresh(Crate crate) {
        for (String key : plugin.getPhysicalCrateManager().getBlocksOf(crate.getId())) create(key, crate.getId());
    }

    /** Rebuilds every hologram (on startup and /gacha reload). */
    public void refreshAll() {
        for (Map.Entry<String, String> entry : plugin.getPhysicalCrateManager().getAllCrateBlocks().entrySet()) {
            create(entry.getKey(), entry.getValue());
        }
    }

    /** Creates holograms for crates in a world that just loaded. */
    public void refreshWorld(String worldName) {
        for (Map.Entry<String, String> entry : plugin.getPhysicalCrateManager().getAllCrateBlocks().entrySet()) {
            if (PhysicalCrateManager.worldOf(entry.getKey()).equals(worldName)) create(entry.getKey(), entry.getValue());
        }
    }

    public void removeAll() {
        if (provider == null) return;
        for (String key : plugin.getPhysicalCrateManager().getAllCrateBlocks().keySet()) remove(key);
    }

    /** Removes all holograms on shutdown. */
    public void shutdown() {
        removeAll();
    }
}
