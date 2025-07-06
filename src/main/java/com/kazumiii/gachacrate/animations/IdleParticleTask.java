// ========================================================================
// File: IdleParticleTask.java
// Location: /src/main/java/com/kazumiii/gachacrate/animations/IdleParticleTask.java
// ========================================================================
package com.kazumiii.gachacrate.animations;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class IdleParticleTask extends BukkitRunnable {

    private final GachaCrate plugin;
    private double angle = 0; // A single angle ticker for all crates to stay in sync

    public IdleParticleTask(GachaCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // We get all physical crate locations every tick. This is efficient and ensures
        // that newly placed or removed crates are instantly included or excluded.
        Map<Location, String> locations = plugin.getPhysicalCrateManager().getAllCrateLocations();
        if (locations.isEmpty()) return;

        // Increment the angle for the next tick, creating the rotation.
        angle += Math.PI / 24;
        if (angle > 2 * Math.PI) {
            angle = 0; // Reset angle to create a seamless loop
        }

        for (Map.Entry<Location, String> entry : locations.entrySet()) {
            Location loc = entry.getKey();
            Crate crate = plugin.getCrateManager().getCrate(entry.getValue());

            if (crate != null && crate.getIdleParticle() != null) {
                spawnHelixParticles(loc, crate.getIdleParticle());
            }
        }
    }

    /**
     * Spawns two intertwining spirals of particles that move up and down the block.
     * @param blockLocation The location of the crate block.
     * @param particle The particle type to spawn.
     */
    private void spawnHelixParticles(Location blockLocation, Particle particle) {
        Location center = blockLocation.clone().add(0.5, 0, 0.5);
        double radius = 0.6;

        // The 'y' position moves up and down using a sine wave based on the angle.
        // This creates a smooth "breathing" or "bobbing" effect.
        double y = 1.0 + Math.sin(angle * 2);

        // First helix strand
        double x1 = center.getX() + radius * Math.cos(angle);
        double z1 = center.getZ() + radius * Math.sin(angle);
        Location particleLoc1 = new Location(center.getWorld(), x1, center.getY() + y, z1);

        // Second helix strand, opposite to the first, creating the intertwining effect.
        double x2 = center.getX() + radius * Math.cos(angle + Math.PI);
        double z2 = center.getZ() + radius * Math.sin(angle + Math.PI);
        Location particleLoc2 = new Location(center.getWorld(), x2, center.getY() + y, z2);

        // Spawn the particles with no extra motion data (the last four 0s).
        center.getWorld().spawnParticle(particle, particleLoc1, 1, 0, 0, 0, 0);
        center.getWorld().spawnParticle(particle, particleLoc2, 1, 0, 0, 0, 0);
    }
}
