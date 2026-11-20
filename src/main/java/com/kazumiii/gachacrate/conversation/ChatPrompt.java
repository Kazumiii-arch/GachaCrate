package com.kazumiii.gachacrate.conversation;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.util.function.Function;

/**
 * Asks a player to type something in chat (used by the GUI editors).
 * <p>
 * The answer goes to {@code handler}. If the handler returns an error message, the player is
 * asked again; if it returns null, the input was accepted. Typing "cancel" or waiting
 * 60 seconds stops the prompt. {@code afterwards} always runs at the end (usually re-opens a menu).
 */
public final class ChatPrompt {

    private ChatPrompt() {
    }

    public static void ask(Player player, String question, Function<String, String> handler, Runnable afterwards) {
        player.closeInventory();

        StringPrompt prompt = new StringPrompt() {
            @Override
            public String getPromptText(ConversationContext context) {
                return Text.prefix() + Text.color(question) + Text.color(" &7(type &ccancel &7to stop)");
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                if (input == null) return this;
                String trimmed = input.trim();
                if (trimmed.equalsIgnoreCase("cancel")) {
                    Text.send(player, "&cCancelled.");
                    return END_OF_CONVERSATION;
                }
                String error = handler.apply(trimmed);
                if (error != null) {
                    Text.send(player, "&c" + error);
                    return this;
                }
                return END_OF_CONVERSATION;
            }
        };

        new ConversationFactory(GachaCrate.getInstance())
                .withFirstPrompt(prompt)
                .withLocalEcho(false)
                .withModality(true)
                .withTimeout(60)
                .addConversationAbandonedListener((ConversationAbandonedEvent event) -> {
                    if (!event.gracefulExit()) Text.send(player, "&cThe prompt timed out.");
                    if (afterwards != null && player.isOnline()) afterwards.run();
                })
                .buildConversation(player)
                .begin();
    }
}
