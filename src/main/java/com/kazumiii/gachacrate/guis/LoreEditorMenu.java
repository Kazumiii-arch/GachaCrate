package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.conversation.ChatPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Edit the lore (description) and name of an item reward.
 */
public class LoreEditorMenu extends Menu {

    private final Crate crate;
    private final Reward reward;

    public LoreEditorMenu(Player player, Crate crate, Reward reward) {
        super(player);
        this.crate = crate;
        this.reward = reward;
    }

    @Override
    protected String getTitle() {
        return "§8Edit Item Lore";
    }

    @Override
    protected int getSize() {
        return 27;
    }

    private List<String> currentLore() {
        ItemMeta meta = reward.getItem().getItemMeta();
        return meta != null && meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
    }

    private void setLore(List<String> lore) {
        ItemStack item = reward.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setLore(lore.isEmpty() ? null : lore);
        item.setItemMeta(meta);
        plugin().getCrateManager().saveCrates();
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);
        item(4, reward.getItem().clone());

        button(10, Items.of(Material.NAME_TAG).name("&bRename Item")
                .lore("&7Change the item's name.", "&7Supports & color codes.", "", "&c► Right-click to reset").build(), e -> {
            if (e.isRightClick()) {
                ItemStack item = reward.getItem();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(null);
                    item.setItemMeta(meta);
                    plugin().getCrateManager().saveCrates();
                }
                refresh();
                return;
            }
            ChatPrompt.ask(player, "&eType the new item name", input -> {
                ItemStack item = reward.getItem();
                ItemMeta meta = item.getItemMeta();
                if (meta == null) return "This item can't be renamed.";
                meta.setDisplayName(Text.color(input));
                item.setItemMeta(meta);
                plugin().getCrateManager().saveCrates();
                Text.send(player, "&aItem renamed.");
                return null;
            }, this::open);
        });

        button(12, Items.of(Material.WRITABLE_BOOK).name("&aAdd Lore Line")
                .lore("&7Adds a line at the bottom.").build(),
                e -> ChatPrompt.ask(player, "&eType the new lore line &7(& color codes work)", input -> {
                    List<String> lore = currentLore();
                    lore.add(Text.color(input));
                    setLore(lore);
                    Text.send(player, "&aLore line added.");
                    return null;
                }, this::open));

        button(14, Items.of(Material.TNT).name("&eRemove Last Line").build(), e -> {
            List<String> lore = currentLore();
            if (lore.isEmpty()) {
                Text.send(player, "&cThis item has no lore.");
                return;
            }
            lore.remove(lore.size() - 1);
            setLore(lore);
            refresh();
        });

        button(16, Items.of(Material.LAVA_BUCKET).name("&cClear All Lore").build(), e -> {
            setLore(new ArrayList<>());
            refresh();
        });

        button(18, Items.of(Material.ARROW).name("&e« Back to Reward").build(),
                e -> new RewardEditorMenu(player, crate, reward).open());
    }
}
