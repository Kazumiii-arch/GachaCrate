// ========================================================================
// File: IdleParticleMenu.java
// Location: /src/main/java/com/kazumiii/gachacrate/animations/IdleParticleMenu.java
// ========================================================================
package com.kazumiii.gachacrate.animations;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.guis.CrateSettingsMenu;
import com.kazumiii.gachacrate.guis.ItemBuilder;
import com.kazumiii.gachacrate.guis.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Idle particle effect picker with selected-state glow.
 */
public class IdleParticleMenu extends Menu {

    private final Crate crate;
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
        inventory = Bukkit.createInventory(this, 36, "§8Idle Particle: " + crate.getDisplayName());
        ItemBuilder.drawBorder(inventory);

        // Particle grid (interior slots only)
        int slot = 10;
        for (Particle particle : availableParticles) {
            if (slot > 33) break;
            if (slot % 9 == 0 || slot % 9 == 8) slot++; // skip border columns

            boolean selected = crate.getIdleParticle() == particle;
            inventory.setItem(slot, new ItemBuilder(Material.PAPER)
                    .name(ChatColor.AQUA + prettyName(particle))
                    .blank()
                    .lore(selected
                            ? ChatColor.GREEN + "✔ Currently selected"
                            : ChatColor.YELLOW + "❯ Click to select")
                    .glowIf(selected)
                    .build());
            slot++;
        }

        // Controls
        inventory.setItem(27, ItemBuilder.backButton("Settings"));
        inventory.setItem(31, new ItemBuilder(Material.BARRIER)
                .name(ChatColor.RED + "Disable Effect")
                .blank()
                .lore(ChatColor.GRAY + "Turn off the idle particle.")
                .build());

        ItemBuilder.fillBackground(inventory);

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
            String particleName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            try {
                Particle selectedParticle = Particle.valueOf(particleName);
                crate.setIdleParticle(selectedParticle);
                GachaCrate.getInstance().getCrateManager().saveCrates();
                player.sendMessage(ChatColor.GREEN + "Idle particle set to " + particleName + "!");
                open();
            } catch (IllegalArgumentException ignored) {
                // Not one of ours — ignore
            }
        }
    }

    /** "ENCHANTMENT_TABLE" -> "Enchantment Table" */
    private String prettyName(Particle particle) {
        String[] words = particle.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(words[i].charAt(0)).append(words[i].substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
