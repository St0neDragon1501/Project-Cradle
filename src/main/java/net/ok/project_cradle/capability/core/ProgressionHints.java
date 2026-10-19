package net.ok.project_cradle.capability.core;

import net.minecraft.network.chat.Component;

/** Read-only descriptions shared by the manual and HUD. Advancement is validated on the server. */
public final class ProgressionHints {
    private ProgressionHints() {
    }
    public static Component iconName(String concept) {
        return Component.literal(java.util.Arrays.stream(concept.split("_"))
                .filter(word -> !word.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" ")));
    }

    public static Component next(Core core) {
        return switch (core.getAdvancement()) {
            case UNFOUNDED, FOUNDATION -> Component.translatable("progression.project_cradle.foundation");
            case COPPER -> Component.translatable("progression.project_cradle.copper", core.getIronPreparation(), ProgressionRules.IRON_PREPARATION_CYCLES);
            case IRON -> Component.translatable("progression.project_cradle.iron", core.getJadeCycling(), ProgressionRules.JADE_SPIRAL_CYCLES);
            case JADE -> Component.translatable("progression.project_cradle.jade", core.getGoldCycling(), ProgressionRules.NATURAL_GOLD_CYCLES);
            case LOWGOLD, HIGHGOLD -> Component.translatable("progression.project_cradle.gold", core.getRemnantMastery(), ProgressionRules.REMNANT_MASTERY_CYCLES);
            case TRUEGOLD -> !core.getIronBodyType().isPerfect()
                    ? Component.translatable("progression.project_cradle.basic_limit")
                    : !core.hasMasteredRemnant() ? Component.translatable("progression.project_cradle.master_remnant", core.getRemnantMastery(), ProgressionRules.REMNANT_MASTERY_CYCLES)
                    : Component.translatable(core.hasSoulspace() ? "progression.project_cradle.past" : "progression.project_cradle.soulspace");
            case UNDERLORD -> Component.translatable("progression.project_cradle.present");
            case OVERLORD -> Component.translatable("progression.project_cradle.future");
            case ARCHLORD -> Component.translatable("progression.project_cradle.archlord", core.getWillpower(), ProgressionRules.HERALD_WILLPOWER);
            case MONARCH -> Component.translatable("progression.project_cradle.monarch");
        };
    }
}
