package com.kazumiii.gachacrate.crates;

import com.kazumiii.gachacrate.GachaCrate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Finds setup problems in a crate, so admins can see at a glance what still needs fixing.
 */
public final class CrateHealth {

    /** How bad the worst problem is. */
    public enum Level { OK, WARNING, ERROR }

    private final Level level;
    private final List<String> problems;

    private CrateHealth(Level level, List<String> problems) {
        this.level = level;
        this.problems = problems;
    }

    public Level getLevel() { return level; }

    /** Problems as colored menu lines (empty when everything is fine). */
    public List<String> getProblems() { return problems; }

    public boolean isOk() { return level == Level.OK; }

    public static CrateHealth check(GachaCrate plugin, Crate crate) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        LocalDate today = LocalDate.now();

        if (crate.getRewards().isEmpty()) {
            errors.add("No rewards yet: it can't be opened");
        } else if (crate.getValidRewards().isEmpty()) {
            errors.add("All rewards are broken: it can't be opened");
        } else {
            int broken = crate.getRewards().size() - crate.getValidRewards().size();
            if (broken > 0) warnings.add(broken + " broken reward" + (broken == 1 ? "" : "s") + " (never won)");
        }

        if (!crate.isEnabled()) warnings.add("Disabled (maintenance mode)");

        if (crate.isTimeLimitEnabled()) {
            if (crate.getStartDate() != null && crate.getEndDate() != null && crate.getEndDate().isBefore(crate.getStartDate())) {
                errors.add("Time limit ends before it starts");
            } else if (crate.getEndDate() != null && today.isAfter(crate.getEndDate())) {
                warnings.add("Time limit ended on " + crate.getEndDate());
            } else if (crate.getStartDate() != null && today.isBefore(crate.getStartDate())) {
                warnings.add("Opens on " + crate.getStartDate());
            }
        }

        if (crate.isPityEnabled()) {
            Rarity minimum = plugin.getRarityManager().getRarity(crate.getPityRarityMinimum());
            if (minimum == null) {
                errors.add("Pity is on but has no rarity set");
            } else {
                boolean reachable = false;
                for (Reward reward : crate.getValidRewards()) {
                    if (reward.getRarity() != null && reward.getRarity().isAtLeast(minimum)) {
                        reachable = true;
                        break;
                    }
                }
                if (!reachable && !crate.getValidRewards().isEmpty()) {
                    errors.add("Pity needs " + minimum.getId() + "+ but no reward has it");
                }
            }
        }

        if (crate.getOpeningMode() == OpeningMode.PHYSICAL_BLOCKS
                && plugin.getPhysicalCrateManager().getBlocksOf(crate.getId()).isEmpty()) {
            warnings.add("No crate block placed (/gacha setblock)");
        }

        if (crate.getKeyPrice() > 0 && !plugin.getShopManager().isEconomyReady()) {
            warnings.add("Has a key price but no economy (Vault)");
        }

        List<String> lines = new ArrayList<>();
        for (String e : errors) lines.add("&c✖ " + e);
        for (String w : warnings) lines.add("&e⚠ " + w);
        Level level = !errors.isEmpty() ? Level.ERROR : !warnings.isEmpty() ? Level.WARNING : Level.OK;
        return new CrateHealth(level, lines);
    }
}
