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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CratePreviewMenu extends Menu {

    private final Crate crate;
    private int page = 1;
    private final int rewardsPerPage = 28; // 4 rows of 7 slots
    private final List<Reward> sortedRewards;

    public CratePreviewMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        // Sort rewards by rarity order (descending) when the menu is created
        this.sortedRewards = crate.getRewards().stream()
                .sorted(Comparator.comparingInt(r -> r.getRarity().getOrder() * -1))
                .collect(Collectors.toList());
    }

    @Override
    public void open() {
        // 54 slots = 6 rows
        inventory = Bukkit.createInventory(this, 54, "§8Preview: " + crate.getName());
        drawItems();
        player.openInventory(inventory);
    }

    private void drawItems() {
        inventory.clear();

        // --- Decorative Borders ---
        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(" ");
        placeholder.setItemMeta(meta);
        for (int i = 0; i < 9; i++) inventory.setItem(i, placeholder); // Top row
        for (int i = 45; i < 54; i++) inventory.setItem(i, placeholder); // Bottom row
        for (int i = 9; i <= 36; i += 9) inventory.setItem(i, placeholder); // Left border
        for (int i = 17; i <= 44; i += 9) inventory.setItem(i, placeholder); // Right border

        // --- Info & Control Buttons ---
        int totalPages = (int) Math.ceil((double) sortedRewards.size() / rewardsPerPage);

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + crate.getName() + " Crate");
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Total Rewards: " + ChatColor.WHITE + sortedRewards.size(),
                ChatColor.GRAY + "Page: " + ChatColor.WHITE + page + "/" + Math.max(1, totalPages)
        ));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);

        if (page > 1) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "« Previous Page");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(48, prevButton);
        }

        if (page < totalPages) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page »");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(50, nextButton);
        }

        // --- Display Rewards for the Current Page ---
        int startIndex = (page - 1) * rewardsPerPage;
        int endIndex = Math.min(startIndex + rewardsPerPage, sortedRewards.size());
        int slot = 10; // Starting slot for rewards

        for (int i = startIndex; i < endIndex; i++) {
            Reward reward = sortedRewards.get(i);
            ItemStack displayItem;
            
            if (reward.getType() == RewardType.ITEM) {
                displayItem = reward.getItem().clone();
            } else {
                displayItem = (reward.getPreviewItem() != null) ? reward.getPreviewItem().clone() : new ItemStack(Material.COMMAND_BLOCK);
            }

            ItemMeta rewardMeta = displayItem.getItemMeta();
            if (rewardMeta != null) {
                List<String> lore = rewardMeta.hasLore() ? new ArrayList<>(rewardMeta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add("§r" + reward.getRarity().getDisplayName());
                lore.add(ChatColor.GOLD + "Chance: " + ChatColor.YELLOW + reward.getChance() + "%");
                rewardMeta.setLore(lore);
                displayItem.setItemMeta(rewardMeta);
            }
            
            inventory.setItem(slot, displayItem);

            // Increment slot, skipping border slots
            slot++;
            if (slot % 9 == 8) {
                slot += 2;
            }
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
