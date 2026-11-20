package com.kazumiii.gachacrate.listeners;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Keeps crate blocks and keys safe:
 * crate blocks can't be broken, blown up or pushed; keys can't be placed or used in crafting;
 * celebration fireworks don't hurt anyone.
 */
public class ProtectionListener implements Listener {

    private final GachaCrate plugin;

    public ProtectionListener(GachaCrate plugin) {
        this.plugin = plugin;
    }

    private boolean isCrate(Block block) {
        return plugin.getPhysicalCrateManager().isCrateBlock(block);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!isCrate(event.getBlock())) return;
        event.setCancelled(true);
        if (event.getPlayer().hasPermission("gacha.admin.setblock")) {
            Text.send(event.getPlayer(), "&eThis is a crate block. Use &f/gacha removeblock &ewhile looking at it to remove it.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isCrate);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(this::isCrate);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isCrate(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isCrate(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlaceKey(BlockPlaceEvent event) {
        if (plugin.getKeyManager().isKey(event.getItemInHand())) event.setCancelled(true);
    }

    @EventHandler
    public void onCraftWithKey(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (plugin.getKeyManager().isKey(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireworkDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework && event.getDamager().getPersistentDataContainer()
                .has(plugin.getCrateOpener().getFireworkTag(), PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.getHologramManager().refreshWorld(event.getWorld().getName());
    }
}
