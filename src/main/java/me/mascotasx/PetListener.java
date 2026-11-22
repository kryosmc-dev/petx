package me.mascotasx;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetListener implements Listener {
    private final MascotasX plugin;
    private final PetManager manager;
    private final Map<UUID, String> naming = new HashMap<>();

    public PetListener(MascotasX plugin) {
        this.plugin = plugin;
        this.manager = plugin.manager();
    }

    public void handleJoin(Player p) {
        Profile pr = manager.profile(p.getUniqueId());
        if (plugin.getConfig().getBoolean("auto-summon", true) && pr.active != null && pr.owned.containsKey(pr.active)) {
            String id = pr.active;
            Bukkit.getScheduler().runTaskLater(plugin, () -> { if (p.isOnline()) manager.summon(p, id); }, 20L);
        }
    }

    @EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent ev) { handleJoin(ev.getPlayer()); }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        naming.remove(ev.getPlayer().getUniqueId());
        manager.unload(ev.getPlayer());
    }

    @EventHandler
    public void onInput(PlayerInputEvent ev) {
        manager.setInput(ev.getPlayer().getUniqueId(), ev.getInput());
    }

    /** Click derecho sobre tu mascota = abre su menu de datos. */
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent ev) {
        if (ev.getHand() != EquipmentSlot.HAND) return;
        UUID owner = manager.ownerOf(ev.getRightClicked());
        if (owner == null) return;
        ev.setCancelled(true);
        Player p = ev.getPlayer();
        if (owner.equals(p.getUniqueId())) {
            Profile pr = manager.profile(p.getUniqueId());
            if (pr.active != null) new DataMenu(plugin, p, pr.active).open();
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent ev) {
        if (manager.isPet(ev.getEntity()) || manager.isPet(ev.getTarget())) ev.setCancelled(true);
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent ev) {
        if (manager.isPet(ev.getEntity())) ev.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageBy(EntityDamageByEntityEvent ev) {
        Entity dmg = ev.getDamager();
        if (dmg instanceof Projectile pr && pr.getShooter() instanceof Entity s) dmg = s;
        Entity victim = ev.getEntity();
        if (manager.isPet(victim) || (manager.isPet(dmg) && victim instanceof Player)) {
            ev.setCancelled(true);
            return;
        }
        if (dmg instanceof Player pl && victim instanceof LivingEntity lv && !(victim instanceof Player)) {
            manager.setTarget(pl, lv);
        } else if (victim instanceof Player pl && dmg instanceof LivingEntity lv) {
            manager.setTarget(pl, lv);
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent ev) {
        if (manager.isPet(ev.getEntity())) ev.setCancelled(true);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent ev) {
        LivingEntity v = ev.getEntity();
        if (v instanceof Player || manager.isPet(v)) return;
        Player owner = null;
        Player killer = v.getKiller();
        if (killer != null && manager.hasActive(killer)) {
            owner = killer;
        } else if (v.getLastDamageCause() instanceof EntityDamageByEntityEvent dbe) {
            UUID o = manager.ownerOf(dbe.getDamager());
            if (o != null) owner = Bukkit.getPlayer(o);
        }
        if (owner != null) manager.reward(owner, v);
    }

    // --------------------------------------------------------------- renombrar por chat
    public void awaitName(Player p, String petId) {
        naming.put(p.getUniqueId(), petId);
        p.closeInventory();
        plugin.msg(p, "name-prompt");
    }

    @EventHandler
    public void onChat(AsyncChatEvent ev) {
        Player p = ev.getPlayer();
        String petId = naming.remove(p.getUniqueId());
        if (petId == null) return;
        ev.setCancelled(true);
        String text = PlainTextComponentSerializer.plainText().serialize(ev.message()).trim();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (text.equalsIgnoreCase("cancelar")) { plugin.msg(p, "name-cancel"); return; }
            int max = plugin.getConfig().getInt("cosmetics.max-name-length", 16);
            String name = text.length() > max ? text.substring(0, max) : text;
            name = name.replace("<", "").replace(">", "");
            if (name.isEmpty()) { plugin.msg(p, "name-cancel"); return; }
            PetData d = manager.profile(p.getUniqueId()).owned.get(petId);
            if (d == null) return;
            d.name = name;
            manager.save(p.getUniqueId());
            manager.refresh(p);
            plugin.msg(p, "name-set", "%name%", name);
            new CosmeticsMenu(plugin, p, petId).open();
        });
    }
}
