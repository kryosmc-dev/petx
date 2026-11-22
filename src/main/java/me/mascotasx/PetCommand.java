package me.mascotasx;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PetCommand implements CommandExecutor, TabCompleter {
    private final MascotasX plugin;

    public PetCommand(MascotasX plugin) { this.plugin = plugin; }

    public static void openPets(MascotasX plugin, Player p) {
        Profile pr = plugin.manager().profile(p.getUniqueId());
        if (pr.owned.isEmpty()) { plugin.msg(p, "no-pets"); return; }
        new PetsMenu(plugin, p, 1).open();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        boolean admin = sender.hasPermission("mascotas.admin");

        switch (sub) {
            case "reload" -> {
                if (!admin) { plugin.msg(sender, "no-perm"); return true; }
                plugin.reload();
                plugin.msg(sender, "reload");
            }
            case "dar" -> {
                if (!admin) { plugin.msg(sender, "no-perm"); return true; }
                if (args.length < 3) { sender.sendMessage("/mascotas dar <jugador> <id>"); return true; }
                Player t = Bukkit.getPlayerExact(args[1]);
                PetType pt = plugin.manager().type(args[2].toLowerCase(Locale.ROOT));
                if (t == null || pt == null) { sender.sendMessage("Jugador o mascota no encontrados."); return true; }
                plugin.manager().give(t, pt);
                plugin.msg(sender, "given", "%pet%", pt.name(), "%player%", t.getName());
            }
            case "nivel" -> {
                if (!admin) { plugin.msg(sender, "no-perm"); return true; }
                if (args.length < 3) { sender.sendMessage("/mascotas nivel <jugador> <nivel>"); return true; }
                Player t = Bukkit.getPlayerExact(args[1]);
                if (t == null) { sender.sendMessage("Jugador no encontrado."); return true; }
                Profile pr = plugin.manager().profile(t.getUniqueId());
                PetData d = pr.active == null ? null : pr.owned.get(pr.active);
                if (d == null) { sender.sendMessage("Ese jugador no tiene una mascota invocada."); return true; }
                try {
                    d.level = Math.max(1, Math.min(plugin.manager().maxLevel(), Integer.parseInt(args[2])));
                    d.xp = 0;
                } catch (NumberFormatException ex) { sender.sendMessage("Nivel invalido."); return true; }
                plugin.manager().refresh(t);
                plugin.manager().save(t.getUniqueId());
                sender.sendMessage("Nivel actualizado.");
            }
            default -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("Solo jugadores."); return true; }
                switch (sub) {
                    case "comprar", "tienda" -> new BuyMenu(plugin, p, 1).open();
                    case "guardar" -> {
                        Profile pr = plugin.manager().profile(p.getUniqueId());
                        if (pr.active != null && plugin.manager().hasActive(p)) plugin.manager().toggle(p, pr.active);
                    }
                    case "" -> openPets(plugin, p);
                    default -> plugin.msg(p, "usage");
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            for (String o : List.of("comprar", "guardar")) if (o.startsWith(a[0].toLowerCase())) out.add(o);
            if (s.hasPermission("mascotas.admin"))
                for (String o : List.of("dar", "nivel", "reload")) if (o.startsWith(a[0].toLowerCase())) out.add(o);
        } else if (a.length == 2 && s.hasPermission("mascotas.admin")) {
            for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        } else if (a.length == 3 && a[0].equalsIgnoreCase("dar")) {
            for (PetType t : plugin.manager().types()) out.add(t.id());
        }
        return out;
    }
}
