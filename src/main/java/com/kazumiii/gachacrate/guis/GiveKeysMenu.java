package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.conversation.ChatPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Give keys with clicks: choose a crate and an amount, then click a player's head.
 * Uses /gacha givekey internally, so offline players and the first-key welcome work the same way.
 */
public class GiveKeysMenu extends Menu {

    private static final int PER_PAGE = 36;
    private static final int[] AMOUNTS = {1, 5, 10, 32, 64};
    private static final int[] AMOUNT_SLOTS = {47, 48, 49, 50, 51};

    private final Menu back;
    private Crate crate;
    private int amount = 1;
    private int page = 0;

    /**
     * @param crate the crate to start with, or null for the first crate
     * @param back  menu to return to, or null to return to the dashboard
     */
    public GiveKeysMenu(Player player, Crate crate, Menu back) {
        super(player);
        this.back = back;
        this.crate = crate;
    }

    @Override
    protected String getTitle() {
        return "§8✦ §6Give Keys";
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected void draw() {
        fillRow(4, Material.ORANGE_STAINED_GLASS_PANE);
        fillRow(5, Material.BLACK_STAINED_GLASS_PANE);

        List<Crate> crates = new ArrayList<>(plugin().getCrateManager().getAllCrates().values());
        if (crates.isEmpty()) {
            item(22, Items.of(Material.BARRIER).name("&cThere are no crates yet").build());
            backButton();
            return;
        }
        if (crate == null || plugin().getCrateManager().getCrate(crate.getName()) == null) crate = crates.get(0);

        // --- Online players ---
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        int pages = Math.max(1, (int) Math.ceil(online.size() / (double) PER_PAGE));
        page = Math.max(0, Math.min(page, pages - 1));
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < online.size(); i++) {
            Player target = online.get(start + i);
            button(i, head(target), e -> give(target.getName()));
        }

        if (page > 0) {
            button(39, Items.of(Material.ARROW).name("&e« Previous Page").build(), e -> {
                page--;
                refresh();
            });
        }
        item(40, Items.of(Material.BOOK).name("&6How it works")
                .lore("&71. Pick a crate &8(bottom left)", "&72. Pick an amount &8(bottom row)",
                        "&73. Click a player's head", "", "&7Online: &f" + online.size(),
                        "&7Page: &f" + (page + 1) + "/" + pages).build());
        if (page < pages - 1) {
            button(41, Items.of(Material.ARROW).name("&eNext Page »").build(), e -> {
                page++;
                refresh();
            });
        }

        // --- Crate selector ---
        int index = Math.max(0, crates.indexOf(crate));
        List<String> crateLore = new ArrayList<>();
        for (Crate c : crates) crateLore.add((c == crate ? "&a▶ " : "&8  ") + Text.strip(c.getDisplayName()));
        crateLore.add("");
        crateLore.add("&7Left-click: next  &7Right-click: previous");
        button(46, Items.copy(plugin().getKeyManager().createKey(crate, 1)).name("&eCrate: " + crate.getDisplayName())
                .lore(crateLore).build(), e -> {
            int next = (index + (e.isRightClick() ? -1 : 1) + crates.size()) % crates.size();
            crate = crates.get(next);
            refresh();
        });

        // --- Amount ---
        for (int i = 0; i < AMOUNTS.length; i++) {
            int value = AMOUNTS[i];
            boolean selected = amount == value;
            button(AMOUNT_SLOTS[i], Items.of(selected ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE)
                    .name((selected ? "&a&l" : "&7") + value + " key" + (value == 1 ? "" : "s"))
                    .lore(selected ? "&aSelected" : "&eClick to select").amount(value).glow(selected).build(), e -> {
                amount = value;
                refresh();
            });
        }

        button(52, Items.of(Material.NAME_TAG).name("&bOther amount / offline player")
                .lore("&7Type a player name and amount,", "&7e.g. &fSteve 3&7.",
                        "&7Offline players get them on join.").build(), e ->
                ChatPrompt.ask(player, "&eType &f<player> [amount]&e for " + crate.getDisplayName(), input -> {
                    String[] parts = input.split("\\s+");
                    int custom = amount;
                    if (parts.length > 1) {
                        try {
                            custom = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException ex) {
                            return "The amount must be a whole number.";
                        }
                    }
                    if (custom < 1 || custom > 2304) return "The amount must be from 1 to 2304.";
                    player.performCommand("gacha givekey " + parts[0] + " " + crate.getName() + " " + custom);
                    return null;
                }, this::open));

        button(53, Items.of(Material.BEACON).name("&6&lGive to everyone online")
                .lore("&7Gives &f" + amount + "x " + Text.strip(crate.getDisplayName()) + " &7to",
                        "&f" + online.size() + " &7online players.", "", "&eClick to confirm").build(), e ->
                new ConfirmMenu(player, "Give everyone " + amount + " keys?", () -> {
                    give("all");
                    open();
                }, this::open).open());

        backButton();
    }

    private void backButton() {
        button(45, Items.of(Material.ARROW).name("&e« Back").build(),
                e -> (back != null ? back : new MainMenu(player)).open());
    }

    private void give(String target) {
        player.performCommand("gacha givekey " + target + " " + crate.getName() + " " + amount);
        successSound();
    }

    private ItemStack head(Player target) {
        ItemStack head = Items.of(Material.PLAYER_HEAD).name("&a" + target.getName())
                .lore("&7Keys of this crate: &f" + plugin().getKeyManager().countKeys(target).getOrDefault(crate.getId(), 0),
                        "", "&eClick to give &f" + amount + "x " + Text.strip(crate.getDisplayName()) + " &ekey"
                                + (amount == 1 ? "" : "s")).build();
        if (head.getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            head.setItemMeta(meta);
        }
        return head;
    }
}
