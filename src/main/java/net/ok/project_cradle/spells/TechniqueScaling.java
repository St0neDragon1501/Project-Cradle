package net.ok.project_cradle.spells;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;

/** Shared progression math. Refinement is core capacity earned toward the next rank. */
public final class TechniqueScaling {
    private static final double[] CAPACITY = {1_000, 1_000, 1_200, 1_400, 1_600,
            2_000, 2_400, 3_600, 5_000, 7_000, 9_000, 12_000, 16_000};

    private TechniqueScaling() { }

    public static double refinement(Player player) {
        int stage = player.getData(ModAttachments.CORE).getAdvancement().order();
        if (stage <= 0) return 0.0;
        double capacity = player.getData(ModAttachments.MAXMADRA).getLevel();
        if (!Double.isFinite(capacity)) return 0.0;
        return Mth.clamp((capacity - CAPACITY[stage]) / (CAPACITY[stage + 1] - CAPACITY[stage]), 0.0, 1.0);
    }

    private static double development(Player player) {
        return Math.max(0, player.getData(ModAttachments.CORE).getAdvancement().order() - 1) + refinement(player);
    }

    public static double power(Player player) {
        Core core = player.getData(ModAttachments.CORE);
        return 1.0 + 0.18 * development(player) + (core.hasSage() ? 0.10 : 0.0) + (core.hasHerald() ? 0.10 : 0.0);
    }

    public static float damage(Player player, float base) {
        return (float) (base * power(player));
    }

    /** The payable cost uses the same power multiplier as damage, rounded to visible madra precision. */
    public static double madraCost(Player player, double base) {
        return Math.round(base * power(player) * 100.0) / 100.0;
    }

    public static double range(Player player, double base) {
        return base * (1.0 + 0.08 * development(player));
    }

    public static int duration(Player player, int baseTicks) {
        return Math.max(1, (int) Math.round(baseTicks * (1.0 + 0.05 * development(player))));
    }
}
