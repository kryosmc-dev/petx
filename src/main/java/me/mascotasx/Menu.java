package me.mascotasx;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class Menu implements InventoryHolder {
    protected final MascotasX plugin;
    protected final Player player;
    protected final Inventory inv;

    protected Menu(MascotasX plugin, Player player, String title) {
        this.plugin = plugin;
        this.player = player;
        this.inv = Bukkit.createInventory(this, 54, Text.c(title));
    }

    protected abstract void build();
    public abstract void click(InventoryClickEvent e);

    public void open() {
        build();
        player.openInventory(inv);
    }

    protected void fill() {
        for (int i = 0; i < 54; i++) inv.setItem(i, Items.filler());
    }

    /** Posicion i dentro del area util: filas 1-5, columnas 1-7 (como en el menu de compras). */
    public static int grid(int i) { return (1 + i / 7) * 9 + 1 + i % 7; }

    protected PetData data(String petId) { return plugin.manager().profile(player.getUniqueId()).owned.get(petId); }

    @Override
    public Inventory getInventory() { return inv; }
}
