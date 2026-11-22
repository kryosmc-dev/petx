package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class PetsMenu extends Menu {
    private static final int PER_PAGE = 28;
    private final int page;
    private final List<String> ids = new ArrayList<>();

    public PetsMenu(MascotasX plugin, Player p, int page) {
        super(plugin, p, plugin.getConfig().getString("titles.pets", "Mascotas"));
        this.page = page;
    }

    @Override
    protected void build() {
        fill();
        ids.clear();
        Profile pr = plugin.manager().profile(player.getUniqueId());
        ids.addAll(pr.owned.keySet());
        int start = (page - 1) * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < ids.size(); i++) {
            String id = ids.get(start + i);
            PetData d = pr.owned.get(id);
            PetType t = plugin.manager().type(id);
            if (t == null) continue;
            boolean on = plugin.manager().isActive(player, id);
            List<String> lore = new ArrayList<>();
            lore.add(" ");
            lore.add("<gray>Nivel: <white>" + d.level + "<gray>/" + plugin.manager().maxLevel());
            if (d.level < plugin.manager().maxLevel())
                lore.add(Text.bar(d.xp / plugin.manager().need(d.level), 14) + " <gray>" + (int) d.xp + "/" + (int) plugin.manager().need(d.level));
            lore.add("<gray>Daño: <red>" + Text.num(plugin.manager().damage(t, d)));
            lore.add("<gray>Estado: " + (on ? "<green>Invocada" : "<red>Guardada"));
            lore.add(" ");
            lore.add("<yellow>Click izquierdo: <white>" + (on ? "guardar" : "invocar"));
            lore.add("<yellow>Click derecho: <white>abrir datos");
            inv.setItem(grid(i), Items.glow(Items.of(t.icon(), "<gold>" + d.displayName(t), lore), on));
        }
        if (page > 1) inv.setItem(45, Items.of(Material.ARROW, "<green>« Anterior"));
        if (start + PER_PAGE < ids.size()) inv.setItem(53, Items.of(Material.ARROW, "<green>Siguiente »"));
        inv.setItem(49, Items.of(Material.BARRIER, "<red>Cerrar"));
    }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 49) { player.closeInventory(); return; }
        if (slot == 45 && page > 1) { new PetsMenu(plugin, player, page - 1).open(); return; }
        if (slot == 53 && inv.getItem(53) != null && inv.getItem(53).getType() == Material.ARROW) { new PetsMenu(plugin, player, page + 1).open(); return; }
        for (int i = 0; i < PER_PAGE; i++) {
            if (grid(i) != slot) continue;
            int idx = (page - 1) * PER_PAGE + i;
            if (idx >= ids.size()) return;
            String id = ids.get(idx);
            if (e.isRightClick()) new DataMenu(plugin, player, id).open();
            else { plugin.manager().toggle(player, id); build(); }
            return;
        }
    }
}
