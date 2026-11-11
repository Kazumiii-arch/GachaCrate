package com.kazumiii.gachacrate.animations;

import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.guis.CrateSettingsMenu;
import com.kazumiii.gachacrate.guis.Menu;
import com.kazumiii.gachacrate.util.Compat;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pick the particle effect that swirls around this crate's blocks.
 * Only particles that exist on the running server version are shown.
 */
public class IdleParticleMenu extends Menu {

    /** Particle name (old or new spelling) -> icon. */
    private static final Map<String, Material> CHOICES = new LinkedHashMap<>();

    static {
        CHOICES.put("ENCHANT", Material.ENCHANTING_TABLE);
        CHOICES.put("TOTEM_OF_UNDYING", Material.TOTEM_OF_UNDYING);
        CHOICES.put("HAPPY_VILLAGER", Material.EMERALD);
        CHOICES.put("PORTAL", Material.OBSIDIAN);
        CHOICES.put("FLAME", Material.BLAZE_POWDER);
        CHOICES.put("SOUL_FIRE_FLAME", Material.SOUL_TORCH);
        CHOICES.put("DRAGON_BREATH", Material.DRAGON_BREATH);
        CHOICES.put("HEART", Material.RED_DYE);
        CHOICES.put("NOTE", Material.NOTE_BLOCK);
        CHOICES.put("SOUL", Material.SOUL_SAND);
        CHOICES.put("END_ROD", Material.END_ROD);
        CHOICES.put("WITCH", Material.CAULDRON);
        CHOICES.put("GLOW", Material.GLOW_INK_SAC);
        CHOICES.put("ELECTRIC_SPARK", Material.LIGHTNING_ROD);
        CHOICES.put("WAX_ON", Material.HONEYCOMB);
        CHOICES.put("SPORE_BLOSSOM_AIR", Material.SPORE_BLOSSOM);
        CHOICES.put("CHERRY_LEAVES", Material.CHERRY_LEAVES);
        CHOICES.put("CRIT", Material.IRON_SWORD);
        CHOICES.put("FIREWORK", Material.FIREWORK_ROCKET);
        CHOICES.put("SNOWFLAKE", Material.SNOWBALL);
    }

    private final Crate crate;

    public IdleParticleMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    protected String getTitle() {
        return "§8Idle Particles: " + crate.getName();
    }

    @Override
    protected int getSize() {
        return 36;
    }

    @Override
    protected void draw() {
        fillRow(3, Material.BLACK_STAINED_GLASS_PANE);

        int slot = 0;
        for (Map.Entry<String, Material> choice : CHOICES.entrySet()) {
            Particle particle = Compat.particle(choice.getKey());
            if (particle == null || slot >= 27) continue;
            boolean selected = crate.getIdleParticle() == particle;
            List<String> lore = new ArrayList<>();
            lore.add(selected ? "&a✔ Selected" : "&e► Click to select");
            button(slot++, Items.of(choice.getValue()).name((selected ? "&a" : "&b") + pretty(particle.name()))
                    .lore(lore).glow(selected).build(), e -> {
                crate.setIdleParticle(particle);
                plugin().getCrateManager().saveCrates();
                Text.send(player, "&aIdle particle set to " + pretty(particle.name()) + ".");
                refresh();
            });
        }

        button(27, Items.of(Material.ARROW).name("&e« Back to Settings").build(),
                e -> new CrateSettingsMenu(player, crate).open());
        button(35, Items.of(Material.BARRIER).name("&cNo Particles")
                .lore(crate.getIdleParticle() == null ? "&a✔ Selected" : "&e► Click to turn off").build(), e -> {
            crate.setIdleParticle(null);
            plugin().getCrateManager().saveCrates();
            Text.send(player, "&aIdle particles turned off.");
            refresh();
        });
    }

    private static String pretty(String name) {
        String lower = name.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
