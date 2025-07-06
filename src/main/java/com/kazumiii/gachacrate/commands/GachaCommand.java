package com.kazumiii.gachacrate.commands;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.guis.CrateEditorMenu;
import com.kazumiii.gachacrate.guis.CratePreviewMenu;
import com.kazumiii.gachacrate.guis.MainMenu;
import com.kazumiii.gachacrate.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GachaCommand implements CommandExecutor {

    private final GachaCrate plugin;

    public GachaCommand(GachaCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Please use '/gacha help' to see available console commands.");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("gacha.admin")) {
                player.sendMessage("§cUnknown command. Type \"/help\" for help.");
                return true;
            }
            new MainMenu(player).open();
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelpMessage(sender);
                break;
            case "create":
                handleCreateCommand(sender, args);
                break;
            case "delete":
                handleDeleteCommand(sender, args);
                break;
            case "additem":
                handleAddItemCommand(sender, args);
                break;
            case "addcmd":
                handleAddCmdCommand(sender, args);
                break;
            case "edit":
                handleEditCommand(sender, args);
                break;
            case "givekey":
                handleGiveKeyCommand(sender, args);
                break;
            case "view":
                handleViewCommand(sender, args);
                break;
            case "stats":
                handleStatsCommand(sender, args);
                break;
            case "setblock":
                handleSetBlock(sender, args);
                break;
            case "removeblock":
                handleRemoveBlock(sender, args);
                break;
            default:
                sender.sendMessage("§cUnknown subcommand. Use /gacha help.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§a§lGachaCrate Help");
        sender.sendMessage("§e/gacha §7- Opens the main admin GUI.");
        sender.sendMessage("§e/gacha create <name> §7- Creates a new crate.");
        sender.sendMessage("§e/gacha delete <name> §7- Deletes a crate permanently.");
        sender.sendMessage("§e/gacha additem <crate> <rarity> <chance> §7- Adds item in hand.");
        sender.sendMessage("§e/gacha addcmd <crate> <rarity> <chance> <cmd> §7- Adds a command reward.");
        sender.sendMessage("§e/gacha givekey <player> <crate> [amount] §7- Gives a player a crate key.");
        sender.sendMessage("§e/gacha view <crate> §7- Shows the possible rewards in a crate.");
        sender.sendMessage("§e/gacha stats [player] §7- Shows crate opening stats.");
        sender.sendMessage("§e/gacha setblock <crate> §7- Sets the block you're looking at as a physical crate.");
        sender.sendMessage("§e/gacha removeblock §7- Removes the physical crate you're looking at.");
    }

    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gacha.admin.create")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /gacha create <name>");
            return;
        }
        String crateName = args[1];
        if (plugin.getCrateManager().getCrate(crateName) != null) {
            sender.sendMessage("§cError: A crate with the name '" + crateName + "' already exists.");
            return;
        }
        plugin.getCrateManager().createCrate(crateName);
        sender.sendMessage("§aSuccess! Crate '" + crateName + "' has been created.");
    }

    private void handleDeleteCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gacha.admin.delete")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /gacha delete <name>");
            return;
        }
        String crateName = args[1];
        if (plugin.getCrateManager().getCrate(crateName) == null) {
            sender.sendMessage("§cError: Crate '" + crateName + "' not found.");
            return;
        }
        plugin.getCrateManager().deleteCrate(crateName);
        sender.sendMessage("§aSuccess! Crate '" + crateName + "' has been permanently deleted.");
    }

    private void handleAddItemCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be run by a player.");
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("gacha.admin.additem")) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return;
        }
        if (args.length < 4) {
            player.sendMessage("§cUsage: /gacha additem <crateName> <rarity> <chance>");
            return;
        }
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage("§cYou must be holding an item to add it to a crate.");
            return;
        }
        String crateName = args[1];
        Crate crate = plugin.getCrateManager().getCrate(crateName);
        if (crate == null) {
            player.sendMessage("§cError: Crate '" + crateName + "' not found.");
            return;
        }
        Rarity rarity = plugin.getRarityManager().getRarity(args[2]);
        if (rarity == null) {
            player.sendMessage("§cError: Invalid rarity. Available rarities: " + plugin.getRarityManager().getRarityIds());
            return;
        }
        double chance;
        try {
            chance = Double.parseDouble(args[3]);
            if (chance <= 0) {
                player.sendMessage("§cError: Chance must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cError: Invalid chance. Please use a number (e.g., 25.5).");
            return;
        }
        Reward reward = new Reward(itemInHand.clone(), rarity, chance);
        crate.addReward(reward);
        plugin.getCrateManager().saveCrates();
        player.sendMessage("§aSuccess! Added " + itemInHand.getType().name() + " to the '" + crateName + "' crate.");
    }

    private void handleAddCmdCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gacha.admin.addcmd")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return;
        }
        if (args.length < 5) {
            sender.sendMessage("§cUsage: /gacha addcmd <crate> <rarity> <chance> <command>");
            sender.sendMessage("§cExample: /gacha addcmd epic_crate MYTHIC 1.0 lp user {player} parent set vip");
            return;
        }
        String crateName = args[1];
        Crate crate = plugin.getCrateManager().getCrate(crateName);
        if (crate == null) {
            sender.sendMessage("§cError: Crate '" + crateName + "' not found.");
            return;
        }
        Rarity rarity = plugin.getRarityManager().getRarity(args[2]);
        if (rarity == null) {
            sender.sendMessage("§cError: Invalid rarity. Available: " + plugin.getRarityManager().getRarityIds());
            return;
        }
        double chance;
        try {
            chance = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cError: Invalid chance specified.");
            return;
        }
        String commandToExecute = Arrays.stream(args, 4, args.length).collect(Collectors.joining(" "));
        Reward reward = new Reward(commandToExecute, rarity, chance);
        crate.addReward(reward);
        plugin.getCrateManager().saveCrates();
        sender.sendMessage("§aSuccess! Added command reward to '" + crateName + "' crate.");
    }

    private void handleEditCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be run by a player.");
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("gacha.admin.edit")) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /gacha edit <crateName>");
            return;
        }
        String crateName = args[1];
        Crate crate = plugin.getCrateManager().getCrate(crateName);
        if (crate == null) {
            player.sendMessage("§cError: Crate '" + crateName + "' not found.");
            return;
        }
        new CrateEditorMenu(player, crate).open();
    }
    
    private void handleGiveKeyCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gacha.admin.givekey")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /gacha givekey <player> <crate> [amount]");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cError: Player '" + args[1] + "' is not online.");
            return;
        }
        String crateName = args[2];
        Crate crate = plugin.getCrateManager().getCrate(crateName);
        if (crate == null) {
            sender.sendMessage("§cError: Crate '" + crateName + "' not found.");
            return;
        }
        int amount = 1;
        if (args.length > 3) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount <= 0) {
                    sender.sendMessage("§cError: Amount must be a positive number.");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cError: Invalid amount. Please use a whole number.");
                return;
            }
        }
        ItemStack key = plugin.getKeyManager().createKey(crate.getName(), amount);
        target.getInventory().addItem(key);
        sender.sendMessage("§aSuccess! Gave " + target.getName() + " " + amount + "x " + crate.getName() + " key(s).");
        target.sendMessage("§aYou have received " + amount + "x " + crate.getName() + " key(s).");
        
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        if (playerData != null && !playerData.hasReceivedFirstKey()) {
            List<String> welcomeMessage = plugin.getConfig().getStringList("messages.first-key-welcome");
            for (String line : welcomeMessage) {
                String formattedLine = ChatColor.translateAlternateColorCodes('&', line)
                        .replace("{player}", target.getName());
                target.sendMessage(formattedLine);
            }
            playerData.setHasReceivedFirstKey(true);
        }
    }

    private void handleViewCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be run by a player.");
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("gacha.user.view")) {
            player.sendMessage("§cYou do not have permission to view crates.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /gacha view <crateName>");
            return;
        }
        String crateName = args[1];
        Crate crate = plugin.getCrateManager().getCrate(crateName);
        if (crate == null) {
            player.sendMessage("§cError: Crate '" + crateName + "' not found.");
            return;
        }
        new CratePreviewMenu(player, crate).open();
    }

    private void handleStatsCommand(CommandSender sender, String[] args) {
        Player target;
        if (args.length > 1) {
            if (!sender.hasPermission("gacha.admin.stats.other")) {
                sender.sendMessage("§cYou don't have permission to view other players' stats.");
                return;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer '" + args[1] + "' not found.");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cPlease specify a player to view stats.");
                return;
            }
            if (!sender.hasPermission("gacha.user.stats.self")) {
                sender.sendMessage("§cYou don't have permission to view your stats.");
                return;
            }
            target = (Player) sender;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage("§cCould not load data for that player.");
            return;
        }
        sender.sendMessage("§a§l--- Crate Stats for " + target.getName() + " ---");
        if (data.getCratesOpenedMap().isEmpty()) {
            sender.sendMessage("§7No crates opened yet.");
        } else {
            data.getCratesOpenedMap().forEach((crateName, count) -> {
                sender.sendMessage("§e" + crateName + ": §f" + count + " opened");
            });
        }
    }

    private void handleSetBlock(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be run by a player.");
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("gacha.admin.setblock")) {
            player.sendMessage("§cYou don't have permission to do that.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /gacha setblock <crateName>");
            return;
        }
        String crateName = args[1];
        if (plugin.getCrateManager().getCrate(crateName) == null) {
            player.sendMessage("§cError: Crate '" + crateName + "' does not exist.");
            return;
        }
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage("§cYou must be looking at a block within 5 blocks of you.");
            return;
        }
        plugin.getPhysicalCrateManager().addCrate(targetBlock.getLocation(), crateName);
        player.sendMessage("§aSuccess! This block is now a physical '" + crateName + "' crate.");
    }

    private void handleRemoveBlock(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be run by a player.");
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("gacha.admin.setblock")) {
            player.sendMessage("§cYou don't have permission to do that.");
            return;
        }
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock == null || !plugin.getPhysicalCrateManager().isCrateLocation(targetBlock.getLocation())) {
            player.sendMessage("§cYou are not looking at a physical crate block.");
            return;
        }
        plugin.getPhysicalCrateManager().removeCrate(targetBlock.getLocation());
        player.sendMessage("§aSuccess! This block is no longer a physical crate.");
    }
            }
            
