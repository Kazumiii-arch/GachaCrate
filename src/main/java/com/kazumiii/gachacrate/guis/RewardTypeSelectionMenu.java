package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.conversation.ChatPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Choose between adding an item reward or a command reward.
 */
public class RewardTypeSelectionMenu extends Menu {

    private final Crate crate;

    public RewardTypeSelectionMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    protected String getTitle() {
        return "§8Add Reward: " + crate.getName();
    }

    @Override
    protected int getSize() {
        return 27;
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);

        button(11, Items.of(Material.DIAMOND_SWORD).name("&bItem Reward")
                .lore("&7Give an item from your inventory.").build(),
                e -> new AddItemMenu(player, crate).open());

        button(13, Items.of(Material.HOPPER).name("&bAdd Item in Hand")
                .lore("&7Quickly add the item you're holding", "&7(common rarity, weight 10).", "",
                        "&7Edit it afterwards to change", "&7the rarity and weight.").build(), e -> {
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                Text.send(player, "&cHold the item you want to add first.");
                return;
            }
            Reward reward = new Reward(player.getInventory().getItemInMainHand().clone(),
                    plugin().getRarityManager().getLowest(), 10);
            crate.addReward(reward);
            plugin().getCrateManager().saveCrates();
            Text.send(player, "&aItem in hand added. You can now set its rarity and weight.");
            new RewardEditorMenu(player, crate, reward).open();
        });

        button(15, Items.of(Material.COMMAND_BLOCK).name("&dCommand Reward")
                .lore("&7Run console commands as a reward,", "&7e.g. give money or a rank.").build(),
                e -> askCommands());

        button(18, Items.of(Material.ARROW).name("&e« Back").build(),
                e -> new CrateEditorMenu(player, crate).open());
    }

    private void askCommands() {
        final Reward[] created = {null};
        ChatPrompt.ask(player, "&eType the command without '/'. Use &f{player} &efor the winner's name. "
                        + "Separate several commands with &f|&e. Example: &feco give {player} 500 | say {player} got rich!",
                input -> {
                    List<String> commands = splitCommands(input);
                    if (commands.isEmpty()) return "Please type at least one command.";
                    Reward reward = new Reward(commands, plugin().getRarityManager().getLowest(), 10);
                    crate.addReward(reward);
                    plugin().getCrateManager().saveCrates();
                    Text.send(player, "&aCommand reward added. Set its rarity, weight and icon next.");
                    created[0] = reward;
                    return null;
                },
                () -> {
                    if (created[0] != null) new RewardEditorMenu(player, crate, created[0]).open();
                    else open();
                });
    }

    /** Splits "cmd one | cmd two" into separate commands. */
    public static List<String> splitCommands(String input) {
        List<String> commands = new ArrayList<>();
        for (String part : input.split("\\|")) {
            String clean = Reward.cleanCommand(part);
            if (!clean.isEmpty()) commands.add(clean);
        }
        return commands;
    }
}
