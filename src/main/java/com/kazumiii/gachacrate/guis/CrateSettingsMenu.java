package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.animations.AnimationSelectorMenu;
import com.kazumiii.gachacrate.animations.IdleParticleMenu;
import com.kazumiii.gachacrate.conversation.ChatPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * All settings of one crate.
 */
public class CrateSettingsMenu extends Menu {

    private final Crate crate;

    public CrateSettingsMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    protected String getTitle() {
        return "§8Settings: " + crate.getName();
    }

    @Override
    protected int getSize() {
        return 45;
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);

        item(4, Items.of(Material.CHEST).name(crate.getDisplayName())
                .lore("&7Id: &f" + crate.getName(), "&7Rewards: &f" + crate.getRewards().size()).build());

        button(10, Items.of(Material.CHEST).name("&bView & Edit Rewards")
                .lore("&7Add, remove or edit the", "&7rewards inside this crate.").build(),
                e -> new CrateEditorMenu(player, crate).open());

        button(11, Items.of(Material.DIAMOND).name("&dPity System")
                .lore("&7Status: " + (crate.isPityEnabled() ? "&aON" : "&cOFF"),
                        "&7Guarantees a good reward", "&7after many unlucky opens.").build(),
                e -> new PityEditorMenu(player, crate).open());

        button(12, Items.of(Material.CLOCK).name("&6Time Limit")
                .lore("&7Status: " + (crate.isTimeLimitEnabled() ? "&aON" : "&cOFF"),
                        "&7Only allow opening between", "&7two dates.").build(),
                e -> new TimeLimitEditorMenu(player, crate).open());

        button(14, Items.of(Material.JUKEBOX).name("&aAnimation")
                .lore("&7Current: &f" + crate.getAnimationType().getDisplayName()).build(),
                e -> new AnimationSelectorMenu(player, crate).open());

        button(15, Items.of(Material.ENDER_EYE).name("&eOpening Mode")
                .lore("&7Current: &f" + crate.getOpeningMode().getDisplayName()).build(),
                e -> new OpeningModeMenu(player, crate).open());

        button(16, Items.of(Material.GLOWSTONE_DUST).name("&6Idle Particles")
                .lore("&7Current: &f" + (crate.getIdleParticle() == null ? "None" : crate.getIdleParticle().name()),
                        "&7Effect shown around crate blocks.").build(),
                e -> new IdleParticleMenu(player, crate).open());

        button(20, Items.of(Material.NAME_TAG).name("&bDisplay Name")
                .lore("&7Current: " + crate.getDisplayName(), "", "&7The name players see.",
                        "&7Supports & color codes.", "", "&e► Click to change", "&e► Right-click to reset").build(),
                e -> {
                    if (e.isRightClick()) {
                        crate.setDisplayName(null);
                        saveAndRefreshHolograms();
                        refresh();
                        return;
                    }
                    ChatPrompt.ask(player, "&eType the new display name &7(e.g. &6&lLegendary Crate&7)", input -> {
                        if (input.length() > 64) return "That name is too long (max 64 characters).";
                        crate.setDisplayName(input);
                        saveAndRefreshHolograms();
                        Text.send(player, "&aDisplay name set to " + crate.getDisplayName());
                        return null;
                    }, this::open);
                });

        button(22, Items.of(Material.TRIPWIRE_HOOK).name("&eGet Keys")
                .lore("&7Click: 1 key", "&7Shift-click: 16 keys").build(), e -> {
            if (!player.hasPermission("gacha.admin.givekey")) {
                Text.send(player, "&cYou don't have permission to give keys.");
                return;
            }
            int amount = e.isShiftClick() ? 16 : 1;
            plugin().getKeyManager().giveKeys(player, crate, amount);
            Text.send(player, "&aYou got " + amount + "x " + crate.getDisplayName() + " &akey.");
        });

        button(24, Items.of(Material.ENDER_CHEST).name("&aTest Open")
                .lore("&7Opens the crate for free", "&7(no key used).").build(), e -> {
            if (!player.hasPermission("gacha.admin.open")) {
                Text.send(player, "&cYou don't have permission to test-open crates.");
                return;
            }
            player.closeInventory();
            plugin().getCrateOpener().openFree(player, crate, 1);
        });

        button(36, Items.of(Material.ARROW).name("&e« Back to Crate List").build(),
                e -> new MainMenu(player).open());

        button(44, Items.of(Material.LAVA_BUCKET).name("&c&lDelete Crate")
                .lore("&7Deletes this crate, its rewards", "&7and its crate blocks.", "", "&cThis can't be undone!").build(),
                e -> {
                    if (!player.hasPermission("gacha.admin.delete")) {
                        Text.send(player, "&cYou don't have permission to delete crates.");
                        return;
                    }
                    new ConfirmMenu(player, "Delete " + crate.getName() + "?", () -> {
                        plugin().getCrateManager().deleteCrate(crate.getName());
                        Text.send(player, "&aCrate &e" + crate.getName() + " &adeleted.");
                        new MainMenu(player).open();
                    }, this::open).open();
                });
    }

    private void saveAndRefreshHolograms() {
        plugin().getCrateManager().saveCrates();
        plugin().getHologramManager().refresh(crate);
    }
}
