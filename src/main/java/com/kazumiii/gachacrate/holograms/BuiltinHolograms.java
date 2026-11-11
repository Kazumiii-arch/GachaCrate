package com.kazumiii.gachacrate.holograms;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.CrateOpener;
import com.kazumiii.gachacrate.crates.PhysicalCrateManager;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holograms made from Minecraft's own text display entities (1.19.4+), so no extra plugin is needed.
 * <p>
 * Every second, each crate block in a loaded chunk is checked and its hologram is created if missing.
 * The entities are never saved with the world (they're removed when the chunk unloads or the server stops)
 * and are simply rebuilt, so there are never leftovers or duplicates.
 * An optional reward item spins above each crate and shows a different prize every few seconds.
 */
class BuiltinHolograms {

    private static final class Hologram {
        TextDisplay text;
        ItemDisplay item;
        String lastText;
        int rewardIndex;

        boolean isValid() {
            return text != null && text.isValid();
        }

        void remove() {
            if (text != null) text.remove();
            if (item != null) item.remove();
            text = null;
            item = null;
        }
    }

    private final GachaCrate plugin;
    private final HologramManager manager;
    private final Map<String, Hologram> holograms = new HashMap<>();
    private final Map<String, Long> hiddenUntil = new HashMap<>();
    private BukkitTask checkTask;
    private BukkitTask spinTask;
    private float spin;
    private int seconds;

    BuiltinHolograms(GachaCrate plugin, HologramManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    void start() {
        checkTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::check, 5L, 20L);
        if (plugin.getConfig().getBoolean("hologram.builtin.show-item", true)) {
            spinTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::spin, 5L, 2L);
        }
    }

    void stop() {
        if (checkTask != null) checkTask.cancel();
        if (spinTask != null) spinTask.cancel();
        removeAll();
    }

    /** Hides a crate's hologram for a while (so it doesn't get in the way of the in-world animation). */
    void hide(String blockKey, long millis) {
        hiddenUntil.put(blockKey, System.currentTimeMillis() + millis);
        invalidate(blockKey);
    }

    /** Forces a rebuild of one crate block's hologram on the next check. */
    void invalidate(String blockKey) {
        Hologram hologram = holograms.remove(blockKey);
        if (hologram != null) hologram.remove();
    }

    void remove(String blockKey) {
        invalidate(blockKey);
    }

    void removeAll() {
        for (Hologram hologram : holograms.values()) hologram.remove();
        holograms.clear();
    }

    // ------------------------------------------------------------------

    private void check() {
        seconds++;
        int updateEvery = Math.max(1, plugin.getConfig().getInt("hologram.builtin.update-seconds", 5));
        int itemEvery = Math.max(1, plugin.getConfig().getInt("hologram.builtin.item-change-seconds", 3));
        boolean papi = plugin.isPlaceholderApiEnabled();

        Map<String, String> blocks = plugin.getPhysicalCrateManager().getAllCrateBlocks();
        Set<String> seen = new HashSet<>();

        for (Map.Entry<String, String> entry : blocks.entrySet()) {
            String key = entry.getKey();
            seen.add(key);
            Crate crate = plugin.getCrateManager().getCrate(entry.getValue());
            Location block = PhysicalCrateManager.toLocation(key);
            Hologram hologram = holograms.get(key);

            Long hidden = hiddenUntil.get(key);
            if (hidden != null) {
                if (hidden > System.currentTimeMillis()) continue;
                hiddenUntil.remove(key);
            }

            boolean loaded = crate != null && block != null && block.getWorld() != null
                    && block.getWorld().isChunkLoaded(block.getBlockX() >> 4, block.getBlockZ() >> 4);
            if (!loaded) {
                if (hologram != null) {
                    hologram.remove();
                    holograms.remove(key);
                }
                continue;
            }

            if (hologram == null || !hologram.isValid()) {
                if (hologram != null) hologram.remove();
                hologram = spawn(block, crate);
                holograms.put(key, hologram);
            }

            // Refresh the text now and then (it may contain PlaceholderAPI placeholders like leaderboards).
            if (seconds % updateEvery == 0 || hologram.lastText == null) {
                String text = buildText(crate, papi);
                if (!text.equals(hologram.lastText)) {
                    hologram.text.setText(text);
                    hologram.lastText = text;
                }
            }

            // Show a different reward on the spinning item every few seconds.
            if (hologram.item != null && seconds % itemEvery == 0) {
                List<Reward> rewards = crate.getValidRewards();
                if (!rewards.isEmpty()) {
                    hologram.rewardIndex = (hologram.rewardIndex + 1) % rewards.size();
                    hologram.item.setItemStack(rewards.get(hologram.rewardIndex).getDisplayItem());
                }
            }
        }

        // Crate blocks that were removed.
        for (String key : new ArrayList<>(holograms.keySet())) {
            if (!seen.contains(key)) holograms.remove(key).remove();
        }
    }

    private String buildText(Crate crate, boolean papi) {
        List<String> lines = manager.linesFor(crate, false);
        String text = Text.color(String.join("\n", lines));
        if (papi && text.indexOf('%') >= 0) text = PlaceholderHook.applyGlobal(text);
        return text;
    }

    private Hologram spawn(Location block, Crate crate) {
        World world = block.getWorld();
        Hologram hologram = new Hologram();
        double height = plugin.getConfig().getDouble("hologram.builtin.height", 1.8);

        TextDisplay text = world.spawn(block.clone().add(0.5, height, 0.5), TextDisplay.class);
        configure(text);
        text.setBillboard(Display.Billboard.CENTER);
        text.setShadowed(true);
        if (!plugin.getConfig().getBoolean("hologram.builtin.background", true)) {
            text.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
        }
        hologram.text = text;

        if (plugin.getConfig().getBoolean("hologram.builtin.show-item", true) && !crate.getValidRewards().isEmpty()) {
            ItemDisplay item = world.spawn(block.clone().add(0.5, 1.45, 0.5), ItemDisplay.class);
            configure(item);
            item.setBillboard(Display.Billboard.FIXED);
            item.setItemStack(CrateOpener.weightedPick(crate.getValidRewards()).getDisplayItem());
            hologram.item = item;
        }
        return hologram;
    }

    private static void configure(Entity entity) {
        entity.setPersistent(false); // Never saved with the world, so no leftovers.
        entity.setInvulnerable(true);
        entity.setSilent(true);
    }

    private void spin() {
        spin = (spin + 6) % 360;
        for (Hologram hologram : holograms.values()) {
            if (hologram.item == null || !hologram.item.isValid()) continue;
            Location at = hologram.item.getLocation();
            at.setYaw(spin);
            hologram.item.teleport(at);
        }
    }
}
