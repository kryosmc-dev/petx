package me.mascotasx;

import java.util.LinkedHashMap;
import java.util.Map;

public class Profile {
    public final Map<String, PetData> owned = new LinkedHashMap<>();
    public String active; // id de la mascota invocada (null = ninguna)
}
