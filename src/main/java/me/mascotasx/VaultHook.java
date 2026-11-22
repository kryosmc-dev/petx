package me.mascotasx;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook implements EconomyHook {
    private Economy eco;

    public boolean setup(MascotasX plugin) {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        eco = rsp.getProvider();
        return eco != null;
    }

    @Override public boolean has(Player p, double a) { return eco.has(p, a); }
    @Override public boolean withdraw(Player p, double a) { return eco.withdrawPlayer(p, a).transactionSuccess(); }
    @Override public void deposit(Player p, double a) { eco.depositPlayer(p, a); }
}
