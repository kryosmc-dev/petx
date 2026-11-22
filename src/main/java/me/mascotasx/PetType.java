package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public record PetType(String id, EntityType entity, String name, double price, double damage,
                      Material icon, int page, int slot, boolean flying) {
}
