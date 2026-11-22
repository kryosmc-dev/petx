package me.mascotasx;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MascotasX extends JavaPlugin {
    private static MascotasX instance;
    private PetManager manager;
    private EconomyHook economy;
    private PetListener listener;

    public static MascotasX get() { return instance; }
    public PetManager manager() { return manager; }
    public EconomyHook economy() { return economy; }
    public PetListener listener() { return listener; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        if (!new File(getDataFolder(), "pets.yml").exists()) saveResource("pets.yml", false);

        if (getConfig().getBoolean("economy.enabled", true) && getServer().getPluginManager().getPlugin("Vault") != null) {
            try {
                VaultHook hook = new VaultHook();
                if (hook.setup(this)) economy = hook;
            } catch (Throwable t) {
                getLogger().warning("No se pudo enlazar con Vault: " + t.getMessage());
            }
        }
        if (economy == null) getLogger().warning("Sin economia (Vault): las mascotas se entregaran gratis.");

        manager = new PetManager(this);
        manager.loadTypes();
        manager.startTasks();

        listener = new PetListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        PetCommand cmd = new PetCommand(this);
        getCommand("mascotas").setExecutor(cmd);
        getCommand("mascotas").setTabCompleter(cmd);

        for (Player p : Bukkit.getOnlinePlayers()) listener.handleJoin(p);
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.shutdown();
    }

    public void reload() {
        reloadConfig();
        manager.loadTypes();
    }

    /** Envia un mensaje de config.yml (messages.<key>) reemplazando pares %clave% -> valor. */
    public void msg(CommandSender to, String key, String... kv) {
        String s = getConfig().getString("messages." + key, key);
        for (int i = 0; i + 1 < kv.length; i += 2) s = s.replace(kv[i], kv[i + 1]);
        to.sendMessage(Text.c(getConfig().getString("messages.prefix", "") + s));
    }
}
