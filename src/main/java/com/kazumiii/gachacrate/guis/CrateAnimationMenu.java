// ========================================================================
// File: CrateAnimationMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/CrateAnimationMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.CrateOpener;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.crates.RewardType;
import com.kazumiii.gachacrate.player.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CrateAnimationMenu extends Menu {

    private final Crate crate;
    private final List<ItemStack> animationReel = new ArrayList<>();

    public CrateAnimationMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        populateAnimationReel();
    }

    /**
     * Creates a weighted list of items to flash during the animation.
     */
    private void populateAnimationReel() {
        if (crate.getRewards().isEmpty()) return;

        for (Reward reward : crate.getRewards()) {
            ItemStack displayItem;
            if (reward.getType() == RewardType.ITEM) {
                displayItem = reward.getItem();
            } else {
                displayItem = reward.getPreviewItem() != null ? reward.getPreviewItem() : new ItemStack(Material.COMMAND_BLOCK);
            }
            // Add more common items to the reel more often to make rare items feel rarer
            int weight = (int) (100 / (reward.getChance() + 1));
            for (int i = 0; i < Math.max(1, weight); i++) {
                animationReel.add(displayItem);
            }
        }
        Collections.shuffle(animationReel);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§eOpening " + crate.getName() + "...");
        runAnimation();
        player.openInventory(inventory);
    }

    private void runAnimation() {
        if (animationReel.isEmpty()) {
            player.sendMessage(ChatColor.RED + "This crate has no rewards to animate!");
            player.closeInventory();
            return;
        }

        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 80; // 4 seconds total
            long nextSoundTick = 0;

            @Override
            public void run() {
                if (ticks >= duration) {
                    this.cancel();
                    revealReward();
                    return;
                }

                // The "Reel" Animation
                for (int i = 10; i <= 16; i++) {
                    int reelIndex = (i + ticks) % animationReel.size();
                    inventory.setItem(i, animationReel.get(reelIndex));
                }

                // Sound Logic - speeds up as it progresses
                if (ticks >= nextSoundTick) {
                    float pitch = 0.5f + (1.5f * ((float) ticks / duration));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, pitch);
                    nextSoundTick = ticks + (duration - ticks) / 10;
                }

                ticks++;
            }
        }.runTaskTimer(GachaCrate.getInstance(), 0, 1L);
    }

    private void revealReward() {
        Reward finalReward = CrateOpener.rollForReward(player, crate);
        if (finalReward == null) {
            player.closeInventory();
            return;
        }

        // Set up the final display
        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(" ");
        placeholder.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, placeholder);
        }

        ItemStack displayItem;
        if (finalReward.getType() == RewardType.ITEM) {
            displayItem = finalReward.getItem();
        } else {
            displayItem = finalReward.getPreviewItem() != null ? finalReward.getPreviewItem() : new ItemStack(Material.COMMAND_BLOCK);
        }
        inventory.setItem(13, displayItem);

        // Dramatic Reveal Sound & Title
        playRevealSound(finalReward.getRarity());
        player.sendTitle(finalReward.getRarity().getDisplayName(), ChatColor.GRAY + "You won a reward!", 10, 40, 20);

        // Give Reward & Update Stats after a delay
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    giveRewardToPlayer(finalReward);
                    updatePlayerData(finalReward);
                    checkAndBroadcast(finalReward);
                    player.closeInventory();
                }
            }
        }.runTaskLater(GachaCrate.getInstance(), 40L);
    }
    
    private void playRevealSound(Rarity rarity) {
        Sound sound;
        float volume = 1.0f;
        float pitch = 1.0f;

        switch (rarity.getOrder()) {
            case 1: // Common
                sound = Sound.ENTITY_PLAYER_LEVELUP;
                pitch = 1.2f;
                break;
            case 2: // Rare
                sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                break;
            case 3: // Epic
                sound = Sound.BLOCK_BEACON_ACTIVATE;
                break;
            case 4: // Mythic
                sound = Sound.ENTITY_ENDER_DRAGON_GROWL;
                break;
            case 5: // Divine or higher
                sound = Sound.ENTITY_WITHER_SPAWN;
                volume = 0.8f;
                break;
            default:
                sound = Sound.ENTITY_PLAYER_LEVELUP;
                break;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private void giveRewardToPlayer(Reward reward) {
        if (reward.getType() == RewardType.ITEM) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), reward.getItem());
                player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so the reward was dropped at your feet!");
            } else {
                player.getInventory().addItem(reward.getItem());
            }
        } else {
            String command = reward.getCommand().replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            player.sendMessage("§aYou have received a special command reward!");
        }
    }

    private void updatePlayerData(Reward reward) {
        PlayerData playerData = GachaCrate.getInstance().getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            playerData.incrementCratesOpened(crate.getName());
            if (crate.isPityEnabled()) {
                Rarity minPityRarity = GachaCrate.getInstance().getRarityManager().getRarity(crate.getPityRarityMinimum());
                if (minPityRarity != null && reward.getRarity().getOrder() >= minPityRarity.getOrder()) {
                    playerData.resetPityCount(crate.getName());
                } else {
                    playerData.incrementPityCount(crate.getName());
                }
            }
        }
    }

    private void checkAndBroadcast(Reward reward) {
        GachaCrate plugin = GachaCrate.getInstance();
        String minRarityId = plugin.getConfig().getString("broadcast-minimum-rarity", "MYTHIC");
        Rarity minRarity = plugin.getRarityManager().getRarity(minRarityId);

        if (reward.getRarity() == null || minRarity == null) return;

        if (reward.getRarity().getOrder() >= minRarity.getOrder()) {
            spawnFirework(player);
            
            String itemName;
            if (reward.getType() == RewardType.ITEM) {
                itemName = reward.getItem().hasItemMeta() && reward.getItem().getItemMeta().hasDisplayName()
                    ? reward.getItem().getItemMeta().getDisplayName()
                    : reward.getItem().getType().name().replace('_', ' ');
            } else {
                if (reward.getPreviewItem() != null && reward.getPreviewItem().hasItemMeta() && reward.getPreviewItem().getItemMeta().hasDisplayName()) {
                    itemName = reward.getPreviewItem().getItemMeta().getDisplayName();
                } else {
                    itemName = reward.getRarity().getDisplayName() + " §rCommand Reward";
                }
            }

            String broadcastMessage = "§6[GachaCrate] §r" + player.getName() + " has won a " +
                reward.getRarity().getDisplayName() + "§r item: " + itemName + "§r from a " + crate.getName() + " crate!";
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

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
    }
            }
                
