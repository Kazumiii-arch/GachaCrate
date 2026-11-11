package com.kazumiii.gachacrate.animations;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.guis.CrateSettingsMenu;
import com.kazumiii.gachacrate.guis.Menu;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pick the opening animation for a crate.
 */
public class AnimationSelectorMenu extends Menu {

    private final Crate crate;

    public AnimationSelectorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    protected String getTitle() {
        return "§8Animation: " + crate.getName();
    }

    @Override
    protected int getSize() {
        return 27;
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);
        AnimationType[] types = AnimationType.values();
        int[] slots = {11, 13, 15};
        for (int i = 0; i < types.length && i < slots.length; i++) {
            AnimationType type = types[i];
            boolean selected = crate.getAnimationType() == type;
            List<String> lore = new ArrayList<>();
            for (String line : type.getDescription()) lore.add("&7" + line);
            lore.add("");
            lore.add(selected ? "&a✔ Selected" : "&e► Click to select");
            button(slots[i], Items.of(type.getIcon()).name((selected ? "&a" : "&b") + type.getDisplayName())
                    .lore(lore).glow(selected).build(), e -> {
                crate.setAnimationType(type);
                plugin().getCrateManager().saveCrates();
                Text.send(player, "&aAnimation set to " + type.getDisplayName() + ".");
                refresh();
            });
        }
        button(18, Items.of(Material.ARROW).name("&e« Back to Settings").build(),
                e -> new CrateSettingsMenu(player, crate).open());
        item(26, Items.of(Material.BOOK).name("&7Tip").lore(Arrays.asList(
                "&7Use &fTest Open &7in the crate", "&7settings to see the animation.")).build());
    }
}
