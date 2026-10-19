package net.ok.project_cradle.server.spells;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MadraPassives;
import net.ok.project_cradle.entities.BasicStrikerProjectile;

/** Aspect-specific impact rules for the Basic Striker technique. */
public final class MadraAspectEffects {
    private MadraAspectEffects() {
    }

    public static float bonusDamage(MadraType.Element aspect) {
        return switch (aspect) {
            case DESTRUCTION -> 2.0F;
            case SWORD -> 1.5F;
            case HUNGER -> 1.0F;
            default -> 0.0F;
        };
    }

    public static void apply(BasicStrikerProjectile projectile, Entity target) {
        if (!(projectile.level() instanceof ServerLevel) || target.level() != projectile.level()) {
            return;
        }
        MadraType.Element aspect = projectile.getMadraElement();
        LivingEntity livingTarget = target instanceof LivingEntity living ? living : null;
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity living ? living : null;

        switch (aspect) {
            case FIRE -> {
                if (livingTarget != null) {
                    livingTarget.igniteForSeconds(3.0F);
                }
            }
            case WATER -> addEffect(livingTarget, MobEffects.SLOWNESS, 60, 0);
            case AIR -> { }
            case EARTH -> addEffect(livingTarget, MobEffects.WEAKNESS, 40, 0);
            case PURE -> addEffect(livingTarget, MobEffects.WEAKNESS, 60, 0);
            case LIFE -> {
                if (owner != null) {
                    owner.heal(1.0F);
                }
            }
            case DEATH -> addEffect(livingTarget, MobEffects.WITHER, 40, 0);
            case LIGHT -> addEffect(livingTarget, MobEffects.GLOWING, 100, 0);
            case SHADOW -> addEffect(livingTarget, MobEffects.DARKNESS, 40, 0);
            case BLOOD -> addEffect(livingTarget, MobEffects.POISON, 60, 0);
            case DREAM -> addEffect(livingTarget, MobEffects.NAUSEA, 80, 0);
            case DESTRUCTION -> { }
            case FORCE -> { }
            case SWORD -> { }
            case HUNGER -> {
                if (owner != null && (!(owner instanceof ServerPlayer player)
                        || !MadraPassives.has(player, MadraType.Element.HUNGER))) {
                    owner.heal(1.0F);
                }
                if (target instanceof ServerPlayer player) {
                    var madra = player.getData(ModAttachments.MADRA);
                    madra.trySpendMadra(2.0);
                    player.syncData(ModAttachments.MADRA);
                }
            }
        }

        double knockback = switch (aspect) {
            case AIR -> 0.75;
            case FORCE -> 1.0;
            case EARTH -> 0.15;
            default -> 0.3;
        };
        push(projectile, target, knockback);
    }

    private static void addEffect(LivingEntity target,
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int duration, int amplifier) {
        if (target != null) {
            target.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, false));
        }
    }

    private static void push(BasicStrikerProjectile projectile, Entity target, double strength) {
        var direction = projectile.getDeltaMovement().normalize();
        target.push(direction.x * strength, 0.12 + strength * 0.12, direction.z * strength);
        target.hurtMarked = true;
    }
}
