package com.kazumiii.gachacrate.holograms;

import org.bukkit.Location;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Uses the FancyHolograms plugin (v2) for crate holograms.
 * <p>
 * FancyHolograms is reached through reflection so GachaCrate doesn't need it to build,
 * and still loads fine on servers without it.
 */
final class FancyHologramsHook implements HologramProvider {

    private final Object manager;
    private final Class<?> textDataClass;
    private final Method create;
    private final Method add;
    private final Method get;
    private final Method removeMethod;
    private final Method setText;
    private final Method setPersistent;

    FancyHologramsHook() throws ReflectiveOperationException {
        Class<?> pluginClass = Class.forName("de.oliver.fancyholograms.api.FancyHologramsPlugin");
        Object plugin = pluginClass.getMethod("get").invoke(null);
        Class<?> managerClass = Class.forName("de.oliver.fancyholograms.api.HologramManager");
        this.manager = pluginClass.getMethod("getHologramManager").invoke(plugin);

        Class<?> dataClass = Class.forName("de.oliver.fancyholograms.api.data.HologramData");
        Class<?> hologramClass = Class.forName("de.oliver.fancyholograms.api.hologram.Hologram");
        this.textDataClass = Class.forName("de.oliver.fancyholograms.api.data.TextHologramData");

        this.create = managerClass.getMethod("create", dataClass);
        this.add = managerClass.getMethod("addHologram", hologramClass);
        this.get = managerClass.getMethod("getHologram", String.class);
        this.removeMethod = managerClass.getMethod("removeHologram", hologramClass);
        this.setText = textDataClass.getMethod("setText", List.class);

        Method persistent;
        try {
            persistent = dataClass.getMethod("setPersistent", boolean.class);
        } catch (NoSuchMethodException e) {
            persistent = null; // Older FancyHolograms; holograms may be saved to its file, GachaCrate replaces them anyway.
        }
        this.setPersistent = persistent;
    }

    @Override
    public String getName() {
        return "FancyHolograms";
    }

    @Override
    public void create(String name, Location location, List<String> lines) {
        remove(name);
        try {
            Object data = textDataClass.getConstructor(String.class, Location.class).newInstance(name, location);
            setText.invoke(data, lines);
            if (setPersistent != null) setPersistent.invoke(data, false);
            Object hologram = create.invoke(manager, data);
            add.invoke(manager, hologram);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("FancyHolograms couldn't create the hologram", e);
        }
    }

    @Override
    public void remove(String name) {
        try {
            Object result = get.invoke(manager, name);
            if (result instanceof Optional && ((Optional<?>) result).isPresent()) {
                removeMethod.invoke(manager, ((Optional<?>) result).get());
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("FancyHolograms couldn't remove the hologram", e);
        }
    }
}
