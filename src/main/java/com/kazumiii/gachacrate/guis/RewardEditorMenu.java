package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.conversation.ChatPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.crates.Reward;
import com.kazumiii.gachacrate.crates.RewardType;
import com.kazumiii.gachacrate.util.Compat;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Edit one reward: rarity, weight, item/icon, lore, glow and commands.
 */
public class RewardEditorMenu extends Menu {

    private final Crate crate;
    private final Reward reward;

    public RewardEditorMenu(Player player, Crate crate, Reward reward) {
        super(player);
        this.crate = crate;
        this.reward = reward;
    }

    @Override
    protected String getTitle() {
        return "§8Editing Reward";
    }

    @Override
    protected int getSize() {
        return 45;
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);

        item(4, Items.copy(reward.getDisplayItem()).addLore(List.of(
                "", "&eRarity: " + reward.getRarity().getDisplayName(),
                "&eWeight: &f" + Text.number(reward.getChance()) + " &7(" + Text.number(crate.getPercent(reward)) + "%)"
        )).build());

        button(20, Items.of(Material.NAME_TAG).name("&bRarity: " + reward.getRarity().getDisplayName())
                .lore("&7Left-click: next rarity", "&7Right-click: previous rarity").build(), e -> {
            reward.setRarity(plugin().getRarityManager().next(reward.getRarity(), !e.isRightClick()));
            save();
            refresh();
        });

        button(22, Items.of(Material.EXPERIENCE_BOTTLE).name("&aWeight: &f" + Text.number(reward.getChance()))
                .lore("&7Real chance: &f" + Text.number(crate.getPercent(reward)) + "%", "",
                        "&e► Click to type a new weight", "&7(decimals allowed, e.g. 0.5)").build(),
                e -> ChatPrompt.ask(player, "&eType the new weight (e.g. &f25 &eor &f0.5&e)", input -> {
                    double value;
                    try {
                        value = Double.parseDouble(input.replace(",", "."));
                    } catch (NumberFormatException ex) {
                        return "That's not a number.";
                    }
                    if (value <= 0 || Double.isNaN(value) || Double.isInfinite(value)) return "The weight must be more than 0.";
                    reward.setChance(value);
                    save();
                    Text.send(player, "&aWeight updated.");
                    return null;
                }, this::open));

        if (reward.getType() == RewardType.ITEM) {
            button(24, Items.of(Material.ANVIL).name("&eReplace Item")
                    .lore("&7Swap this reward's item", "&7for another one.").build(),
                    e -> new SetRewardItemMenu(player, crate, reward).open());

            boolean glowing = Compat.hasGlow(reward.getItem().getItemMeta());
            button(30, Items.of(glowing ? Material.LIME_DYE : Material.GRAY_DYE)
                    .name("&dGlow Effect: " + (glowing ? "&aON" : "&cOFF")).lore("&e► Click to toggle").build(), e -> {
                ItemStack item = reward.getItem();
                ItemMeta meta = item.getItemMeta();
                Compat.setGlow(meta, !glowing);
                item.setItemMeta(meta);
                save();
                refresh();
            });

            button(32, Items.of(Material.WRITABLE_BOOK).name("&3Edit Lore")
                    .lore("&7Add, remove or clear", "&7the item's description.").build(),
                    e -> new LoreEditorMenu(player, crate, reward).open());
        } else {
            button(24, Items.of(Material.ITEM_FRAME).name("&eSet Icon")
                    .lore("&7The item shown in menus", "&7and animations for this reward.").build(),
                    e -> new SetRewardItemMenu(player, crate, reward).open());

            List<String> lore = new ArrayList<>();
            lore.add("&7Current:");
            lore.addAll(reward.describeCommands());
            lore.add("");
            lore.add("&e► Left-click to add a command");
            lore.add("&c► Right-click to remove the last one");
            button(31, Items.of(Material.COMMAND_BLOCK).name("&dCommands").lore(lore).build(), e -> {
                if (e.isRightClick()) {
                    if (reward.getCommands().size() <= 1) {
                        Text.send(player, "&cA command reward needs at least one command.");
                        return;
                    }
                    reward.getCommands().remove(reward.getCommands().size() - 1);
                    save();
                    refresh();
                    return;
                }
                ChatPrompt.ask(player, "&eType the command to add &7(without '/', {player} = winner)", input -> {
                    List<String> commands = RewardTypeSelectionMenu.splitCommands(input);
                    if (commands.isEmpty()) return "Please type a command.";
                    reward.getCommands().addAll(commands);
                    save();
                    Text.send(player, "&aCommand added.");
                    return null;
                }, this::open);
            });
        }

        button(36, Items.of(Material.ARROW).name("&e« Back to Rewards").build(),
                e -> new CrateEditorMenu(player, crate).open());

        button(44, Items.of(Material.LAVA_BUCKET).name("&cRemove Reward").build(),
                e -> new ConfirmMenu(player, "Remove this reward?", () -> {
                    crate.removeReward(reward);
                    plugin().getCrateManager().saveCrates();
                    Text.send(player, "&aReward removed.");
                    new CrateEditorMenu(player, crate).open();
                }, this::open).open());
    }

    private void save() {
        plugin().getCrateManager().saveCrates();
    }
}
