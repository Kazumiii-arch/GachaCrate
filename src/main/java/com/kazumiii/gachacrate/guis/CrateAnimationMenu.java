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
import java.util.List;
import java.util.Random;

public class CrateAnimationMenu extends Menu {

    private final Crate crate;

    public CrateAnimationMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "Opening " + crate.getName() + "...");
        runAnimation();
        player.openInventory(inventory);
    }

    private void runAnimation() {
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 60;
            private final Random random = new Random();
            private final List<Reward> rewards = crate.getRewards();
            @Override
            public void run() {
                if (ticks >= duration) {
                    this.cancel();
                    revealReward();
                    return;
                }
                for (int i = 0; i < inventory.getSize(); i++) {
                    if (rewards.isEmpty()) break;
                    Reward randomReward = rewards.get(random.nextInt(rewards.size()));
                    
                    // Display the correct item during the spin
                    if (randomReward.getType() == RewardType.ITEM) {
                        inventory.setItem(i, randomReward.getItem());
                    } else {
                        if (randomReward.getPreviewItem() != null) {
                            inventory.setItem(i, randomReward.getPreviewItem());
                        } else {
                            inventory.setItem(i, new ItemStack(Material.COMMAND_BLOCK));
                        }
                    }
                }
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                ticks += 2;
            }
        }.runTaskTimer(GachaCrate.getInstance(), 0, 2);
    }

    private void revealReward() {
        Reward finalReward = CrateOpener.rollForReward(player, crate);
        if (finalReward == null) {
            player.sendMessage(ChatColor.RED + "This crate has no rewards inside!");
            player.closeInventory();
            return;
        }

        PlayerData playerData = GachaCrate.getInstance().getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            playerData.incrementCratesOpened(crate.getName());
            if (crate.isPityEnabled()) {
                playerData.incrementPityCount(crate.getName());
            }
        }

        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(" ");
        placeholder.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, placeholder);
        }

        // Determine what to display in the final slot
        ItemStack displayItem;
        if (finalReward.getType() == RewardType.ITEM) {
            displayItem = finalReward.getItem();
        } else {
            if (finalReward.getPreviewItem() != null) {
                displayItem = finalReward.getPreviewItem();
            } else {
                displayItem = new ItemStack(Material.COMMAND_BLOCK);
            }
        }
        inventory.setItem(13, displayItem);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        checkAndBroadcast(finalReward);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (finalReward.getType() == RewardType.ITEM) {
                    if (player.getInventory().firstEmpty() == -1) {
                        player.getWorld().dropItem(player.getLocation(), finalReward.getItem());
                        player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so the item was dropped at your feet!");
                    } else {
                        player.getInventory().addItem(finalReward.getItem());
                    }
                } else {
                    String command = finalReward.getCommand().replace("{player}", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    player.sendMessage("§aYou have received a special command reward!");
                }
                player.closeInventory();
            }
        }.runTaskLater(GachaCrate.getInstance(), 40);
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
