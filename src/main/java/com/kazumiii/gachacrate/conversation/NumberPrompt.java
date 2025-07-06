package com.kazumiii.gachacrate.conversation;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.guis.Menu;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class NumberPrompt extends StringPrompt {

    private final String setting;
    private final Crate crate;
    private final Menu parentMenu;


    public NumberPrompt(String setting, Crate crate, Menu parentMenu) {
        this.setting = setting;
        this.crate = crate;
        this.parentMenu = parentMenu;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return "§ePlease type the new value for §f" + setting + "§e in chat, or type 'cancel'.";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cOperation cancelled.");
            parentMenu.open();
            return END_OF_CONVERSATION;
        }

        try {
            int value = Integer.parseInt(input);
            if (value <= 0) {
                player.sendMessage("§cValue must be a positive number. Please try again.");
                return this;
            }

            if ("pity-threshold".equals(setting)) {
                crate.setPityThreshold(value);
            }
            // This can be expanded with else-if for other future integer settings.
            
            GachaCrate.getInstance().getCrateManager().saveCrates();
            player.sendMessage("§aSetting updated successfully!");
            parentMenu.open(); // Re-open the previous menu
            return END_OF_CONVERSATION;
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid number. Please try again.");
            return this;
        }
    }
}
