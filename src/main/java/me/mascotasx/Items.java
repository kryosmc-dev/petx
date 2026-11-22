package me.mascotasx;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Items {
    private Items() {}

    public static ItemStack of(Material m, String name, String... lore) {
        return of(m, 1, name, Arrays.asList(lore));
    }

    public static ItemStack of(Material m, String name, List<String> lore) {
        return of(m, 1, name, lore);
    }

    public static ItemStack of(Material m, int amount, String name, List<String> lore) {
        ItemStack it = new ItemStack(m, Math.max(1, Math.min(64, amount)));
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Text.c(name));
        if (lore != null && !lore.isEmpty()) {
            List<Component> l = new ArrayList<>();
            for (String s : lore) l.add(Text.c(s));
            meta.lore(l);
        }
        meta.addItemFlags(ItemFlag.values());
        it.setItemMeta(meta);
        return it;
    }

    public static ItemStack glow(ItemStack it, boolean on) {
        if (!on) return it;
        ItemMeta m = it.getItemMeta();
        m.setEnchantmentGlintOverride(true);
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack filler() {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = it.getItemMeta();
        m.displayName(Component.text(" "));
        m.addItemFlags(ItemFlag.values());
        it.setItemMeta(m);
        return it;
    }
}
