package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class BuyMenu extends Menu {
    private final int page;

    public BuyMenu(MascotasX plugin, Player p, int page) {
        super(plugin, p, plugin.getConfig().getString("titles.buy", "Comprar"));
        this.page = page;
    }

    @Override
    protected void build() {
        fill();
        Profile pr = plugin.manager().profile(player.getUniqueId());
        for (PetType t : plugin.manager().types()) {
            if (t.page() != page) continue;
            boolean own = pr.owned.containsKey(t.id());
            List<String> lore = new ArrayList<>();
            lore.add(" ");
            lore.add("<gray>Daño base: <red>" + Text.num(t.damage()));
            lore.add("<gray>Precio: <green>" + Text.money(t.price()));
            lore.add(" ");
            lore.add(own ? "<red>✖ Ya tienes esta mascota" : "<yellow>Click para comprar");
            inv.setItem(t.slot(), Items.glow(Items.of(t.icon(), "<gold>" + t.name(), lore), own));
        }
        if (page > 1) inv.setItem(45, Items.of(Material.ARROW, "<green>« Anterior"));
        if (page < plugin.manager().pages()) inv.setItem(53, Items.of(Material.ARROW, "<green>Siguiente »"));
    }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 45 && page > 1) { new BuyMenu(plugin, player, page - 1).open(); return; }
        if (slot == 53 && page < plugin.manager().pages()) { new BuyMenu(plugin, player, page + 1).open(); return; }
        for (PetType t : plugin.manager().types()) {
            if (t.page() == page && t.slot() == slot) {
                plugin.manager().buy(player, t);
                build();
                return;
            }
        }
    }
}
