package com.kazumiii.gachacrate.backup;

import com.kazumiii.gachacrate.GachaCrate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Keeps copies of crates.yml and locations.yml in plugins/GachaCrate/backups/, so a bad edit
 * (or a deleted crate) can be undone with /gacha backup restore or the Backups menu.
 * <p>
 * A backup is made automatically when the plugin starts and, at most once per interval,
 * right before crates.yml is overwritten. Old backups are removed when there are too many.
 */
public class BackupManager {

    /** Files that are backed up, relative to the plugin folder. */
    private static final String[] FILES = {"crates.yml", "locations.yml"};

    private static final DateTimeFormatter NAME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.ROOT);
    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z0-9_-][A-Za-z0-9_.-]{0,63}$");

    private final GachaCrate plugin;
    private final File folder;
    private long lastAutoBackup;

    public BackupManager(GachaCrate plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "backups");
    }

    private boolean enabled() {
        return plugin.getConfig().getBoolean("backups.enabled", true);
    }

    /** Called right before crates.yml is replaced. Makes a backup if the last one is old enough. */
    public void backupIfDue(File cratesFile) {
        if (!enabled() || !cratesFile.exists()) return;
        long interval = Math.max(1, plugin.getConfig().getLong("backups.interval-minutes", 30)) * 60_000L;
        long now = System.currentTimeMillis();
        if (now - lastAutoBackup < interval) return;
        lastAutoBackup = now;
        create("auto");
    }

    /**
     * Makes a backup now.
     *
     * @param reason a short word added to the folder name, e.g. "manual" or "startup"
     * @return the backup's name, or null if there was nothing to back up or it failed
     */
    public String create(String reason) {
        // Automatic backups are skipped when nothing changed since the newest one.
        if (!reason.equals("manual") && sameAsNewest()) return null;
        String name = LocalDateTime.now().format(NAME) + "_" + reason.replaceAll("[^A-Za-z0-9-]", "");
        File target = new File(folder, name);
        int copied = 0;
        try {
            for (String fileName : FILES) {
                File source = new File(plugin.getDataFolder(), fileName);
                if (!source.exists()) continue;
                if (!target.exists() && !target.mkdirs()) throw new IOException("Could not create " + target);
                Files.copy(source.toPath(), new File(target, fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                copied++;
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not create a backup.", e);
            return null;
        }
        if (copied == 0) return null;
        prune();
        return name;
    }

    private boolean sameAsNewest() {
        List<String> names = list();
        if (names.isEmpty()) return false;
        File newest = new File(folder, names.get(0));
        try {
            for (String fileName : FILES) {
                File current = new File(plugin.getDataFolder(), fileName);
                File saved = new File(newest, fileName);
                if (current.exists() != saved.exists()) return false;
                if (current.exists() && Files.mismatch(current.toPath(), saved.toPath()) != -1) return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /** Backup names, newest first. */
    public List<String> list() {
        File[] dirs = folder.listFiles(File::isDirectory);
        List<String> names = new ArrayList<>();
        if (dirs == null) return names;
        Arrays.sort(dirs, Comparator.comparing(File::getName).reversed());
        for (File dir : dirs) names.add(dir.getName());
        return names;
    }

    /** Number of crates stored in a backup (for menus), or -1 if unknown. */
    public int countCrates(String name) {
        File file = new File(new File(folder, name), "crates.yml");
        if (!file.exists()) return -1;
        org.bukkit.configuration.ConfigurationSection crates =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file).getConfigurationSection("crates");
        return crates == null ? 0 : crates.getKeys(false).size();
    }

    /**
     * Puts a backup's files back and reloads the plugin. The current files are backed up first,
     * so a restore can itself be undone.
     *
     * @return null on success, otherwise an error message
     */
    public String restore(String name) {
        if (name == null || !SAFE_NAME.matcher(name).matches()) return "Invalid backup name.";
        File source = new File(folder, name);
        if (!source.isDirectory()) return "Backup '" + name + "' not found.";

        create("before-restore");
        try {
            for (String fileName : FILES) {
                File from = new File(source, fileName);
                File to = new File(plugin.getDataFolder(), fileName);
                if (from.exists()) {
                    Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not restore backup " + name, e);
            return "Could not copy the backup files: " + e.getMessage();
        }
        // Don't let the reload's own save immediately make another "auto" backup.
        lastAutoBackup = System.currentTimeMillis();
        plugin.reloadAll();
        return null;
    }

    private void prune() {
        int keep = Math.max(1, plugin.getConfig().getInt("backups.keep", 20));
        List<String> names = list();
        for (int i = keep; i < names.size(); i++) {
            File dir = new File(folder, names.get(i));
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) f.delete();
            dir.delete();
        }
    }
}
