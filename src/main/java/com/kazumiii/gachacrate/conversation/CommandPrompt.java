// ========================================================================
// File: CommandPrompt.java
// Location: /src/main/java/com/kazumiii/gachacrate/conversation/CommandPrompt.java
// ========================================================================
package com.kazumiii.gachacrate.conversation;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.guis.CrateEditorMenu;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class CommandPrompt extends StringPrompt {

    private final Crate crate;

    public CommandPrompt(Crate crate) {
        this.crate = crate;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        // Use getAllSessionData() to get the map and then check the key
        if (!context.getAllSessionData().containsKey("command")) {
            return "§eEnter the command to run (without '/'). Use {player} as a placeholder. Type 'cancel' to abort.";
        }
        if (!context.getAllSessionData().containsKey("rarity")) {
            return "§eNow enter the rarity ID for this command reward.";
        }
        return "§eFinally, enter the drop chance for this command reward (e.g., 5.5).";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cOperation cancelled.");
            new CrateEditorMenu(player, crate).open();
            return END_OF_CONVERSATION;
        }

        // Use getAllSessionData() to get the map and then check the key
        if (!context.getAllSessionData().containsKey("command")) {
            context.setSessionData("command", input);
            return this; // Ask for rarity next
        }

        if (!context.getAllSessionData().containsKey("rarity")) {
            Rarity rarity = GachaCrate.getInstance().getRarityManager().getRarity(input);
            if (rarity == null) {
                player.sendMessage("§cInvalid rarity! Please try again. Available: " + GachaCrate.getInstance().getRarityManager().getRarityIds());
                return this;
            }
            context.setSessionData("rarity", rarity);
            return this; // Ask for chance next
        }

        try {
            double chance = Double.parseDouble(input);
            if (chance <= 0) {
                player.sendMessage("§cChance must be a positive number.");
                return this;
            }

            // All data collected, create the reward
            String command = (String) context.getSessionData("command");
            Rarity rarity = (Rarity) context.getSessionData("rarity");

            Reward newReward = new Reward(command, rarity, chance);
            crate.addReward(newReward);
            GachaCrate.getInstance().getCrateManager().saveCrates();

            player.sendMessage("§aCommand reward added successfully!");
            new CrateEditorMenu(player, crate).open();
            return END_OF_CONVERSATION;

        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid number. Please try again.");
            return this;
        }
    }
}
