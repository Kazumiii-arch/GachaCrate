// ========================================================================
// File: CrateAnimationMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/CrateAnimationMenu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.animations.AnimationType;
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
    private final Random random = new Random();

    public CrateAnimationMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
        populateAnimationReel();
    }

    private void populateAnimationReel() {
        if (crate.getRewards().isEmpty()) return;
        for (Reward reward : crate.getRewards()) {
            ItemStack displayItem;
            if (reward.getType() == RewardType.ITEM) {
                displayItem = reward.getItem();
            } else {
                displayItem = reward.getPreviewItem() != null ? reward.getPreviewItem() : new ItemStack(Material.COMMAND_BLOCK);
            }
            int weight = (int) (100 / (reward.getChance() + 1));
            for (int i = 0; i < Math.max(1, weight); i++) {
                animationReel.add(displayItem);
            }
        }
        Collections.shuffle(animationReel);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 45, "§eOpening " + crate.getName() + "...");
        
        switch (crate.getAnimationType()) {
            case ROULETTE:
                runRouletteAnimation();
                break;
            case REEL:
            default:
                runReelAnimation();
                break;
        }
        player.openInventory(inventory);
    }

    // --- REDESIGNED "REEL" (SLOT MACHINE) ANIMATION ---
    private void runReelAnimation() {
        if (animationReel.isEmpty()) { player.closeInventory(); return; }
        
        // Set up the static parts of the GUI
        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(" ");
        placeholder.setItemMeta(meta);
        for(int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, placeholder);

        ItemStack pointer = new ItemStack(Material.BEACON);
        ItemMeta pointerMeta = pointer.getItemMeta();
        pointerMeta.setDisplayName("§e▲");
        pointer.setItemMeta(pointerMeta);
        inventory.setItem(4, pointer); // Top pointer
        inventory.setItem(40, pointer); // Bottom pointer

        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 100; // 5 seconds
            final int reel1StopTime = 40, reel2StopTime = 60, reel3StopTime = 80;

            @Override
            public void run() {
                if (ticks >= duration) {
                    this.cancel();
                    revealReward();
                    return;
                }

                // Spin the reels
                if (ticks < reel1StopTime) spinReel(12);
                if (ticks < reel2StopTime) spinReel(13);
                if (ticks < reel3StopTime) spinReel(14);
                
                // Stop the reels one by one
                if (ticks == reel1StopTime || ticks == reel2StopTime || ticks == reel3StopTime) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
                }

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.5f);
                ticks++;
            }
        }.runTaskTimer(GachaCrate.getInstance(), 0, 1L);
    }
    
    private void spinReel(int centerSlot) {
        inventory.setItem(centerSlot - 9, animationReel.get(random.nextInt(animationReel.size())));
        inventory.setItem(centerSlot, animationReel.get(random.nextInt(animationReel.size())));
        inventory.setItem(centerSlot + 9, animationReel.get(random.nextInt(animationReel.size())));
    }

    // --- REDESIGNED "ROULETTE" (CYCLONE) ANIMATION ---
    private void runRouletteAnimation() {
        if (animationReel.isEmpty()) { player.closeInventory(); return; }

        final int[] path = {0, 1, 2, 3, 4, 5, 6, 7, 8, 17, 26, 35, 44, 43, 42, 41, 40, 39, 38, 37, 36, 27, 18, 9};
        final Material[] panes = {Material.YELLOW_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE};

        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 100;

            @Override
            public void run() {
                if (ticks >= duration) {
                    this.cancel();
                    revealReward();
                    return;
                }

                // Animate the cyclone
                for (int i = 0; i < path.length; i++) {
                    int pathIndex = (ticks + i) % path.length;
                    int paneIndex = (ticks + i) % panes.length;
                    inventory.setItem(path[pathIndex], new ItemStack(panes[paneIndex]));
                }
                
                // Flash a potential prize in the center
                if (ticks % 10 == 0) {
                    inventory.setItem(22, animationReel.get(random.nextInt(animationReel.size())));
                }

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f + ((float) ticks / duration));
                ticks++;
            }
        }.runTaskTimer(GachaCrate.getInstance(), 0, 1L);
    }

    private void revealReward() {
        Reward finalReward = GachaCrate.getInstance().getCrateOpener().rollForReward(player, crate);
        if (finalReward == null) {
            player.closeInventory();
            return;
        }
        
        // Final display setup
        ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(" ");
        placeholder.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, placeholder);

        ItemStack displayItem;
        if (finalReward.getType() == RewardType.ITEM) {
            displayItem = finalReward.getItem();
        } else {
            displayItem = finalReward.getPreviewItem() != null ? finalReward.getPreviewItem() : new ItemStack(Material.COMMAND_BLOCK);
        }
        
        int finalSlot = (inventory.getSize() == 45) ? 22 : 13; // Center slot for 5 or 3 rows
        inventory.setItem(finalSlot, displayItem);

        GachaCrate.getInstance().getCrateOpener().finalizeReward(player, crate, finalReward);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) player.closeInventory();
            }
        }.runTaskLater(GachaCrate.getInstance(), 40L);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
    }
                            }
