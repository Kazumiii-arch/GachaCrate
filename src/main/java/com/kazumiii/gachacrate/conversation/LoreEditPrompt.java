package com.kazumiii.gachacrate.conversation;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.guis.LoreEditorMenu;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LoreEditPrompt extends StringPrompt {

    private final Crate crate;
    private final Reward reward;
    private final LoreEditorMenu parentMenu;

    public LoreEditPrompt(Crate crate, Reward reward, LoreEditorMenu parentMenu) {
        this.crate = crate;
        this.reward = reward;
        this.parentMenu = parentMenu;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return "§ePlease type the new line of lore in chat. Use '&' for color codes, or type 'cancel' to abort.";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cOperation cancelled.");
            parentMenu.open();
            return END_OF_CONVERSATION;
        }

        ItemStack item = reward.getItem();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // Add the new line with color codes translated
        lore.add(ChatColor.translateAlternateColorCodes('&', input));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        GachaCrate.getInstance().getCrateManager().saveCrates();
        player.sendMessage("§aLore line added successfully!");
        
        parentMenu.open(); // Re-open the lore editor
        return END_OF_CONVERSATION;
    }
}
