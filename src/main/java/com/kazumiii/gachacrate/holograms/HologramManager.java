package com.kazumiii.gachacrate.holograms;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.PhysicalCrateManager;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Shows a hologram above every physical crate when DecentHolograms is installed.
 * All DecentHolograms code lives in {@link DecentHologramsHook}, so the plugin still loads without it.
 */
public class HologramManager {

    private final GachaCrate plugin;
    private DecentHologramsHook hook;

    public HologramManager(GachaCrate plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("DecentHolograms") != null) {
            try {
                hook = new DecentHologramsHook();
                plugin.getLogger().info("DecentHolograms found. Crate holograms enabled.");
            } catch (Throwable t) {
                hook = null;
                plugin.getLogger().log(Level.WARNING, "DecentHolograms is installed but couldn't be used. Holograms disabled.", t);
            }
        } else {
            plugin.getLogger().info("DecentHolograms not found. Crate holograms are disabled.");
        }
    }

    public boolean isEnabled() {
        return hook != null;
    }

    private static String hologramName(String blockKey) {
        return "gachacrate-" + blockKey.replace(';', '-').replaceAll("[^A-Za-z0-9_-]", "_");
    }

    /** Creates (or replaces) the hologram for one crate block. Does nothing if the world isn't loaded. */
    public void create(String blockKey, String crateName) {
        if (hook == null) return;
        Crate crate = plugin.getCrateManager().getCrate(crateName);
        Location block = PhysicalCrateManager.toLocation(blockKey);
        if (crate == null || block == null) return;

        double height = plugin.getConfig().getDouble("hologram.height", 2.2);
        Location location = block.clone().add(0.5, height, 0.5);

        List<String> lines = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("hologram.lines")) {
            lines.add(fill(line, crate));
        }
        String pityLine = plugin.getConfig().getString("hologram.pity-line", "");
        if (crate.isPityEnabled() && pityLine != null && !pityLine.isEmpty()
                && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            lines.add(fill(pityLine, crate));
        }

        try {
            hook.create(hologramName(blockKey), location, lines);
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Could not create hologram for crate block " + blockKey, t);
        }
    }

    private static String fill(String line, Crate crate) {
        return line.replace("{crate}", crate.getDisplayName()).replace("{id}", crate.getId());
    }

    public void remove(String blockKey) {
        if (hook == null) return;
        try {
            hook.remove(hologramName(blockKey));
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Could not remove hologram for crate block " + blockKey, t);
        }
    }

    /** Rebuilds the holograms of one crate, e.g. after its name or pity settings changed. */
    public void refresh(Crate crate) {
        if (hook == null) return;
        for (String key : plugin.getPhysicalCrateManager().getBlocksOf(crate.getId())) {
            create(key, crate.getId());
        }
    }

    /** Rebuilds every hologram (on startup and /gacha reload). */
    public void refreshAll() {
        if (hook == null) return;
        for (Map.Entry<String, String> entry : plugin.getPhysicalCrateManager().getAllCrateBlocks().entrySet()) {
            create(entry.getKey(), entry.getValue());
        }
    }

    /** Creates holograms for crates in a world that just loaded. */
    public void refreshWorld(String worldName) {
        if (hook == null) return;
        for (Map.Entry<String, String> entry : plugin.getPhysicalCrateManager().getAllCrateBlocks().entrySet()) {
            if (PhysicalCrateManager.worldOf(entry.getKey()).equals(worldName)) {
                create(entry.getKey(), entry.getValue());
            }
        }
    }

    public void removeAll() {
        if (hook == null) return;
        for (String key : plugin.getPhysicalCrateManager().getAllCrateBlocks().keySet()) {
            remove(key);
        }
    }
}
