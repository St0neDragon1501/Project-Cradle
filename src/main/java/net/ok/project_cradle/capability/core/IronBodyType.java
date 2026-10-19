package net.ok.project_cradle.capability.core;

import java.util.Locale;

/** The quality and specialization of a sacred artist's remade Iron body. */
public enum IronBodyType {
    NONE("none"),
    BASIC("basic"),
    PERFECT("perfect"),
    BLOODFORGED("bloodforged"),
    RAINDROP("raindrop"),
    STEELFORGED("steelforged"),
    PUPPETEER("puppeteer");

    private final String id;

    IronBodyType(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    /** A perfect foundation can endure the soulfire transformation into the Lord realm. */
    public boolean isPerfect() {
        return this != NONE && this != BASIC;
    }

    public String translationKey() {
        return "iron_body.project_cradle." + this.id;
    }

    public static IronBodyType fromId(String id) {
        if (id == null || id.isBlank()) {
            return NONE;
        }
        String normalized = id.toLowerCase(Locale.ROOT)
                .replace("_", "").replace("-", "").replace(" ", "");
        for (IronBodyType type : values()) {
            if (type.id.equals(normalized)) {
                return type;
            }
        }
        return NONE;
    }
}
