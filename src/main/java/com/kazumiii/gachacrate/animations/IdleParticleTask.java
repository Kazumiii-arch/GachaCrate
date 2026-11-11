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
 * Draws the idle particle effect of every crate block (style + particle chosen per crate).
 * Runs on the main thread every 2 ticks, and skips crates with no players nearby
 * or in unloaded chunks, so it stays cheap on big servers.
 */
public class IdleParticleTask extends BukkitRunnable {

    private final GachaCrate plugin;
    private int tick = 0;

    public IdleParticleTask(GachaCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("particles.enabled", true)) return;
        Map<String, String> blocks = plugin.getPhysicalCrateManager().getAllCrateBlocks();
        if (blocks.isEmpty()) return;
        tick++;

        double view = plugin.getConfig().getDouble("particles.view-distance", 32);
        double viewSquared = view * view;

        for (Map.Entry<String, String> entry : blocks.entrySet()) {
            Crate crate = plugin.getCrateManager().getCrate(entry.getValue());
            if (crate == null || crate.getIdleParticle() == null) continue;

            Location block = PhysicalCrateManager.toLocation(entry.getKey());
            if (block == null) continue;
            World world = block.getWorld();
            if (world == null || !world.isChunkLoaded(block.getBlockX() >> 4, block.getBlockZ() >> 4)) continue;
            if (!anyPlayerNear(world, block, viewSquared)) continue;

            draw(world, block.getX() + 0.5, block.getY(), block.getZ() + 0.5, crate.getIdleParticle(), crate.getParticleStyle());
        }
    }

    private static boolean anyPlayerNear(World world, Location location, double viewSquared) {
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= viewSquared) return true;
        }
        return false;
    }

    private void draw(World world, double cx, double by, double cz, Particle particle, ParticleStyle style) {
        double t = tick * Math.PI / 16;
        try {
            switch (style) {
                case RING: {
                    double y = by + 1.15 + 0.05 * Math.sin(t);
                    for (int i = 0; i < 3; i++) {
                        double a = t + i * (2 * Math.PI / 3);
                        point(world, particle, cx + 0.55 * Math.cos(a), y, cz + 0.55 * Math.sin(a));
                    }
                    break;
                }
                case ORBIT: {
                    for (int i = 0; i < 3; i++) {
                        double a = t * 1.3 + i * (2 * Math.PI / 3);
                        double tilt = Math.sin(a + i) * 0.35;
                        point(world, particle, cx + 0.8 * Math.cos(a), by + 0.6 + tilt, cz + 0.8 * Math.sin(a));
                    }
                    break;
                }
                case FOUNTAIN: {
                    world.spawnParticle(particle, cx, by + 1.05, cz, 2, 0.15, 0.05, 0.15, 0.03);
                    break;
                }
                case PULSE: {
                    int phase = tick % 20;
                    if (phase < 10) {
                        double r = 0.3 + phase * 0.09;
                        for (int i = 0; i < 8; i++) {
                            double a = i * Math.PI / 4;
                            point(world, particle, cx + r * Math.cos(a), by + 0.1, cz + r * Math.sin(a));
                        }
                    }
                    break;
                }
                case TORNADO: {
                    double h = (tick % 20) / 20.0;
                    double r = 0.2 + h * 0.6;
                    double a = t * 2;
                    point(world, particle, cx + r * Math.cos(a), by + 1.0 + h * 1.2, cz + r * Math.sin(a));
                    point(world, particle, cx - r * Math.cos(a), by + 1.0 + h * 1.2, cz - r * Math.sin(a));
                    break;
                }
                case HELIX:
                default: {
                    double y = by + 1.0 + 0.5 * Math.sin(t * 2);
                    point(world, particle, cx + 0.6 * Math.cos(t), y, cz + 0.6 * Math.sin(t));
                    point(world, particle, cx - 0.6 * Math.cos(t), y, cz - 0.6 * Math.sin(t));
                    break;
                }
            }
        } catch (IllegalArgumentException ignored) {
            // A particle that needs extra data slipped through; skip it.
        }
    }

    private static void point(World world, Particle particle, double x, double y, double z) {
        world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
    }

    /** A quick burst at a crate block when it's opened. */
    public static void openBurst(Location crateBlock, Particle particle) {
        World world = crateBlock.getWorld();
        if (world == null) return;
        double cx = crateBlock.getX() + 0.5;
        double cy = crateBlock.getY() + 1.0;
        double cz = crateBlock.getZ() + 0.5;
        try {
            for (int i = 0; i < 16; i++) {
                double a = i * Math.PI / 8;
                world.spawnParticle(particle, cx + 0.7 * Math.cos(a), cy, cz + 0.7 * Math.sin(a), 1, 0, 0.05, 0, 0.02);
            }
            world.spawnParticle(particle, cx, cy + 0.3, cz, 10, 0.2, 0.3, 0.2, 0.05);
        } catch (IllegalArgumentException ignored) {
            // Skip particles that need extra data.
        }
    }
}
