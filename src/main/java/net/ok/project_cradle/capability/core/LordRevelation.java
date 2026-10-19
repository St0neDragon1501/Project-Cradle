package net.ok.project_cradle.capability.core;

import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;

/** Each Lord transformation asks a different personal question. */
public enum LordRevelation {
    PAST(AdvancementLevel.TRUEGOLD, AdvancementLevel.UNDERLORD),
    PRESENT(AdvancementLevel.UNDERLORD, AdvancementLevel.OVERLORD),
    FUTURE(AdvancementLevel.OVERLORD, AdvancementLevel.ARCHLORD);

    private final AdvancementLevel from;
    private final AdvancementLevel target;

    LordRevelation(AdvancementLevel from, AdvancementLevel target) {
        this.from = from;
        this.target = target;
    }

    public AdvancementLevel target() {
        return this.target;
    }

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public static LordRevelation forStage(AdvancementLevel rank) {
        for (LordRevelation revelation : values()) {
            if (revelation.from == rank) {
                return revelation;
            }
        }
        return null;
    }
}
