package com.kazumiii.gachacrate.conversation;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.guis.Menu;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class ChancePrompt extends StringPrompt {

    private final Crate crate;
    private final Reward reward;
    private final Menu parentMenu;


    public ChancePrompt(Crate crate, Reward reward, Menu parentMenu) {
        this.crate = crate;
        this.reward = reward;
        this.parentMenu = parentMenu;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return "§ePlease type the new drop chance in chat (e.g., 25.5), or type 'cancel' to abort.";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cOperation cancelled.");
            parentMenu.open(); // Use the parentMenu to re-open
            return END_OF_CONVERSATION;
        }

        try {
            double newChance = Double.parseDouble(input);
            if (newChance <= 0) {
                player.sendMessage("§cChance must be a positive number. Please try again.");
                return this;
            }
            reward.setChance(newChance);
            GachaCrate.getInstance().getCrateManager().saveCrates();
            player.sendMessage("§aChance updated successfully!");
            
            parentMenu.open(); // Re-open the previous menu
            return END_OF_CONVERSATION;
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid number. Please try again.");
            return this;
        }
    }
}
