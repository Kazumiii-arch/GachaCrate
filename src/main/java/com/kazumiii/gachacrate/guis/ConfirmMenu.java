package com.kazumiii.gachacrate.guis;

import com.kazumiii.gachacrate.util.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * A simple "are you sure?" menu.
 */
public class ConfirmMenu extends Menu {

    private final String question;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmMenu(Player player, String question, Runnable onConfirm, Runnable onCancel) {
        super(player);
        this.question = question;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    protected String getTitle() {
        return "§8Are you sure?";
    }

    @Override
    protected int getSize() {
        return 27;
    }

    @Override
    protected void draw() {
        fill(Material.GRAY_STAINED_GLASS_PANE);
        item(4, Items.of(Material.PAPER).name("&e" + question).build());
        button(11, Items.of(Material.LIME_CONCRETE).name("&a&lYES").build(), e -> onConfirm.run());
        button(15, Items.of(Material.RED_CONCRETE).name("&c&lNO").build(), e -> onCancel.run());
    }
}
