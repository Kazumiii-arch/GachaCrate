package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.history.WinRecord;
import com.kazumiii.gachacrate.player.PlayerData;
import com.kazumiii.gachacrate.util.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A player's recent wins, or (when {@code target} is null) the server's recent big wins.
 */
public class HistoryMenu extends Menu {

    private final UUID target;
    private final String targetName;

    public HistoryMenu(Player player, UUID target, String targetName) {
        super(player);
        this.target = target;
        this.targetName = targetName;
    }

    private boolean isServerFeed() {
        return target == null;
    }

    @Override
    protected String getTitle() {
        if (isServerFeed()) return "§8Recent Big Wins";
        return target.equals(player.getUniqueId()) ? "§8My Recent Wins" : "§8Wins: " + targetName;
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected void draw() {
        fillRow(5, Material.BLACK_STAINED_GLASS_PANE);

        List<WinRecord> records;
        if (isServerFeed()) {
            records = plugin().getHistoryManager().getServerWins();
        } else {
            PlayerData data = plugin().getPlayerManager().getOfflineData(target);
            records = data == null ? new ArrayList<>() : data.getHistory();
        }

        for (int i = 0; i < records.size() && i < 45; i++) {
            item(i, icon(records.get(i)));
        }
        if (records.isEmpty()) {
            item(22, Items.of(Material.PAPER).name("&7Nothing here yet")
                    .lore(isServerFeed() ? "&7Big wins will show up here." : "&7Open a crate to start!").build());
        }

        button(45, Items.of(Material.ARROW).name("&e« Back").build(), e -> new PlayerMenu(player).open());
        if (isServerFeed()) {
            button(49, Items.of(Material.BOOK).name("&eMy Recent Wins").build(),
                    e -> new HistoryMenu(player, player.getUniqueId(), player.getName()).open());
        } else {
            button(49, Items.of(Material.NETHER_STAR).name("&dRecent Big Wins").build(),
                    e -> new HistoryMenu(player, null, null).open());
        }
    }

    private ItemStack icon(WinRecord record) {
        Crate crate = plugin().getCrateManager().getCrate(record.getCrateId());
        Reward reward = crate == null ? null : crate.getRewardById(record.getRewardId());
        Rarity rarity = plugin().getRarityManager().getRarity(record.getRarityId());

        ItemStack base = reward != null ? reward.getDisplayItem() : new ItemStack(Material.PAPER);
        List<String> lore = new ArrayList<>();
        lore.add("");
        if (isServerFeed()) lore.add("&7Won by &f" + record.getPlayerName());
        lore.add("&7Reward: &f" + record.getRewardName());
        if (rarity != null) lore.add("&7Rarity: " + rarity.getDisplayName());
        lore.add("&7Crate: " + (crate == null ? "&f" + record.getCrateId() : crate.getDisplayName()));
        lore.add("&8" + record.ago());
        return Items.copy(base).addLore(lore).build();
    }
}
