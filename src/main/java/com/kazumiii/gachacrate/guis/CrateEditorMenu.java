package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.crates.RewardType;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.kazumiii.gachacrate.crates.Rarity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            lore.add("&b► Right-click to duplicate");
            lore.add("&c► Shift + right-click to remove");

            button(i, Items.copy(reward.getDisplayItem()).addLore(lore).build(), e -> {
                if (e.isShiftClick() && e.isRightClick()) {
                    crate.removeReward(reward);
                    plugin().getCrateManager().saveCrates();
                    plugin().getHologramManager().refresh(crate);
                    Text.send(player, "&aReward removed.");
                    refresh();
                } else if (e.isRightClick()) {
                    plugin().getCrateManager().duplicateReward(crate, reward);
                    plugin().getHologramManager().refresh(crate);
                    successSound();
                    Text.send(player, "&aReward duplicated. Edit the copy to change it.");
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
                .lore(summary(rewards.size(), page, pages)).build());

        button(46, Items.of(Material.COMPARATOR).name("&eSort by Rarity")
                .lore("&7Best rarity first, then rarest.", "&7Also the order players see.").build(), e -> {
            crate.sortRewards();
            plugin().getCrateManager().saveCrates();
            successSound();
            refresh();
        });

        button(47, Items.of(Material.SHULKER_BOX).name("&aBulk Add Items")
                .lore("&7Drop up to 36 items at once.").build(),
                e -> new BulkAddMenu(player, crate).open());

        button(52, Items.of(Material.SPYGLASS).name("&bPreview as Player")
                .lore("&7See this crate the way", "&7players see it.").build(),
                e -> new CratePreviewMenu(player, crate).open());
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

    /** Info lines: totals and the chance of each rarity, so the crate's balance is easy to check. */
    private List<String> summary(int total, int page, int pages) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Total: &f" + total);
        lore.add("&7Page: &f" + (page + 1) + "/" + pages);

        Map<Rarity, Double> byRarity = new LinkedHashMap<>();
        List<Rarity> rarities = new ArrayList<>(plugin().getRarityManager().getRarities());
        rarities.sort((a, b) -> Integer.compare(b.getOrder(), a.getOrder()));
        for (Rarity rarity : rarities) byRarity.put(rarity, 0.0);
        for (Reward reward : crate.getValidRewards()) {
            if (reward.getRarity() != null) byRarity.merge(reward.getRarity(), crate.getPercent(reward), Double::sum);
        }
        lore.add("");
        lore.add("&7Chance per rarity:");
        for (Map.Entry<Rarity, Double> entry : byRarity.entrySet()) {
            if (entry.getValue() <= 0) continue;
            lore.add("  " + entry.getKey().getDisplayName() + " &8» &f" + Text.number(entry.getValue()) + "%");
        }
        lore.add("");
        lore.add("&7Weight = how likely a reward is.");
        lore.add("&7The % is its real chance.");
        return lore;
    }
}
