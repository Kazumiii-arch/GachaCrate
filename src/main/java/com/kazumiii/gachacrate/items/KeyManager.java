// ========================================================================
// File: KeyManager.java
// Location: /src/main/java/com/kazumiii/gachacrate/items/KeyManager.java
// ========================================================================
package com.kazumiii.gachacrate.items;

import com.kazumiii.gachacrate.GachaCrate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Locale;

public class KeyManager {

    private final NamespacedKey keyIdKey;
    private final GachaCrate plugin;

    // Settings loaded from config
    private Material keyMaterial;
    private boolean isGlowing;

    public KeyManager(JavaPlugin plugin) {
        this.plugin = (GachaCrate) plugin;
        this.keyIdKey = new NamespacedKey(plugin, "gacha_crate_key_id");
        loadKeySettings();
    }

    /**
     * Loads key appearance settings from the config.yml.
     */
    private void loadKeySettings() {
        String materialName = plugin.getConfig().getString("key-appearance.material", "TRIPWIRE_HOOK");
        this.keyMaterial = Material.matchMaterial(materialName);

        // If the material in the config is invalid, log an error and use a default.
        if (this.keyMaterial == null) {
            plugin.getLogger().warning("Invalid key material '" + materialName + "' in config.yml. Defaulting to TRIPWIRE_HOOK.");
            this.keyMaterial = Material.TRIPWIRE_HOOK;
        }

        this.isGlowing = plugin.getConfig().getBoolean("key-appearance.glowing", true);
    }

    public ItemStack createKey(String crateName, int amount) {
        // Use the material loaded from the config
        ItemStack key = new ItemStack(this.keyMaterial, amount);
        ItemMeta meta = key.getItemMeta();

        if (meta != null) {
            String displayName = crateName == null ? "Gacha" : crateName;
            meta.setDisplayName("§6" + displayName + " Gacha Key");
            meta.setLore(Arrays.asList(
                "§7A special key for the " + displayName + " crate.",
                "",
                "§e❯ Right-click while holding this key",
                "§e   to open the crate!"
            ));

            // Add the glowing effect if enabled in the config.
            // Enchantment.LUCK is the 1.20.1 constant (LUCK_OF_THE_SEA only exists in 1.20.5+).
            if (this.isGlowing) {
                meta.addEnchant(Enchantment.LUCK, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            // Store the crate name in the item's data container (lowercase = registry key)
            meta.getPersistentDataContainer().set(keyIdKey, PersistentDataType.STRING,
                    displayName.toLowerCase(Locale.ROOT));
            key.setItemMeta(meta);
        }
        return key;
    }

    public String getCrateFromKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(keyIdKey, PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(keyIdKey, PersistentDataType.STRING);
        }
        return null;
    }
}
