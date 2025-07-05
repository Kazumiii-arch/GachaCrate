// ========================================================================
// File: CrateOpener.java
// Location: /src/main/java/com/kazumiii/gachacrate/crates/CrateOpener.java
// ========================================================================
package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.player.PlayerData;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CrateOpener {

    public static Reward rollForReward(Player player, Crate crate) {
        List<Reward> rewards = crate.getRewards();
        if (rewards.isEmpty()) return null;

        GachaCrate plugin = GachaCrate.getInstance();
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        
        if (crate.isPityEnabled() && playerData.getPityCount(crate.getName()) >= crate.getPityThreshold()) {
            player.sendMessage("§d§lPITY!§r §eYou've been guaranteed a rare reward!");
            Rarity minRarity = plugin.getRarityManager().getRarity(crate.getPityRarityMinimum());
            
            List<Reward> pityRewards = rewards.stream()
                .filter(r -> r.getRarity().getOrder() >= minRarity.getOrder())
                .collect(Collectors.toList());
            
            if (pityRewards.isEmpty()) {
                 return performWeightedRoll(rewards);
            } else {
                playerData.resetPityCount(crate.getName());
                return performWeightedRoll(pityRewards);
            }
        }
        
        Reward finalReward = performWeightedRoll(rewards);
        
        if (crate.isPityEnabled()) {
            Rarity minRarity = plugin.getRarityManager().getRarity(crate.getPityRarityMinimum());
            if (finalReward.getRarity().getOrder() >= minRarity.getOrder()) {
                playerData.resetPityCount(crate.getName());
            }
        }
        
        return finalReward;
    }

    private static Reward performWeightedRoll(List<Reward> rewards) {
        double totalWeight = rewards.stream().mapToDouble(Reward::getChance).sum();
        double randomValue = new Random().nextDouble() * totalWeight;

        for (Reward reward : rewards) {
            randomValue -= reward.getChance();
            if (randomValue <= 0) {
                return reward;
            }
        }
        return rewards.get(0);
    }
}
