package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.OpeningMode;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Physical Block: right-click a crate block with a key.
 * Key in Hand: right-click anywhere while holding a key.
 */
public class OpeningModeMenu extends Menu {

    private final Crate crate;

    public OpeningModeMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    protected String getTitle() {
        return "§8Opening Mode: " + crate.getName();
    }

    @Override
    protected int getSize() {
        return 27;
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);
        modeButton(11, OpeningMode.PHYSICAL_BLOCKS, Material.CHEST,
                "&7Players right-click a crate block", "&7while holding a key.", "&7Set blocks with &f/gacha setblock");
        modeButton(15, OpeningMode.KEY_IN_HAND, Material.TRIPWIRE_HOOK,
                "&7Players right-click anywhere", "&7while holding a key.");
        button(18, Items.of(Material.ARROW).name("&e« Back to Settings").build(),
                e -> new CrateSettingsMenu(player, crate).open());
    }

    private void modeButton(int slot, OpeningMode mode, Material icon, String... description) {
        boolean selected = crate.getOpeningMode() == mode;
        java.util.List<String> lore = new java.util.ArrayList<>(java.util.Arrays.asList(description));
        lore.add("");
        lore.add(selected ? "&a✔ Selected" : "&e► Click to select");
        button(slot, Items.of(icon).name((selected ? "&a" : "&f") + mode.getDisplayName()).lore(lore).glow(selected).build(), e -> {
            crate.setOpeningMode(mode);
            plugin().getCrateManager().saveCrates();
            Text.send(player, "&aOpening mode set to " + mode.getDisplayName() + ".");
            refresh();
        });
    }
}
