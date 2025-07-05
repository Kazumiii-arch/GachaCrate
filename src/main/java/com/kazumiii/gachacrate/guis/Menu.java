// ========================================================================
// File: Menu.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/Menu.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.entity.Player;

public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;
    protected Player player;

    public Menu(Player player) {
        this.player = player;
    }

    /**
     * This method will be implemented by each specific menu to define
     * what happens when a player clicks inside the inventory.
     * @param event The InventoryClickEvent.
     */
    public abstract void handleMenu(InventoryClickEvent event);

    /**
     * This method will be implemented by each menu to set up the
     * items, titles, and size of the inventory.
     */
    public abstract void open();

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
