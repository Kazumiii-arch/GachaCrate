package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * Add many item rewards at once: drop up to 36 items into the top area, pick one rarity and weight,
 * and confirm. Every stack becomes its own reward (with its amount). The items always go back to
 * the player; the rewards are copies.
 */
public class BulkAddMenu extends Menu {

    private static final int AREA = 36;
    private static final Set<Integer> FREE = new HashSet<>();

    static {
        for (int i = 0; i < AREA; i++) FREE.add(i);
    }

    private final Crate crate;
    private Rarity rarity;
    private double chance = 10;

    public BulkAddMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        this.rarity = plugin().getRarityManager().getLowest();
    }

    @Override
    protected String getTitle() {
        return "§8✦ §aBulk Add §8┃ §7" + crate.getName();
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected Set<Integer> getFreeSlots() {
        return FREE;
    }

    @Override
    protected void draw() {
        fillRow(4, Material.LIME_STAINED_GLASS_PANE);
        fillRow(5, Material.BLACK_STAINED_GLASS_PANE);

        item(40, Items.of(Material.BOOK).name("&aDrop items in the area above")
                .lore("&7Up to &f36 &7stacks. Each stack becomes", "&7one reward with the same rarity", "&7and weight.",
                        "", "&7Pick up items from your inventory", "&7and click or drag them into place.",
                        "", "&7Your items are always given back.").build());

        button(45, Items.of(Material.RED_STAINED_GLASS_PANE).name("&c« Cancel")
                .lore("&7Your items will be returned.").build(),
                e -> new RewardTypeSelectionMenu(player, crate).open());

        button(47, Items.of(Material.NAME_TAG).name("&bRarity: " + rarity.getDisplayName())
                .lore("&7Left-click: next rarity", "&7Right-click: previous rarity").build(), e -> {
            rarity = plugin().getRarityManager().next(rarity, !e.isRightClick());
            refresh();
        });

        button(49, Items.of(Material.EXPERIENCE_BOTTLE).name("&aWeight: &f" + Text.number(chance))
                .lore("&7For every reward added.", "",
                        "&7Left-click: &a+1  &7Right-click: &c-1",
                        "&7Shift + left: &a+10  &7Shift + right: &c-10").build(), e -> {
            double step = e.isShiftClick() ? 10 : 1;
            chance = Math.max(1, chance + (e.isRightClick() ? -step : step));
            refresh();
        });

        button(53, Items.of(Material.LIME_CONCRETE).name("&a&lADD ALL").glow(true)
                .lore("&7Adds every item above", "&7as a " + rarity.getDisplayName() + " &7reward.").build(), e -> confirm());
    }

    private void confirm() {
        int added = 0;
        for (int slot = 0; slot < AREA; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            crate.addReward(new Reward(item.clone(), rarity, chance));
            added++;
        }
        if (added == 0) {
            errorSound();
            Text.send(player, "&cPut some items in the top area first!");
            return;
        }
        plugin().getCrateManager().saveCrates();
        plugin().getHologramManager().refresh(crate);
        successSound();
        Text.send(player, "&aAdded &e" + added + " &areward" + (added == 1 ? "" : "s") + " as " + rarity.getDisplayName()
                + "&a. Your items were given back.");
        new CrateEditorMenu(player, crate).open();
    }

    @Override
    protected void onClose() {
        returnFreeSlotItems();
    }
}
