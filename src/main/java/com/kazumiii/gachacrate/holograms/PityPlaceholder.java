// ========================================================================
// File: PityPlaceholder.java
// Location: /src/main/java/com/kazumiii/gachacrate/holograms/PityPlaceholder.java
// ========================================================================
package com.kazumiii.gachacrate.holograms;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.player.PlayerData;
import eu.decentsoftware.holograms.api.holograms.Hologram;
// --- THIS IS THE FIX ---
// The correct package is 'placeholders', not 'placeholder'.
import eu.decentsoftware.holograms.api.placeholders.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PityPlaceholder extends PlaceholderExpansion {

    private final GachaCrate plugin;

    public PityPlaceholder(GachaCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "gachacrate";
    }

    @Override
    public String getAuthor() {
        return "Kazumiii";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(Player player, Hologram hologram, String placeholder) {
        if (player == null) {
            return "";
        }

        if (placeholder.startsWith("pity_text_")) {
            String crateName = placeholder.substring("pity_text_".length());
            Crate crate = plugin.getCrateManager().getCrate(crateName);

            if (crate == null || !crate.isPityEnabled()) {
                return "";
            }

            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (playerData == null) {
                return "";
            }

            int currentPity = playerData.getPityCount(crateName);
            int maxPity = crate.getPityThreshold();
            String rarityName = crate.getPityRarityMinimum();

            return ChatColor.LIGHT_PURPLE + "Pity: " + ChatColor.WHITE + currentPity + " / " + maxPity + " for " + rarityName;
        }

        return null;
    }
}
