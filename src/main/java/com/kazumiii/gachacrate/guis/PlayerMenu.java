package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.history.LeaderboardManager;
import com.kazumiii.gachacrate.player.PlayerData;
import com.kazumiii.gachacrate.util.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * The players' main menu (/gacha): crates, daily keys, shop, history and leaderboard.
 */
public class PlayerMenu extends Menu {

    public PlayerMenu(Player player) {
        super(player);
    }

    @Override
    protected String getTitle() {
        return "§8Gacha Crates";
    }

    @Override
    protected int getSize() {
        return 45;
    }

    @Override
    protected void draw() {
        fill(Material.BLACK_STAINED_GLASS_PANE);
        PlayerData data = plugin().getPlayerManager().getPlayerData(player.getUniqueId());
        int opened = data == null ? 0 : data.getTotalCratesOpened();
        int rank = plugin().getLeaderboardManager().rankOf(player.getUniqueId(), null);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (head.getItemMeta() instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) head.getItemMeta();
            skull.setOwningPlayer(player);
            head.setItemMeta(skull);
        }
        item(4, Items.copy(head).name("&6" + player.getName())
                .lore("&7Crates opened: &f" + opened, "&7Rank: &f" + (rank > 0 ? "#" + rank : "-")).build());

        button(13, Items.of(Material.CHEST).name("&bBrowse Crates")
                .lore("&7See every crate, its rewards", "&7and their chances.").build(),
                e -> new CrateBrowserMenu(player).open());

        // Daily keys
        if (player.hasPermission("gacha.user.daily") && plugin().getShopManager().anyDailyCrates()) {
            List<Crate> ready = plugin().getShopManager().getClaimableDaily(player);
            List<String> lore = new ArrayList<>();
            if (ready.isEmpty()) {
                lore.add("&7You already claimed today's keys.");
                lore.add("&7Come back tomorrow!");
            } else {
                lore.add("&7Ready to claim:");
                for (Crate crate : ready) lore.add("  &8• &e" + crate.getDailyFreeKeys() + "x " + crate.getDisplayName());
                lore.add("");
                lore.add("&a► Click to claim");
            }
            button(20, Items.of(ready.isEmpty() ? Material.CLOCK : Material.SUNFLOWER)
                    .name(ready.isEmpty() ? "&7Daily Keys" : "&a&lDaily Keys Ready!").lore(lore).glow(!ready.isEmpty()).build(), e -> {
                plugin().getShopManager().claimDaily(player);
                refresh();
            });
        }

        // Shop
        if (player.hasPermission("gacha.user.shop") && plugin().getShopManager().isEconomyReady()
                && !plugin().getShopManager().getCratesForSale().isEmpty()) {
            button(22, Items.of(Material.EMERALD).name("&aKey Shop")
                    .lore("&7Buy crate keys with money.", "", "&7Balance: &f"
                            + plugin().getShopManager().format(plugin().getShopManager().balance(player))).build(),
                    e -> new ShopMenu(player).open());
        }

        if (player.hasPermission("gacha.user.history")) {
            button(24, Items.of(Material.BOOK).name("&eMy Recent Wins")
                    .lore("&7Your last rewards.").build(),
                    e -> new HistoryMenu(player, player.getUniqueId(), player.getName()).open());

            button(30, Items.of(Material.NETHER_STAR).name("&dRecent Big Wins")
                    .lore("&7The best rewards anyone", "&7on the server won lately.").build(),
                    e -> new HistoryMenu(player, null, null).open());
        }

        if (player.hasPermission("gacha.user.top")) {
            List<LeaderboardManager.Entry> top = plugin().getLeaderboardManager().top(null, 3);
            List<String> lore = new ArrayList<>();
            String[] medals = {"&6#1", "&7#2", "&c#3"};
            for (int i = 0; i < top.size(); i++) {
                lore.add(medals[i] + " &f" + top.get(i).name + " &7- " + top.get(i).value);
            }
            if (top.isEmpty()) lore.add("&7Nobody has opened a crate yet.");
            lore.add("");
            lore.add("&e► Click for the full list");
            button(32, Items.of(Material.GOLD_INGOT).name("&6Top Openers").lore(lore).build(),
                    e -> new TopMenu(player).open());
        }

        if (player.hasPermission("gacha.admin.edit")) {
            button(36, Items.of(Material.COMMAND_BLOCK).name("&cAdmin Menu").build(), e -> new MainMenu(player).open());
        }
        button(40, Items.of(Material.BARRIER).name("&cClose").build(), e -> player.closeInventory());
    }
}
