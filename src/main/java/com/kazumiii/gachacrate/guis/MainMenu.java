package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.conversation.ChatPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.CrateHealth;
import com.kazumiii.gachacrate.crates.CrateManager;
import com.kazumiii.gachacrate.crates.StarterCrates;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /gacha — the admin dashboard: server stats, every crate with its health, and quick actions.
 */
public class MainMenu extends Menu {

    private static final int[] CRATE_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private int page = 0;
    /** Only show crates that have problems. */
    private boolean onlyProblems = false;

    public MainMenu(Player player) {
        super(player);
    }

    @Override
    protected String getTitle() {
        return "§8✦ §2§lGacha§a§lCrate §8┃ §7Dashboard";
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected void draw() {
        fill(Material.BLACK_STAINED_GLASS_PANE);
        fillRow(0, Material.GREEN_STAINED_GLASS_PANE);
        fillRow(5, Material.GREEN_STAINED_GLASS_PANE);
        for (int slot : CRATE_SLOTS) inventory.setItem(slot, null);

        List<Crate> all = new ArrayList<>(plugin().getCrateManager().getAllCrates().values());
        List<Crate> shown = new ArrayList<>();
        int problems = 0;
        int totalRewards = 0;
        for (Crate crate : all) {
            totalRewards += crate.getRewards().size();
            boolean ok = CrateHealth.check(plugin(), crate).isOk();
            if (!ok) problems++;
            if (!onlyProblems || !ok) shown.add(crate);
        }

        int pages = Math.max(1, (int) Math.ceil(shown.size() / (double) CRATE_SLOTS.length));
        page = Math.max(0, Math.min(page, pages - 1));

        int start = page * CRATE_SLOTS.length;
        for (int i = 0; i < CRATE_SLOTS.length && start + i < shown.size(); i++) {
            Crate crate = shown.get(start + i);
            button(CRATE_SLOTS[i], crateIcon(crate), e -> {
                if (e.isShiftClick() && e.isRightClick()) {
                    if (!allowed("gacha.admin.edit", "edit crates")) return;
                    crate.setEnabled(!crate.isEnabled());
                    plugin().getCrateManager().saveCrates();
                    plugin().getHologramManager().refresh(crate);
                    Text.send(player, crate.getDisplayName() + (crate.isEnabled() ? " &ais open again." : " &eis now closed for maintenance."));
                    refresh();
                } else if (e.isShiftClick()) {
                    new CratePreviewMenu(player, crate).open();
                } else if (e.isRightClick()) {
                    new CrateEditorMenu(player, crate).open();
                } else {
                    new CrateSettingsMenu(player, crate).open();
                }
            });
        }

        if (shown.isEmpty()) {
            item(22, Items.of(onlyProblems ? Material.LIME_DYE : Material.PAPER)
                    .name(onlyProblems ? "&aEvery crate is set up correctly!" : "&7No crates yet")
                    .lore(onlyProblems ? new String[]{"&7Nothing needs fixing."}
                            : new String[]{"&7Click &a✚ Create Crate &7below,", "&7or &dStarter Crates &7for 4 ready-made ones."})
                    .build());
        }

        // --- Header: server overview ---
        long opens = plugin().getLeaderboardManager().totalOpens();
        boolean economy = plugin().getShopManager().isEconomyReady();
        List<String> header = new ArrayList<>();
        header.add("&8Version " + plugin().getDescription().getVersion());
        header.add("");
        header.add("&7Crates: &f" + all.size() + (problems > 0 ? " &8(&e" + problems + " need attention&8)" : " &8(&aall good&8)"));
        header.add("&7Rewards: &f" + totalRewards);
        header.add("&7Crate blocks: &f" + plugin().getPhysicalCrateManager().getAllCrateBlocks().size());
        header.add("&7Crates opened: &f" + (plugin().getLeaderboardManager().isLoaded() ? opens : "loading..."));
        header.add("&7Players: &f" + plugin().getLeaderboardManager().playerCount());
        header.add("");
        header.add("&7Economy (shop): " + (economy ? "&aready" : "&cnot found"));
        header.add("&7PlaceholderAPI: " + (plugin().isPlaceholderApiEnabled() ? "&ahooked" : "&7not installed"));
        header.add("&7Holograms: " + (plugin().getHologramManager().isEnabled() ? "&aon" : "&7off"));
        header.add("");
        header.add("&7Page &f" + (page + 1) + "&7/&f" + pages);
        item(4, Items.of(Material.NETHER_STAR).name("&2&lGacha&a&lCrate &7Dashboard").lore(header).glow(true).build());

        button(0, Items.of(Material.KNOWLEDGE_BOOK).name("&aHow to use this menu")
                .lore("&7On a crate:",
                        "&e Left-click &8» &7settings",
                        "&e Right-click &8» &7rewards",
                        "&e Shift + left &8» &7preview as a player",
                        "&e Shift + right &8» &7open / close (maintenance)",
                        "",
                        "&a● &7ready  &e● &7warning  &c● &7broken").build(), null);

        button(8, Items.of(onlyProblems ? Material.SPYGLASS : Material.HOPPER)
                .name(onlyProblems ? "&eShowing: &fcrates with problems" : "&eShowing: &fall crates")
                .lore("&7Click to switch.", problems > 0 ? "&e" + problems + " crate(s) need attention." : "&aNo problems found.")
                .glow(onlyProblems).build(), e -> {
            onlyProblems = !onlyProblems;
            page = 0;
            refresh();
        });

        // --- Bottom bar: quick actions ---
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

        button(46, Items.of(Material.CHEST_MINECART).name("&dStarter Crates")
                .lore("&7Creates 4 ready-made crates, from", "&aVote &7→ &9Rare &7→ &5Epic &7→ &6Legendary&7,",
                        "&7with balanced rewards to edit.", "", "&7Existing crates aren't touched.").build(), e -> {
            if (!allowed("gacha.admin.create", "create crates")) return;
            List<String> created = new StarterCrates(plugin()).create();
            if (created.isEmpty()) {
                Text.send(player, "&eThe starter crates already exist.");
            } else {
                successSound();
                Text.send(player, "&aCreated: &e" + String.join(", ", created));
            }
            refresh();
        });

        button(47, Items.of(Material.TRIPWIRE_HOOK).name("&6Give Keys").glow(true)
                .lore("&7Pick a crate, an amount and a", "&7player, all with clicks.").build(), e -> {
            if (!allowed("gacha.admin.givekey", "give keys")) return;
            new GiveKeysMenu(player, null, this).open();
        });

        button(48, Items.of(Material.COMPARATOR).name("&6Reload Plugin")
                .lore("&7Reloads config.yml, crates", "&7and crate blocks from disk.").build(), e -> {
            if (!allowed("gacha.admin.reload", "reload")) return;
            player.closeInventory();
            plugin().reloadAll();
            Text.send(player, "&aGachaCrate reloaded.");
        });

        button(49, Items.of(Material.EMERALD).name("&a&l✚ Create Crate").glow(true)
                .lore("&7Click and type a name in chat.").build(), e -> {
            if (!allowed("gacha.admin.create", "create crates")) return;
            ChatPrompt.ask(player, "&eType a name for the new crate &7(letters, numbers, _ and -)",
                    input -> {
                        if (!CrateManager.isValidName(input)) {
                            return "Use only letters, numbers, _ and - (max 32 characters).";
                        }
                        if (plugin().getCrateManager().getCrate(input) != null) {
                            return "A crate called '" + input + "' already exists.";
                        }
                        Crate crate = plugin().getCrateManager().createCrate(input);
                        Text.send(player, "&aCrate &e" + input + " &acreated! Now add some rewards.");
                        openLater(new CrateSettingsMenu(player, crate));
                        return null;
                    },
                    this::open);
        });

        button(50, Items.of(Material.ENDER_CHEST).name("&bBackups")
                .lore("&7Undo mistakes: restore your crates", "&7from an automatic backup.",
                        "", "&7Backups: &f" + plugin().getBackupManager().list().size()).build(), e -> {
            if (!allowed("gacha.admin.backup", "manage backups")) return;
            new BackupMenu(player).open();
        });

        button(51, Items.of(Material.WRITABLE_BOOK).name("&aRecent Big Wins")
                .lore("&7See what players won lately.").build(), e -> new HistoryMenu(player, null, null).open());

        button(52, Items.of(Material.PLAYER_HEAD).name("&bPlayer Menu")
                .lore("&7See what players see", "&7with /gacha.").build(), e -> new PlayerMenu(player).open());
    }

    private org.bukkit.inventory.ItemStack crateIcon(Crate crate) {
        CrateHealth health = CrateHealth.check(plugin(), crate);
        String dot = health.getLevel() == CrateHealth.Level.ERROR ? "&c● "
                : health.getLevel() == CrateHealth.Level.WARNING ? "&e● " : "&a● ";
        int blocks = plugin().getPhysicalCrateManager().getBlocksOf(crate.getId()).size();

        List<String> lore = new ArrayList<>();
        lore.add("&8" + crate.getName() + (crate.isEnabled() ? "" : " &8• &cclosed"));
        lore.add("");
        lore.add("&7Rewards: &f" + crate.getRewards().size());
        lore.add("&7Crate blocks: &f" + blocks);
        lore.add("&7Opened: &f" + plugin().getLeaderboardManager().totalOpens(crate.getId()) + " times");
        lore.add("&7Animation: &f" + crate.getAnimationType().getDisplayName());
        if (crate.isPityEnabled()) lore.add("&7Pity: &d" + crate.getPityThreshold() + " opens");
        if (crate.getDailyFreeKeys() > 0) lore.add("&7Daily keys: &f" + crate.getDailyFreeKeys());
        if (crate.getKeyPrice() > 0) lore.add("&7Key price: &f" + plugin().getShopManager().format(crate.getKeyPrice()));
        if (crate.isTimeLimitEnabled()) {
            lore.add("&7Available: &f" + (crate.getStartDate() == null ? "?" : crate.getStartDate())
                    + " &7→ &f" + (crate.getEndDate() == null ? "?" : crate.getEndDate()));
        }
        if (!health.isOk()) {
            lore.add("");
            lore.addAll(health.getProblems());
        }
        lore.add("");
        lore.add("&eLeft &8» &7settings  &eRight &8» &7rewards");
        lore.add("&eShift+L &8» &7preview  &eShift+R &8» &7" + (crate.isEnabled() ? "close" : "open"));

        Material icon = !crate.isEnabled() ? Material.BARREL
                : health.getLevel() == CrateHealth.Level.ERROR ? Material.TRAPPED_CHEST : Material.CHEST;
        return Items.of(icon).name(dot + crate.getDisplayName()).lore(lore)
                .amount(Math.max(1, Math.min(64, crate.getRewards().size()))).build();
    }
}
