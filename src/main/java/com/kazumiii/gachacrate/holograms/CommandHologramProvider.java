package com.kazumiii.gachacrate.holograms;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * Works with any hologram plugin that has console commands.
 * The commands are set in config.yml under hologram.commands. Placeholders:
 * {name}, {world}, {x}, {y}, {z}, {line} (in line-commands), {line1}, {line2}, ... and {lines} (all lines joined with ' | ').
 */
final class CommandHologramProvider implements HologramProvider {

    private final GachaCrate plugin;

    CommandHologramProvider(GachaCrate plugin) {
        this.plugin = plugin;
    }

    private ConfigurationSection section() {
        return plugin.getConfig().getConfigurationSection("hologram.commands");
    }

    @Override
    public String getName() {
        return "custom commands";
    }

    @Override
    public void create(String name, Location location, List<String> lines) {
        ConfigurationSection section = section();
        if (section == null) return;
        remove(name);
        for (String command : section.getStringList("create")) run(command, name, location, lines, null);
        for (String line : lines) {
            for (String command : section.getStringList("add-line")) run(command, name, location, lines, line);
        }
    }

    @Override
    public void remove(String name) {
        ConfigurationSection section = section();
        if (section == null) return;
        for (String command : section.getStringList("delete")) run(command, name, null, null, null);
    }

    private void run(String template, String name, Location location, List<String> lines, String line) {
        String command = template.replace("{name}", name);
        if (location != null && location.getWorld() != null) {
            command = command.replace("{world}", location.getWorld().getName())
                    .replace("{x}", String.valueOf(location.getX()))
                    .replace("{y}", String.valueOf(location.getY()))
                    .replace("{z}", String.valueOf(location.getZ()));
        }
        if (lines != null) {
            command = command.replace("{lines}", String.join(" | ", lines));
            for (int i = 0; i < lines.size(); i++) command = command.replace("{line" + (i + 1) + "}", lines.get(i));
        }
        if (line != null) command = command.replace("{line}", line);
        if (command.startsWith("/")) command = command.substring(1);
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (Exception e) {
            plugin.getLogger().warning("Hologram command failed: /" + command + " (" + e.getMessage() + ")");
        }
    }
}
