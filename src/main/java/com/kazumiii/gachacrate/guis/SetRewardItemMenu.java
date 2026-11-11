package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.crates.RewardType;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;

/**
 * Replace the item of an item reward, or the icon of a command reward.
 * The placed item is always given back; the reward stores a copy.
 */
public class SetRewardItemMenu extends Menu {

    private static final int ITEM_SLOT = 13;

    private final Crate crate;
    private final Reward reward;

    public SetRewardItemMenu(Player player, Crate crate, Reward reward) {
        super(player);
        this.crate = crate;
        this.reward = reward;
    }

    private boolean isIcon() {
        return reward.getType() == RewardType.COMMAND;
    }

    @Override
    protected String getTitle() {
        return isIcon() ? "§8Set Reward Icon" : "§8Replace Reward Item";
    }

    @Override
    protected int getSize() {
        return 27;
    }

    @Override
    protected Set<Integer> getFreeSlots() {
        return Collections.singleton(ITEM_SLOT);
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);

        item(4, Items.of(Material.PAPER).name("&ePlace the new item below")
                .lore("&7Current:", "&f" + reward.getDisplayName()).build());

        button(21, Items.of(Material.LIME_STAINED_GLASS_PANE).name("&a&lCONFIRM").build(), e -> {
            ItemStack item = inventory.getItem(ITEM_SLOT);
            if (item == null || item.getType() == Material.AIR) {
                Text.send(player, "&cPut an item in the empty slot first!");
                return;
            }
            if (isIcon()) reward.setPreviewItem(item.clone());
            else reward.setItem(item.clone());
            plugin().getCrateManager().saveCrates();
            Text.send(player, isIcon() ? "&aIcon updated! Your item was given back." : "&aReward item updated! Your item was given back.");
            new RewardEditorMenu(player, crate, reward).open();
        });

        if (isIcon() && reward.getPreviewItem() != null) {
            button(22, Items.of(Material.BARRIER).name("&cRemove Icon")
                    .lore("&7Use the default command block icon.").build(), e -> {
                reward.setPreviewItem(null);
                plugin().getCrateManager().saveCrates();
                new RewardEditorMenu(player, crate, reward).open();
            });
        }

        button(23, Items.of(Material.RED_STAINED_GLASS_PANE).name("&c&lCANCEL").build(),
                e -> new RewardEditorMenu(player, crate, reward).open());
    }

    @Override
    protected void onClose() {
        returnFreeSlotItems();
    }
}
