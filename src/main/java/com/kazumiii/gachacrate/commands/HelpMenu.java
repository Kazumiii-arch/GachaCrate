package com.kazumiii.gachacrate.commands;

import com.kazumiii.gachacrate.GachaCrate;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * The green, clickable /gacha help. Players can click a command to put it in their chat box,
 * and hover to see what it does. Only commands the reader has permission for are shown.
 */
final class HelpMenu {

    private static final int PER_PAGE = 8;

    private static final class Entry {
        final String section;
        final String permission;
        final String usage;
        final String description;

        Entry(String section, String permission, String usage, String description) {
            this.section = section;
            this.permission = permission;
            this.usage = usage;
            this.description = description;
        }
    }

    private final GachaCrate plugin;
    private final List<Entry> entries = new ArrayList<>();

    HelpMenu(GachaCrate plugin, String label) {
        this.plugin = plugin;
        String c = "/" + label;
        // Player commands
        add("Player", "gacha.user.menu", c, "Open the crate menu");
        add("Player", "gacha.user.view", c + " view <crate>", "See a crate's rewards and chances");
        add("Player", "gacha.user.daily", c + " daily", "Claim today's free keys");
        add("Player", "gacha.user.shop", c + " shop", "Open the key shop");
        add("Player", "gacha.user.shop", c + " buy <crate> [amount]", "Buy keys right away");
        add("Player", "gacha.user.keys", c + " keys", "See how many keys you have");
        add("Player", "gacha.user.history", c + " history", "Your recent wins");
        add("Player", "gacha.user.history", c + " wins", "The server's latest big wins");
        add("Player", "gacha.user.top", c + " top [crate]", "Top crate openers");
        add("Player", "gacha.user.stats.self", c + " stats", "Your stats and pity progress");
        // Admin commands
        add("Admin", "gacha.admin.edit", c + " admin", "Open the admin menu");
        add("Admin", "gacha.admin.edit", c + " edit <crate>", "Edit a crate in the menu");
        add("Admin", "gacha.admin.edit", c + " list", "List all crates");
        add("Admin", "gacha.admin.create", c + " create <name>", "Create a new crate");
        add("Admin", "gacha.admin.create", c + " setup", "Create 4 ready-made crates (Vote to Legendary)");
        add("Admin", "gacha.admin.delete", c + " delete <crate>", "Delete a crate");
        add("Admin", "gacha.admin.additem", c + " additem <crate> <rarity> <weight>", "Add the item in your hand");
        add("Admin", "gacha.admin.addcmd", c + " addcmd <crate> <rarity> <weight> <cmd>", "Add a command reward");
        add("Admin", "gacha.admin.givekey", c + " givekey <player|all> <crate> [amount]", "Give keys (works for offline players too)");
        add("Admin", "gacha.admin.open", c + " open <crate> [player] [amount]", "Open a crate for free");
        add("Admin", "gacha.admin.setblock", c + " setblock <crate>", "Turn the block you look at into a crate");
        add("Admin", "gacha.admin.setblock", c + " removeblock", "Remove the crate you look at");
        add("Admin", "gacha.admin.stats.other", c + " stats <player>", "Another player's stats");
        add("Admin", "gacha.admin.history.other", c + " history <player>", "Another player's wins");
        add("Admin", "gacha.admin.resetpity", c + " resetpity <player> <crate|all>", "Reset pity");
        add("Admin", "gacha.admin.reload", c + " reload", "Reload all files");
    }

    private void add(String section, String permission, String usage, String description) {
        entries.add(new Entry(section, permission, usage, description));
    }

    void send(CommandSender sender, String label, int requestedPage) {
        List<Entry> visible = new ArrayList<>();
        for (Entry entry : entries) {
            if (sender.hasPermission(entry.permission)) visible.add(entry);
        }
        int pages = Math.max(1, (int) Math.ceil(visible.size() / (double) PER_PAGE));
        int page = Math.max(1, Math.min(requestedPage, pages));
        boolean player = sender instanceof Player;

        sender.sendMessage("");
        sender.sendMessage(com.kazumiii.gachacrate.util.Text.color(
                "&2&m          &r &a&l✦ &2&lGacha&a&lCrate &a&l✦ &2&m          "));
        sender.sendMessage(com.kazumiii.gachacrate.util.Text.color(
                "   &7Version &a" + plugin.getDescription().getVersion()
                        + (player ? " &8• &7Click a command to use it" : "")));

        String section = null;
        int start = (page - 1) * PER_PAGE;
        for (int i = start; i < Math.min(start + PER_PAGE, visible.size()); i++) {
            Entry entry = visible.get(i);
            if (!entry.section.equals(section)) {
                section = entry.section;
                sender.sendMessage(com.kazumiii.gachacrate.util.Text.color(
                        "&8» &2&l" + section.toUpperCase() + " COMMANDS"));
            }
            line(sender, entry, player);
        }

        if (pages > 1) footer(sender, label, page, pages, player);
        sender.sendMessage(com.kazumiii.gachacrate.util.Text.color("&2&m                                              "));
    }

    private void line(CommandSender sender, Entry entry, boolean player) {
        String legacy = "  &a▸ &f" + colorUsage(entry.usage) + " &8- &7" + entry.description;
        if (!player) {
            sender.sendMessage(com.kazumiii.gachacrate.util.Text.color(legacy));
            return;
        }
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(com.kazumiii.gachacrate.util.Text.color(legacy)));
        String suggest = entry.usage.contains("<") ? entry.usage.substring(0, entry.usage.indexOf('<')) : entry.usage;
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest.trim() + " "));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                com.kazumiii.gachacrate.util.Text.color("&a" + entry.usage + "\n&7" + entry.description + "\n\n&2Click to type it in chat"))));
        sender.spigot().sendMessage(component);
    }

    /** Makes <required> and [optional] arguments a different shade. */
    private static String colorUsage(String usage) {
        return usage.replace("<", "&2<").replace(">", ">&f").replace("[", "&8[").replace("]", "]&f");
    }

    private void footer(CommandSender sender, String label, int page, int pages, boolean player) {
        if (!player) {
            sender.sendMessage(com.kazumiii.gachacrate.util.Text.color(
                    "   &7Page &a" + page + "&7/&a" + pages + " &8• &7/" + label + " help <page>"));
            return;
        }
        List<BaseComponent> parts = new ArrayList<>();
        parts.add(legacy("   "));
        if (page > 1) {
            TextComponent previous = legacy("&a&l« Previous");
            previous.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + label + " help " + (page - 1)));
            previous.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Go to page " + (page - 1))));
            parts.add(previous);
        } else {
            parts.add(legacy("&8« Previous"));
        }
        parts.add(legacy("  &7Page &a" + page + "&7/&a" + pages + "  "));
        if (page < pages) {
            TextComponent next = legacy("&a&lNext »");
            next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + label + " help " + (page + 1)));
            next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Go to page " + (page + 1))));
            parts.add(next);
        } else {
            parts.add(legacy("&8Next »"));
        }
        sender.spigot().sendMessage(parts.toArray(new BaseComponent[0]));
    }

    private static TextComponent legacy(String text) {
        return new TextComponent(TextComponent.fromLegacyText(com.kazumiii.gachacrate.util.Text.color(text)));
    }
}
