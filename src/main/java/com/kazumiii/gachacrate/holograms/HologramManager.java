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
 * Shows a hologram above every physical crate.
 * <p>
 * Two ways to do it, picked with {@code hologram.provider} in config.yml:
 * <ul>
 *   <li><b>builtin</b>: Minecraft's own text display entities. No extra plugin needed,
 *       and it can show a spinning reward item above the crate.</li>
 *   <li><b>decentholograms</b>: uses the DecentHolograms plugin (supports per-player placeholders).</li>
 * </ul>
 * {@code auto} (the default) uses the built-in holograms.
 */
public class HologramManager {

    private final GachaCrate plugin;
    private DecentHologramsHook decent;
    private BuiltinHolograms builtin;

    public HologramManager(GachaCrate plugin) {
        this.plugin = plugin;
        setup();
    }

    /** Picks the provider from config.yml. Called on startup and reload. */
    public void setup() {
        if (builtin != null) builtin.stop();
        builtin = null;
        decent = null;

        String provider = plugin.getConfig().getString("hologram.provider", "auto").toLowerCase(Locale.ROOT);
        boolean hasDecent = plugin.getServer().getPluginManager().getPlugin("DecentHolograms") != null;

        if (provider.equals("none") || provider.equals("off") || provider.equals("false")) {
            plugin.getLogger().info("Crate holograms are turned off.");
            return;
        }
        if (provider.equals("decentholograms") && hasDecent) {
            try {
                decent = new DecentHologramsHook();
                plugin.getLogger().info("Using DecentHolograms for crate holograms.");
                return;
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "DecentHolograms couldn't be used. Falling back to built-in holograms.", t);
            }
        } else if (provider.equals("decentholograms")) {
            plugin.getLogger().warning("hologram.provider is 'decentholograms' but it isn't installed. Using built-in holograms.");
        }
        builtin = new BuiltinHolograms(plugin, this);
        builtin.start();
        plugin.getLogger().info("Using built-in crate holograms.");
    }

    public boolean isEnabled() {
        return decent != null || builtin != null;
    }

    private static String hologramName(String blockKey) {
        return "gachacrate-" + blockKey.replace(';', '-').replaceAll("[^A-Za-z0-9_-]", "_");
    }

    /** The hologram lines for a crate, with {crate}, {id}, {rewards}, {rarity} and {threshold} filled in. */
    List<String> linesFor(Crate crate, boolean perPlayerPlaceholders) {
        List<String> lines = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("hologram.lines")) lines.add(fill(line, crate));

        if (crate.isPityEnabled()) {
            String pityLine;
            if (perPlayerPlaceholders && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                pityLine = plugin.getConfig().getString("hologram.pity-line", "");
            } else {
                // Built-in holograms look the same for everyone, so they show the rule instead of a personal counter.
                pityLine = plugin.getConfig().getString("hologram.builtin.pity-line",
                        "&dGuaranteed {rarity} &dwithin {threshold} opens");
            }
            if (pityLine != null && !pityLine.isEmpty()) lines.add(fill(pityLine, crate));
        }
        return lines;
    }

    private String fill(String line, Crate crate) {
        Rarity rarity = plugin.getRarityManager().getRarity(crate.getPityRarityMinimum());
        return line.replace("{crate}", crate.getDisplayName())
                .replace("{id}", crate.getId())
                .replace("{rewards}", String.valueOf(crate.getValidRewards().size()))
                .replace("{rarity}", rarity == null ? "?" : rarity.getDisplayName())
                .replace("{threshold}", String.valueOf(crate.getPityThreshold()));
    }

    /** Creates (or replaces) the hologram for one crate block. Does nothing if the world isn't loaded. */
    public void create(String blockKey, String crateName) {
        if (builtin != null) {
            builtin.invalidate(blockKey);
            return; // The built-in holograms rebuild themselves within a second.
        }
        if (decent == null) return;
        Crate crate = plugin.getCrateManager().getCrate(crateName);
        Location block = PhysicalCrateManager.toLocation(blockKey);
        if (crate == null || block == null) return;
        double height = plugin.getConfig().getDouble("hologram.height", 2.2);
        try {
            decent.create(hologramName(blockKey), block.clone().add(0.5, height, 0.5), linesFor(crate, true));
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Could not create hologram for crate block " + blockKey, t);
        }
    }

    public void remove(String blockKey) {
        if (builtin != null) {
            builtin.remove(blockKey);
            return;
        }
        if (decent == null) return;
        try {
            decent.remove(hologramName(blockKey));
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Could not remove hologram for crate block " + blockKey, t);
        }
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
        if (builtin != null) {
            builtin.removeAll();
            return;
        }
        for (String key : plugin.getPhysicalCrateManager().getAllCrateBlocks().keySet()) remove(key);
    }

    /** Hides a crate block's built-in hologram for a few seconds (used by the in-world animation). */
    public void hide(Location crateBlock, long millis) {
        if (builtin != null && crateBlock != null) builtin.hide(PhysicalCrateManager.key(crateBlock), millis);
    }

    /** Stops the built-in hologram tasks and removes their entities (on shutdown). */
    public void shutdown() {
        if (builtin != null) builtin.stop();
        else removeAll();
    }
}
