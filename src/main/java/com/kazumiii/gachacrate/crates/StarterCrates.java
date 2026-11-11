package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.animations.AnimationType;
import com.kazumiii.gachacrate.util.Items;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * /gacha setup: creates four ready-to-use example crates so a new server can start right away.
 * Crates that already exist are never touched. Everything can be edited afterwards.
 */
public final class StarterCrates {

    private final GachaCrate plugin;
    private final List<Rarity> rarities;

    public StarterCrates(GachaCrate plugin) {
        this.plugin = plugin;
        this.rarities = plugin.getRarityManager().getRarities();
    }

    /** Rarity by rank: 0 = worst. Falls back to the best one if there are fewer rarities. */
    private Rarity r(int rank) {
        return rarities.get(Math.min(rank, rarities.size() - 1));
    }

    /** Creates the crates that don't exist yet and returns their names. */
    public List<String> create() {
        List<String> created = new ArrayList<>();
        if (make("vote", created)) vote();
        if (make("rare", created)) rare();
        if (make("legendary", created)) legendary();
        if (make("event", created)) event();
        plugin.getCrateManager().saveCrates();
        return created;
    }

    private boolean make(String name, List<String> created) {
        if (plugin.getCrateManager().getCrate(name) != null) return false;
        plugin.getCrateManager().createCrate(name);
        created.add(name);
        return true;
    }

    private void vote() {
        Crate c = plugin.getCrateManager().getCrate("vote");
        c.setDisplayName("&a&lVote Crate");
        c.setAnimationType(AnimationType.REEL);
        c.setDailyFreeKeys(1);
        add(c, item(Material.COOKED_BEEF, 16, null), 0, 30);
        add(c, item(Material.IRON_INGOT, 8, null), 0, 25);
        addCmd(c, "xp add {player} 100 points", item(Material.EXPERIENCE_BOTTLE, 1, "&a100 XP"), 0, 20);
        add(c, item(Material.GOLD_INGOT, 4, null), 1, 15);
        add(c, item(Material.DIAMOND, 2, null), 1, 10);
        add(c, item(Material.GOLDEN_APPLE, 1, null), 2, 5);
        addCmd(c, "gacha givekey {player} rare 1", item(Material.TRIPWIRE_HOOK, 1, "&9&lRare Crate Key"), 3, 2);
    }

    private void rare() {
        Crate c = plugin.getCrateManager().getCrate("rare");
        c.setDisplayName("&9&lRare Crate");
        c.setAnimationType(AnimationType.ROULETTE);
        c.setKeyPrice(1000);
        add(c, item(Material.DIAMOND, 8, null), 1, 30);
        add(c, item(Material.EMERALD, 16, null), 1, 25);
        add(c, item(Material.GOLDEN_APPLE, 3, null), 2, 15);
        add(c, enchanted(item(Material.DIAMOND_SWORD, 1, "&bSharp Diamond Sword"), "sharpness", 3), 2, 10);
        add(c, item(Material.ENCHANTED_GOLDEN_APPLE, 1, null), 3, 5);
        add(c, item(Material.NETHERITE_INGOT, 1, null), 3, 3);
        addCmd(c, "gacha givekey {player} legendary 1", item(Material.TRIPWIRE_HOOK, 1, "&6&lLegendary Crate Key"), 4, 2);
    }

    private void legendary() {
        Crate c = plugin.getCrateManager().getCrate("legendary");
        c.setDisplayName("&6&lLegendary Crate");
        c.setAnimationType(AnimationType.CARDS);
        c.setKeyPrice(5000);
        c.setPityEnabled(true);
        c.setPityThreshold(30);
        c.setPityRarityMinimum(r(rarities.size() - 2).getId());
        add(c, item(Material.NETHERITE_INGOT, 2, null), 2, 30);
        add(c, item(Material.ENCHANTED_GOLDEN_APPLE, 3, null), 3, 20);
        add(c, item(Material.TOTEM_OF_UNDYING, 1, null), 3, 15);
        add(c, item(Material.BEACON, 1, null), 3, 10);
        add(c, item(Material.ELYTRA, 1, "&d&lWings of the Gods"), 4, 5);
        add(c, enchanted(item(Material.NETHERITE_SWORD, 1, "&6&lDragonslayer"), "sharpness", 5), 4, 3);
    }

    private void event() {
        Crate c = plugin.getCrateManager().getCrate("event");
        c.setDisplayName("&d&lEvent Crate");
        c.setAnimationType(AnimationType.WORLD);
        c.setTimeLimitEnabled(true);
        c.setStartDate(LocalDate.now());
        c.setEndDate(LocalDate.now().plusDays(30));
        add(c, item(Material.FIREWORK_ROCKET, 16, "&dParty Rockets"), 0, 30);
        add(c, item(Material.CAKE, 1, "&dCelebration Cake"), 1, 20);
        add(c, item(Material.MUSIC_DISC_PIGSTEP, 1, null), 1, 15);
        add(c, item(Material.TOTEM_OF_UNDYING, 1, null), 2, 10);
        add(c, item(Material.ELYTRA, 1, null), 3, 3);
        add(c, item(Material.DRAGON_HEAD, 1, "&5&lTrophy Dragon Head"), 4, 1);
    }

    // ------------------------------------------------------------------

    private void add(Crate crate, ItemStack item, int rank, double weight) {
        crate.addReward(new Reward(item, r(rank), weight));
    }

    private void addCmd(Crate crate, String command, ItemStack icon, int rank, double weight) {
        Reward reward = new Reward(Collections.singletonList(command), r(rank), weight);
        reward.setPreviewItem(icon);
        crate.addReward(reward);
    }

    private static ItemStack item(Material material, int amount, String name) {
        Items builder = Items.of(material).amount(amount);
        if (name != null) builder.name(name);
        return builder.build();
    }

    private static ItemStack enchanted(ItemStack item, String enchantKey, int level) {
        Enchantment enchantment;
        try {
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantKey));
        } catch (Throwable t) {
            enchantment = null;
        }
        ItemMeta meta = item.getItemMeta();
        if (enchantment != null && meta != null) {
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        }
        return item;
    }
}
