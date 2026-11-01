// ========================================================================
// File: CrateOpener.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/CrateOpener.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.player.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CrateOpener {

    /**
     * Determines which reward a player should win based on weighted chances and their pity status.
     * @param player The player opening the crate.
     * @param crate The crate being opened.
     * @return The winning Reward object, or null if the crate is empty.
     */
    public Reward rollForReward(Player player, Crate crate) {
        List<Reward> rewards = crate.getRewards();
        if (rewards.isEmpty()) return null;

        GachaCrate plugin = GachaCrate.getInstance();
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        // Guard: if player data is somehow not loaded, roll without pity logic.
        if (playerData != null && crate.isPityEnabled() && crate.getPityThreshold() > 0) {
            Rarity minRarity = plugin.getRarityManager().getRarity(crate.getPityRarityMinimum());
            boolean pityTriggered = playerData.getPityCount(crate.getName()) >= crate.getPityThreshold();

            if (pityTriggered && minRarity != null) {
                player.sendMessage("§d§lPITY!§r §eYou've been guaranteed a rare reward!");
                final int minOrder = minRarity.getOrder();

                List<Reward> pityRewards = rewards.stream()
                        .filter(r -> r.getRarity() != null && r.getRarity().getOrder() >= minOrder)
                        .collect(Collectors.toList());

                if (!pityRewards.isEmpty()) {
                    playerData.resetPityCount(crate.getName());
                    return performWeightedRoll(pityRewards);
                }
                // No rewards match the pity rarity — fall through to a normal roll.
            }
        }

        Reward finalReward = performWeightedRoll(rewards);

        // Reset the pity counter when the player wins a reward at or above the minimum.
        if (playerData != null && crate.isPityEnabled()) {
            Rarity minRarity = plugin.getRarityManager().getRarity(crate.getPityRarityMinimum());
            if (minRarity != null && finalReward.getRarity() != null
                    && finalReward.getRarity().getOrder() >= minRarity.getOrder()) {
                playerData.resetPityCount(crate.getName());
            }
        }

        return finalReward;
    }

    /**
     * Performs a simple weighted random roll on a given list of rewards.
     * @param rewards The list of rewards to roll from.
     * @return The chosen Reward.
     */
    private Reward performWeightedRoll(List<Reward> rewards) {
        double totalWeight = rewards.stream().mapToDouble(Reward::getChance).sum();
        if (totalWeight <= 0) {
            return rewards.get(new Random().nextInt(rewards.size()));
        }

        double randomValue = new Random().nextDouble() * totalWeight;

        for (Reward reward : rewards) {
            randomValue -= reward.getChance();
            if (randomValue <= 0) {
                return reward;
            }
        }
        return rewards.get(0); // Fallback
    }

    /**
     * This handles all logic that occurs AFTER an animation is complete.
     * It plays sounds, gives the item, updates stats, and broadcasts.
     * @param player The winning player.
     * @param crate The crate that was opened.
     * @param finalReward The reward that was won.
     */
    public void finalizeReward(Player player, Crate crate, Reward finalReward) {
        playRevealSound(player, finalReward.getRarity());
        player.sendTitle(finalReward.getRarity().getDisplayName(), ChatColor.GRAY + "You won a reward!", 10, 40, 20);
        giveRewardToPlayer(player, finalReward);
        updatePlayerData(player, crate, finalReward);
        checkAndBroadcast(player, crate, finalReward);
    }

    private void playRevealSound(Player player, Rarity rarity) {
        Sound sound;
        float volume = 1.0f;
        float pitch = 1.0f;
        switch (rarity.getOrder()) {
            case 1: sound = Sound.ENTITY_PLAYER_LEVELUP; pitch = 1.2f; break;
            case 2: sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP; break;
            case 3: sound = Sound.BLOCK_BEACON_ACTIVATE; break;
            case 4: sound = Sound.ENTITY_ENDER_DRAGON_GROWL; break;
            default: sound = Sound.ENTITY_WITHER_SPAWN; volume = 0.8f; break;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private void giveRewardToPlayer(Player player, Reward reward) {
        if (reward.getType() == RewardType.ITEM) {
            if (reward.getItem() == null) return;
            // Clone so the crate's stored template item is never mutated.
            var item = reward.getItem().clone();
            var leftover = player.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                leftover.values().forEach(rest -> player.getWorld().dropItem(player.getLocation(), rest));
                player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so the reward was dropped at your feet!");
            }
        } else if (reward.getCommand() != null) {
            String command = reward.getCommand().replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            player.sendMessage("§aYou have received a special command reward!");
        }
    }

    private void updatePlayerData(Player player, Crate crate, Reward reward) {
        PlayerData playerData = GachaCrate.getInstance().getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            playerData.incrementCratesOpened(crate.getName());
            if (crate.isPityEnabled()) {
                playerData.incrementPityCount(crate.getName());
            }
        }
    }

    private void checkAndBroadcast(Player player, Crate crate, Reward reward) {
        GachaCrate plugin = GachaCrate.getInstance();
        String minRarityId = plugin.getConfig().getString("broadcast-minimum-rarity", "MYTHIC");
        Rarity minRarity = plugin.getRarityManager().getRarity(minRarityId);
        if (reward.getRarity() == null || minRarity == null) return;
        if (reward.getRarity().getOrder() >= minRarity.getOrder()) {
            spawnFirework(player);
            String itemName;
            if (reward.getType() == RewardType.ITEM && reward.getItem() != null) {
                itemName = reward.getItem().hasItemMeta() && reward.getItem().getItemMeta().hasDisplayName()
                        ? reward.getItem().getItemMeta().getDisplayName()
                        : reward.getItem().getType().name().replace('_', ' ');
            } else {
                itemName = (reward.getPreviewItem() != null && reward.getPreviewItem().hasItemMeta() && reward.getPreviewItem().getItemMeta().hasDisplayName())
                        ? reward.getPreviewItem().getItemMeta().getDisplayName()
                        : reward.getRarity().getDisplayName() + " §rCommand Reward";
            }
            String broadcastMessage = "§6[GachaCrate] §r" + player.getName() + " has won a " +
                    reward.getRarity().getDisplayName() + "§r item: " + itemName + "§r from a " + crate.getDisplayName() + " crate!";
            Bukkit.broadcastMessage(broadcastMessage);
        }
    }

    private void spawnFirework(Player player) {
        Location loc = player.getLocation();
        Firework fw = player.getWorld().spawn(loc, Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.setPower(1);
        fwm.addEffect(FireworkEffect.builder().withColor(Color.ORANGE, Color.YELLOW).flicker(true).with(FireworkEffect.Type.BALL_LARGE).build());
        fw.setFireworkMeta(fwm);
    }
}
