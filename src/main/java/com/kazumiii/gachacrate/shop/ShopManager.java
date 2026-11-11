package com.kazumiii.gachacrate.shop;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.player.PlayerData;
import com.kazumiii.gachacrate.util.Perms;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Daily free keys and buying keys with money.
 */
public class ShopManager {

    private final GachaCrate plugin;
    private EconomyHook economy;

    public ShopManager(GachaCrate plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            try {
                economy = new EconomyHook();
                plugin.getLogger().info("Vault found. The key shop is available once an economy plugin is loaded.");
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Vault is installed but couldn't be used. The key shop is disabled.", t);
            }
        }
    }

    // ------------------------------------------------------------------
    // Economy
    // ------------------------------------------------------------------

    /** True if Vault and an economy plugin are both installed. */
    public boolean isEconomyReady() {
        try {
            return economy != null && economy.isReady();
        } catch (Throwable t) {
            return false;
        }
    }

    public String format(double amount) {
        try {
            if (isEconomyReady()) return economy.format(amount);
        } catch (Throwable ignored) {
            // Fall through.
        }
        return Text.number(amount);
    }

    public double balance(Player player) {
        return isEconomyReady() ? economy.balance(player) : 0;
    }

    /** Crates that can be bought right now. */
    public List<Crate> getCratesForSale() {
        List<Crate> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Crate crate : plugin.getCrateManager().getAllCrates().values()) {
            if (crate.getKeyPrice() > 0 && crate.isAvailable(today)) list.add(crate);
        }
        return list;
    }

    /** Buys keys. Returns true if it worked; the player is told why if not. */
    public boolean buyKeys(Player player, Crate crate, int amount) {
        if (!player.hasPermission("gacha.user.shop")) {
            Text.send(player, "&cYou don't have permission to buy keys.");
            return false;
        }
        if (!isEconomyReady()) {
            Text.send(player, "&cThe key shop isn't available (the server needs Vault and an economy plugin).");
            return false;
        }
        if (crate.getKeyPrice() <= 0) {
            Text.send(player, "&cKeys for this crate can't be bought.");
            return false;
        }
        if (!crate.isAvailable(LocalDate.now())) {
            Text.send(player, "&cThis crate isn't available right now.");
            return false;
        }
        int max = Math.max(1, plugin.getConfig().getInt("shop.max-keys-per-purchase", 64));
        if (amount < 1 || amount > max) {
            Text.send(player, "&cYou can buy 1 to " + max + " keys at a time.");
            return false;
        }

        double cost = crate.getKeyPrice() * amount;
        if (!economy.has(player, cost)) {
            Text.send(player, "&cYou need &f" + format(cost) + " &cbut only have &f" + format(economy.balance(player)) + "&c.");
            return false;
        }
        String error = economy.withdraw(player, cost);
        if (error != null) {
            Text.send(player, "&c" + error);
            return false;
        }
        try {
            plugin.getKeyManager().giveKeys(player, crate, amount);
        } catch (RuntimeException e) {
            economy.refund(player, cost);
            Text.send(player, "&cSomething went wrong. You were refunded.");
            plugin.getLogger().log(Level.WARNING, "Could not give bought keys to " + player.getName(), e);
            return false;
        }
        Text.send(player, "&aBought &e" + amount + "x " + crate.getDisplayName() + " &akey" + (amount == 1 ? "" : "s")
                + " &afor &f" + format(cost) + "&a.");
        return true;
    }

    // ------------------------------------------------------------------
    // Daily keys
    // ------------------------------------------------------------------

    /** Crates with daily keys the player hasn't claimed today. */
    public List<Crate> getClaimableDaily(Player player) {
        List<Crate> list = new ArrayList<>();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || !player.hasPermission("gacha.user.daily")) return list;
        LocalDate today = LocalDate.now();
        for (Crate crate : plugin.getCrateManager().getAllCrates().values()) {
            if (crate.getDailyFreeKeys() > 0 && crate.isAvailable(today) && !data.hasClaimedDaily(crate.getName(), today)) {
                list.add(crate);
            }
        }
        return list;
    }

    /** True if any crate gives daily keys at all. */
    public boolean anyDailyCrates() {
        for (Crate crate : plugin.getCrateManager().getAllCrates().values()) {
            if (crate.getDailyFreeKeys() > 0) return true;
        }
        return false;
    }

    /** Claims all of today's free keys. Returns how many keys were given. */
    public int claimDaily(Player player) {
        if (!player.hasPermission("gacha.user.daily")) {
            Text.send(player, "&cYou don't have permission to claim daily keys.");
            return 0;
        }
        List<Crate> claimable = getClaimableDaily(player);
        if (claimable.isEmpty()) {
            if (anyDailyCrates()) Text.send(player, "&eYou already claimed today's free keys. Come back tomorrow!");
            else Text.send(player, "&eThere are no daily free keys on this server.");
            return 0;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        LocalDate today = LocalDate.now();
        int total = 0;
        Text.send(player, "&a&lDaily keys claimed!");
        // Ranks can get more: gacha.daily.multiplier.2 = double daily keys, .3 = triple, ...
        int multiplier = Math.max(1, Perms.highest(player, "gacha.daily.multiplier.", 1));
        for (Crate crate : claimable) {
            int amount = crate.getDailyFreeKeys() * multiplier;
            plugin.getKeyManager().giveKeys(player, crate, amount);
            data.setDailyClaimed(crate.getName(), today);
            total += amount;
            player.sendMessage(Text.color("  &a▸ &f" + amount + "x ") + crate.getDisplayName() + Text.color(" &7key"
                    + (multiplier > 1 ? " &a(x" + multiplier + " rank bonus)" : "")));
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        return total;
    }
}
