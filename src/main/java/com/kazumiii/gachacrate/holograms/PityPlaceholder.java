// ========================================================================
// File: PityPlaceholder.java
// Location: /src/main/java/com/kazumiii/gachacrate/holograms/PityPlaceholder.java
// ========================================================================
package com.kazumiii.gachacrate.holograms;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.player.PlayerData;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.placeholder.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PityPlaceholder extends PlaceholderExpansion {

    private final GachaCrate plugin;

    public PityPlaceholder(GachaCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        // This is the name of our placeholder group.
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

    /**
     * This method is called by DecentHolograms whenever it sees our placeholder.
     * @param player The player looking at the hologram.
     * @param hologram The hologram being viewed.
     * @param placeholder The full placeholder string (e.g., "pity_text").
     * @return The formatted string to display to the player.
     */
    @Override
    public String onRequest(Player player, Hologram hologram, String placeholder) {
        if (player == null) {
            return ""; // Cannot get pity for a non-player
        }

        // The placeholder will be in the format %gachacrate_pity_text_CrateName%
        if (placeholder.startsWith("pity_text_")) {
            String crateName = placeholder.substring("pity_text_".length());
            Crate crate = plugin.getCrateManager().getCrate(crateName);

            if (crate == null || !crate.isPityEnabled()) {
                return ""; // Don't show anything if the crate doesn't exist or has no pity system
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

        return null; // Return null for unknown placeholders
    }
}
