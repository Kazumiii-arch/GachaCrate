package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.util.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lets players see every crate and open its preview.
 */
public class CrateBrowserMenu extends Menu {

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private int page = 0;

    public CrateBrowserMenu(Player player) {
        super(player);
    }

    @Override
    protected String getTitle() {
        return "§8Crates";
    }

    @Override
    protected int getSize() {
        return 45;
    }

    @Override
    protected void draw() {
        fill(Material.BLACK_STAINED_GLASS_PANE);
        for (int slot : SLOTS) inventory.setItem(slot, null);

        LocalDate today = LocalDate.now();
        List<Crate> crates = new ArrayList<>();
        for (Crate crate : plugin().getCrateManager().getAllCrates().values()) {
            if (!crate.getValidRewards().isEmpty() && crate.isAvailable(today)) crates.add(crate);
        }
        Map<String, Integer> keys = plugin().getKeyManager().countKeys(player);
        boolean shop = plugin().getShopManager().isEconomyReady();

        int pages = Math.max(1, (int) Math.ceil(crates.size() / (double) SLOTS.length));
        page = Math.max(0, Math.min(page, pages - 1));
        int start = page * SLOTS.length;

        for (int i = 0; i < SLOTS.length && start + i < crates.size(); i++) {
            Crate crate = crates.get(start + i);
            List<String> lore = new ArrayList<>();
            lore.add("&7Rewards: &f" + crate.getValidRewards().size());
            lore.add("&7Your keys: &f" + keys.getOrDefault(crate.getId(), 0));
            if (crate.getDailyFreeKeys() > 0) lore.add("&7Daily free keys: &a" + crate.getDailyFreeKeys());
            if (shop && crate.getKeyPrice() > 0) lore.add("&7Key price: &a" + plugin().getShopManager().format(crate.getKeyPrice()));
            if (crate.isTimeLimitEnabled() && crate.getEndDate() != null) lore.add("&cLimited! &7Until " + crate.getEndDate());
            lore.add("");
            lore.add("&e► Click to see rewards");
            button(SLOTS[i], Items.of(Material.CHEST).name(crate.getDisplayName()).lore(lore).build(),
                    e -> new CratePreviewMenu(player, crate).open());
        }
        if (crates.isEmpty()) item(22, Items.of(Material.BARRIER).name("&cNo crates available").build());

        if (page > 0) {
            button(39, Items.of(Material.ARROW).name("&e« Previous Page").build(), e -> {
                page--;
                refresh();
            });
        }
        if (page < pages - 1) {
            button(41, Items.of(Material.ARROW).name("&eNext Page »").build(), e -> {
                page++;
                refresh();
            });
        }
        button(36, Items.of(Material.ARROW).name("&e« Back").build(), e -> new PlayerMenu(player).open());
    }
}
