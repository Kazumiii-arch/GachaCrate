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
 *   <li>%gachacrate_daily_&lt;crate&gt;% – "Ready" or "Claimed"</li>
 *   <li>%gachacrate_daily_ready% – how many crates have daily keys ready</li>
 *   <li>%gachacrate_keys_total% / %gachacrate_pending_keys% – keys held / waiting from offline gifts</li>
 *   <li>%gachacrate_last_win%, _last_win_rarity, _last_win_crate – the player's latest win</li>
 *   <li>%gachacrate_crate_&lt;crate&gt;_name / _rewards / _price / _daily / _pity% – crate info</li>
 *   <li>%gachacrate_rank% – the player's leaderboard position</li>
 *   <li>%gachacrate_top_&lt;n&gt;_name% / _value – leaderboard (all crates)</li>
 *   <li>%gachacrate_top_&lt;crate&gt;_&lt;n&gt;_name% / _value – leaderboard for one crate</li>
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

        if (id.equals("keys_total")) {
            if (offline.getPlayer() == null) return "0";
            int total = 0;
            for (int count : plugin.getKeyManager().countKeys(offline.getPlayer()).values()) total += count;
            return String.valueOf(total);
        }
        if (id.equals("pending_keys")) {
            int total = 0;
            if (data != null) for (int count : data.getPendingKeys().values()) total += count;
            return String.valueOf(total);
        }
        if (id.equals("daily_ready")) {
            if (offline.getPlayer() == null) return "0";
            return String.valueOf(plugin.getShopManager().getClaimableDaily(offline.getPlayer()).size());
        }
        if (id.equals("last_win") || id.equals("last_win_rarity") || id.equals("last_win_crate")) {
            if (data == null || data.getHistory().isEmpty()) return "-";
            com.kazumiii.gachacrate.history.WinRecord last = data.getHistory().get(0);
            if (id.equals("last_win")) return last.getRewardName();
            if (id.equals("last_win_crate")) {
                Crate crate = plugin.getCrateManager().getCrate(last.getCrateId());
                return crate == null ? last.getCrateId() : crate.getDisplayName();
            }
            Rarity rarity = plugin.getRarityManager().getRarity(last.getRarityId());
            return rarity == null ? last.getRarityId() : rarity.getDisplayName();
        }
        if (id.startsWith("crate_")) {
            return crateInfo(id.substring(6));
        }
        if (id.equals("opened_total")) {
            return String.valueOf(data == null ? 0 : data.getTotalCratesOpened());
        }
        if (id.startsWith("keys_")) {
            if (offline.getPlayer() == null) return "0";
            Integer count = plugin.getKeyManager().countKeys(offline.getPlayer()).get(id.substring(5));
            return String.valueOf(count == null ? 0 : count);
        }
        if (id.startsWith("top_")) {
            return top(id.substring(4));
        }
        if (id.equals("rank")) {
            int rank = plugin.getLeaderboardManager().rankOf(offline.getUniqueId(), null);
            return rank > 0 ? String.valueOf(rank) : "-";
        }
        if (id.startsWith("daily_")) {
            Crate crate = plugin.getCrateManager().getCrate(id.substring(6));
            if (crate == null || crate.getDailyFreeKeys() <= 0 || data == null) return "";
            return data.hasClaimedDaily(crate.getName(), java.time.LocalDate.now()) ? "Claimed" : "Ready";
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

    /**
     * "1_name", "1_value" (all crates) or "vote_1_name", "vote_1_value" (one crate).
     */
    private String top(String rest) {
        String[] parts = rest.split("_");
        if (parts.length < 2) return null;
        String field = parts[parts.length - 1];
        int position;
        try {
            position = Integer.parseInt(parts[parts.length - 2]);
        } catch (NumberFormatException e) {
            return null;
        }
        String crateId = null;
        if (parts.length > 2) crateId = String.join("_", java.util.Arrays.copyOfRange(parts, 0, parts.length - 2));
        if (position < 1 || position > 100) return "";
        java.util.List<com.kazumiii.gachacrate.history.LeaderboardManager.Entry> top =
                plugin.getLeaderboardManager().top(crateId, position);
        if (top.size() < position) return field.equals("value") ? "0" : "-";
        com.kazumiii.gachacrate.history.LeaderboardManager.Entry entry = top.get(position - 1);
        return field.equals("value") ? String.valueOf(entry.value) : entry.name;
    }

    /** crate_<crate>_name / _rewards / _price / _daily / _pity. */
    private String crateInfo(String rest) {
        int split = rest.lastIndexOf('_');
        if (split <= 0) return null;
        Crate crate = plugin.getCrateManager().getCrate(rest.substring(0, split));
        if (crate == null) return "";
        switch (rest.substring(split + 1)) {
            case "name": return crate.getDisplayName();
            case "rewards": return String.valueOf(crate.getValidRewards().size());
            case "price": return crate.getKeyPrice() > 0 ? plugin.getShopManager().format(crate.getKeyPrice()) : "-";
            case "daily": return String.valueOf(crate.getDailyFreeKeys());
            case "pity": return crate.isPityEnabled() ? String.valueOf(crate.getPityThreshold()) : "-";
            default: return null;
        }
    }
}
