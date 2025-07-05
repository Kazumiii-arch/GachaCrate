// ========================================================================
// File: MenuListener.java
// Location: /src/main/java/com/kazumiii/gachacrate/guis/MenuListener.java
// ========================================================================
package com.kazumiii.gachacrate.guis;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Check if the inventory clicked is one of our custom menus
        if (holder instanceof Menu) {
            // Let the specific menu implementation handle the click event
            ((Menu) holder).handleMenu(event);
        }
    }
}
