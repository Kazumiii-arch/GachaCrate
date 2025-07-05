// ========================================================================
// File: KeyManager.java
// Location: /src/main/java/com/kazumiii/gachacrate/items/KeyManager.java
// ========================================================================
package com.kazumiii.gachacrate.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Collections;

public class KeyManager {

    private final NamespacedKey keyIdKey;

    public KeyManager(JavaPlugin plugin) {
        this.keyIdKey = new NamespacedKey(plugin, "gacha_crate_key_id");
    }

    public ItemStack createKey(String crateName, int amount) {
        ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK, amount);
        ItemMeta meta = key.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + crateName + " Gacha Key");
            meta.setLore(Collections.singletonList("§7Use this to open a " + crateName + " crate!"));
            // Store the crate name in the item's persistent data container
            meta.getPersistentDataContainer().set(keyIdKey, PersistentDataType.STRING, crateName.toLowerCase());
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

