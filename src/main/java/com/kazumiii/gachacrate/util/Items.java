package com.kazumiii.gachacrate.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent builder for GUI items.
 */
public final class Items {

    private final ItemStack item;
    private final ItemMeta meta;

    private Items(ItemStack item) {
        this.item = item;
        ItemMeta existing = item.getItemMeta();
        this.meta = existing != null ? existing : Bukkit.getItemFactory().getItemMeta(item.getType());
    }

    public static Items of(Material material) {
        return new Items(new ItemStack(material));
    }

    /** Starts from a copy of an existing item. */
    public static Items copy(ItemStack base) {
        return new Items(base == null ? new ItemStack(Material.BARRIER) : base.clone());
    }

    public static ItemStack filler(Material pane) {
        return of(pane).name(" ").build();
    }

    public Items name(String name) {
        if (meta != null) meta.setDisplayName(Text.color(name));
        return this;
    }

    public Items lore(String... lines) {
        return lore(Arrays.asList(lines));
    }

    public Items lore(List<String> lines) {
        if (meta != null) meta.setLore(Text.color(lines));
        return this;
    }

    /** Adds lines below any lore the item already has. */
    public Items addLore(List<String> lines) {
        if (meta == null) return this;
        List<String> lore = meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.addAll(Text.color(lines));
        meta.setLore(lore);
        return this;
    }

    public Items glow(boolean glow) {
        if (glow) Compat.setGlow(meta, true);
        return this;
    }

    public Items amount(int amount) {
        item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    public Items hideAll() {
        if (meta != null) meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemStack build() {
        if (meta != null) item.setItemMeta(meta);
        return item;
    }
}
