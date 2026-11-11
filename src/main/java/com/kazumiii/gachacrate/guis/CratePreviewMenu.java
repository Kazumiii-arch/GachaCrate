package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.player.PlayerData;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * What players see with /gacha view or by left-clicking a crate block.
 * Rewards are sorted best-first and show their real chance in %.
 */
public class CratePreviewMenu extends Menu {

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final Crate crate;
    private final List<Reward> sorted;
    private int page = 0;

    public CratePreviewMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        this.sorted = new ArrayList<>(crate.getValidRewards());
        this.sorted.sort(Comparator.comparingInt((Reward r) -> r.getRarity().getOrder()).reversed()
                .thenComparing(Comparator.comparingDouble(Reward::getChance)));
    }

    @Override
    protected String getTitle() {
        String title = "§8Preview: " + Text.strip(crate.getDisplayName());
        return title.length() > 32 ? title.substring(0, 32) : title;
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected void draw() {
        fill(Material.BLACK_STAINED_GLASS_PANE);
        for (int slot : SLOTS) inventory.setItem(slot, null);

        int pages = Math.max(1, (int) Math.ceil(sorted.size() / (double) SLOTS.length));
        page = Math.max(0, Math.min(page, pages - 1));

        List<String> info = new ArrayList<>();
        info.add("&7Rewards: &f" + sorted.size());
        if (pages > 1) info.add("&7Page: &f" + (page + 1) + "/" + pages);
        if (crate.isTimeLimitEnabled()) {
            info.add("&7Available until: &f" + (crate.getEndDate() == null ? "?" : crate.getEndDate()));
        }
        Rarity pityRarity = plugin().getRarityManager().getRarity(crate.getPityRarityMinimum());
        if (crate.isPityEnabled() && pityRarity != null) {
            PlayerData data = plugin().getPlayerManager().getPlayerData(player.getUniqueId());
            int count = data == null ? 0 : data.getPityCount(crate.getName());
            int left = Math.max(1, crate.getPityThreshold() - count);
            info.add("");
            info.add("&d&lPity");
            info.add("&7Guaranteed " + pityRarity.getDisplayName() + " &7or better");
            info.add("&7in at most &f" + left + " &7more open" + (left == 1 ? "" : "s") + ".");
            info.add("&7Progress: &f" + count + "&7/&f" + crate.getPityThreshold());
        }
        item(4, Items.of(Material.BOOK).name(crate.getDisplayName()).lore(info).build());

        int start = page * SLOTS.length;
        for (int i = 0; i < SLOTS.length && start + i < sorted.size(); i++) {
            Reward reward = sorted.get(start + i);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(reward.getRarity().getDisplayName());
            lore.add("&6Chance: &e" + Text.number(crate.getPercent(reward)) + "%");
            item(SLOTS[i], Items.copy(reward.getDisplayItem()).addLore(lore).build());
        }

        if (sorted.isEmpty()) {
            item(22, Items.of(Material.BARRIER).name("&cNo rewards yet").build());
        }

        if (page > 0) {
            button(48, Items.of(Material.ARROW).name("&e« Previous Page").build(), e -> {
                page--;
                refresh();
            });
        }
        if (page < pages - 1) {
            button(50, Items.of(Material.ARROW).name("&eNext Page »").build(), e -> {
                page++;
                refresh();
            });
        }
        button(49, Items.of(Material.BARRIER).name("&cClose").build(), e -> player.closeInventory());

        // Buy keys here if the crate is sold in the shop.
        if (crate.getKeyPrice() > 0 && player.hasPermission("gacha.user.shop") && plugin().getShopManager().isEconomyReady()) {
            String price = plugin().getShopManager().format(crate.getKeyPrice());
            button(45, Items.of(Material.EMERALD).name("&aBuy Keys")
                    .lore("&7Price: &f" + price + " &7each", "", "&a► Click: buy 1", "&a► Shift-click: buy 10").build(), e -> {
                if (plugin().getShopManager().buyKeys(player, crate, e.isShiftClick() ? 10 : 1)) refresh();
            });
        }

        // Claim today's free keys for this crate.
        if (plugin().getShopManager().getClaimableDaily(player).contains(crate)) {
            button(53, Items.of(Material.SUNFLOWER).name("&a&lFree Daily Keys!")
                    .lore("&7Claim today's free keys", "&7for all crates.", "", "&a► Click to claim").glow(true).build(), e -> {
                plugin().getShopManager().claimDaily(player);
                refresh();
            });
        }
    }
}
