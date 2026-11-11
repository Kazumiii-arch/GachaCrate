package com.kazumiii.gachacrate.commands;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.CrateManager;
import com.kazumiii.gachacrate.crates.OpeningMode;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.guis.CratePreviewMenu;
import com.kazumiii.gachacrate.guis.CrateSettingsMenu;
import com.kazumiii.gachacrate.guis.HistoryMenu;
import com.kazumiii.gachacrate.guis.MainMenu;
import com.kazumiii.gachacrate.guis.PlayerMenu;
import com.kazumiii.gachacrate.guis.ShopMenu;
import com.kazumiii.gachacrate.guis.TopMenu;
import com.kazumiii.gachacrate.history.LeaderboardManager;
import com.kazumiii.gachacrate.guis.RewardTypeSelectionMenu;
import com.kazumiii.gachacrate.player.PlayerData;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * /gacha (aliases /gc, /crate) and its tab completion.
 */
public class GachaCommand implements TabExecutor {

    private static final String NO_PERMISSION = "&cYou don't have permission to do that.";

    private final GachaCrate plugin;

    public GachaCommand(GachaCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player && sender.hasPermission("gacha.admin.edit")) {
                new MainMenu((Player) sender).open();
            } else if (sender instanceof Player) {
                new PlayerMenu((Player) sender).open();
            } else {
                sendHelp(sender, label);
            }
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help": sendHelp(sender, label); break;
            case "menu": menu(sender); break;
            case "daily": daily(sender); break;
            case "shop": shop(sender); break;
            case "buy": buy(sender, args); break;
            case "history": history(sender, args); break;
            case "wins": wins(sender); break;
            case "top": top(sender, args); break;
            case "create": create(sender, args); break;
            case "delete": delete(sender, args); break;
            case "list": list(sender); break;
            case "edit": edit(sender, args); break;
            case "view": case "preview": view(sender, args); break;
            case "additem": addItem(sender, args); break;
            case "addcmd": addCommand(sender, args); break;
            case "givekey": case "give": giveKey(sender, args); break;
            case "open": open(sender, args); break;
            case "stats": stats(sender, args); break;
            case "resetpity": resetPity(sender, args); break;
            case "setblock": setBlock(sender, args); break;
            case "removeblock": removeBlock(sender); break;
            case "reload": reload(sender); break;
            default:
                Text.send(sender, "&cUnknown subcommand. Use &f/" + label + " help&c.");
                break;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Help
    // ------------------------------------------------------------------

    private void sendHelp(CommandSender sender, String label) {
        String c = "/" + label;
        sender.sendMessage(Text.color("&6&lGachaCrate &7v" + plugin.getDescription().getVersion()));
        help(sender, "gacha.user.view", c + " menu", "Open the crate menu");
        help(sender, "gacha.user.view", c + " view <crate>", "Preview a crate's rewards");
        help(sender, "gacha.user.daily", c + " daily", "Claim today's free keys");
        help(sender, "gacha.user.shop", c + " shop", "Buy keys");
        help(sender, "gacha.user.shop", c + " buy <crate> [amount]", "Buy keys directly");
        help(sender, "gacha.user.history", c + " history", "Your recent wins");
        help(sender, "gacha.user.history", c + " wins", "Recent big wins on the server");
        help(sender, "gacha.user.top", c + " top [crate]", "Top crate openers");
        help(sender, "gacha.user.stats.self", c + " stats", "Your opening stats and pity");
        help(sender, "gacha.admin.edit", c, "Open the admin menu");
        help(sender, "gacha.admin.edit", c + " edit <crate>", "Edit a crate in the menu");
        help(sender, "gacha.admin.edit", c + " list", "List all crates");
        help(sender, "gacha.admin.create", c + " create <name>", "Create a crate");
        help(sender, "gacha.admin.delete", c + " delete <name>", "Delete a crate");
        help(sender, "gacha.admin.additem", c + " additem <crate> <rarity> <weight>", "Add the item in your hand");
        help(sender, "gacha.admin.addcmd", c + " addcmd <crate> <rarity> <weight> <command>", "Add a command reward");
        help(sender, "gacha.admin.givekey", c + " givekey <player|all> <crate> [amount]", "Give keys");
        help(sender, "gacha.admin.open", c + " open <crate> [player] [amount]", "Open a crate for free");
        help(sender, "gacha.admin.stats.other", c + " stats <player>", "Someone else's stats");
        help(sender, "gacha.admin.history.other", c + " history <player>", "Someone else's wins");
        help(sender, "gacha.admin.resetpity", c + " resetpity <player> <crate|all>", "Reset pity");
        help(sender, "gacha.admin.setblock", c + " setblock <crate>", "Make the block you look at a crate");
        help(sender, "gacha.admin.setblock", c + " removeblock", "Remove the crate you look at");
        help(sender, "gacha.admin.reload", c + " reload", "Reload all files");
    }

    private void help(CommandSender sender, String permission, String usage, String description) {
        if (sender.hasPermission(permission)) sender.sendMessage(Text.color("&e" + usage + " &7- " + description));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private boolean check(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        Text.send(sender, NO_PERMISSION);
        return false;
    }

    private Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player) return (Player) sender;
        Text.send(sender, "&cThis command can only be used in-game.");
        return null;
    }

    private Crate requireCrate(CommandSender sender, String name) {
        Crate crate = plugin.getCrateManager().getCrate(name);
        if (crate == null) {
            Text.send(sender, "&cCrate '&f" + name + "&c' not found. Crates: &f" + String.join(", ", plugin.getCrateManager().getCrateNames()));
        }
        return crate;
    }

    private Double parseWeight(CommandSender sender, String input) {
        try {
            double value = Double.parseDouble(input.replace(",", "."));
            if (value > 0 && !Double.isNaN(value) && !Double.isInfinite(value)) return value;
        } catch (NumberFormatException ignored) {
            // Handled below.
        }
        Text.send(sender, "&cThe weight must be a number above 0 (e.g. 25 or 0.5).");
        return null;
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer findPlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        return offline.hasPlayedBefore() ? offline : null;
    }

    // ------------------------------------------------------------------
    // Subcommands
    // ------------------------------------------------------------------

    private void menu(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player != null) new PlayerMenu(player).open();
    }

    private void daily(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player != null) plugin.getShopManager().claimDaily(player);
    }

    private void shop(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null || !check(sender, "gacha.user.shop")) return;
        if (!plugin.getShopManager().isEconomyReady()) {
            Text.send(sender, "&cThe key shop isn't available (the server needs Vault and an economy plugin).");
            return;
        }
        new ShopMenu(player).open();
    }

    private void buy(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return;
        if (args.length < 2) {
            Text.send(sender, "&cUsage: /gacha buy <crate> [amount]");
            return;
        }
        Crate crate = requireCrate(sender, args[1]);
        if (crate == null) return;
        int amount = 1;
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                Text.send(sender, "&cThe amount must be a whole number.");
                return;
            }
        }
        plugin.getShopManager().buyKeys(player, crate, amount);
    }

    private void history(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null || !check(sender, "gacha.user.history")) return;
        if (args.length > 1) {
            if (!check(sender, "gacha.admin.history.other")) return;
            OfflinePlayer target = findPlayer(args[1]);
            if (target == null) {
                Text.send(sender, "&cPlayer '" + args[1] + "' has never joined.");
                return;
            }
            new HistoryMenu(player, target.getUniqueId(), target.getName() == null ? args[1] : target.getName()).open();
            return;
        }
        new HistoryMenu(player, player.getUniqueId(), player.getName()).open();
    }

    private void wins(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null || !check(sender, "gacha.user.history")) return;
        new HistoryMenu(player, null, null).open();
    }

    private void top(CommandSender sender, String[] args) {
        if (!check(sender, "gacha.user.top")) return;
        String crateId = null;
        if (args.length > 1) {
            Crate crate = requireCrate(sender, args[1]);
            if (crate == null) return;
            crateId = crate.getId();
        }
        if (sender instanceof Player && crateId == null) {
            new TopMenu((Player) sender).open();
            return;
        }
        List<LeaderboardManager.Entry> top = plugin.getLeaderboardManager().top(crateId, 10);
        Text.send(sender, "&6&lTop crate openers" + (crateId == null ? "" : " &7(" + crateId + ")"));
        if (top.isEmpty()) sender.sendMessage(Text.color("  &7Nobody yet."));
        for (int i = 0; i < top.size(); i++) {
            sender.sendMessage(Text.color("  &e#" + (i + 1) + " &f" + top.get(i).name + " &7- &a" + top.get(i).value));
        }
    }

    private void create(CommandSender sender, String[] args) {
        if (!check(sender, "gacha.admin.create")) return;
        if (args.length < 2) {
            Text.send(sender, "&cUsage: /gacha create <name>");
            return;
        }
        String name = args[1];
        if (!CrateManager.isValidName(name)) {
            Text.send(sender, "&cCrate names can only use letters, numbers, _ and - (max 32).");
            return;
        }
        if (plugin.getCrateManager().getCrate(name) != null) {
            Text.send(sender, "&cA crate called '" + name + "' already exists.");
            return;
        }
        Crate crate = plugin.getCrateManager().createCrate(name);
        Text.send(sender, "&aCrate &e" + name + " &acreated!");
        if (sender instanceof Player && sender.hasPermission("gacha.admin.edit")) {
            new CrateSettingsMenu((Player) sender, crate).open();
        }
    }

    private void delete(CommandSender sender, String[] args) {
        if (!check(sender, "gacha.admin.delete")) return;
        if (args.length < 2) {
            Text.send(sender, "&cUsage: /gacha delete <name>");
            return;
        }
        Crate crate = requireCrate(sender, args[1]);
        if (crate == null) return;
        int blocks = plugin.getPhysicalCrateManager().getBlocksOf(crate.getId()).size();
        plugin.getCrateManager().deleteCrate(crate.getName());
        Text.send(sender, "&aCrate &e" + crate.getName() + " &adeleted" + (blocks > 0 ? " (and " + blocks + " crate blocks)." : "."));
    }

    private void list(CommandSender sender) {
        if (!check(sender, "gacha.admin.edit")) return;
        Collection<Crate> crates = plugin.getCrateManager().getAllCrates().values();
        if (crates.isEmpty()) {
            Text.send(sender, "&7No crates yet. Create one with &f/gacha create <name>&7.");
            return;
        }
        Text.send(sender, "&6Crates (" + crates.size() + "):");
        for (Crate crate : crates) {
            int blocks = plugin.getPhysicalCrateManager().getBlocksOf(crate.getId()).size();
            sender.sendMessage(Text.color("  &8• &e" + crate.getName() + " &7(") + crate.getDisplayName()
                    + Text.color("&7) - " + crate.getRewards().size() + " rewards, " + blocks + " blocks"));
        }
    }

    private void edit(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null || !check(sender, "gacha.admin.edit")) return;
        if (args.length < 2) {
            new MainMenu(player).open();
            return;
        }
        Crate crate = requireCrate(sender, args[1]);
        if (crate != null) new CrateSettingsMenu(player, crate).open();
    }

    private void view(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null || !check(sender, "gacha.user.view")) return;
        if (args.length < 2) {
            Text.send(sender, "&cUsage: /gacha view <crate>");
            return;
        }
        Crate crate = requireCrate(sender, args[1]);
        if (crate != null) new CratePreviewMenu(player, crate).open();
    }

    private void addItem(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null || !check(sender, "gacha.admin.additem")) return;
        if (args.length < 4) {
            Text.send(sender, "&cUsage: /gacha additem <crate> <rarity> <weight>");
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            Text.send(sender, "&cHold the item you want to add.");
            return;
        }
        Crate crate = requireCrate(sender, args[1]);
        if (crate == null) return;
        Rarity rarity = plugin.getRarityManager().getRarity(args[2]);
        if (rarity == null) {
            Text.send(sender, "&cUnknown rarity. Use one of: &f" + String.join(", ", plugin.getRarityManager().getRarityIds()));
            return;
        }
        Double weight = parseWeight(sender, args[3]);
        if (weight == null) return;

        crate.addReward(new Reward(hand.clone(), rarity, weight));
        plugin.getCrateManager().saveCrates();
        Text.send(sender, "&aAdded &f" + hand.getAmount() + "x " + hand.getType().name() + " &ato &e" + crate.getName()
                + " &a(" + Text.number(crate.getPercent(crate.getRewards().get(crate.getRewards().size() - 1))) + "% chance).");
    }

    private void addCommand(CommandSender sender, String[] args) {
        if (!check(sender, "gacha.admin.addcmd")) return;
        if (args.length < 5) {
            Text.send(sender, "&cUsage: /gacha addcmd <crate> <rarity> <weight> <command>");
            Text.send(sender, "&7Example: &f/gacha addcmd vote RARE 10 eco give {player} 500");
            Text.send(sender, "&7Several commands: separate them with &f|");
            return;
        }
        Crate crate = requireCrate(sender, args[1]);
        if (crate == null) return;
        Rarity rarity = plugin.getRarityManager().getRarity(args[2]);
        if (rarity == null) {
            Text.send(sender, "&cUnknown rarity. Use one of: &f" + String.join(", ", plugin.getRarityManager().getRarityIds()));
            return;
        }
        Double weight = parseWeight(sender, args[3]);
        if (weight == null) return;
        List<String> commands = RewardTypeSelectionMenu.splitCommands(String.join(" ", Arrays.copyOfRange(args, 4, args.length)));
        if (commands.isEmpty()) {
            Text.send(sender, "&cPlease give a command.");
            return;
        }
        crate.addReward(new Reward(commands, rarity, weight));
        plugin.getCrateManager().saveCrates();
        Text.send(sender, "&aCommand reward added to &e" + crate.getName() + "&a.");
    }

    private void giveKey(CommandSender sender, String[] args) {
        if (!check(sender, "gacha.admin.givekey")) return;
        if (args.length < 3) {
            Text.send(sender, "&cUsage: /gacha givekey <player|all> <crate> [amount]");
            return;
        }
        Crate crate = requireCrate(sender, args[2]);
        if (crate == null) return;

        int amount = 1;
        if (args.length > 3) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                amount = -1;
            }
            if (amount < 1 || amount > 2304) {
                Text.send(sender, "&cThe amount must be a whole number from 1 to 2304.");
                return;
            }
        }

        List<Player> targets = new ArrayList<>();
        if (args[1].equalsIgnoreCase("all") || args[1].equals("*")) {
            targets.addAll(Bukkit.getOnlinePlayers());
        } else {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                Text.send(sender, "&cPlayer '" + args[1] + "' isn't online.");
                return;
            }
            targets.add(target);
        }

        for (Player target : targets) {
            plugin.getKeyManager().giveKeys(target, crate, amount);
            Text.send(target, "&aYou received &e" + amount + "x " + crate.getDisplayName() + " &akey" + (amount == 1 ? "" : "s") + "!");
            sendFirstKeyWelcome(target);
        }
        String who = targets.size() == 1 ? targets.get(0).getName() : targets.size() + " players";
        if (!(targets.size() == 1 && targets.get(0).equals(sender))) {
            Text.send(sender, "&aGave " + who + " " + amount + "x " + crate.getName() + " key" + (amount == 1 ? "" : "s") + ".");
        }
    }

    private void sendFirstKeyWelcome(Player target) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        if (data == null || data.hasReceivedFirstKey()) return;
        for (String line : plugin.getConfig().getStringList("messages.first-key-welcome")) {
            target.sendMessage(Text.color(line).replace("{player}", target.getName()));
        }
        data.setHasReceivedFirstKey(true);
    }

    private void open(CommandSender sender, String[] args) {
        if (!check(sender, "gacha.admin.open")) return;
        if (args.length < 2) {
            Text.send(sender, "&cUsage: /gacha open <crate> [player] [amount]");
            return;
        }
        Crate crate = requireCrate(sender, args[1]);
        if (crate == null) return;

        Player target;
        if (args.length > 2) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                Text.send(sender, "&cPlayer '" + args[2] + "' isn't online.");
                return;
            }
        } else {
            target = requirePlayer(sender);
            if (target == null) return;
        }

        int amount = 1;
        if (args.length > 3) {
            try {
                amount = Math.max(1, Math.min(256, Integer.parseInt(args[3])));
            } catch (NumberFormatException e) {
                Text.send(sender, "&cThe amount must be a whole number.");
                return;
            }
        }
        plugin.getCrateOpener().openFree(target, crate, amount);
    }

    private void stats(CommandSender sender, String[] args) {
        UUID uuid;
        String name;
        if (args.length > 1) {
            if (!check(sender, "gacha.admin.stats.other")) return;
            OfflinePlayer target = findPlayer(args[1]);
            if (target == null) {
                Text.send(sender, "&cPlayer '" + args[1] + "' has never joined.");
                return;
            }
            uuid = target.getUniqueId();
            name = target.getName() == null ? args[1] : target.getName();
        } else {
            Player player = requirePlayer(sender);
            if (player == null || !check(sender, "gacha.user.stats.self")) return;
            uuid = player.getUniqueId();
            name = player.getName();
        }

        PlayerData data = plugin.getPlayerManager().getOfflineData(uuid);
        Text.send(sender, "&6&lCrate stats for " + name);
        if (data == null || data.getCratesOpenedMap().isEmpty()) {
            sender.sendMessage(Text.color("  &7No crates opened yet."));
            return;
        }
        sender.sendMessage(Text.color("  &7Total opened: &f" + data.getTotalCratesOpened()));
        for (Map.Entry<String, Integer> entry : data.getCratesOpenedMap().entrySet()) {
            Crate crate = plugin.getCrateManager().getCrate(entry.getKey());
            String crateName = crate == null ? entry.getKey() : crate.getDisplayName();
            String line = Text.color("  &8• ") + crateName + Text.color(" &7- &f" + entry.getValue() + " &7opened");
            if (crate != null && crate.isPityEnabled()) {
                line += Text.color(" &8| &dpity " + data.getPityCount(crate.getName()) + "/" + crate.getPityThreshold());
            }
            sender.sendMessage(line);
        }
    }

    private void resetPity(CommandSender sender, String[] args) {
        if (!check(sender, "gacha.admin.resetpity")) return;
        if (args.length < 3) {
            Text.send(sender, "&cUsage: /gacha resetpity <player> <crate|all>");
            return;
        }
        OfflinePlayer target = findPlayer(args[1]);
        if (target == null) {
            Text.send(sender, "&cPlayer '" + args[1] + "' has never joined.");
            return;
        }
        List<String> crates = new ArrayList<>();
        if (args[2].equalsIgnoreCase("all")) {
            crates.addAll(plugin.getCrateManager().getAllCrates().keySet());
        } else {
            Crate crate = requireCrate(sender, args[2]);
            if (crate == null) return;
            crates.add(crate.getId());
        }

        PlayerData online = target.isOnline() ? plugin.getPlayerManager().getPlayerData(target.getUniqueId()) : null;
        for (String crate : crates) {
            if (online != null) online.resetPityCount(crate);
            else plugin.getPlayerManager().resetPityOffline(target.getUniqueId(), crate);
        }
        Text.send(sender, "&aReset pity for &e" + (target.getName() == null ? args[1] : target.getName()) + "&a.");
    }

    private void setBlock(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null || !check(sender, "gacha.admin.setblock")) return;
        if (args.length < 2) {
            Text.send(sender, "&cUsage: /gacha setblock <crate>");
            return;
        }
        Crate crate = requireCrate(sender, args[1]);
        if (crate == null) return;
        Block block = player.getTargetBlockExact(6);
        if (block == null || block.getType().isAir()) {
            Text.send(sender, "&cLook at a block within 6 blocks of you.");
            return;
        }
        String existing = plugin.getPhysicalCrateManager().getCrateNameAt(block.getLocation());
        if (existing != null) {
            Text.send(sender, "&cThat block is already a '" + existing + "' crate. Use /gacha removeblock first.");
            return;
        }
        plugin.getPhysicalCrateManager().addCrate(block.getLocation(), crate.getId());
        Text.send(sender, "&aThis " + block.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ')
                + " is now a " + crate.getDisplayName() + " &acrate!");
        if (crate.getOpeningMode() != OpeningMode.PHYSICAL_BLOCKS) {
            Text.send(sender, "&eNote: this crate uses 'Key in Hand' mode. The block will open it too, but players can also open it anywhere.");
        }
    }

    private void removeBlock(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null || !check(sender, "gacha.admin.setblock")) return;
        Block block = player.getTargetBlockExact(6);
        if (block == null || !plugin.getPhysicalCrateManager().isCrateBlock(block)) {
            Text.send(sender, "&cYou're not looking at a crate block.");
            return;
        }
        plugin.getPhysicalCrateManager().removeCrate(block.getLocation());
        Text.send(sender, "&aThat block is no longer a crate.");
    }

    private void reload(CommandSender sender) {
        if (!check(sender, "gacha.admin.reload")) return;
        long start = System.currentTimeMillis();
        plugin.reloadAll();
        Text.send(sender, "&aGachaCrate reloaded in " + (System.currentTimeMillis() - start) + " ms ("
                + plugin.getCrateManager().getAllCrates().size() + " crates).");
    }

    // ------------------------------------------------------------------
    // Tab completion
    // ------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            addIf(sender, subs, "gacha.user.view", "view", "menu");
            addIf(sender, subs, "gacha.user.daily", "daily");
            addIf(sender, subs, "gacha.user.shop", "shop", "buy");
            addIf(sender, subs, "gacha.user.history", "history", "wins");
            addIf(sender, subs, "gacha.user.top", "top");
            addIf(sender, subs, "gacha.user.stats.self", "stats");
            subs.add("help");
            addIf(sender, subs, "gacha.admin.edit", "edit", "list");
            addIf(sender, subs, "gacha.admin.create", "create");
            addIf(sender, subs, "gacha.admin.delete", "delete");
            addIf(sender, subs, "gacha.admin.additem", "additem");
            addIf(sender, subs, "gacha.admin.addcmd", "addcmd");
            addIf(sender, subs, "gacha.admin.givekey", "givekey");
            addIf(sender, subs, "gacha.admin.open", "open");
            addIf(sender, subs, "gacha.admin.resetpity", "resetpity");
            addIf(sender, subs, "gacha.admin.setblock", "setblock", "removeblock");
            addIf(sender, subs, "gacha.admin.reload", "reload");
            return filter(subs, args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        List<String> crates = plugin.getCrateManager().getCrateNames();
        List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());

        if (args.length == 2) {
            switch (sub) {
                case "view": case "preview": case "edit": case "delete": case "additem": case "addcmd":
                case "open": case "setblock": case "top":
                    return filter(crates, args[1]);
                case "buy": {
                    List<String> forSale = new ArrayList<>();
                    for (Crate crate : plugin.getShopManager().getCratesForSale()) forSale.add(crate.getName());
                    return filter(forSale, args[1]);
                }
                case "history":
                    return sender.hasPermission("gacha.admin.history.other") ? filter(players, args[1]) : Collections.emptyList();
                case "givekey": case "give": {
                    List<String> options = new ArrayList<>(players);
                    options.add("all");
                    return filter(options, args[1]);
                }
                case "stats": case "resetpity":
                    return sender.hasPermission("gacha.admin.stats.other") || sub.equals("resetpity")
                            ? filter(players, args[1]) : Collections.emptyList();
                default:
                    return Collections.emptyList();
            }
        }

        if (args.length == 3) {
            switch (sub) {
                case "additem": case "addcmd":
                    return filter(plugin.getRarityManager().getRarityIds(), args[2]);
                case "givekey": case "give":
                    return filter(crates, args[2]);
                case "buy":
                    return filter(Arrays.asList("1", "5", "10"), args[2]);
                case "open":
                    return filter(players, args[2]);
                case "resetpity": {
                    List<String> options = new ArrayList<>(crates);
                    options.add("all");
                    return filter(options, args[2]);
                }
                default:
                    return Collections.emptyList();
            }
        }

        if (args.length == 4) {
            switch (sub) {
                case "additem": case "addcmd":
                    return filter(Arrays.asList("1", "5", "10", "25", "50"), args[3]);
                case "givekey": case "give": case "open":
                    return filter(Arrays.asList("1", "5", "10", "64"), args[3]);
                default:
                    return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private static void addIf(CommandSender sender, List<String> list, String permission, String... values) {
        if (sender.hasPermission(permission)) list.addAll(Arrays.asList(values));
    }

    private static List<String> filter(List<String> options, String typed) {
        String lower = typed.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(lower)).collect(Collectors.toList());
    }
}
