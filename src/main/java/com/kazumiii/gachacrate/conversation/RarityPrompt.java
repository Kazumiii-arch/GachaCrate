package com.kazumiii.gachacrate.conversation;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Rarity;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.guis.Menu;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class RarityPrompt extends StringPrompt {

    private final String setting;
    private final Crate crate;
    private final Reward reward; // Can be null if not editing a specific reward
    private final Menu parentMenu;
    
    public RarityPrompt(Crate crate, Reward reward, Menu parentMenu) {
        this.setting = "reward-rarity";
        this.crate = crate;
        this.reward = reward;
        this.parentMenu = parentMenu;
    }

    public RarityPrompt(String setting, Crate crate, Menu parentMenu) {
        this.setting = setting;
        this.crate = crate;
        this.reward = null; // Not editing a specific reward
        this.parentMenu = parentMenu;
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
            parentMenu.open();
            return END_OF_CONVERSATION;
        }

        Rarity newRarity = GachaCrate.getInstance().getRarityManager().getRarity(input);
        if (newRarity == null) {
            player.sendMessage("§cInvalid rarity! Please try again.");
            return this; // Re-ask the same prompt
        }

        if ("reward-rarity".equals(setting) && reward != null) {
            reward.setRarity(newRarity);
        } else if ("pity-rarity".equals(setting)) {
            crate.setPityRarityMinimum(newRarity.getId());
        }
        
        GachaCrate.getInstance().getCrateManager().saveCrates();
        player.sendMessage("§aRarity updated successfully!");
        parentMenu.open(); // Re-open the previous menu
        return END_OF_CONVERSATION;
    }
}
