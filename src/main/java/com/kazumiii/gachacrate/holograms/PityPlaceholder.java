package com.kazumiii.gachacrate.holograms;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.player.PlayerData;
import com.kazumiii.gachacrate.util.Text;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * PlaceholderAPI placeholders:
 * <ul>
 *   <li>%gachacrate_pity_&lt;crate&gt;% – current pity counter</li>
 *   <li>%gachacrate_pity_max_&lt;crate&gt;% – pity threshold</li>
 *   <li>%gachacrate_pity_left_&lt;crate&gt;% – opens left until guaranteed</li>
 *   <li>%gachacrate_pity_text_&lt;crate&gt;% – ready-made hologram line</li>
 *   <li>%gachacrate_opened_&lt;crate&gt;% – how many times the player opened the crate</li>
 *   <li>%gachacrate_opened_total% – total crates opened</li>
 *   <li>%gachacrate_keys_&lt;crate&gt;% – keys of that crate in the player's inventory</li>
 * </ul>
 */
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
        return plugin.getDescription().getVersion();
    }

    /** Keeps the placeholders registered when PlaceholderAPI reloads. */
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offline, String params) {
        if (offline == null) return "";
        String id = params.toLowerCase();
        PlayerData data = plugin.getPlayerManager().getOfflineData(offline.getUniqueId());

        if (id.equals("opened_total")) {
            return String.valueOf(data == null ? 0 : data.getTotalCratesOpened());
        }
        if (id.startsWith("keys_")) {
            if (offline.getPlayer() == null) return "0";
            Integer count = plugin.getKeyManager().countKeys(offline.getPlayer()).get(id.substring(5));
            return String.valueOf(count == null ? 0 : count);
        }
        if (id.startsWith("opened_")) {
            return String.valueOf(data == null ? 0 : data.getCratesOpened(id.substring(7)));
        }

        // Longest prefixes first, so "pity_text_x" isn't read as crate "text_x".
        String crateName;
        String kind;
        if (id.startsWith("pity_text_")) {
            kind = "text";
            crateName = id.substring(10);
        } else if (id.startsWith("pity_left_")) {
            kind = "left";
            crateName = id.substring(10);
        } else if (id.startsWith("pity_max_")) {
            kind = "max";
            crateName = id.substring(9);
        } else if (id.startsWith("pity_")) {
            kind = "count";
            crateName = id.substring(5);
        } else {
            return null;
        }

        Crate crate = plugin.getCrateManager().getCrate(crateName);
        if (crate == null || !crate.isPityEnabled()) return "";
        int count = data == null ? 0 : data.getPityCount(crate.getName());
        int max = crate.getPityThreshold();

        switch (kind) {
            case "count":
                return String.valueOf(count);
            case "max":
                return String.valueOf(max);
            case "left":
                return String.valueOf(Math.max(1, max - count));
            default:
                Rarity rarity = plugin.getRarityManager().getRarity(crate.getPityRarityMinimum());
                String rarityName = rarity == null ? "?" : rarity.getDisplayName();
                return Text.color("&dPity: &f" + count + "&7/&f" + max + " &7for ") + rarityName;
        }
    }
}
