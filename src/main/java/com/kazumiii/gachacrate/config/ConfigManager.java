package com.kazumiii.gachacrate.config;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {

    private final GachaCrate plugin;

    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration animations;
    private FileConfiguration sounds;

    private File messagesFile;
    private File animationsFile;
    private File soundsFile;

    public ConfigManager(GachaCrate plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();

        loadMessages();
        loadAnimations();
        loadSounds();
    }

    // ------------------------------
    // Messages.yml
    // ------------------------------
    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    // ------------------------------
    // Animations.yml
    // ------------------------------
    private void loadAnimations() {
        animationsFile = new File(plugin.getDataFolder(), "animations.yml");

        if (!animationsFile.exists()) {
            plugin.saveResource("animations.yml", false);
        }

        animations = YamlConfiguration.loadConfiguration(animationsFile);
    }

    // ------------------------------
    // Sounds.yml
    // ------------------------------
    private void loadSounds() {
        soundsFile = new File(plugin.getDataFolder(), "sounds.yml");

        if (!soundsFile.exists()) {
            plugin.saveResource("sounds.yml", false);
        }

        sounds = YamlConfiguration.loadConfiguration(soundsFile);
    }

    // ------------------------------
    // Save utilities
    // ------------------------------
    public void saveMessages() {
        try {
            messages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save messages.yml", e);
        }
    }

    public void saveAnimations() {
        try {
            animations.save(animationsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save animations.yml", e);
        }
    }

    public void saveSounds() {
        try {
            sounds.save(soundsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save sounds.yml", e);
        }
    }

    // ------------------------------
    // Getters for config values
    // ------------------------------

    public String getMessage(String path) {
        return messages.getString(path, "§cMissing message: " + path);
    }

    public String getSound(String path) {
        return sounds.getString(path, "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public int getSaveIntervalSeconds() {
        return config.getInt("save-interval-seconds", 60);
    }

    public int getBackupIntervalMinutes() {
        return config.getInt("backup-interval-minutes", 30);
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getAnimations() {
        return animations;
    }

    public FileConfiguration getSounds() {
        return sounds;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
