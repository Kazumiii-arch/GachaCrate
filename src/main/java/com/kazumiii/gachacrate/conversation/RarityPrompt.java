// ========================================================================
// File: RarityPrompt.java
// Location: /src/main/java/com/kazumiii/gachacrate/conversation/RarityPrompt.java
// ========================================================================
package com.kazumiii.gachacrate.conversation;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.guis.RewardEditorMenu;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class RarityPrompt extends StringPrompt {

    private final Crate crate;
    private final Reward reward;

    public RarityPrompt(Crate crate, Reward reward) {
        this.crate = crate;
        this.reward = reward;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Player player = (Player) context.getForWhom();
        player.sendMessage("§aAvailable Rarities: §f" + GachaCrate.getInstance().getRarityManager().getRarityIds());
        return "§ePlease type the new rarity ID in chat, or type 'cancel' to abort.";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cOperation cancelled.");
            new RewardEditorMenu(player, crate, reward).open();
            return END_OF_CONVERSATION;
        }

        Rarity newRarity = GachaCrate.getInstance().getRarityManager().getRarity(input);
        if (newRarity == null) {
            player.sendMessage("§cInvalid rarity! Please try again.");
            // We return "this" to re-ask the same prompt.
            return this;
        }

        reward.setRarity(newRarity);
        GachaCrate.getInstance().getCrateManager().saveCrates();
        player.sendMessage("§aRarity updated successfully!");
        
        // Re-open the menu to show the change
        new RewardEditorMenu(player, crate, reward).open();
        return END_OF_CONVERSATION;
    }
}
