package com.kazumiii.gachacrate.listeners;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.OpeningMode;
import com.kazumiii.gachacrate.guis.CratePreviewMenu;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles clicking crate blocks and using keys.
 * <ul>
 *   <li>Left-click a crate block: preview rewards.</li>
 *   <li>Right-click a crate block with its key: open (sneak to open many).</li>
 *   <li>Right-click anywhere with a "Key in Hand" key: open.</li>
 * </ul>
 */
public class CrateListener implements Listener {

    private final GachaCrate plugin;
    private final Map<UUID, Long> lastHint = new HashMap<>();

    public CrateListener(GachaCrate plugin) {
        this.plugin = plugin;
    }

    // Not ignoreCancelled: right-clicking air is always "cancelled" by default, and region
    // plugins often cancel clicks at spawn, where crates usually are.
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();

        // --- Crate blocks ---
        if (block != null && (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)
                && plugin.getPhysicalCrateManager().isCrateBlock(block)) {
            event.setCancelled(true);
            // The off-hand fires a second event; ignore it so the crate isn't used twice.
            if (event.getHand() != EquipmentSlot.HAND) return;

            Crate crate = plugin.getCrateManager().getCrate(plugin.getPhysicalCrateManager().getCrateNameAt(block.getLocation()));
            if (crate == null) {
                Text.send(player, "&cThis crate no longer exists. An admin can remove it with /gacha removeblock.");
                return;
            }

            if (action == Action.LEFT_CLICK_BLOCK) {
                openPreview(player, crate);
                return;
            }

            String keyCrate = plugin.getKeyManager().getCrateFromKey(player.getInventory().getItemInMainHand());
            boolean holdingThisKey = keyCrate != null && keyCrate.equalsIgnoreCase(crate.getId());
            if (holdingThisKey) {
                plugin.getCrateOpener().openWithKey(player, crate, player.isSneaking());
            } else if (keyCrate != null) {
                Text.send(player, "&cThat key is for a different crate. You need a " + crate.getDisplayName() + " &ckey.");
            } else if (crate.getOpeningMode() == OpeningMode.PHYSICAL_BLOCKS) {
                Text.send(player, "&cYou need a " + crate.getDisplayName() + " &ckey to open this! &7(Left-click to preview)");
            } else {
                openPreview(player, crate);
            }
            return;
        }

        // --- Keys used anywhere else ---
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) {
            // Stop keys in the off-hand from being placed (tripwire hooks are blocks).
            if (event.getItem() != null && plugin.getKeyManager().isKey(event.getItem())) event.setCancelled(true);
            return;
        }

        String keyCrate = plugin.getKeyManager().getCrateFromKey(player.getInventory().getItemInMainHand());
        if (keyCrate == null) return;
        event.setCancelled(true); // Never place or use the key item itself.

        Crate crate = plugin.getCrateManager().getCrate(keyCrate);
        if (crate == null) {
            Text.send(player, "&cThe crate for this key no longer exists.");
            return;
        }
        if (crate.getOpeningMode() == OpeningMode.KEY_IN_HAND) {
            plugin.getCrateOpener().openWithKey(player, crate, player.isSneaking());
        } else if (shouldHint(player)) {
            Text.send(player, "&eRight-click a " + crate.getDisplayName() + " &ecrate block to use this key.");
        }
    }

    private void openPreview(Player player, Crate crate) {
        if (!player.hasPermission("gacha.user.view")) {
            Text.send(player, "&cYou don't have permission to preview crates.");
            return;
        }
        new CratePreviewMenu(player, crate).open();
    }

    /** Limits the "use it on a crate block" hint to once every 3 seconds. */
    private boolean shouldHint(Player player) {
        long now = System.currentTimeMillis();
        Long last = lastHint.get(player.getUniqueId());
        if (last != null && now - last < 3000) return false;
        lastHint.put(player.getUniqueId(), now);
        return true;
    }

    public void forget(Player player) {
        lastHint.remove(player.getUniqueId());
    }
}
