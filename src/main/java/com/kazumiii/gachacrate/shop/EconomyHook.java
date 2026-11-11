package com.kazumiii.gachacrate.shop;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * The only class that touches the Vault API. Only used when Vault is installed.
 * The economy is looked up when first needed, because economy plugins (EssentialsX, CMI, ...)
 * may register after GachaCrate has started.
 */
final class EconomyHook {

    private Economy economy;

    private Economy economy() {
        if (economy == null) {
            RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (provider != null) economy = provider.getProvider();
        }
        return economy;
    }

    boolean isReady() {
        return economy() != null;
    }

    double balance(OfflinePlayer player) {
        return economy().getBalance(player);
    }

    boolean has(OfflinePlayer player, double amount) {
        return economy().has(player, amount);
    }

    /** Returns null on success, or an error message. */
    String withdraw(OfflinePlayer player, double amount) {
        EconomyResponse response = economy().withdrawPlayer(player, amount);
        if (response.transactionSuccess()) return null;
        return response.errorMessage == null || response.errorMessage.isEmpty() ? "Payment failed." : response.errorMessage;
    }

    void refund(OfflinePlayer player, double amount) {
        economy().depositPlayer(player, amount);
    }

    String format(double amount) {
        return economy().format(amount);
    }
}
