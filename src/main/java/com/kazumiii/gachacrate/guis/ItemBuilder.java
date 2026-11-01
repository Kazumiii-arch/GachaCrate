// ========================================================================
// File: ItemBuilder.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/ItemBuilder.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent builder for GUI items so every menu shares one visual language:
 *  - colored display names
 *  - clean lore with blank spacer lines
 *  - optional enchant glow for "selected" states
 *  - hidden attribute clutter
 */
public class ItemBuilder {

    private final ItemStack item;
    private final List<String> lore = new ArrayList<>();
    private String name;
    private boolean glow;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    public ItemBuilder(ItemStack item) {
        this.item = item;
    }

    public ItemBuilder name(String name) {
        this.name = name;
        return this;
    }

    /** Appends one lore line. */
    public ItemBuilder lore(String line) {
        this.lore.add(line);
        return this;
    }

    /** Appends a blank spacer line. */
    public ItemBuilder blank() {
        this.lore.add("");
        return this;
    }

    /** Appends several lore lines. */
    public ItemBuilder loreAll(List<String> lines) {
        this.lore.addAll(lines);
        return this;
    }

    public ItemBuilder loreAll(String... lines) {
        this.lore.addAll(Arrays.asList(lines));
        return this;
    }

    /** Adds the hidden enchant glow (used for selected/current options). */
    public ItemBuilder glow() {
        this.glow = true;
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, amount));
        return this;
    }

    public ItemStack build() {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(name);
            }
            if (!lore.isEmpty()) {
                meta.setLore(new ArrayList<>(lore));
            }
            if (glow) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ------------------------------------------------------------------
    // Shared design constants — one place to restyle the whole plugin UI.
    // ------------------------------------------------------------------

    /** Border pane used by all menus. */
    public static ItemStack border() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();
    }

    /** Darker filler pane for inner areas. */
    public static ItemStack filler() {
        return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ")
                .build();
    }

    /** Standard back button, bottom-left convention. */
    public static ItemStack backButton(String target) {
        return new ItemBuilder(Material.ARROW)
                .name(ChatColor.RED + "❯ " + ChatColor.YELLOW + "Back" + ChatColor.GRAY + " — " + target)
                .build();
    }

    /** Standard confirm button, bottom-right convention. */
    public static ItemStack confirmButton(String label) {
        return new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .name(ChatColor.GREEN + "" + ChatColor.BOLD + "✔ " + label)
                .build();
    }

    /** Standard cancel button. */
    public static ItemStack cancelButton() {
        return new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .name(ChatColor.RED + "" + ChatColor.BOLD + "✖ Cancel")
                .build();
    }

    /** Fills the border ring of an inventory with the shared border pane. */
    public static void drawBorder(org.bukkit.inventory.Inventory inventory) {
        int size = inventory.getSize();
        ItemStack pane = border();
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == size / 9 - 1 || col == 0 || col == 8) {
                inventory.setItem(i, pane);
            }
        }
    }

    /** Fills all empty slots with the dark filler pane. */
    public static void fillBackground(org.bukkit.inventory.Inventory inventory) {
        ItemStack pane = filler();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, pane);
            }
        }
    }
}
