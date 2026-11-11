package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.conversation.ChatPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.CrateManager;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /gacha — the admin overview of all crates.
 */
public class MainMenu extends Menu {

    private static final int[] CRATE_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private int page = 0;

    public MainMenu(Player player) {
        super(player);
    }

    @Override
    protected String getTitle() {
        return "§8GachaCrate Admin Menu";
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected void draw() {
        fill(Material.BLACK_STAINED_GLASS_PANE);
        for (int slot : CRATE_SLOTS) inventory.setItem(slot, null);

        List<Crate> crates = new ArrayList<>(plugin().getCrateManager().getAllCrates().values());
        int pages = Math.max(1, (int) Math.ceil(crates.size() / (double) CRATE_SLOTS.length));
        page = Math.max(0, Math.min(page, pages - 1));

        int start = page * CRATE_SLOTS.length;
        for (int i = 0; i < CRATE_SLOTS.length && start + i < crates.size(); i++) {
            Crate crate = crates.get(start + i);
            int blocks = plugin().getPhysicalCrateManager().getBlocksOf(crate.getId()).size();
            List<String> lore = new ArrayList<>();
            lore.add("&7Id: &f" + crate.getName());
            lore.add("&7Rewards: &f" + crate.getRewards().size());
            lore.add("&7Crate blocks: &f" + blocks);
            lore.add("&7Animation: &f" + crate.getAnimationType().getDisplayName());
            lore.add("&7Opening: &f" + crate.getOpeningMode().getDisplayName());
            if (crate.isPityEnabled()) lore.add("&7Pity: &d" + crate.getPityThreshold() + " opens");
            if (crate.isTimeLimitEnabled()) {
                lore.add("&7Available: &f" + (crate.getStartDate() == null ? "?" : crate.getStartDate())
                        + " &7→ &f" + (crate.getEndDate() == null ? "?" : crate.getEndDate()));
            }
            lore.add("");
            lore.add("&e► Click to manage");
            button(CRATE_SLOTS[i], Items.of(Material.CHEST).name(crate.getDisplayName()).lore(lore).build(),
                    e -> new CrateSettingsMenu(player, crate).open());
        }

        if (crates.isEmpty()) {
            item(22, Items.of(Material.PAPER).name("&7No crates yet")
                    .lore("&7Click the nether star below", "&7to create your first crate.").build());
        }

        item(4, Items.of(Material.BOOK).name("&6&lGachaCrate")
                .lore("&7Crates: &f" + crates.size(), "&7Page: &f" + (page + 1) + "/" + pages).build());

        if (page > 0) {
            button(45, Items.of(Material.ARROW).name("&e« Previous Page").build(), e -> {
                page--;
                refresh();
            });
        }
        if (page < pages - 1) {
            button(53, Items.of(Material.ARROW).name("&eNext Page »").build(), e -> {
                page++;
                refresh();
            });
        }

        button(48, Items.of(Material.COMPARATOR).name("&6Reload Plugin")
                .lore("&7Reloads config.yml, crates", "&7and crate blocks from disk.").build(), e -> {
            if (!player.hasPermission("gacha.admin.reload")) {
                Text.send(player, "&cYou don't have permission to reload.");
                return;
            }
            player.closeInventory();
            plugin().reloadAll();
            Text.send(player, "&aGachaCrate reloaded.");
        });

        button(50, Items.of(Material.NETHER_STAR).name("&a✚ Create New Crate")
                .lore("&7Click and type a name in chat.").build(), e -> {
            if (!player.hasPermission("gacha.admin.create")) {
                Text.send(player, "&cYou don't have permission to create crates.");
                return;
            }
            ChatPrompt.ask(player, "&eType a name for the new crate &7(letters, numbers, _ and -)",
                    input -> {
                        if (!CrateManager.isValidName(input)) {
                            return "Use only letters, numbers, _ and - (max 32 characters).";
                        }
                        if (plugin().getCrateManager().getCrate(input) != null) {
                            return "A crate called '" + input + "' already exists.";
                        }
                        plugin().getCrateManager().createCrate(input);
                        Text.send(player, "&aCrate &e" + input + " &acreated!");
                        return null;
                    },
                    this::open);
        });
    }
}
