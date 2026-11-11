package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Base class for every GUI.
 * <p>
 * Buttons are tied to slots (not to item types), clicks in the player's own inventory never
 * trigger buttons, and items can only be placed into slots a menu marks as "free".
 * This stops players from taking or duplicating GUI items.
 */
public abstract class Menu implements InventoryHolder {

    /** Menu each player currently has open, so /gacha reload can close them. */
    private static final Map<UUID, Menu> OPEN_MENUS = new HashMap<>();

    protected final Player player;
    protected Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();

    protected Menu(Player player) {
        this.player = player;
    }

    protected abstract String getTitle();

    /** Inventory size, a multiple of 9. */
    protected abstract int getSize();

    /** Puts the items and buttons into {@link #inventory}. */
    protected abstract void draw();

    /** Slots where the player may place and take their own items. */
    protected Set<Integer> getFreeSlots() {
        return Collections.emptySet();
    }

    /** Called when the menu is closed (by the player or by opening another menu). */
    protected void onClose() {
    }

    protected GachaCrate plugin() {
        return GachaCrate.getInstance();
    }

    /** Opens the menu (creates a fresh inventory). */
    public void open() {
        inventory = Bukkit.createInventory(this, getSize(), getTitle());
        actions.clear();
        draw();
        OPEN_MENUS.put(player.getUniqueId(), this);
        player.openInventory(inventory);
    }

    /** Closes every open GachaCrate menu (used before reloading). */
    public static void closeAll() {
        for (Menu menu : new ArrayList<>(OPEN_MENUS.values())) {
            if (menu.player.isOnline()) menu.player.closeInventory();
        }
        OPEN_MENUS.clear();
    }

    /** Redraws the menu in place, keeping whatever is in the free slots. */
    public void refresh() {
        if (inventory == null) {
            open();
            return;
        }
        Map<Integer, ItemStack> kept = new HashMap<>();
        for (int slot : getFreeSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null) kept.put(slot, item);
        }
        actions.clear();
        inventory.clear();
        draw();
        kept.forEach(inventory::setItem);
    }

    protected void button(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        inventory.setItem(slot, item);
        if (action != null) actions.put(slot, action);
        else actions.remove(slot);
    }

    protected void item(int slot, ItemStack item) {
        button(slot, item, null);
    }

    protected void fill(Material pane) {
        ItemStack filler = Items.filler(pane);
        for (int i = 0; i < inventory.getSize(); i++) {
            if (!getFreeSlots().contains(i)) inventory.setItem(i, filler);
        }
    }

    protected void fillRow(int row, Material pane) {
        ItemStack filler = Items.filler(pane);
        for (int i = row * 9; i < row * 9 + 9; i++) inventory.setItem(i, filler);
    }

    // ------------------------------------------------------------------
    // Event handling (called by MenuListener)
    // ------------------------------------------------------------------

    public void handleClick(InventoryClickEvent event) {
        Inventory clicked = event.getClickedInventory();
        boolean inTop = clicked != null && clicked.equals(event.getInventory());
        Set<Integer> free = getFreeSlots();

        // Double-click collecting could pull items out of the menu.
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }

        if (!inTop) {
            // The player's own inventory: normal clicks are fine, shift-clicks would move items into the menu.
            if (event.isShiftClick()) event.setCancelled(true);
            return;
        }

        if (free.contains(event.getSlot())) {
            // Hotbar number keys / offhand swap are allowed too; they only move between free slot and player.
            return;
        }

        event.setCancelled(true);
        Consumer<InventoryClickEvent> action = actions.get(event.getSlot());
        if (action != null) action.accept(event);
    }

    public void handleDrag(InventoryDragEvent event) {
        int topSize = event.getInventory().getSize();
        Set<Integer> free = getFreeSlots();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize && !free.contains(rawSlot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public void handleClose(InventoryCloseEvent event) {
        OPEN_MENUS.remove(player.getUniqueId(), this);
        onClose();
    }

    /** Gives back anything left in the free slots (e.g. when a menu is closed without confirming). */
    protected void returnFreeSlotItems() {
        if (inventory == null) return;
        for (int slot : getFreeSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            inventory.setItem(slot, null);
            for (ItemStack leftover : player.getInventory().addItem(item).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }

    /** Opens another menu one tick later (safe to call from inside click handlers). */
    protected void openLater(Menu menu) {
        Bukkit.getScheduler().runTask(plugin(), menu::open);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
