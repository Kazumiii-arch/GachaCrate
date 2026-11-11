package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.shop.ShopManager;
import com.kazumiii.gachacrate.util.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Buy crate keys with money (Vault).
 */
public class ShopMenu extends Menu {

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    public ShopMenu(Player player) {
        super(player);
    }

    @Override
    protected String getTitle() {
        return "§8Key Shop";
    }

    @Override
    protected int getSize() {
        return 45;
    }

    @Override
    protected void draw() {
        fill(Material.GREEN_STAINED_GLASS_PANE);
        for (int slot : SLOTS) inventory.setItem(slot, null);
        ShopManager shop = plugin().getShopManager();

        item(4, Items.of(Material.EMERALD).name("&aKey Shop")
                .lore("&7Balance: &f" + shop.format(shop.balance(player)), "",
                        "&7Left-click: buy 1 key", "&7Shift-click: buy 10 keys", "&7Right-click: see rewards").build());

        List<Crate> crates = shop.getCratesForSale();
        Map<String, Integer> keys = plugin().getKeyManager().countKeys(player);
        for (int i = 0; i < SLOTS.length && i < crates.size(); i++) {
            Crate crate = crates.get(i);
            List<String> lore = new ArrayList<>();
            lore.add("&7Price: &a" + shop.format(crate.getKeyPrice()) + " &7each");
            lore.add("&7You have: &f" + keys.getOrDefault(crate.getId(), 0) + " keys");
            lore.add("");
            lore.add("&a► Left-click: buy 1");
            lore.add("&a► Shift-click: buy 10 &7(" + shop.format(crate.getKeyPrice() * 10) + ")");
            lore.add("&e► Right-click: see rewards");
            button(SLOTS[i], Items.of(Material.TRIPWIRE_HOOK).name(crate.getDisplayName() + " &6Key").lore(lore).glow(true).build(), e -> {
                if (e.isRightClick() && !e.isShiftClick()) {
                    new CratePreviewMenu(player, crate).open();
                    return;
                }
                if (shop.buyKeys(player, crate, e.isShiftClick() ? 10 : 1)) refresh();
            });
        }
        if (crates.isEmpty()) item(22, Items.of(Material.BARRIER).name("&cNothing for sale right now").build());

        button(36, Items.of(Material.ARROW).name("&e« Back").build(), e -> new PlayerMenu(player).open());
    }
}
