package com.kazumiii.gachacrate.crate;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reward inside a crate.
 * Supports item rewards, commands, money, bundle rewards, rarity, glow effects,
 * custom lore, custom display name, broadcast announcements, and jackpots.
 */
public class Reward {

    // -----------------------------
    // Core Reward Settings
    // -----------------------------
    private String id;
    private RewardType type = RewardType.ITEM;

    // Item reward
    private ItemStack item;

    // Command reward list
    private List<String> commands = new ArrayList<>();

    // Money reward
    private double money = 0;

    // Bundle
    private List<Reward> bundle = new ArrayList<>();

    // Chance
    private double chance = 1.0;

    // Rarity
    private Rarity rarity = Rarity.COMMON;

    // -----------------------------
    // Visual Effects (Premium)
    // -----------------------------
    private boolean glow = false;
    private boolean broadcast = false;
    private boolean firework = false;
    private boolean jackpot = false;

    private String title = null;
    private String soundKey = "reward-win";

    private String displayName = null; // reward name shown in GUIs
    private List<String> lore = new ArrayList<>();

    // -----------------------------
    // Constructors
    // -----------------------------
    public Reward() {}

    public Reward(String id, double chance, Rarity rarity) {
        this.id = id;
        this.chance = chance;
        this.rarity = rarity;
    }

    // -----------------------------
    // Apply the reward to a player
    // -----------------------------
    public void apply(Player player) {

        switch (type) {
            case ITEM:
                if (item != null) {
                    player.getInventory().addItem(item.clone());
                }
                break;

            case COMMAND:
                for (String cmd : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            cmd.replace("%player%", player.getName()));
                }
                break;

            case MONEY:
                if (money > 0) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "eco give " + player.getName() + " " + money);
                }
                break;

            case BUNDLE:
                for (Reward r : bundle) {
                    r.apply(player);
                }
                break;

            case NOTHING:
                // no reward
                break;
        }

        // Effects
        if (firework) {
            player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Firework.class);
        }

        if (title != null) {
            player.sendTitle(title, "", 10, 40, 10);
        }

        // Play win sound
        if (soundKey != null) {
            try {
                Sound sound = Sound.valueOf(soundKey.toUpperCase());
                player.playSound(player.getLocation(), sound, 1f, 1f);
            } catch (IllegalArgumentException ignored) {}
        }

        // Broadcast if enabled
        if (broadcast) {
            Bukkit.broadcastMessage("§e" + player.getName() + " §7won §6" +
                    getDisplayName() + "§7 from a crate!");
        }
    }

    // -----------------------------
    // Getters & Setters
    // -----------------------------

    public RewardType getType() {
        return type;
    }

    public void setType(RewardType type) {
        this.type = type;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void addCommand(String command) {
        this.commands.add(command);
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public List<Reward> getBundle() {
        return bundle;
    }

    public void setBundle(List<Reward> bundle) {
        this.bundle = bundle;
    }

    public void addToBundle(Reward reward) {
        this.bundle.add(reward);
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public boolean isGlow() {
        return glow;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public boolean isFirework() {
        return firework;
    }

    public void setFirework(boolean firework) {
        this.firework = firework;
    }

    public boolean isJackpot() {
        return jackpot;
    }

    public void setJackpot(boolean jackpot) {
        this.jackpot = jackpot;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSoundKey() {
        return soundKey;
    }

    public void setSoundKey(String soundKey) {
        this.soundKey = soundKey;
    }

    public String getDisplayName() {
        return displayName == null ? id : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void addLore(String line) {
        this.lore.add(line);
    }

    public String getId() {
        return id;
    }
    }
