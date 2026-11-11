package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.history.LeaderboardManager;
import com.kazumiii.gachacrate.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Leaderboard: who opened the most crates (all crates, or one crate).
 */
public class TopMenu extends Menu {

    private static final int[] SLOTS = {
            13,
            21, 23,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private int filter = -1; // -1 = all crates, otherwise index into the crate list

    public TopMenu(Player player) {
        super(player);
    }

    @Override
    protected String getTitle() {
        return "§8Top Openers";
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected void draw() {
        fill(Material.BLACK_STAINED_GLASS_PANE);

        List<Crate> crates = new ArrayList<>(plugin().getCrateManager().getAllCrates().values());
        if (filter >= crates.size()) filter = -1;
        Crate selected = filter < 0 ? null : crates.get(filter);
        String crateId = selected == null ? null : selected.getId();

        List<LeaderboardManager.Entry> top = plugin().getLeaderboardManager().top(crateId, SLOTS.length);
        for (int i = 0; i < top.size(); i++) {
            LeaderboardManager.Entry entry = top.get(i);
            String color = i == 0 ? "&6&l" : i == 1 ? "&f&l" : i == 2 ? "&c&l" : "&e";
            item(SLOTS[i], head(entry, color + "#" + (i + 1) + " &f" + entry.name,
                    "&7Crates opened: &a" + entry.value));
        }
        if (top.isEmpty()) {
            item(22, Items.of(Material.PAPER).name(plugin().getLeaderboardManager().isLoaded()
                    ? "&7Nobody has opened this crate yet" : "&7Loading...").build());
        }

        int rank = plugin().getLeaderboardManager().rankOf(player.getUniqueId(), crateId);
        item(4, Items.of(Material.GOLD_INGOT).name("&6Top Openers")
                .lore("&7Showing: &f" + (selected == null ? "All crates" : selected.getDisplayName()),
                        "&7Your rank: &f" + (rank > 0 ? "#" + rank : "-")).build());

        List<String> lore = new ArrayList<>();
        lore.add((filter < 0 ? "&a► " : "&7") + "All crates");
        for (int i = 0; i < crates.size() && i < 12; i++) {
            lore.add((filter == i ? "&a► " : "&7") + crates.get(i).getName());
        }
        lore.add("");
        lore.add("&e► Click to change");
        button(49, Items.of(Material.HOPPER).name("&bFilter by Crate").lore(lore).build(), e -> {
            int size = crates.size();
            if (size == 0) return;
            filter = e.isRightClick() ? (filter <= -1 ? size - 1 : filter - 1) : (filter + 1 >= size ? -1 : filter + 1);
            refresh();
        });
        button(45, Items.of(Material.ARROW).name("&e« Back").build(), e -> new PlayerMenu(player).open());
    }

    private ItemStack head(LeaderboardManager.Entry entry, String name, String line) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (skull.getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.uuid));
            skull.setItemMeta(meta);
        }
        return Items.copy(skull).name(name).lore(line).build();
    }
}
