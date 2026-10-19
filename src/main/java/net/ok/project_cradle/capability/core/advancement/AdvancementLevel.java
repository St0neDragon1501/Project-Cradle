package net.ok.project_cradle.capability.core.advancement;

import java.util.Locale;

/**
 * Persistent advancement stages for a sacred artist. Herald and Sage are
 * recorded alongside the underlying rank. Sage can begin at Underlord;
 * full Herald and Monarch require peak Archlord development.
 */
public enum AdvancementLevel {
    UNFOUNDED("unfounded", 0),
    FOUNDATION("foundation", 1),
    COPPER("copper", 2),
    IRON("iron", 3),
    JADE("jade", 4),
    LOWGOLD("lowgold", 5),
    HIGHGOLD("highgold", 6),
    TRUEGOLD("truegold", 7),
    UNDERLORD("underlord", 8),
    OVERLORD("overlord", 9),
    ARCHLORD("archlord", 10),
    MONARCH("monarch", 11);

    private final String id;
    private final int order;

    AdvancementLevel(String id, int order) {
        this.id = id;
        this.order = order;
    }

    public String id() {
        return this.id;
    }

    public int order() {
        return this.order;
    }

    /** Advancement reached by meeting the next core-capacity requirement, if any. */
    public AdvancementLevel capacityAdvancement() {
        return switch (this) {
            case FOUNDATION -> COPPER;
            case IRON -> JADE;
            case LOWGOLD -> HIGHGOLD;
            case HIGHGOLD -> TRUEGOLD;
            case JADE -> LOWGOLD;
            default -> null;
        };
    }

    /** Next milestone, including those that require more than capacity. */
    public AdvancementLevel nextAdvancement() {
        return switch (this) {
            case COPPER -> IRON;
            case TRUEGOLD -> UNDERLORD;
            case UNDERLORD -> OVERLORD;
            case OVERLORD -> ARCHLORD;
            default -> capacityAdvancement();
        };
    }

    /** Maximum Madra required to reach the next capacity-based advancement. */
    public double requiredMaxMadra() {
        return switch (this) {
            case FOUNDATION -> 1_200.0;
            case COPPER -> 1_400.0;
            case IRON -> 1_600.0;
            case JADE -> 2_000.0;
            case LOWGOLD -> 2_400.0;
            case HIGHGOLD -> 3_600.0;
            case TRUEGOLD -> 5_000.0;
            case UNDERLORD -> 7_000.0;
            case OVERLORD -> 9_000.0;
            default -> 0.0;
        };
    }

    /** Soulfire required in addition to maximum Madra for Lord advancement. */
    public double requiredSoulfire() {
        return switch (this) {
            case TRUEGOLD -> 1.0;
            case UNDERLORD -> 5.0;
            case OVERLORD -> 10.0;
            default -> 0.0;
        };
    }

    public int techniqueSlots() {
        return switch (this) {
            case UNFOUNDED -> 0;
            case FOUNDATION, COPPER, IRON -> 1;
            case JADE, LOWGOLD, HIGHGOLD -> 3;
            default -> 4;
        };
    }

    /** Permanent extra hearts granted by reaching this stage. */
    public int bonusHearts() {
        return switch (this) {
            case IRON -> 2;
            case UNDERLORD, ARCHLORD -> 3;
            case OVERLORD -> 2;
            default -> 0;
        };
    }

    public static AdvancementLevel fromId(String id) {
        if (id == null || id.isBlank()) {
            return UNFOUNDED;
        }

        String normalized = id.toLowerCase(Locale.ROOT).replace("_", "").replace(" ", "");
        if (normalized.equals("lowgold")) {
            return LOWGOLD;
        }
        if (normalized.equals("highgold")) {
            return HIGHGOLD;
        }
        if (normalized.equals("truegold")) {
            return TRUEGOLD;
        }

        for (AdvancementLevel level : values()) {
            if (level.id.equals(normalized)) {
                return level;
            }
        }
        return UNFOUNDED;
    }
}
