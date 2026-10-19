package net.ok.project_cradle.capability.core;

import java.util.Locale;

/** The source of the power that carried an artist into the Gold realm. */
public enum GoldRoute {
    NONE("none"),
    NATURAL("natural"),
    REMNANT("remnant"),
    CONTRACT("contract");

    private final String id;

    GoldRoute(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public static GoldRoute fromId(String id) {
        if (id == null) {
            return NONE;
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (GoldRoute route : values()) {
            if (route.id.equals(normalized)) {
                return route;
            }
        }
        return NONE;
    }
}
