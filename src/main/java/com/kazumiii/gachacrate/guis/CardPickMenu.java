package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.CrateOpener;
import com.kazumiii.gachacrate.crates.OpeningSession;
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
 * Card pick: nine face-down cards. The player picks one and it flips to show their prize
 * (already decided). Then the other cards flip one by one to show what they "missed".
 * If the player waits too long or closes the menu, a card is picked for them.
 */
public class CardPickMenu extends Menu implements OpeningSession {

    private static final int[] CARDS = {11, 13, 15, 20, 22, 24, 29, 31, 33};
    private static final int AUTO_PICK_SECONDS = 20;

    private final Crate crate;
    private final CrateOpener.Roll roll;
    private final List<Reward> pool;
    private final List<ItemStack> missed = new ArrayList<>();

    private BukkitTask timer;
    private BukkitTask flipTask;
    private int picked = -1;
    private boolean finished;
    private boolean closed;

    public CardPickMenu(Player player, Crate crate, CrateOpener.Roll roll) {
        super(player);
        this.crate = crate;
        this.roll = roll;
        this.pool = crate.getValidRewards();
        // The "missed" cards: random rewards, with the best reward usually among them for a near-miss feeling.
        Reward best = null;
        for (Reward r : pool) {
            if (best == null || r.getRarity().getOrder() > best.getRarity().getOrder()) best = r;
        }
        for (int i = 0; i < CARDS.length - 1; i++) missed.add(CrateOpener.weightedPick(pool).getDisplayItem());
        if (best != null && best != roll.reward && Math.random() < 0.7) {
            missed.set((int) (Math.random() * missed.size()), best.getDisplayItem());
        }
    }

    @Override
    protected String getTitle() {
        String title = "§8Pick a card! " + Text.strip(crate.getDisplayName());
        return title.length() > 32 ? title.substring(0, 32) : title;
    }

    @Override
    protected int getSize() {
        return 45;
    }

    @Override
    public void start() {
        open();
        timer = new BukkitRunnable() {
            int seconds = AUTO_PICK_SECONDS;

            @Override
            public void run() {
                if (picked >= 0 || finished) {
                    cancel();
                    return;
                }
                if (!player.isOnline()) {
                    finish();
                    return;
                }
                if (--seconds <= 0) {
                    cancel();
                    pick(CARDS[4]);
                }
            }
        }.runTaskTimer(plugin(), 20L, 20L);
    }

    @Override
    protected void draw() {
        fill(Material.BLACK_STAINED_GLASS_PANE);
        item(4, Items.of(Material.BOOK).name("&ePick a card!")
                .lore("&7One of these holds your prize.", "&7Choose wisely...").build());
        for (int slot : CARDS) {
            ItemStack card = Items.of(Material.PURPLE_STAINED_GLASS_PANE).name("&d&l?")
                    .lore("&7Click to flip this card").build();
            button(slot, card, e -> pick(slot));
        }
    }

    private void pick(int slot) {
        if (picked >= 0 || finished) return;
        picked = slot;
        if (timer != null) timer.cancel();

        // Flip the chosen card.
        inventory.setItem(slot, Items.copy(roll.reward.getDisplayItem())
                .addLore(List.of("", roll.reward.getRarity().getDisplayName(), "&a&lYOUR PRIZE")).glow(true).build());
        for (int s : CARDS) if (s != slot) inventory.setItem(s, Items.of(Material.GRAY_STAINED_GLASS_PANE).name("&8?").build());
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);

        // Then flip the rest one by one.
        flipTask = new BukkitRunnable() {
            int index = 0;
            int missedIndex = 0;
            int wait = 10;

            @Override
            public void run() {
                if (finished) {
                    cancel();
                    return;
                }
                if (wait-- > 0) return;
                while (index < CARDS.length && CARDS[index] == picked) index++;
                if (index >= CARDS.length) {
                    cancel();
                    finish();
                    long delay = Math.max(20, plugin().getConfig().getLong("opening.close-delay-ticks", 40));
                    plugin().getServer().getScheduler().runTaskLater(plugin(), () -> {
                        if (player.isOnline() && !closed) player.closeInventory();
                    }, delay);
                    return;
                }
                ItemStack shown = Items.copy(missed.get(missedIndex++)).addLore(List.of("", "&8Missed")).build();
                inventory.setItem(CARDS[index++], shown);
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.6f, 1.3f);
                wait = 3;
            }
        }.runTaskTimer(plugin(), 1L, 1L);
    }

    @Override
    public void finish() {
        if (finished) return;
        finished = true;
        if (timer != null) timer.cancel();
        if (flipTask != null) flipTask.cancel();
        plugin().getCrateOpener().animationDone(player, crate, roll);
    }

    @Override
    protected void onClose() {
        closed = true;
        if (!finished) finish(); // Closing early still gives the prize.
    }
}
