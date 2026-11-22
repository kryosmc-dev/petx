package me.mascotasx;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Text {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private Text() {}

    public static Component c(String s) {
        return MM.deserialize(s).decoration(TextDecoration.ITALIC, false);
    }

    public static String bar(double frac, int len) {
        frac = Math.max(0, Math.min(1, frac));
        int f = (int) Math.round(frac * len);
        return "<green>" + "▌".repeat(f) + "<dark_gray>" + "▌".repeat(len - f);
    }

    public static String money(double v) { return "$" + String.format("%,.0f", v); }

    public static String num(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format("%.1f", v);
    }
}
