package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;

/**
 * Add an item reward: put the item in the empty slot, pick rarity and weight, confirm.
 * Everything is done with clicks (no chat), so the placed item can't get lost.
 * The placed item always goes back to the player when the menu closes; the reward is a copy.
 */
public class AddItemMenu extends Menu {

    private static final int ITEM_SLOT = 22;

    private final Crate crate;
    private Rarity rarity;
    private double chance = 10;

    public AddItemMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        this.rarity = plugin().getRarityManager().getLowest();
    }

    @Override
    protected String getTitle() {
        return "§8Add Item Reward";
    }

    @Override
    protected int getSize() {
        return 45;
    }

    @Override
    protected Set<Integer> getFreeSlots() {
        return Collections.singleton(ITEM_SLOT);
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);

        item(13, Items.of(Material.PAPER).name("&ePlace the item below")
                .lore("&7Pick up an item from your inventory", "&7and put it in the empty slot.").build());

        button(38, Items.of(Material.NAME_TAG).name("&bRarity: " + rarity.getDisplayName())
                .lore("&7Left-click: next rarity", "&7Right-click: previous rarity").build(), e -> {
            rarity = plugin().getRarityManager().next(rarity, !e.isRightClick());
            refresh();
        });

        button(40, Items.of(Material.EXPERIENCE_BOTTLE).name("&aWeight: &f" + Text.number(chance))
                .lore("&7How likely this reward is,", "&7compared to the other rewards.", "",
                        "&7Left-click: &a+1  &7Right-click: &c-1",
                        "&7Shift + left: &a+10  &7Shift + right: &c-10",
                        "", "&7Need decimals? Edit the reward", "&7afterwards to type an exact value.").build(),
                this::changeChance);

        button(42, Items.of(Material.LIME_STAINED_GLASS_PANE).name("&a&lCONFIRM & ADD").build(), e -> confirm());

        button(36, Items.of(Material.RED_STAINED_GLASS_PANE).name("&c« Cancel")
                .lore("&7Your item will be returned.").build(),
                e -> new RewardTypeSelectionMenu(player, crate).open());
    }

    private void changeChance(InventoryClickEvent e) {
        double step = e.isShiftClick() ? 10 : 1;
        chance += e.isRightClick() ? -step : step;
        if (chance < 1) chance = 1;
        refresh();
    }

    private void confirm() {
        ItemStack item = inventory.getItem(ITEM_SLOT);
        if (item == null || item.getType() == Material.AIR) {
            Text.send(player, "&cPut an item in the empty slot first!");
            return;
        }
        // A copy becomes the reward; the original goes back to the player when the menu closes.
        crate.addReward(new Reward(item.clone(), rarity, chance));
        plugin().getCrateManager().saveCrates();
        Text.send(player, "&aItem reward added! Your item was given back.");
        new CrateEditorMenu(player, crate).open();
    }

    @Override
    protected void onClose() {
        returnFreeSlotItems();
    }
}
