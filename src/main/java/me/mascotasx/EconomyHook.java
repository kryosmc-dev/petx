package me.mascotasx;

import org.bukkit.entity.Player;

public interface EconomyHook {
    boolean has(Player p, double amount);
    boolean withdraw(Player p, double amount);
    void deposit(Player p, double amount);
}
