package com.kazumiii.gachacrate.util;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks GitHub for a newer GachaCrate release and tells the console and admins
 * (permission gacha.admin.update) when they join. Runs in the background; never blocks the server.
 */
public class UpdateChecker implements Listener {

    private static final Pattern TAG = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern URL = Pattern.compile("\"html_url\"\\s*:\\s*\"([^\"]+/releases/tag/[^\"]+)\"");

    private final GachaCrate plugin;
    private volatile String latestVersion;
    private volatile String downloadUrl;

    public UpdateChecker(GachaCrate plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("update-checker.enabled", true)) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::check);
        // Check again every 6 hours for long-running servers.
        long sixHours = 6L * 60 * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::check, sixHours, sixHours);
    }

    private void check() {
        String repo = plugin.getConfig().getString("update-checker.github-repository", "Kazumiii-arch/GachaCrate");
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/" + repo + "/releases/latest"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "GachaCrate-UpdateChecker")
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return; // No releases yet, or GitHub unavailable.

            Matcher tag = TAG.matcher(response.body());
            if (!tag.find()) return;
            String latest = tag.group(1).replaceFirst("^[vV]", "");
            Matcher url = URL.matcher(response.body());
            String link = url.find() ? url.group(1) : "https://github.com/" + repo + "/releases/latest";

            String current = plugin.getDescription().getVersion();
            if (isNewer(latest, current)) {
                boolean firstTime = latestVersion == null || !latestVersion.equals(latest);
                latestVersion = latest;
                downloadUrl = link;
                if (firstTime) {
                    plugin.getLogger().info("A new version of GachaCrate is available: " + latest + " (you have " + current + ").");
                    plugin.getLogger().info("Download: " + link);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().fine("Update check failed: " + e.getMessage());
        }
    }

    /** True if version a is higher than b (compares numbers part by part: 2.10.0 > 2.9.1). */
    public static boolean isNewer(String a, String b) {
        String[] pa = a.split("[.-]");
        String[] pb = b.split("[.-]");
        for (int i = 0; i < Math.max(pa.length, pb.length); i++) {
            int na = i < pa.length ? number(pa[i]) : 0;
            int nb = i < pb.length ? number(pb[i]) : 0;
            if (na != nb) return na > nb;
        }
        return false;
    }

    private static int number(String part) {
        String digits = part.replaceAll("\\D", "");
        if (digits.isEmpty()) return 0;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (latestVersion == null || !player.hasPermission("gacha.admin.update")) return;
        if (!plugin.getConfig().getBoolean("update-checker.notify-admins", true)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            Text.send(player, "&eA new version of GachaCrate is out: &a" + latestVersion
                    + " &7(you have " + plugin.getDescription().getVersion() + ")");
            Text.send(player, "&7Download: &b" + downloadUrl);
        }, 80L);
    }
}
