package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Lists the crate backups. Click one to restore it (the current crates are backed up first).
 */
public class BackupMenu extends Menu {

    private static final int PER_PAGE = 45;

    private int page = 0;

    public BackupMenu(Player player) {
        super(player);
    }

    @Override
    protected String getTitle() {
        return "§8✦ §bBackups";
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected void draw() {
        fillRow(5, Material.BLACK_STAINED_GLASS_PANE);

        List<String> backups = plugin().getBackupManager().list();
        int pages = Math.max(1, (int) Math.ceil(backups.size() / (double) PER_PAGE));
        page = Math.max(0, Math.min(page, pages - 1));
        int start = page * PER_PAGE;

        for (int i = 0; i < PER_PAGE && start + i < backups.size(); i++) {
            String name = backups.get(start + i);
            int crates = plugin().getBackupManager().countCrates(name);
            Material icon = name.endsWith("manual") ? Material.WRITTEN_BOOK
                    : name.endsWith("startup") ? Material.BOOK
                    : name.endsWith("before-restore") ? Material.ENCHANTED_BOOK
                    : name.endsWith("before-delete") ? Material.LAVA_BUCKET : Material.PAPER;
            button(i, Items.of(icon).name("&b" + pretty(name))
                    .lore("&7Type: &f" + type(name),
                            "&7Crates: &f" + (crates < 0 ? "?" : crates),
                            "", "&eClick to restore this backup",
                            "&8Your current crates are backed up first.").build(), e ->
                    new ConfirmMenu(player, "Restore " + pretty(name) + "?", () -> {
                        player.closeInventory();
                        String error = plugin().getBackupManager().restore(name);
                        if (error != null) {
                            errorSound();
                            Text.send(player, "&c" + error);
                        } else {
                            successSound();
                            Text.send(player, "&aRestored backup &e" + pretty(name) + "&a. Crates reloaded.");
                        }
                    }, this::open).open());
        }

        if (backups.isEmpty()) {
            item(22, Items.of(Material.PAPER).name("&7No backups yet")
                    .lore("&7They are made automatically when", "&7the server starts and while you edit.").build());
        }

        button(45, Items.of(Material.ARROW).name("&e« Back").build(), e -> new MainMenu(player).open());
        if (page > 0) {
            button(48, Items.of(Material.ARROW).name("&e« Previous Page").build(), e -> {
                page--;
                refresh();
            });
        }
        item(49, Items.of(Material.KNOWLEDGE_BOOK).name("&bAbout backups")
                .lore("&7Saved in &fplugins/GachaCrate/backups&7.",
                        "&7Each one has crates.yml and locations.yml.",
                        "",
                        "&7Made when the server starts and every",
                        "&f" + plugin().getConfig().getInt("backups.interval-minutes", 30) + " min &7while crates are edited.",
                        "&7Keeping the newest &f" + plugin().getConfig().getInt("backups.keep", 20) + "&7.",
                        "", "&7Page: &f" + (page + 1) + "/" + pages).build());
        if (page < pages - 1) {
            button(50, Items.of(Material.ARROW).name("&eNext Page »").build(), e -> {
                page++;
                refresh();
            });
        }
        button(53, Items.of(Material.EMERALD).name("&a&l✚ Back Up Now").glow(true)
                .lore("&7Saves a copy of your crates", "&7right now.").build(), e -> {
            plugin().getCrateManager().saveCrates();
            String name = plugin().getBackupManager().create("manual");
            if (name == null) {
                errorSound();
                Text.send(player, "&cNothing to back up yet (no crates saved).");
            } else {
                successSound();
                Text.send(player, "&aBackup created: &e" + pretty(name));
            }
            refresh();
        });
    }

    /** "2026-11-20_14-03-11_auto" -> "2026-11-20 14:03:11". */
    static String pretty(String name) {
        String[] parts = name.split("_");
        if (parts.length < 2) return name;
        return parts[0] + " " + parts[1].replace('-', ':');
    }

    private static String type(String name) {
        if (name.endsWith("manual")) return "Manual";
        if (name.endsWith("startup")) return "Server start";
        if (name.endsWith("before-restore")) return "Before a restore";
        if (name.endsWith("before-delete")) return "Before a crate was deleted";
        return "Automatic";
    }
}
