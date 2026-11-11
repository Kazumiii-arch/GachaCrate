package com.kazumiii.gachacrate.animations;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.CrateOpener;
import com.kazumiii.gachacrate.crates.OpeningSession;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.util.Compat;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * The in-world show: the prize rises out of the crate block, spins while cycling through
 * rewards, then stops on the winner with a burst of particles in the rarity's color.
 * Everyone nearby can watch. The entities are temporary and never saved with the world.
 */
public class WorldAnimation implements OpeningSession {

    private static final int RISE_TICKS = 50;
    private static final int HOLD_TICKS = 40;

    private final GachaCrate plugin;
    private final Player player;
    private final Crate crate;
    private final CrateOpener.Roll roll;
    private final Location base; // Center of the top of the crate block (or a spot in front of the player).
    private final List<Reward> pool;

    private ItemDisplay item;
    private TextDisplay label;
    private BukkitTask task;
    private boolean finished;

    public WorldAnimation(GachaCrate plugin, Player player, Crate crate, CrateOpener.Roll roll, Location crateBlock) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.roll = roll;
        this.pool = crate.getValidRewards();
        if (crateBlock != null) {
            this.base = crateBlock.clone().add(0.5, 1.0, 0.5);
            plugin.getHologramManager().hide(crateBlock, (RISE_TICKS + HOLD_TICKS) * 50L + 1000L);
        } else {
            // No crate block (key in hand): play it a couple of blocks in front of the player.
            Vector look = player.getLocation().getDirection().setY(0);
            if (look.lengthSquared() < 0.01) look = new Vector(1, 0, 0);
            this.base = player.getLocation().add(look.normalize().multiply(2.0)).add(0, 0.5, 0);
        }
    }

    @Override
    public void start() {
        World world = base.getWorld();
        if (world == null) {
            finish();
            return;
        }

        item = world.spawn(base.clone(), ItemDisplay.class);
        item.setPersistent(false);
        item.setItemStack(roll.reward.getDisplayItem());
        item.setBillboard(Display.Billboard.FIXED);

        label = world.spawn(base.clone().add(0, 1.4, 0), TextDisplay.class);
        label.setPersistent(false);
        label.setBillboard(Display.Billboard.CENTER);
        label.setText(Text.color("&e" + player.getName() + " &7is opening") + "\n" + crate.getDisplayName());

        world.playSound(base, Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 0.8f);

        task = new BukkitRunnable() {
            int tick = 0;
            int nextSwap = 0;
            float yaw = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !item.isValid()) {
                    finish();
                    return;
                }

                if (tick < RISE_TICKS) {
                    double progress = tick / (double) RISE_TICKS;
                    double height = 0.3 + 1.0 * (1 - Math.pow(1 - progress, 3)); // Ease out.
                    yaw += (float) (40 * (1 - progress) + 4);
                    Location at = base.clone().add(0, height, 0);
                    at.setYaw(yaw);
                    item.teleport(at);

                    if (tick >= nextSwap) {
                        item.setItemStack(CrateOpener.weightedPick(pool).getDisplayItem());
                        world.playSound(base, Sound.BLOCK_NOTE_BLOCK_HAT, 0.6f, 1.2f + (float) progress);
                        nextSwap = tick + 2 + (int) (progress * 8); // Slows down.
                    }
                    spiral(world, at, tick);
                } else if (tick == RISE_TICKS) {
                    reveal(world);
                } else if (tick >= RISE_TICKS + HOLD_TICKS) {
                    finish();
                    return;
                } else {
                    // Slow spin while showing off the prize.
                    yaw += 3;
                    Location at = item.getLocation();
                    at.setYaw(yaw);
                    item.teleport(at);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void spiral(World world, Location center, int tick) {
        Particle sparkle = Compat.particle("END_ROD");
        if (sparkle == null) return;
        double angle = tick * 0.5;
        for (int i = 0; i < 2; i++) {
            double a = angle + i * Math.PI;
            world.spawnParticle(sparkle, center.getX() + 0.7 * Math.cos(a), center.getY() - 0.3,
                    center.getZ() + 0.7 * Math.sin(a), 1, 0, 0, 0, 0);
        }
    }

    private void reveal(World world) {
        Reward reward = roll.reward;
        item.setItemStack(reward.getDisplayItem());
        label.setText(reward.getRarity().getDisplayName() + "\n" + reward.getDisplayName()
                + "\n" + Text.color("&7won by &e" + player.getName()));

        Location at = item.getLocation();
        Color color = CrateOpener.colorOf(reward.getRarity());
        Particle dust = Compat.dust();
        if (dust != null) {
            try {
                world.spawnParticle(dust, at, 60, 0.6, 0.6, 0.6, 0, new Particle.DustOptions(color, 1.6f));
            } catch (IllegalArgumentException ignored) {
                // Some versions want different data; the rest of the effect still plays.
            }
        }
        Particle burst = Compat.particle("FIREWORK");
        if (burst != null) world.spawnParticle(burst, at, 40, 0.2, 0.2, 0.2, 0.15);
        world.playSound(at, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f);
    }

    @Override
    public void finish() {
        if (finished) return;
        finished = true;
        if (task != null) task.cancel();
        if (item != null) item.remove();
        if (label != null) label.remove();
        plugin.getCrateOpener().animationDone(player, crate, roll);
    }
}
