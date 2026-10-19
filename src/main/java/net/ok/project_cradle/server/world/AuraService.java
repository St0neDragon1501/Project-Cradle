package net.ok.project_cradle.server.world;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MadraType.Element;

/** Natural aura is sampled by aspect. Pure madra has no environmental aura source. */
public final class AuraService {
    private AuraService() {
    }

    public static Reading detect(ServerPlayer player) {
        var level = player.level();
        Map<Element, Integer> densities = new EnumMap<>(Element.class);
        BlockPos center = player.blockPosition();
        for (int x = -6; x <= 6; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.hasChunkAt(pos)) continue;
                    var state = level.getBlockState(pos);
                    if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.getFluidState().is(FluidTags.LAVA)) add(densities, Element.FIRE, 1);
                    if (state.getFluidState().is(FluidTags.WATER)) add(densities, Element.WATER, 1);
                    if (state.isAir() && level.canSeeSky(pos)) add(densities, Element.AIR, 1);
                    if (!state.isAir() && state.getFluidState().isEmpty() && state.getDestroySpeed(level, pos) >= 0.0F) add(densities, Element.EARTH, 1);
                    if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS)
                            || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK)) add(densities, Element.LIFE, 1);
                    if (state.getLightEmission() >= 10) add(densities, Element.LIGHT, 1);
                }
            }
        }
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.0), entity -> entity.isAlive())) {
            add(densities, Element.LIFE, 12);
            add(densities, Element.BLOOD, 12);
            if (living.getDeltaMovement().lengthSqr() > 0.01) add(densities, Element.FORCE, 12);
        }
        return Reading.of(densities);
    }

    private static void add(Map<Element, Integer> densities, Element aspect, int amount) {
        densities.merge(aspect, amount, Integer::sum);
    }

    public record Reading(Element aspect, int strength, Map<Element, Integer> densities) {
        public Reading {
            densities = Map.copyOf(densities);
        }
        public static Reading of(Map<Element, Integer> samples) {
            Map<Element, Integer> natural = new EnumMap<>(Element.class);
            samples.forEach((aspect, count) -> { if (aspect != Element.PURE && count > 0) natural.put(aspect, count); });
            Element dominant = Element.PURE; // No aura: sentinel only, never an absorbable source.
            int strongest = 0;
            for (var entry : natural.entrySet()) {
                if (entry.getValue() > strongest) { dominant = entry.getKey(); strongest = entry.getValue(); }
            }
            return new Reading(dominant, strongest, natural);
        }
        public boolean hasAura() { return this.strength > 0; }
        public boolean isBalanced() { return this.densities.values().stream().filter(amount -> amount >= 8).count() >= 3; }
        public double madraYield() { return yieldFor(this.strength); }
        public double compatibleYield(MadraType type) {
            int matching = type.getTypes().stream().filter(aspect -> aspect != Element.PURE)
                    .mapToInt(aspect -> this.densities.getOrDefault(aspect, 0)).sum();
            return yieldFor(matching);
        }
        private static double yieldFor(int amount) { return amount <= 0 ? 0.0 : Math.max(1.0, Math.min(20.0, amount / 12.0)); }
    }
}
