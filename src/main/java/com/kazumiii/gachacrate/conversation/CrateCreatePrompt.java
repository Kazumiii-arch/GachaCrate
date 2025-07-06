package com.kazumiii.gachacrate.conversation;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.guis.MainMenu;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class CrateCreatePrompt extends StringPrompt {

    @Override
    public String getPromptText(ConversationContext context) {
        return "§ePlease type the name for the new crate in chat, or type 'cancel' to abort.";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();

        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cOperation cancelled.");
            new MainMenu(player).open(); // Re-open the main menu
            return END_OF_CONVERSATION;
        }

        // Check if a crate with this name already exists
        if (GachaCrate.getInstance().getCrateManager().getCrate(input) != null) {
            player.sendMessage("§cError: A crate with the name '" + input + "' already exists. Please try another name.");
            // Re-ask the same prompt
            return this;
        }

        // Create the crate
        GachaCrate.getInstance().getCrateManager().createCrate(input);
        player.sendMessage("§aSuccess! Crate '" + input + "' has been created.");
        
        // Re-open the main menu to show the new crate
        new MainMenu(player).open();
        return END_OF_CONVERSATION;
    }
}
