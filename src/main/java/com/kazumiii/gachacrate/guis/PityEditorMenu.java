package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.conversation.ChatPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Pity: after N opens without a reward of the chosen rarity (or better), the next one is guaranteed.
 */
public class PityEditorMenu extends Menu {

    private final Crate crate;

    public PityEditorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    protected String getTitle() {
        return "§8Pity: " + crate.getName();
    }

    @Override
    protected int getSize() {
        return 27;
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);
        boolean enabled = crate.isPityEnabled();
        Rarity rarity = plugin().getRarityManager().getRarity(crate.getPityRarityMinimum());

        item(4, Items.of(Material.BOOK).name("&dHow pity works").lore(
                "&7If a player opens this crate &f" + crate.getPityThreshold() + "&7 times",
                "&7in a row without getting " + (rarity == null ? "&fthe chosen rarity" : rarity.getDisplayName()),
                "&7or better, that open is guaranteed to be",
                "&7at least that rarity. The counter resets",
                "&7whenever they win one.").build());

        button(11, Items.of(enabled ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&6Pity System: " + (enabled ? "&aON" : "&cOFF")).lore("&e► Click to toggle").build(), e -> {
            crate.setPityEnabled(!crate.isPityEnabled());
            if (crate.isPityEnabled() && plugin().getRarityManager().getRarity(crate.getPityRarityMinimum()) == null) {
                // Default to the second-best rarity so pity works right away.
                java.util.List<Rarity> all = plugin().getRarityManager().getRarities();
                crate.setPityRarityMinimum(all.get(Math.max(0, all.size() - 2)).getId());
            }
            save();
            refresh();
        });

        button(13, Items.of(Material.EXPERIENCE_BOTTLE).name("&bOpens Needed: &f" + crate.getPityThreshold())
                .lore("&e► Click to change").build(),
                e -> ChatPrompt.ask(player, "&eHow many opens until pity kicks in? &7(e.g. 50)", input -> {
                    int value;
                    try {
                        value = Integer.parseInt(input);
                    } catch (NumberFormatException ex) {
                        return "Please type a whole number.";
                    }
                    if (value < 1) return "It must be at least 1.";
                    crate.setPityThreshold(value);
                    save();
                    Text.send(player, "&aPity threshold set to " + value + ".");
                    return null;
                }, this::open));

        button(15, Items.of(Material.NAME_TAG).name("&dGuaranteed Rarity: " + (rarity == null ? "&cNot set" : rarity.getDisplayName()))
                .lore("&7The minimum rarity pity gives.", "", "&7Left-click: next", "&7Right-click: previous").build(), e -> {
            Rarity next = plugin().getRarityManager().next(rarity, !e.isRightClick());
            crate.setPityRarityMinimum(next.getId());
            save();
            refresh();
        });

        button(18, Items.of(Material.ARROW).name("&e« Back to Settings").build(),
                e -> new CrateSettingsMenu(player, crate).open());
    }

    private void save() {
        plugin().getCrateManager().saveCrates();
        plugin().getHologramManager().refresh(crate);
    }
}
