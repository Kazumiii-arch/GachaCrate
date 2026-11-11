package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.conversation.ChatPrompt;
import com.kazumiii.gachacrate.crates.Crate;
import com.kazumiii.gachacrate.util.Items;
import com.kazumiii.gachacrate.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

/**
 * Limit a crate to a date range (e.g. a holiday event crate). Both dates are included.
 */
public class TimeLimitEditorMenu extends Menu {

    private final Crate crate;

    public TimeLimitEditorMenu(Player player, Crate crate) {
        super(player);
        this.crate = crate;
    }

    @Override
    protected String getTitle() {
        return "§8Time Limit: " + crate.getName();
    }

    @Override
    protected int getSize() {
        return 27;
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);
        boolean enabled = crate.isTimeLimitEnabled();
        boolean openNow = crate.isAvailable(LocalDate.now());

        item(4, Items.of(Material.BOOK).name("&6Time Limit")
                .lore("&7Today: &f" + LocalDate.now(),
                        "&7Can be opened now: " + (openNow ? "&aYes" : "&cNo")).build());

        button(11, Items.of(enabled ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&6Time Limit: " + (enabled ? "&aON" : "&cOFF")).lore("&e► Click to toggle").build(), e -> {
            crate.setTimeLimitEnabled(!crate.isTimeLimitEnabled());
            if (crate.isTimeLimitEnabled() && crate.getStartDate() == null) crate.setStartDate(LocalDate.now());
            if (crate.isTimeLimitEnabled() && crate.getEndDate() == null) crate.setEndDate(LocalDate.now().plusDays(7));
            save();
            refresh();
        });

        button(13, Items.of(Material.OAK_SIGN).name("&bStart Date: &f" + orNone(crate.getStartDate()))
                .lore("&e► Click to change").build(),
                e -> askDate("start", crate::setStartDate));

        button(15, Items.of(Material.DARK_OAK_SIGN).name("&bEnd Date: &f" + orNone(crate.getEndDate()))
                .lore("&7The crate can still be opened", "&7on this day.", "", "&e► Click to change").build(),
                e -> askDate("end", crate::setEndDate));

        button(18, Items.of(Material.ARROW).name("&e« Back to Settings").build(),
                e -> new CrateSettingsMenu(player, crate).open());
    }

    private static String orNone(LocalDate date) {
        return date == null ? "Not set" : date.toString();
    }

    private void askDate(String which, Consumer<LocalDate> setter) {
        ChatPrompt.ask(player, "&eType the " + which + " date as &fYYYY-MM-DD &7(e.g. " + LocalDate.now().plusDays(7) + ")", input -> {
            LocalDate date;
            try {
                date = LocalDate.parse(input);
            } catch (DateTimeParseException ex) {
                return "Invalid date. Use YYYY-MM-DD, e.g. 2026-12-25.";
            }
            setter.accept(date);
            if (crate.getStartDate() != null && crate.getEndDate() != null && crate.getEndDate().isBefore(crate.getStartDate())) {
                Text.send(player, "&eHeads up: the end date is before the start date, so the crate can't be opened.");
            }
            save();
            Text.send(player, "&aDate updated.");
            return null;
        }, this::open);
    }

    private void save() {
        plugin().getCrateManager().saveCrates();
    }
}
