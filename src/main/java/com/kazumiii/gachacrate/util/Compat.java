package com.kazumiii.gachacrate.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Keeps the plugin working across Minecraft versions (1.20 - 1.21+).
 * Some enchantment and particle names changed in 1.20.5, so they are looked up
 * by name instead of through fields that may not exist on the running server.
 */
public final class Compat {

    /** Old particle name -> new name (1.20.5 renamed many particles). Also used the other way round. */
    private static final Map<String, String> PARTICLE_ALIASES = new HashMap<>();

    static {
        alias("ENCHANTMENT_TABLE", "ENCHANT");
        alias("TOTEM", "TOTEM_OF_UNDYING");
        alias("VILLAGER_HAPPY", "HAPPY_VILLAGER");
        alias("VILLAGER_ANGRY", "ANGRY_VILLAGER");
        alias("SPELL_WITCH", "WITCH");
        alias("FIREWORKS_SPARK", "FIREWORK");
        alias("SNOW_SHOVEL", "ITEM_SNOWBALL");
        alias("REDSTONE", "DUST");
        alias("SMOKE_NORMAL", "SMOKE");
        alias("WATER_DROP", "RAIN");
    }

    private static Enchantment glowEnchant;
    private static boolean glowLookupDone;

    private Compat() {
    }

    private static void alias(String oldName, String newName) {
        PARTICLE_ALIASES.put(oldName, newName);
        PARTICLE_ALIASES.put(newName, oldName);
    }

    /**
     * Finds a particle by its old or new name. Returns null if this server has neither,
     * or if the particle needs extra data (those can't be used as simple idle effects).
     */
    public static Particle particle(String name) {
        if (name == null || name.isEmpty()) return null;
        String upper = name.toUpperCase(Locale.ROOT);
        Particle particle = particleExact(upper);
        if (particle == null && PARTICLE_ALIASES.containsKey(upper)) {
            particle = particleExact(PARTICLE_ALIASES.get(upper));
        }
        if (particle != null && particle.getDataType() != Void.class) return null;
        return particle;
    }

    private static Particle particleExact(String name) {
        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** The harmless enchantment used to make items glow (Luck of the Sea). */
    public static Enchantment glowEnchantment() {
        if (!glowLookupDone) {
            glowLookupDone = true;
            try {
                glowEnchant = Enchantment.getByKey(NamespacedKey.minecraft("luck_of_the_sea"));
            } catch (Throwable ignored) {
                glowEnchant = null;
            }
        }
        return glowEnchant;
    }

    public static void setGlow(ItemMeta meta, boolean glow) {
        Enchantment enchant = glowEnchantment();
        if (meta == null || enchant == null) return;
        if (glow) {
            meta.addEnchant(enchant, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeEnchant(enchant);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }

    public static boolean hasGlow(ItemMeta meta) {
        Enchantment enchant = glowEnchantment();
        return meta != null && enchant != null && meta.hasEnchant(enchant) && meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
    }
}
