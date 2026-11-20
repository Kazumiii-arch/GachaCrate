package com.kazumiii.gachacrate.items;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.OpeningMode;
import com.kazumiii.gachacrate.util.Compat;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates crate keys and recognizes them. The crate id is stored inside the item,
 * so renaming the item or changing the key material doesn't break existing keys.
 */
public class KeyManager {

    private final GachaCrate plugin;
    private final NamespacedKey keyIdKey;
    private Material keyMaterial;
    private boolean glowing;

    public KeyManager(GachaCrate plugin) {
        this.plugin = plugin;
        this.keyIdKey = new NamespacedKey(plugin, "gacha_crate_key_id");
        reload();
    }

    public void reload() {
        String materialName = plugin.getConfig().getString("key-appearance.material", "TRIPWIRE_HOOK");
        Material material = Material.matchMaterial(materialName == null ? "" : materialName);
        if (material == null || !material.isItem() || material == Material.AIR) {
            plugin.getLogger().warning("Invalid key material '" + materialName + "' in config.yml. Using TRIPWIRE_HOOK.");
            material = Material.TRIPWIRE_HOOK;
        }
        this.keyMaterial = material;
        this.glowing = plugin.getConfig().getBoolean("key-appearance.glowing", true);
    }

    public ItemStack createKey(Crate crate, int amount) {
        ItemStack key = new ItemStack(keyMaterial, Math.max(1, Math.min(amount, keyMaterial.getMaxStackSize())));
        ItemMeta meta = key.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(crate.getDisplayName() + Text.color(" &6Key"));
            List<String> lore = new ArrayList<>();
            lore.add(Text.color("&7A key for the " + crate.getDisplayName() + "&7 crate."));
            lore.add("");
            if (crate.getOpeningMode() == OpeningMode.KEY_IN_HAND) {
                lore.add(Text.color("&e► Right-click while holding it to open!"));
            } else {
                lore.add(Text.color("&e► Right-click the crate block"));
                lore.add(Text.color("&e   while holding it to open!"));
            }
            if (plugin.getConfig().getBoolean("opening.mass-open.enabled", true)) {
                lore.add(Text.color("&7Sneak + right-click to open them all."));
            }
            meta.setLore(lore);
            if (glowing) Compat.setGlow(meta, true);
            meta.getPersistentDataContainer().set(keyIdKey, PersistentDataType.STRING, crate.getId());
            key.setItemMeta(meta);
        }
        return key;
    }

    /**
     * Gives keys to a player. Large amounts are split into stacks; anything that doesn't fit
     * is dropped at the player's feet.
     */
    public void giveKeys(Player player, Crate crate, int amount) {
        int maxStack = keyMaterial.getMaxStackSize();
        int remaining = amount;
        while (remaining > 0) {
            int stack = Math.min(remaining, maxStack);
            remaining -= stack;
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(createKey(crate, stack));
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
    }

    /** The crate id stored in a key, or null if the item isn't a key. */
    public String getCrateFromKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(keyIdKey, PersistentDataType.STRING);
    }

    public boolean isKey(ItemStack item) {
        return getCrateFromKey(item) != null;
    }

    /** Counts a player's keys per crate (for /gacha keys). */
    public Map<String, Integer> countKeys(Player player) {
        Map<String, Integer> counts = new HashMap<>();
        for (ItemStack item : player.getInventory().getContents()) {
            String crate = getCrateFromKey(item);
            if (crate != null) counts.merge(crate, item.getAmount(), Integer::sum);
        }
        return counts;
    }
}
