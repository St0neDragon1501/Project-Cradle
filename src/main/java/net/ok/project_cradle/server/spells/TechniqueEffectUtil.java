package net.ok.project_cradle.server.spells;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.ok.project_cradle.capability.madra.MadraType;

/** Particle effects shared by aspect-aligned techniques. */
final class TechniqueEffectUtil {
    private TechniqueEffectUtil() {
    }

    static void emitAround(ServerLevel level, MadraType.Element aspect, Entity entity, int count) {
        level.sendParticles(particleFor(aspect), entity.getX(), entity.getY() + entity.getBbHeight() * 0.5,
                entity.getZ(), count, 0.45, entity.getBbHeight() * 0.4, 0.45, 0.04);
    }

    private static ParticleOptions particleFor(MadraType.Element aspect) {
        return switch (aspect) {
            case FIRE -> ParticleTypes.FLAME;
            case WATER -> ParticleTypes.SPLASH;
            case AIR -> ParticleTypes.CLOUD;
            case EARTH -> new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
            case PURE -> ParticleTypes.ENCHANT;
            case LIFE -> ParticleTypes.HAPPY_VILLAGER;
            case DEATH -> ParticleTypes.SOUL;
            case LIGHT -> ParticleTypes.END_ROD;
            case SHADOW -> ParticleTypes.SMOKE;
            case BLOOD -> ParticleTypes.DAMAGE_INDICATOR;
            case DREAM -> ParticleTypes.WITCH;
            case DESTRUCTION -> ParticleTypes.EXPLOSION;
            case FORCE -> ParticleTypes.GUST;
            case SWORD -> ParticleTypes.CRIT;
            case HUNGER -> ParticleTypes.SQUID_INK;
        };
    }
}
