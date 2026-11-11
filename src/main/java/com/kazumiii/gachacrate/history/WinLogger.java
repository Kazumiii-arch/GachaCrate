package com.kazumiii.gachacrate.history;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * Writes every win to plugins/GachaCrate/logs/wins-YYYY-MM-DD.log, so staff can check
 * what a player won (handy for support tickets on big servers).
 * Lines are queued and written in the background every few seconds.
 */
public class WinLogger {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GachaCrate plugin;
    private final File folder;
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    public WinLogger(GachaCrate plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "logs");
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::flush, 100L, 100L);
    }

    public void log(Player player, Crate crate, Reward reward, boolean pity) {
        if (!plugin.getConfig().getBoolean("logging.wins-to-file", true)) return;
        queue.add("[" + LocalDateTime.now().format(TIME) + "] " + player.getName() + " (" + player.getUniqueId() + ") opened "
                + crate.getName() + " and won " + Text.strip(reward.getDisplayName()) + " [" + reward.getRarity().getId() + "]"
                + (pity ? " (pity)" : ""));
    }

    /** Writes queued lines to today's log file. */
    public synchronized void flush() {
        if (queue.isEmpty()) return;
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = queue.poll()) != null) lines.add(line);
        try {
            if (!folder.exists()) folder.mkdirs();
            File file = new File(folder, "wins-" + LocalDate.now() + ".log");
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not write the win log", e);
        }
    }
}
