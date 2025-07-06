package com.kazumiii.gachacrate.conversation;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.guis.Menu;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DatePrompt extends StringPrompt {

    private final String setting;
    private final Crate crate;
    private final Menu parentMenu;
    
    public DatePrompt(String setting, Crate crate, Menu parentMenu) {
        this.setting = setting;
        this.crate = crate;
        this.parentMenu = parentMenu;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return "§eEnter the date for §f" + setting + "§e (YYYY-MM-DD), or type 'cancel'.";
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
            LocalDate date = LocalDate.parse(input);
            if ("start-date".equals(setting)) {
                crate.setStartDate(date);
            } else if ("end-date".equals(setting)) {
                crate.setEndDate(date);
            }

            GachaCrate.getInstance().getCrateManager().saveCrates();
            player.sendMessage("§aDate updated successfully!");
            parentMenu.open(); // Re-open the previous menu
            return END_OF_CONVERSATION;
        } catch (DateTimeParseException e) {
            player.sendMessage("§cInvalid date format! Use YYYY-MM-DD. Please try again.");
            return this;
        }
    }
}
