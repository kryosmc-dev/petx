package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class DataMenu extends Menu {
    private final String petId;

    public DataMenu(MascotasX plugin, Player p, String petId) {
        super(plugin, p, plugin.getConfig().getString("titles.data", "Datos"));
        this.petId = petId;
    }

    private static String state(boolean b) { return b ? "<green>Activado" : "<red>Desactivado"; }

    @Override
    protected void build() {
        fill();
        PetData d = data(petId);
        PetType t = plugin.manager().type(petId);
        if (d == null || t == null) { player.closeInventory(); return; }
        boolean on = plugin.manager().isActive(player, petId);

        // Informacion
        List<String> info = new ArrayList<>();
        info.add(" ");
        info.add("<gray>Nivel: <white>" + d.level + "<gray>/" + plugin.manager().maxLevel());
        if (d.level < plugin.manager().maxLevel())
            info.add(Text.bar(d.xp / plugin.manager().need(d.level), 14) + " <gray>" + (int) d.xp + "/" + (int) plugin.manager().need(d.level));
        info.add("<gray>Daño: <red>" + Text.num(plugin.manager().damage(t, d)));
        inv.setItem(4, Items.of(t.icon(), "<gold>" + d.displayName(t), info));

        // Fila superior
        inv.setItem(10, Items.of(Material.BELL, "<yellow>Silenciar", " ", "<gray>Estado: " + state(d.muted), " ", "<yellow>Click para cambiar"));
        inv.setItem(11, Items.of(d.burning ? Material.LAVA_BUCKET : Material.BUCKET, "<yellow>Ardiendo", " ", "<gray>Estado: " + state(d.burning), " ", "<yellow>Click para cambiar"));

        // Fila inferior
        inv.setItem(46, Items.of(Material.ARMOR_STAND, "<light_purple>Cosméticos", " ", "<gray>Montar, sombrero, nombrar,", "<gray>voltear y más.", " ", "<yellow>Click para abrir"));
        inv.setItem(47, Items.of(Material.EXPERIENCE_BOTTLE, "<green>Niveles", " ", "<gray>Mira las recompensas de cada nivel.", " ", "<yellow>Click para abrir"));
        inv.setItem(48, Items.of(Material.BREWING_STAND, "<aqua>Efectos", " ", "<gray>Efectos que te da tu mascota.", " ", "<yellow>Click para abrir"));
        inv.setItem(49, Items.glow(Items.of(Material.DIAMOND_SWORD, "<red>Modo de ataque", " ", "<gray>Estado: " + state(d.attack), " ", "<yellow>Click para cambiar"), d.attack));
        inv.setItem(51, Items.of(on ? Material.LEAD : Material.ENDER_PEARL, on ? "<red>Guardar mascota" : "<green>Invocar mascota", " ", "<yellow>Click para " + (on ? "guardarla" : "invocarla")));
        inv.setItem(52, Items.of(Material.BARRIER, "<red>Volver"));
    }

    @Override
    public void click(InventoryClickEvent e) {
        PetData d = data(petId);
        if (d == null) return;
        switch (e.getRawSlot()) {
            case 10 -> { d.muted = !d.muted; changed(); }
            case 11 -> { d.burning = !d.burning; changed(); }
            case 46 -> new CosmeticsMenu(plugin, player, petId).open();
            case 47 -> new LevelsMenu(plugin, player, petId, 1).open();
            case 48 -> new EffectsMenu(plugin, player, petId).open();
            case 49 -> { d.attack = !d.attack; changed(); }
            case 51 -> { plugin.manager().toggle(player, petId); build(); }
            case 52 -> PetCommand.openPets(plugin, player);
            default -> { }
        }
    }

    private void changed() {
        plugin.manager().save(player.getUniqueId());
        plugin.manager().refresh(player);
        build();
    }
}
