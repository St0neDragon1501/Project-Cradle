package net.ok.project_cradle.capability.core;

import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;

/** Playable training thresholds, rather than numerical claims about the novels. */
public final class ProgressionRules {
    public static final int IRON_PREPARATION_CYCLES = 20;
    public static final int JADE_SPIRAL_CYCLES = 40;
    public static final int NATURAL_GOLD_CYCLES = 200;
    public static final int REMNANT_MASTERY_CYCLES = 100;
    public static final int ICON_WILLPOWER = 100;
    public static final int HERALD_WILLPOWER = 200;
    public static final double IRON_MADRA = 1_400.0;
    public static final double PEAK_ARCHLORD_MADRA = 12_000.0;

    private ProgressionRules() {
    }

    public static boolean canBecomeMonarch(AdvancementLevel rank, boolean sage, boolean herald) {
        return rank.order() >= AdvancementLevel.ARCHLORD.order() && sage && herald;
    }
}
