package net.ok.project_cradle.server.spells;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.ok.project_cradle.capability.madra.MadraPassives;
import net.ok.project_cradle.capability.madra.MadraType.Element;
import net.ok.project_cradle.server.progression.DreamPacificationService;

/** Shared ownership and hostile-hit rules for damaging and status-only sacred techniques. */
public final class TechniqueCombatService {
    private TechniqueCombatService() {
    }

    public static boolean hurt(ServerPlayer caster, Entity direct, Entity target, float damage) {
        if (!Float.isFinite(damage) || damage <= 0.0F || !canHit(caster, target)
                || (direct != null && direct.level() != caster.level())) {
            return false;
        }
        Entity source = direct == null ? caster : direct;
        DamageSource magic = caster.damageSources().indirectMagic(source, caster);
        return target.hurtServer(caster.level(), new TechniqueDamageSource(magic, source, caster), damage);
    }

    /**
     * Check before applying a hostile status technique. True means its ordinary effects are allowed,
     * even when the caster has none of these Gold passives. This hook never deals damage or heals.
     */
    public static boolean onEffectHit(ServerPlayer caster, LivingEntity target) {
        if (!canHit(caster, target)) return false;
        if (MadraPassives.has(caster, Element.FIRE)) target.igniteForSeconds(4.0F);
        if (MadraPassives.has(caster, Element.LIGHT)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, true, false, false));
        }
        if (MadraPassives.has(caster, Element.DREAM)) {
            target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0, true, false, false));
            DreamPacificationService.apply(caster, target, 60);
        }
        return true;
    }

    private static boolean canHit(ServerPlayer caster, Entity target) {
        if (caster == null || target == null || !(caster.level() instanceof ServerLevel)
                || !caster.isAlive() || caster.isSpectator() || !target.isAlive()
                || target == caster || target.level() != caster.level()
                || (target instanceof Player player && (player.isSpectator() || !caster.canHarmPlayer(player)))) {
            return false;
        }
        return !(target instanceof LivingEntity living) || !DreamPacificationService.isBlocked(caster, living);
    }

    /** Only damage deliberately routed through hurt is a technique; ordinary Enforcer melee stays melee. */
    public static final class TechniqueDamageSource extends DamageSource {
        private TechniqueDamageSource(DamageSource magic, Entity direct, ServerPlayer caster) {
            super(magic.typeHolder(), direct, caster);
        }
    }
}
