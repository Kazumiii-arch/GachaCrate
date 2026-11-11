package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.animations.WorldAnimation;
import com.kazumiii.gachacrate.guis.CardPickMenu;
import com.kazumiii.gachacrate.guis.CrateAnimationMenu;
import com.kazumiii.gachacrate.holograms.PlaceholderHook;
import com.kazumiii.gachacrate.player.PlayerData;
import com.kazumiii.gachacrate.util.Compat;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Everything about opening a crate: checks, using up keys, rolling the reward (with pity),
 * starting the animation and handing out the prize.
 * <p>
 * The reward is decided <b>before</b> the animation starts and pity is updated right away,
 * so logging out or closing the menu mid-spin can't be used to re-roll or dodge pity.
 */
public class CrateOpener {

    private final GachaCrate plugin;
    private final NamespacedKey fireworkTag;
    private final Map<UUID, OpeningSession> activeAnimations = new HashMap<>();

    public CrateOpener(GachaCrate plugin) {
        this.plugin = plugin;
        this.fireworkTag = new NamespacedKey(plugin, "gacha_firework");
    }

    /** Result of a roll: the reward and whether pity forced it. */
    public static final class Roll {
        public final Reward reward;
        public final boolean pity;

        Roll(Reward reward, boolean pity) {
            this.reward = reward;
            this.pity = pity;
        }
    }

    // ------------------------------------------------------------------
    // Opening
    // ------------------------------------------------------------------

    /**
     * Checks whether a player may open a crate right now and tells them why not.
     */
    public boolean canOpen(Player player, Crate crate) {
        if (isOpening(player)) {
            Text.send(player, "&cYou're already opening a crate!");
            return false;
        }
        if (!player.hasPermission(crate.getOpenPermission()) && !player.hasPermission("gacha.user.open.*")) {
            Text.send(player, "&cYou don't have permission to open this crate.");
            return false;
        }
        LocalDate today = LocalDate.now();
        if (!crate.isAvailable(today)) {
            if (crate.getStartDate() != null && today.isBefore(crate.getStartDate())) {
                Text.send(player, "&cThis crate opens on &e" + crate.getStartDate() + "&c.");
            } else {
                Text.send(player, "&cThis crate closed on &e" + crate.getEndDate() + "&c.");
            }
            return false;
        }
        if (crate.getValidRewards().isEmpty()) {
            Text.send(player, "&cThis crate has no rewards yet.");
            return false;
        }
        return true;
    }

    /**
     * Opens a crate using the key the player holds in their main hand.
     *
     * @param mass true to open as many keys as allowed at once (sneak + right-click)
     */
    public void openWithKey(Player player, Crate crate, boolean mass) {
        openWithKey(player, crate, mass, null);
    }

    /**
     * @param crateBlock the crate block that was clicked, or null (used by the in-world animation)
     */
    public void openWithKey(Player player, Crate crate, boolean mass, Location crateBlock) {
        if (!canOpen(player, crate)) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        String keyCrate = plugin.getKeyManager().getCrateFromKey(hand);
        if (keyCrate == null || !keyCrate.equalsIgnoreCase(crate.getId())) {
            Text.send(player, "&cYou need a " + crate.getDisplayName() + " &ckey to open this!");
            return;
        }

        int amount = 1;
        boolean massAllowed = mass
                && plugin.getConfig().getBoolean("opening.mass-open.enabled", true)
                && player.hasPermission("gacha.user.massopen");
        if (massAllowed) {
            int max = Math.max(1, plugin.getConfig().getInt("opening.mass-open.max-per-click", 64));
            amount = Math.min(hand.getAmount(), max);
        }

        // Use up the keys.
        if (hand.getAmount() <= amount) {
            player.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(hand.getAmount() - amount);
            player.getInventory().setItemInMainHand(hand);
        }

        if (amount > 1) {
            openMany(player, crate, amount);
        } else {
            openOne(player, crate, crateBlock);
        }
    }

    /** Opens a crate without a key (admin /gacha open). Skips the permission and date checks. */
    public void openFree(Player player, Crate crate, int amount) {
        if (isOpening(player)) {
            Text.send(player, "&cThat player is already opening a crate.");
            return;
        }
        if (crate.getValidRewards().isEmpty()) {
            Text.send(player, "&cThis crate has no rewards yet.");
            return;
        }
        if (amount > 1) openMany(player, crate, amount);
        else openOne(player, crate, null);
    }

    private void openOne(Player player, Crate crate, Location crateBlock) {
        Roll roll = roll(player, crate);
        OpeningSession session;
        switch (crate.getAnimationType()) {
            case INSTANT:
                deliver(player, crate, roll, false);
                return;
            case WORLD:
                session = new WorldAnimation(plugin, player, crate, roll, crateBlock);
                break;
            case CARDS:
                session = new CardPickMenu(player, crate, roll);
                break;
            default:
                session = new CrateAnimationMenu(player, crate, roll);
                break;
        }
        activeAnimations.put(player.getUniqueId(), session);
        session.start();
    }

    private void openMany(Player player, Crate crate, int amount) {
        Map<String, Integer> byRarity = new LinkedHashMap<>();
        List<String> best = new ArrayList<>();
        int bestOrder = Integer.MIN_VALUE;
        boolean fireworkDone = false;

        for (int i = 0; i < amount; i++) {
            Roll roll = roll(player, crate);
            boolean announced = deliverQuiet(player, crate, roll, !fireworkDone);
            fireworkDone |= announced;

            Rarity rarity = roll.reward.getRarity();
            byRarity.merge(rarity.getDisplayName(), 1, Integer::sum);
            if (rarity.getOrder() > bestOrder) {
                bestOrder = rarity.getOrder();
                best.clear();
            }
            if (rarity.getOrder() == bestOrder && best.size() < 3) best.add(roll.reward.getDisplayName());
        }

        Text.send(player, "&aYou opened &e" + amount + "x " + crate.getDisplayName() + "&a!");
        for (Map.Entry<String, Integer> entry : byRarity.entrySet()) {
            player.sendMessage(Text.color("  &8• ") + entry.getKey() + Text.color(" &7x" + entry.getValue()));
        }
        if (!best.isEmpty()) {
            player.sendMessage(Text.color("  &7Best: ") + String.join(Text.color("&7, "), best));
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
    }

    // ------------------------------------------------------------------
    // Rolling
    // ------------------------------------------------------------------

    /**
     * Picks the reward and updates the player's stats and pity counter.
     * <p>
     * Pity: with a threshold of 50, the 50th open in a row without a reward of the pity
     * rarity (or better) is guaranteed to give one.
     */
    public Roll roll(Player player, Crate crate) {
        List<Reward> rewards = crate.getValidRewards();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Rarity pityRarity = crate.isPityEnabled() ? plugin.getRarityManager().getRarity(crate.getPityRarityMinimum()) : null;

        Reward reward = null;
        boolean pity = false;

        if (pityRarity != null && data != null && data.getPityCount(crate.getName()) + 1 >= crate.getPityThreshold()) {
            List<Reward> good = new ArrayList<>();
            for (Reward r : rewards) {
                if (r.getRarity().isAtLeast(pityRarity)) good.add(r);
            }
            if (!good.isEmpty()) {
                reward = weightedPick(good);
                pity = true;
            }
        }
        if (reward == null) reward = weightedPick(rewards);

        if (data != null) {
            data.incrementCratesOpened(crate.getName());
            if (pityRarity != null) {
                if (reward.getRarity().isAtLeast(pityRarity)) data.resetPityCount(crate.getName());
                else data.incrementPityCount(crate.getName());
            }
        }
        plugin.getHistoryManager().record(player, data, crate, reward);
        plugin.getLeaderboardManager().update(data);
        return new Roll(reward, pity);
    }

    /** Weighted random pick: higher chance = more likely. */
    public static Reward weightedPick(List<Reward> rewards) {
        double total = 0;
        for (Reward r : rewards) total += Math.max(0, r.getChance());
        if (total <= 0) return rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));

        double value = ThreadLocalRandom.current().nextDouble() * total;
        for (Reward r : rewards) {
            value -= Math.max(0, r.getChance());
            if (value < 0) return r;
        }
        return rewards.get(rewards.size() - 1);
    }

    // ------------------------------------------------------------------
    // Giving the reward
    // ------------------------------------------------------------------

    /** Hands out a reward with title, sound and chat message. */
    public void deliver(Player player, Crate crate, Roll roll, boolean afterAnimation) {
        Reward reward = roll.reward;
        give(player, crate, reward);

        Rarity rarity = reward.getRarity();
        if (roll.pity) Text.send(player, "&d&lPITY! &eYour guaranteed reward has arrived!");
        revealEffects(player, rarity, reward);

        String message = plugin.getConfig().getString("messages.win", "&aYou won {reward}&a!");
        Text.send(player, fill(message, player, crate, reward));
        announce(player, crate, reward, true);
    }

    /** Hands out a reward without title/sound (mass opening). Returns true if it was announced with a firework. */
    private boolean deliverQuiet(Player player, Crate crate, Roll roll, boolean allowFirework) {
        give(player, crate, roll.reward);
        return announce(player, crate, roll.reward, allowFirework);
    }

    private void give(Player player, Crate crate, Reward reward) {
        if (reward.getType() == RewardType.ITEM) {
            ItemStack item = reward.getItem().clone();
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
                Text.send(player, "&eYour inventory was full, so the reward was dropped at your feet!");
            }
            return;
        }
        for (String command : reward.getCommands()) {
            String filled = command
                    .replace("{player}", player.getName())
                    .replace("%player%", player.getName())
                    .replace("{uuid}", player.getUniqueId().toString())
                    .replace("{crate}", crate.getName());
            if (plugin.isPlaceholderApiEnabled()) filled = PlaceholderHook.apply(player, filled);
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), filled);
            } catch (Exception e) {
                plugin.getLogger().warning("Reward command failed in crate '" + crate.getName() + "': /" + filled + " (" + e.getMessage() + ")");
            }
        }
    }

    /** Broadcasts the win if the rarity is high enough. Returns true if a firework was launched. */
    private boolean announce(Player player, Crate crate, Reward reward, boolean allowFirework) {
        Rarity minimum = plugin.getRarityManager().getRarity(plugin.getConfig().getString("broadcast-minimum-rarity", "MYTHIC"));
        if (minimum == null || !reward.getRarity().isAtLeast(minimum)) return false;

        String message = plugin.getConfig().getString("messages.broadcast",
                "&6{player} &7won {reward} &7({rarity}&7) from the {crate}&7 crate!");
        Bukkit.broadcastMessage(Text.prefix() + fill(message, player, crate, reward));

        if (allowFirework && plugin.getConfig().getBoolean("broadcast-firework", true)) {
            spawnFirework(player, reward.getRarity());
            return true;
        }
        return false;
    }

    private String fill(String message, Player player, Crate crate, Reward reward) {
        return Text.color(message)
                .replace("{player}", player.getName())
                .replace("{reward}", reward.getDisplayName())
                .replace("{rarity}", reward.getRarity().getDisplayName())
                .replace("{crate}", crate.getDisplayName());
    }

    /** 0 = worst rarity ... size-1 = best. */
    private int rankOf(Rarity rarity) {
        List<Rarity> all = plugin.getRarityManager().getRarities();
        int rank = 0;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getOrder() <= rarity.getOrder()) rank = i;
        }
        return rank;
    }

    /**
     * Gacha-style reveal. The best rarity gets a gold "jackpot" title, a totem animation and a sound
     * fanfare; the second best gets a flashy title; everything else a normal title.
     */
    private void revealEffects(Player player, Rarity rarity, Reward reward) {
        int top = plugin.getRarityManager().getRarities().size() - 1;
        int rank = rankOf(rarity);
        boolean best = top > 0 && rank == top;
        boolean great = top > 1 && rank == top - 1;

        if (best) {
            String loud = rarity.getColor().toString() + ChatColor.BOLD + Text.strip(rarity.getDisplayName()).toUpperCase(Locale.ROOT);
            player.sendTitle(Text.color("&6&l✦ ") + loud + Text.color(" &6&l✦"),
                    reward.getDisplayName(), 5, 60, 20);
            if (plugin.getConfig().getBoolean("effects.totem-on-best-rarity", true)) {
                player.playEffect(EntityEffect.TOTEM_RESURRECT);
            }
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.6f);
            Particle sparkle = Compat.particle("TOTEM_OF_UNDYING");
            if (sparkle != null) player.getWorld().spawnParticle(sparkle, player.getLocation().add(0, 1, 0), 80, 0.5, 1, 0.5, 0.4);
        } else if (great) {
            player.sendTitle(Text.color("&e&l★ ") + rarity.getDisplayName() + Text.color(" &e&l★"), reward.getDisplayName(), 5, 50, 15);
            playRevealSound(player, rank);
            Particle sparkle = Compat.particle("ENCHANT");
            if (sparkle != null) player.getWorld().spawnParticle(sparkle, player.getLocation().add(0, 1.5, 0), 60, 0.5, 0.5, 0.5, 1);
        } else {
            player.sendTitle(rarity.getDisplayName(), reward.getDisplayName(), 5, 40, 15);
            playRevealSound(player, rank);
        }
    }

    private void playRevealSound(Player player, int rank) {
        switch (Math.min(rank, 4)) {
            case 0:
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                break;
            case 1:
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                break;
            case 2:
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.2f);
                break;
            case 3:
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.2f);
                break;
            default:
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                break;
        }
    }

    private void spawnFirework(Player player, Rarity rarity) {
        Firework firework = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        Color color = colorOf(rarity);
        meta.addEffect(FireworkEffect.builder().withColor(color, Color.WHITE).withFade(Color.YELLOW)
                .flicker(true).trail(true).with(FireworkEffect.Type.BALL_LARGE).build());
        firework.setFireworkMeta(meta);
        // Tagged so the damage listener knows it's harmless.
        firework.getPersistentDataContainer().set(fireworkTag, PersistentDataType.BYTE, (byte) 1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (firework.isValid()) firework.detonate();
        }, 2L);
    }

    /** A firework/particle color that matches the rarity's chat color. */
    public static Color colorOf(Rarity rarity) {
        switch (rarity.getColor()) {
            case AQUA: return Color.AQUA;
            case BLUE: case DARK_BLUE: return Color.BLUE;
            case DARK_PURPLE: return Color.PURPLE;
            case LIGHT_PURPLE: return Color.FUCHSIA;
            case GOLD: return Color.ORANGE;
            case YELLOW: return Color.YELLOW;
            case GREEN: case DARK_GREEN: return Color.LIME;
            case RED: case DARK_RED: return Color.RED;
            default: return Color.WHITE;
        }
    }

    public NamespacedKey getFireworkTag() {
        return fireworkTag;
    }

    // ------------------------------------------------------------------
    // Running animations
    // ------------------------------------------------------------------

    public boolean isOpening(Player player) {
        return activeAnimations.containsKey(player.getUniqueId());
    }

    /** Called by the animation when it's finished (normally, skipped or interrupted). */
    public void animationDone(Player player, Crate crate, Roll roll) {
        activeAnimations.remove(player.getUniqueId());
        deliver(player, crate, roll, true);
    }

    /** Ends a player's animation right away and gives the reward (on quit). */
    public void finishNow(Player player) {
        OpeningSession session = activeAnimations.get(player.getUniqueId());
        if (session != null) session.finish();
    }

    /** Ends every animation and gives the rewards (on shutdown / reload). */
    public void finishAll() {
        for (OpeningSession session : new ArrayList<>(activeAnimations.values())) session.finish();
        activeAnimations.clear();
    }
}
