package com.kazumiii.gachacrate.animations;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.PhysicalCrateManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

/**
 * Draws two spiralling particle streams around crate blocks that have an idle effect.
 * Runs on the main thread and skips crates with no players nearby or in unloaded chunks.
 */
public class IdleParticleTask extends BukkitRunnable {

    private static final double VIEW_DISTANCE_SQUARED = 32 * 32;

    private final GachaCrate plugin;
    private double angle = 0;

    public IdleParticleTask(GachaCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Map<String, String> blocks = plugin.getPhysicalCrateManager().getAllCrateBlocks();
        if (blocks.isEmpty()) return;

        angle += Math.PI / 16;
        if (angle > 2 * Math.PI) angle -= 2 * Math.PI;

        for (Map.Entry<String, String> entry : blocks.entrySet()) {
            Crate crate = plugin.getCrateManager().getCrate(entry.getValue());
            if (crate == null || crate.getIdleParticle() == null) continue;

            Location block = PhysicalCrateManager.toLocation(entry.getKey());
            if (block == null) continue;
            World world = block.getWorld();
            if (world == null || !world.isChunkLoaded(block.getBlockX() >> 4, block.getBlockZ() >> 4)) continue;
            if (!anyPlayerNear(world, block)) continue;

            spawnHelix(block, crate.getIdleParticle());
        }
    }

    private boolean anyPlayerNear(World world, Location location) {
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= VIEW_DISTANCE_SQUARED) return true;
        }
        return false;
    }

    private void spawnHelix(Location block, Particle particle) {
        World world = block.getWorld();
        double cx = block.getX() + 0.5;
        double cz = block.getZ() + 0.5;
        double radius = 0.6;
        double y = block.getY() + 1.0 + 0.5 * Math.sin(angle * 2);
        try {
            world.spawnParticle(particle, cx + radius * Math.cos(angle), y, cz + radius * Math.sin(angle), 1, 0, 0, 0, 0);
            world.spawnParticle(particle, cx - radius * Math.cos(angle), y, cz - radius * Math.sin(angle), 1, 0, 0, 0, 0);
        } catch (IllegalArgumentException ignored) {
            // A particle that needs extra data slipped through; skip it.
        }
    }
}
