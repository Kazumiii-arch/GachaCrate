package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.crates.RewardType;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists a crate's rewards. Click to edit, shift + right-click to remove.
 */
public class CrateEditorMenu extends Menu {

    private static final int PER_PAGE = 45;

    private final Crate crate;
    private int page = 0;

    public CrateEditorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    protected String getTitle() {
        return "§8Rewards: " + crate.getName();
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected void draw() {
        fillRow(5, Material.BLACK_STAINED_GLASS_PANE);

        List<Reward> rewards = crate.getRewards();
        int pages = Math.max(1, (int) Math.ceil(rewards.size() / (double) PER_PAGE));
        page = Math.max(0, Math.min(page, pages - 1));

        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < rewards.size(); i++) {
            Reward reward = rewards.get(start + i);
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (reward.getType() == RewardType.COMMAND) {
                lore.add("&7Commands:");
                lore.addAll(reward.describeCommands());
                lore.add("");
            }
            lore.add("&eRarity: " + reward.getRarity().getDisplayName());
            lore.add("&eWeight: &f" + Text.number(reward.getChance())
                    + " &7(" + Text.number(crate.getPercent(reward)) + "%)");
            if (!reward.isValid()) lore.add("&c⚠ This reward is broken and can't be won.");
            lore.add("");
            lore.add("&a► Click to edit");
            lore.add("&c► Shift + right-click to remove");

            button(i, Items.copy(reward.getDisplayItem()).addLore(lore).build(), e -> {
                if (e.isShiftClick() && e.isRightClick()) {
                    crate.removeReward(reward);
                    plugin().getCrateManager().saveCrates();
                    Text.send(player, "&aReward removed.");
                    refresh();
                } else {
                    new RewardEditorMenu(player, crate, reward).open();
                }
            });
        }

        button(45, Items.of(Material.ARROW).name("&e« Back to Settings").build(),
                e -> new CrateSettingsMenu(player, crate).open());

        if (page > 0) {
            button(48, Items.of(Material.ARROW).name("&e« Previous Page").build(), e -> {
                page--;
                refresh();
            });
        }
        item(49, Items.of(Material.BOOK).name("&6Rewards")
                .lore("&7Total: &f" + rewards.size(), "&7Page: &f" + (page + 1) + "/" + pages, "",
                        "&7Weight = how likely a reward is.", "&7The % is its real chance.").build());
        if (page < pages - 1) {
            button(50, Items.of(Material.ARROW).name("&eNext Page »").build(), e -> {
                page++;
                refresh();
            });
        }

        button(53, Items.of(Material.NETHER_STAR).name("&a✚ Add New Reward")
                .lore("&7Add an item or command reward.").build(),
                e -> new RewardTypeSelectionMenu(player, crate).open());
    }
}
