// ========================================================================
// File: IdleParticleMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/animations/IdleParticleMenu.java
// ========================================================================
package com.kazumiii.gachacrate.animations;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.guis.CrateSettingsMenu;
import com.kazumiii.gachacrate.guis.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class IdleParticleMenu extends Menu {

    private final Crate crate;
    // An expanded list of attractive particles for more customization
    private final List<Particle> availableParticles = Arrays.asList(
            Particle.ENCHANTMENT_TABLE, Particle.TOTEM, Particle.VILLAGER_HAPPY,
            Particle.PORTAL, Particle.FLAME, Particle.SOUL_FIRE_FLAME,
            Particle.DRAGON_BREATH, Particle.HEART, Particle.NOTE, Particle.SOUL,
            Particle.SPORE_BLOSSOM_AIR, Particle.CHERRY_LEAVES
    );

    public IdleParticleMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    public void open() {
        // Increased size to 36 to hold more options
        inventory = Bukkit.createInventory(this, 36, "§8Idle Particle Effect");

        // --- Particle Selection ---
        for (int i = 0; i < availableParticles.size(); i++) {
            if (i >= 27) break; // Prevent overflow, fits 3 rows of particles
            
            Particle particle = availableParticles.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + particle.name());

            if (crate.getIdleParticle() == particle) {
                meta.addEnchant(Enchantment.LUCK, 1, false);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.setLore(Arrays.asList(ChatColor.GREEN + "Currently Selected"));
            } else {
                meta.setLore(Arrays.asList(ChatColor.YELLOW + "► Click to select"));
            }
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }

        // --- Control Buttons ---
        ItemStack disableButton = new ItemStack(Material.BARRIER);
        ItemMeta disableMeta = disableButton.getItemMeta();
        disableMeta.setDisplayName(ChatColor.RED + "Disable Effect");
        disableButton.setItemMeta(disableMeta);

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "« Back to Settings");
        backButton.setItemMeta(backMeta);

        inventory.setItem(27, backButton);
        inventory.setItem(35, disableButton);

        player.openInventory(inventory);
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Material clickedType = event.getCurrentItem().getType();
        if (clickedType == Material.ARROW) {
            new CrateSettingsMenu(player, crate).open();
            return;
        }
        if (clickedType == Material.BARRIER) {
            crate.setIdleParticle(null);
            GachaCrate.getInstance().getCrateManager().saveCrates();
            player.sendMessage(ChatColor.GREEN + "Idle particle effect disabled for this crate.");
            open();
            return;
        }
        
        if (clickedType == Material.PAPER) {
            try {
                String particleName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                Particle selectedParticle = Particle.valueOf(particleName);
                crate.setIdleParticle(selectedParticle);
                GachaCrate.getInstance().getCrateManager().saveCrates();
                player.sendMessage(ChatColor.GREEN + "Idle particle set to " + particleName + "!");
                open(); // Refresh the menu to show the selection
            } catch (Exception e) {
                // Clicked on something else, do nothing
            }
        }
    }
    }
