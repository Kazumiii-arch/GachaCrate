// ========================================================================
// File: CratePreviewMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/CratePreviewMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.crates.RewardType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Player-facing preview of all rewards in a crate, paginated and sorted
 * by rarity (best first).
 */
public class CratePreviewMenu extends Menu {

    private static final int REWARDS_PER_PAGE = 28; // 4 rows of 7 slots

    private final Crate crate;
    private int page = 1;
    private final List<Reward> sortedRewards;

    public CratePreviewMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        this.sortedRewards = new ArrayList<>(crate.getRewards());
        this.sortedRewards.sort((a, b) -> {
            int orderA = a.getRarity() != null ? a.getRarity().getOrder() : 0;
            int orderB = b.getRarity() != null ? b.getRarity().getOrder() : 0;
            return Integer.compare(orderB, orderA); // best rarity first
        });
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 54, "§8Preview: " + crate.getDisplayName());
        drawItems();
        player.openInventory(inventory);
    }

    private void drawItems() {
        inventory.clear();
        ItemBuilder.drawBorder(inventory);

        int totalPages = Math.max(1, (int) Math.ceil((double) sortedRewards.size() / REWARDS_PER_PAGE));

        // Header — crate info & pagination
        inventory.setItem(4, new ItemBuilder(Material.BOOK)
                .name(ChatColor.GOLD + crate.getDisplayName() + " Crate")
                .blank()
                .lore(ChatColor.GRAY + "Rewards: " + ChatColor.WHITE + sortedRewards.size())
                .lore(ChatColor.GRAY + "Page: " + ChatColor.WHITE + page + " / " + totalPages)
                .build());

        // Pagination controls (only when applicable)
        if (page > 1) {
            inventory.setItem(48, new ItemBuilder(Material.ARROW)
                    .name(ChatColor.YELLOW + "« Previous Page")
                    .build());
        }
        if (page < totalPages) {
            inventory.setItem(50, new ItemBuilder(Material.ARROW)
                    .name(ChatColor.YELLOW + "Next Page »")
                    .build());
        }

        // Rewards for the current page
        int startIndex = (page - 1) * REWARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + REWARDS_PER_PAGE, sortedRewards.size());
        int slot = 10;

        for (int i = startIndex; i < endIndex; i++) {
            Reward reward = sortedRewards.get(i);
            if (slot % 9 == 0 || slot % 9 == 8) slot++; // skip border columns

            ItemStack displayItem;
            if (reward.getType() == RewardType.ITEM && reward.getItem() != null) {
                displayItem = reward.getItem().clone();
            } else {
                displayItem = reward.getPreviewItem() != null
                        ? reward.getPreviewItem().clone()
                        : new ItemStack(Material.COMMAND_BLOCK);
            }

            ItemMeta rewardMeta = displayItem.getItemMeta();
            if (rewardMeta != null) {
                List<String> lore = rewardMeta.hasLore() && rewardMeta.getLore() != null
                        ? new ArrayList<>(rewardMeta.getLore())
                        : new ArrayList<>();
                lore.add("");
                if (reward.getRarity() != null) {
                    lore.add("§r" + reward.getRarity().getStyledName());
                }
                lore.add(ChatColor.GOLD + "Chance: " + ChatColor.YELLOW + reward.getChance() + "%");
                rewardMeta.setLore(lore);
                displayItem.setItemMeta(rewardMeta);
            }

            inventory.setItem(slot, displayItem);
            slot++;
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.ARROW) return;

        String displayName = clicked.getItemMeta().getDisplayName();
        if (displayName.contains("Next")) {
            page++;
            drawItems();
        } else if (displayName.contains("Previous")) {
            page--;
            drawItems();
        }
    }
}
