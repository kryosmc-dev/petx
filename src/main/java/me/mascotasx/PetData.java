package me.mascotasx;

import java.util.LinkedHashSet;
import java.util.Set;

public class PetData {
    public final String type;
    public int level = 1;
    public double xp = 0;
    public String name;            // nombre personalizado (null = el del tipo)
    public boolean muted, burning, aura, upside, baby;
    public boolean attack = true;
    public int color, variant;
    public final Set<String> effects = new LinkedHashSet<>();

    public PetData(String type) { this.type = type; }

    public String displayName(PetType t) { return name != null ? name : t.name(); }
}
