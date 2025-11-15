package com.kazumiii.gachacrate.ui;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crate.Crate;
import com.kazumiii.gachacrate.crate.Reward;
import com.kazumiii.gachacrate.crate.Rarity;
import com.kazumiii.gachacrate.crate.SpinAnimation;
import com.kazumiii.gachacrate.stats.PlayerStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager implements Listener {

    private final GachaCrate plugin;

    public GUIManager(GachaCrate plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------
    // PUBLIC: Open crate animation
    // ------------------------------------------------------------
    public void openCrateAnimation(Player player, Crate crate) {
        switch (crate.getAnimation()) {
            case ROLLBAR:
                new RollBarAnimation(plugin, player, crate).start();
                break;

            case SLOT:
                new SlotAnimation(plugin, player, crate).start();
                break;

            case WHEEL:
                new WheelAnimation(plugin, player, crate).start();
                break;

            case SIMPLE:
            default:
                openSimpleResult(player, crate);
                break;
        }
    }

    // ------------------------------------------------------------
    // Simple instant animation (no GUI)
    // ------------------------------------------------------------
    private void openSimpleResult(Player player, Crate crate) {
        Reward reward = plugin.getCrateManager().rollReward(crate);
        if (reward == null) {
            player.sendMessage("§cThis crate has no rewards!");
            return;
        }

        reward.apply(player);

        player.sendMessage("§eYou opened §6" + crate.getDisplayName() +
                "§e and received §b" + reward.getDisplayName() + "§e!");

        // Update stats
        plugin.getStatsManager().rewardReceived(
                player,
                crate.getId(),
                reward.getDisplayName(),
                reward.getRarity(),
                reward.getMoney(),
                reward.getCommands().size(),
                reward.isJackpot()
        );
    }

    // ------------------------------------------------------------
    // PUBLIC: Open crate preview menu
    // ------------------------------------------------------------
    public void openPreview(Player player, Crate crate) {
        Inventory inv = Bukkit.createInventory(null, 54,
                crate.getColor() + crate.getDisplayName() + " §7Preview");

        int slot = 0;
        for (Reward reward : crate.getRewards()) {
            if (slot >= 54) break;

            inv.setItem(slot++, buildPreviewItem(reward));
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    // ------------------------------------------------------------
    // Helper: Preview item formatting
    // ------------------------------------------------------------
    private ItemStack buildPreviewItem(Reward reward) {
        ItemStack item = reward.getItem() != null
                ? reward.getItem().clone()
                : new ItemStack(Material.NAME_TAG);

        ItemMeta meta = item.getItemMeta();

        List<String> lore = new ArrayList<>();

        lore.add("§7Rarity: " + reward.getRarity().getStyledName());
        lore.add("§7Chance: §f" + reward.getChance());
        lore.add("");

        if (reward.getType().name().equals("COMMAND")) {
            lore.add("§7Commands:");
            for (String cmd : reward.getCommands()) {
                lore.add("  §f" + cmd);
            }
        }

        if (reward.isJackpot()) {
            lore.add("");
            lore.add("§d§lJACKPOT REWARD!");
        }

        if (reward.getLore() != null && !reward.getLore().isEmpty()) {
            lore.add("");
            lore.addAll(reward.getLore());
        }

        if (meta != null) {
            meta.setDisplayName(
                    reward.getRarity().getColor() + reward.getDisplayName()
            );
            meta.setLore(lore);

            if (reward.isGlow()) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    // ------------------------------------------------------------
    // Util: fill GUI with background
    // ------------------------------------------------------------
    public ItemStack glassPane(Material color) {
        ItemStack item = new ItemStack(color);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}
