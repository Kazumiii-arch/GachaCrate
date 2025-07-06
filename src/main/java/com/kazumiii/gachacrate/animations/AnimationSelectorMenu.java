package com.kazumiii.gachacrate.animations;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.guis.CrateSettingsMenu;
import com.kazumiii.gachacrate.guis.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class AnimationSelectorMenu extends Menu {

    private final Crate crate;

    public AnimationSelectorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Select Animation Style");

        for (AnimationType type : AnimationType.values()) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + type.getDisplayName());
            
            
            if (crate.getAnimationType() == type) {
                meta.addEnchant(Enchantment.LUCK, 1, false);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.setLore(Collections.singletonList(ChatColor.GREEN + "Currently Selected"));
            } else {
                meta.setLore(Collections.singletonList(ChatColor.YELLOW + "► Click to select"));
            }
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "« Back to Settings");
        backButton.setItemMeta(backMeta);
        inventory.setItem(18, backButton);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        if (event.getCurrentItem().getType() == Material.ARROW) {
            new CrateSettingsMenu(player, crate).open();
            return;
        }

        try {
            String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            AnimationType selectedType = AnimationType.valueOf(displayName.toUpperCase().replace(" ", "_"));
            
            crate.setAnimationType(selectedType);
            GachaCrate.getInstance().getCrateManager().saveCrates();
            player.sendMessage(ChatColor.GREEN + "Animation style set to " + selectedType.getDisplayName() + "!");
            open();
        } catch (Exception e) {
    }
}
