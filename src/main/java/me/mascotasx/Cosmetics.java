package me.mascotasx;

import org.bukkit.DyeColor;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.List;

/** Cosmeticos visuales que dependen del tipo de mob (colores, variantes). */
public final class Cosmetics {
    private Cosmetics() {}

    private static <T extends Keyed> List<T> list(Registry<T> r) {
        List<T> l = new ArrayList<>();
        for (T t : r) l.add(t);
        return l;
    }

    public static boolean hasColor(Class<?> c) {
        return Sheep.class.isAssignableFrom(c) || Wolf.class.isAssignableFrom(c) || Cat.class.isAssignableFrom(c);
    }

    public static boolean isAgeable(Class<?> c) {
        return Ageable.class.isAssignableFrom(c);
    }

    public static int variantCount(Class<?> c) {
        if (Axolotl.class.isAssignableFrom(c)) return Axolotl.Variant.values().length;
        if (Rabbit.class.isAssignableFrom(c)) return Rabbit.Type.values().length;
        if (Fox.class.isAssignableFrom(c)) return Fox.Type.values().length;
        if (Parrot.class.isAssignableFrom(c)) return Parrot.Variant.values().length;
        if (Horse.class.isAssignableFrom(c)) return Horse.Color.values().length;
        if (Llama.class.isAssignableFrom(c)) return Llama.Color.values().length;
        if (MushroomCow.class.isAssignableFrom(c)) return MushroomCow.Variant.values().length;
        if (Cat.class.isAssignableFrom(c)) return list(Registry.CAT_VARIANT).size();
        if (Wolf.class.isAssignableFrom(c)) return list(Registry.WOLF_VARIANT).size();
        if (Frog.class.isAssignableFrom(c)) return list(Registry.FROG_VARIANT).size();
        if (Villager.class.isAssignableFrom(c)) return list(Registry.VILLAGER_TYPE).size();
        return 0;
    }

    public static void apply(LivingEntity e, PetData d) {
        DyeColor dye = DyeColor.values()[Math.floorMod(d.color, DyeColor.values().length)];
        if (e instanceof Sheep s) s.setColor(dye);
        if (e instanceof Wolf w) { w.setTamed(true); w.setCollarColor(dye); }
        if (e instanceof Cat c) { c.setTamed(true); c.setCollarColor(dye); }

        int n = variantCount(e.getClass());
        if (n == 0) return;
        int i = Math.floorMod(d.variant, n);
        if (e instanceof Axolotl a) a.setVariant(Axolotl.Variant.values()[i]);
        else if (e instanceof Rabbit r) r.setRabbitType(Rabbit.Type.values()[i]);
        else if (e instanceof Fox f) f.setFoxType(Fox.Type.values()[i]);
        else if (e instanceof Parrot p) p.setVariant(Parrot.Variant.values()[i]);
        else if (e instanceof Horse h) h.setColor(Horse.Color.values()[i]);
        else if (e instanceof Llama l) l.setColor(Llama.Color.values()[i]);
        else if (e instanceof MushroomCow m) m.setVariant(MushroomCow.Variant.values()[i]);
        else if (e instanceof Cat c) c.setCatType(list(Registry.CAT_VARIANT).get(i));
        else if (e instanceof Wolf w) w.setVariant(list(Registry.WOLF_VARIANT).get(i));
        else if (e instanceof Frog fr) fr.setVariant(list(Registry.FROG_VARIANT).get(i));
        else if (e instanceof Villager v) v.setVillagerType(list(Registry.VILLAGER_TYPE).get(i));
    }
}
