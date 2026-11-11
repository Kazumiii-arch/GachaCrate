package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.CrateOpener;
import com.kazumiii.gachacrate.crates.OpeningSession;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * The opening animation. The reward was already picked; the animation just lands on it.
 * <p>
 * Closing the menu skips to the end (if allowed in config). Logging out also gives the reward.
 */
public class CrateAnimationMenu extends Menu implements OpeningSession {

    private static final int[] ROULETTE_PATH = {0, 1, 2, 3, 4, 5, 6, 7, 8, 17, 26, 35, 44, 43, 42, 41, 40, 39, 38, 37, 36, 27, 18, 9};
    private static final Material[] LIGHTS = {
            Material.YELLOW_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE,
            Material.MAGENTA_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE
    };

    private final Crate crate;
    private final CrateOpener.Roll roll;
    private final List<Reward> pool;
    private BukkitTask task;
    private boolean finished;
    private boolean revealed;
    private boolean closed;

    public CrateAnimationMenu(Player player, Crate crate, CrateOpener.Roll roll) {
        super(player);
        this.crate = crate;
        this.roll = roll;
        this.pool = crate.getValidRewards();
    }

    @Override
    protected String getTitle() {
        String title = "§8Opening " + Text.strip(crate.getDisplayName()) + "...";
        return title.length() > 32 ? title.substring(0, 32) : title;
    }

    @Override
    protected int getSize() {
        switch (crate.getAnimationType()) {
            case ROULETTE:
                return 45;
            case REEL:
            default:
                return 27;
        }
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);
    }

    @Override
    public void start() {
        open();
    }

    @Override
    public void open() {
        super.open();
        switch (crate.getAnimationType()) {
            case ROULETTE:
                startRoulette();
                break;
            case REEL:
            default:
                startReel();
                break;
        }
    }

    private Reward bestReward() {
        Reward best = null;
        for (Reward r : pool) {
            if (best == null || r.getRarity().getOrder() > best.getRarity().getOrder()) best = r;
        }
        return best;
    }

    /** A random reward icon, weighted by chance (so common items show up more often). */
    private ItemStack randomIcon() {
        return CrateOpener.weightedPick(pool).getDisplayItem();
    }

    // ------------------------------------------------------------------
    // Reel: a row of items scrolls left and slows down; the winner stops under the arrows.
    // ------------------------------------------------------------------

    private void startReel() {
        ItemStack arrow = Items.of(Material.HOPPER).name("&e▼").build();
        inventory.setItem(4, arrow);
        inventory.setItem(22, Items.of(Material.HOPPER).name("&e▲").build());

        final int steps = 32;
        final List<ItemStack> reel = new ArrayList<>();
        for (int i = 0; i < steps + 8; i++) reel.add(randomIcon());
        reel.set(steps - 1 + 4, roll.reward.getDisplayItem()); // Lands in the middle slot (13) on the last step.
        // Near miss: often put the best reward right next to the winner, so it "almost" stopped there.
        Reward best = bestReward();
        if (best != null && best != roll.reward && Math.random() < 0.5) {
            reel.set(steps - 1 + 5, best.getDisplayItem());
        }

        task = new BukkitRunnable() {
            int step = 0;
            int wait = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    finish();
                    return;
                }
                if (wait > 0) {
                    wait--;
                    return;
                }
                for (int i = 0; i < 9; i++) inventory.setItem(9 + i, reel.get(step + i));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.6f, 1.6f);

                if (step >= steps - 1) {
                    cancel();
                    reveal(13);
                    return;
                }
                step++;
                // Starts fast, ends slow.
                double progress = step / (double) steps;
                wait = (int) Math.round(progress * progress * 6);
            }
        }.runTaskTimer(plugin(), 1L, 1L);
    }

    // ------------------------------------------------------------------
    // Roulette: lights run around the border while the middle item changes.
    // ------------------------------------------------------------------

    private void startRoulette() {
        final int duration = 80;
        task = new BukkitRunnable() {
            int tick = 0;
            int nextChange = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    finish();
                    return;
                }
                if (tick >= duration) {
                    cancel();
                    reveal(22);
                    return;
                }
                for (int i = 0; i < ROULETTE_PATH.length; i++) {
                    inventory.setItem(ROULETTE_PATH[i], Items.filler(LIGHTS[(tick + i) % LIGHTS.length]));
                }
                if (tick >= nextChange) {
                    inventory.setItem(22, randomIcon());
                    float pitch = 0.5f + (float) tick / duration;
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, pitch);
                    nextChange = tick + 1 + tick / 10; // Slows down over time.
                }
                tick++;
            }
        }.runTaskTimer(plugin(), 1L, 1L);
    }

    // ------------------------------------------------------------------
    // End
    // ------------------------------------------------------------------

    private void reveal(int slot) {
        revealed = true;
        Material frame = paneFor(roll.reward.getRarity());
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, Items.filler(frame));
        inventory.setItem(slot, Items.copy(roll.reward.getDisplayItem())
                .addLore(List.of("", roll.reward.getRarity().getDisplayName())).glow(true).build());

        finish();

        long delay = Math.max(0, plugin().getConfig().getLong("opening.close-delay-ticks", 40));
        plugin().getServer().getScheduler().runTaskLater(plugin(), () -> {
            if (player.isOnline() && !closed) {
                player.closeInventory();
            }
        }, delay);
    }

    /** Ends the animation and hands out the reward. Safe to call more than once. */
    @Override
    public void finish() {
        if (finished) return;
        finished = true;
        if (task != null) task.cancel();
        plugin().getCrateOpener().animationDone(player, crate, roll);
    }

    @Override
    protected void onClose() {
        closed = true;
        if (finished || revealed) return;
        if (plugin().getConfig().getBoolean("opening.allow-skip", true)) {
            finish(); // Skipped: give the reward now.
        }
        // Otherwise the animation keeps running in the background and gives the reward at the end.
    }

    private static Material paneFor(Rarity rarity) {
        switch (rarity.getColor()) {
            case AQUA: return Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            case BLUE: case DARK_BLUE: return Material.BLUE_STAINED_GLASS_PANE;
            case DARK_AQUA: return Material.CYAN_STAINED_GLASS_PANE;
            case DARK_PURPLE: return Material.PURPLE_STAINED_GLASS_PANE;
            case LIGHT_PURPLE: return Material.MAGENTA_STAINED_GLASS_PANE;
            case GOLD: return Material.ORANGE_STAINED_GLASS_PANE;
            case YELLOW: return Material.YELLOW_STAINED_GLASS_PANE;
            case GREEN: return Material.LIME_STAINED_GLASS_PANE;
            case DARK_GREEN: return Material.GREEN_STAINED_GLASS_PANE;
            case RED: case DARK_RED: return Material.RED_STAINED_GLASS_PANE;
            case BLACK: case DARK_GRAY: return Material.BLACK_STAINED_GLASS_PANE;
            case GRAY: return Material.GRAY_STAINED_GLASS_PANE;
            default: return Material.WHITE_STAINED_GLASS_PANE;
        }
    }

}
