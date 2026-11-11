package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;
import com.kazumiii.gachacrate.animations.AnimationType;
import com.kazumiii.gachacrate.animations.ParticleStyle;
import com.kazumiii.gachacrate.util.Compat;
import com.kazumiii.gachacrate.util.Items;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * /gacha setup: four ready-made crates that form a ladder from Vote up to Legendary.
 * <pre>
 *   Vote  ──(rare drop: Rare key)──►  Rare  ──►  Epic  ──►  Legendary
 * </pre>
 * Each tier has better loot, its own animation, particles and colors. Higher crates can
 * drop keys for the next tier, so players always have something to work towards.
 * Crates that already exist are never touched, and everything can be edited afterwards.
 */
public final class StarterCrates {

    private final GachaCrate plugin;
    private final List<Rarity> rarities;

    public StarterCrates(GachaCrate plugin) {
        this.plugin = plugin;
        this.rarities = plugin.getRarityManager().getRarities();
    }

    /** Rarity by rank: 0 = worst. Uses the best one if the server has fewer rarities. */
    private Rarity r(int rank) {
        return rarities.get(Math.max(0, Math.min(rank, rarities.size() - 1)));
    }

    /** Creates the crates that don't exist yet and returns their names. */
    public List<String> create() {
        List<String> created = new ArrayList<>();
        if (make("vote", created)) vote();
        if (make("rare", created)) rare();
        if (make("epic", created)) epic();
        if (make("legendary", created)) legendary();
        plugin.getCrateManager().saveCrates();
        return created;
    }

    private boolean make(String name, List<String> created) {
        if (plugin.getCrateManager().getCrate(name) != null) return false;
        plugin.getCrateManager().createCrate(name);
        created.add(name);
        return true;
    }

    // ------------------------------------------------------------------
    // Tier 1: Vote. Free daily key, everyday supplies, a small chance at a Rare key.
    // ------------------------------------------------------------------
    private void vote() {
        Crate c = plugin.getCrateManager().getCrate("vote");
        c.setDisplayName("&a&lVote Crate");
        c.setAnimationType(AnimationType.REEL);
        c.setDailyFreeKeys(1);
        style(c, "HAPPY_VILLAGER", ParticleStyle.RING);

        add(c, item(Material.COOKED_BEEF, 32, "&fHearty Steak", "&7Never go hungry."), 0, 25);
        add(c, item(Material.IRON_INGOT, 16, "&fIron Bundle", "&7Building basics."), 0, 22);
        add(c, item(Material.OAK_LOG, 64, "&fStack of Logs", "&7For your next build."), 0, 18);
        addCmd(c, "xp add {player} 250 points", item(Material.EXPERIENCE_BOTTLE, 1, "&a+250 XP", "&7Instant experience."), 0, 15);
        add(c, item(Material.GOLD_INGOT, 12, "&eGold Stash", "&7Shiny!"), 1, 10);
        add(c, item(Material.DIAMOND, 3, "&bDiamond Trio", "&7A lucky find."), 1, 6);
        add(c, item(Material.GOLDEN_APPLE, 2, null, null), 2, 3);
        addCmd(c, "gacha givekey {player} rare 1",
                key("&9&lRare Crate Key", "&7Unlocks the &9Rare Crate&7!"), 3, 1);
    }

    // ------------------------------------------------------------------
    // Tier 2: Rare. Good gear, enchanted tools, a chance at an Epic key.
    // ------------------------------------------------------------------
    private void rare() {
        Crate c = plugin.getCrateManager().getCrate("rare");
        c.setDisplayName("&9&lRare Crate");
        c.setAnimationType(AnimationType.ROULETTE);
        c.setKeyPrice(1000);
        style(c, "ENCHANT", ParticleStyle.ORBIT);

        add(c, item(Material.DIAMOND, 8, "&bDiamond Pouch", null), 1, 24);
        add(c, item(Material.EMERALD, 24, "&aEmerald Pile", "&7Trade with villagers."), 1, 20);
        add(c, enchant(item(Material.DIAMOND_PICKAXE, 1, "&bMiner's Pick", "&7Digs fast and lasts long."),
                "efficiency", 4, "unbreaking", 3), 1, 14);
        add(c, enchant(item(Material.DIAMOND_SWORD, 1, "&bKnight's Blade", null), "sharpness", 3, "unbreaking", 2), 2, 12);
        add(c, item(Material.GOLDEN_APPLE, 6, null, null), 2, 10);
        add(c, item(Material.ENCHANTED_GOLDEN_APPLE, 1, null, null), 3, 6);
        add(c, item(Material.NETHERITE_SCRAP, 2, "&8Ancient Scraps", "&7Half way to netherite."), 3, 5);
        addCmd(c, "gacha givekey {player} epic 1",
                key("&5&lEpic Crate Key", "&7Unlocks the &5Epic Crate&7!"), 3, 3);
    }

    // ------------------------------------------------------------------
    // Tier 3: Epic. Strong enchanted gear, rare items, pity, and a chance at a Legendary key.
    // ------------------------------------------------------------------
    private void epic() {
        Crate c = plugin.getCrateManager().getCrate("epic");
        c.setDisplayName("&5&lEpic Crate");
        c.setAnimationType(AnimationType.CARDS);
        c.setKeyPrice(3000);
        c.setPityEnabled(true);
        c.setPityThreshold(20);
        c.setPityRarityMinimum(r(3).getId());
        style(c, "WITCH", ParticleStyle.HELIX);

        add(c, item(Material.DIAMOND_BLOCK, 3, "&bDiamond Blocks", null), 2, 22);
        add(c, enchant(item(Material.DIAMOND_CHESTPLATE, 1, "&5Epic Chestplate", "&7Tough as stone."),
                "protection", 4, "unbreaking", 3), 2, 16);
        add(c, enchant(item(Material.BOW, 1, "&5Hunter's Bow", null), "power", 5, "infinity", 1), 2, 14);
        add(c, item(Material.ENCHANTED_GOLDEN_APPLE, 2, null, null), 3, 12);
        add(c, item(Material.NETHERITE_INGOT, 1, "&8Netherite Ingot", "&7The strongest metal."), 3, 10);
        add(c, item(Material.TOTEM_OF_UNDYING, 1, "&eSecond Life", "&7Cheat death once."), 3, 8);
        add(c, item(Material.SHULKER_BOX, 1, "&dPocket Storage", null), 3, 8);
        addCmd(c, "gacha givekey {player} legendary 1",
                key("&6&lLegendary Crate Key", "&7Unlocks the &6Legendary Crate&7!"), 4, 3);
    }

    // ------------------------------------------------------------------
    // Tier 4: Legendary. The best loot on the server, shown off with the in-world animation.
    // ------------------------------------------------------------------
    private void legendary() {
        Crate c = plugin.getCrateManager().getCrate("legendary");
        c.setDisplayName("&6&lLegendary Crate");
        c.setAnimationType(AnimationType.WORLD);
        c.setKeyPrice(7500);
        c.setPityEnabled(true);
        c.setPityThreshold(30);
        c.setPityRarityMinimum(r(rarities.size() - 1).getId());
        style(c, "TOTEM_OF_UNDYING", ParticleStyle.TORNADO);

        add(c, item(Material.NETHERITE_INGOT, 3, "&8Netherite Stack", null), 3, 22);
        add(c, item(Material.ENCHANTED_GOLDEN_APPLE, 5, "&6Golden Feast", null), 3, 18);
        add(c, item(Material.BEACON, 1, "&bBeacon of Power", null), 3, 14);
        add(c, item(Material.ELYTRA, 1, "&d&lWings of the Sky", "&7Take flight."), 4, 10);
        add(c, enchant(item(Material.NETHERITE_SWORD, 1, "&6&lDragonslayer", "&7Forged in dragon fire."),
                "sharpness", 5, "unbreaking", 3, "looting", 3, "mending", 1), 4, 8);
        add(c, enchant(item(Material.NETHERITE_PICKAXE, 1, "&6&lWorldbreaker", "&7Nothing stands in its way."),
                "efficiency", 5, "unbreaking", 3, "fortune", 3, "mending", 1), 4, 8);
        add(c, item(Material.DRAGON_EGG, 1, "&5&l✦ Dragon Egg ✦", "&7The rarest trophy of all."), 4, 2);
    }

    // ------------------------------------------------------------------

    private void style(Crate crate, String particle, ParticleStyle style) {
        crate.setIdleParticle(Compat.particle(particle));
        crate.setParticleStyle(style);
    }

    private void add(Crate crate, ItemStack item, int rank, double weight) {
        crate.addReward(new Reward(item, r(rank), weight));
    }

    private void addCmd(Crate crate, String command, ItemStack icon, int rank, double weight) {
        Reward reward = new Reward(Collections.singletonList(command), r(rank), weight);
        reward.setPreviewItem(icon);
        crate.addReward(reward);
    }

    private static ItemStack key(String name, String lore) {
        return Items.of(Material.TRIPWIRE_HOOK).name(name).lore(lore).glow(true).build();
    }

    private static ItemStack item(Material material, int amount, String name, String lore) {
        Items builder = Items.of(material).amount(amount);
        if (name != null) builder.name(name);
        if (lore != null) builder.lore(lore);
        return builder.build();
    }

    /** Adds enchantments given as name, level, name, level... Unknown names are skipped. */
    private static ItemStack enchant(ItemStack item, Object... pairs) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            Enchantment enchantment;
            try {
                enchantment = Enchantment.getByKey(NamespacedKey.minecraft((String) pairs[i]));
            } catch (Throwable t) {
                enchantment = null;
            }
            if (enchantment != null) meta.addEnchant(enchantment, (Integer) pairs[i + 1], true);
        }
        item.setItemMeta(meta);
        return item;
    }
}
