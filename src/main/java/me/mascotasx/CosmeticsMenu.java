package me.mascotasx;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CosmeticsMenu extends Menu {
    private final String petId;

    public CosmeticsMenu(MascotasX plugin, Player p, String petId) {
        super(plugin, p, plugin.getConfig().getString("titles.cosmetics", "Cosmeticos"));
        this.petId = petId;
    }

    private int mountLevel() { return plugin.getConfig().getInt("cosmetics.mount-level", 10); }
    private int hatLevel() { return plugin.getConfig().getInt("cosmetics.hat-level", 5); }

    private static String state(boolean b) { return b ? "<green>Activado" : "<red>Desactivado"; }

    @Override
    protected void build() {
        fill();
        PetData d = data(petId);
        PetType t = plugin.manager().type(petId);
        if (d == null || t == null) { player.closeInventory(); return; }
        Class<?> cls = t.entity().getEntityClass();
        if (cls == null) cls = org.bukkit.entity.Entity.class;

        inv.setItem(10, Items.of(Material.SADDLE, "<gold>Montar", " ", "<gray>Súbete a tu mascota y recorre", "<gray>el mundo con ella.",
                d.level >= mountLevel() ? "<yellow>Click para usar" : "<red>Requiere nivel " + mountLevel()));
        inv.setItem(11, Items.of(Material.DIAMOND_HELMET, "<aqua>Sombrero", " ", "<gray>Lleva a tu mascota en la cabeza.",
                d.level >= hatLevel() ? "<yellow>Click para usar" : "<red>Requiere nivel " + hatLevel()));
        inv.setItem(12, Items.of(Material.NAME_TAG, "<yellow>Nombrar", " ", "<gray>Nombre actual: <white>" + d.displayName(t), " ", "<yellow>Click para cambiar"));
        inv.setItem(13, Items.of(Material.CHORUS_FLOWER, "<light_purple>Voltear", " ", "<gray>Estado: " + state(d.upside), " ", "<yellow>Click para cambiar"));

        if (Cosmetics.isAgeable(cls))
            inv.setItem(14, Items.of(Material.WHEAT, "<green>Bebé", " ", "<gray>Gratis y sin subir niveles.", "<gray>Estado: " + state(d.baby), " ", "<yellow>Click para cambiar"));
        else
            inv.setItem(14, Items.of(Material.BARRIER, "<red>Bebé", " ", "<gray>Este mob no tiene versión bebé."));
        inv.setItem(15, Items.of(Material.COOKIE, "<light_purple>Acariciar", " ", "<gray>Dale cariño a tu mascota.", " ", "<yellow>Click para acariciar"));

        int next = 19;
        if (Cosmetics.hasColor(cls)) {
            DyeColor dye = DyeColor.values()[Math.floorMod(d.color, 16)];
            Material m = Material.matchMaterial(dye.name() + "_DYE");
            inv.setItem(next++, Items.of(m == null ? Material.INK_SAC : m, "<gold>Color", " ", "<gray>Actual: <white>" + dye.name().toLowerCase().replace('_', ' '), " ", "<yellow>Click para cambiar"));
        }
        int vc = Cosmetics.variantCount(cls);
        if (vc > 0) {
            inv.setItem(next, Items.of(Material.PAINTING, "<gold>Aspecto", " ", "<gray>Variante: <white>" + (Math.floorMod(d.variant, vc) + 1) + "<gray>/" + vc, " ", "<yellow>Click para cambiar"));
        }
        inv.setItem(52, Items.of(Material.BARRIER, "<red>Volver"));
    }

    @Override
    public void click(InventoryClickEvent e) {
        PetData d = data(petId);
        PetType t = plugin.manager().type(petId);
        if (d == null || t == null) return;
        Class<?> cls = t.entity().getEntityClass();
        if (cls == null) cls = org.bukkit.entity.Entity.class;
        int slot = e.getRawSlot();

        if (slot == 52) { new DataMenu(plugin, player, petId).open(); return; }

        switch (slot) {
            case 10, 11 -> {
                boolean mount = slot == 10;
                int req = mount ? mountLevel() : hatLevel();
                if (d.level < req) { plugin.msg(player, "locked", "%level%", String.valueOf(req)); return; }
                LivingEntity pet = plugin.manager().isActive(player, petId) ? plugin.manager().activeEntity(player) : null;
                if (pet == null) { plugin.msg(player, "need-active"); return; }
                if (mount) {
                    if (player.getVehicle() == pet) player.leaveVehicle();
                    else { player.removePassenger(pet); pet.addPassenger(player); }
                } else {
                    if (pet.getHeight() > 1.6) { plugin.msg(player, "too-big"); return; }
                    if (player.getPassengers().contains(pet)) player.removePassenger(pet);
                    else { if (player.getVehicle() == pet) player.leaveVehicle(); player.addPassenger(pet); }
                }
                player.closeInventory();
            }
            case 12 -> plugin.listener().awaitName(player, petId);
            case 13 -> { d.upside = !d.upside; changed(); }
            case 14 -> { if (Cosmetics.isAgeable(cls)) { d.baby = !d.baby; changed(); } }
            case 15 -> {
                LivingEntity pet = plugin.manager().isActive(player, petId) ? plugin.manager().activeEntity(player) : null;
                if (pet == null) { plugin.msg(player, "need-active"); return; }
                pet.getWorld().spawnParticle(Particle.HEART, pet.getLocation().add(0, pet.getHeight() + 0.3, 0), 6, 0.4, 0.3, 0.4);
                player.playSound(player.getLocation(), Sound.ENTITY_CAT_PURR, 1f, 1f);
                plugin.msg(player, "petted");
            }
            case 19 -> {
                if (Cosmetics.hasColor(cls)) { d.color = Math.floorMod(d.color + 1, 16); changed(); }
                else if (Cosmetics.variantCount(cls) > 0) { d.variant++; changed(); }
            }
            case 20 -> { if (Cosmetics.hasColor(cls) && Cosmetics.variantCount(cls) > 0) { d.variant++; changed(); } }
            default -> { }
        }
    }

    private void changed() {
        plugin.manager().save(player.getUniqueId());
        plugin.manager().refresh(player);
        build();
    }
}
