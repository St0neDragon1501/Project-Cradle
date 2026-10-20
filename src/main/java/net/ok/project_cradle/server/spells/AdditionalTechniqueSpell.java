package net.ok.project_cradle.server.spells;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.ok.project_cradle.entities.SpellBoltProjectile;
import net.ok.project_cradle.spells.AdditionalTechnique;
import net.ok.project_cradle.spells.TechniqueScaling;

/** Completion-only spell execution. Windup, cancellation, and input are handled by TechniqueCastingService. */
public final class AdditionalTechniqueSpell {
    private AdditionalTechniqueSpell() {
    }

    public static boolean cast(ServerPlayer player, ItemStack stack, AdditionalTechnique technique) {
        return MadraTechniqueSpell.cast(player, stack, technique.madraCost(), technique.cooldownTicks(),
                (level, caster) -> switch (technique) {
                    case SHIELD -> ShieldSpell.cast(caster);
                    case FIREBALL, ICE_LANCE -> launch(level, caster, stack, technique);
                    case CHAIN_LIGHTNING -> chainLightning(level, caster);
                    case BLINK -> blink(level, caster);
                    case HEALING_PULSE -> healingPulse(level, caster);
                    case COUNTERSPELL -> counterspell(level, caster);
                    case GRAVITY_WELL -> GravityWellService.create(level, caster, aimPoint(level, caster, 12.0));
                });
    }

    private static boolean launch(ServerLevel level, ServerPlayer player, ItemStack stack,
            AdditionalTechnique technique) {
        SpellBoltProjectile projectile = new SpellBoltProjectile(level, player, stack.copyWithCount(1));
        projectile.setTechnique(technique);
        projectile.setSpellDamage(technique == AdditionalTechnique.FIREBALL ? 10.0F : 7.0F);
        projectile.setMaxTravelDistance(TechniqueScaling.range(player, technique == AdditionalTechnique.FIREBALL ? 24.0 : 30.0));
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                technique == AdditionalTechnique.FIREBALL ? 0.8F : 1.65F, 0.0F);
        return level.addFreshEntity(projectile);
    }

    private static boolean chainLightning(ServerLevel level, ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double range = TechniqueScaling.range(player, 18.0);
        double jumpRange = TechniqueScaling.range(player, 5.0);
        LivingEntity first = level.getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(range), target -> isHostileTarget(player, target)
                                && player.hasLineOfSight(target)
                                && inCone(eye, look, target.getEyePosition(), range, 0.94))
                .stream().min(Comparator.comparingDouble((LivingEntity target) ->
                        1.0 - target.getEyePosition().subtract(eye).normalize().dot(look))
                        .thenComparingDouble(player::distanceToSqr)).orElse(null);
        if (first == null) return fail(player, "spell_no_target");

        Set<LivingEntity> struck = new HashSet<>();
        LivingEntity target = first;
        Vec3 from = eye;
        for (int jump = 0; jump < 5 && target != null; jump++) {
            lightning(level, from, target.getEyePosition());
            TechniqueCombatService.hurt(player, player, target, Math.max(3.0F, 8.0F - jump));
            struck.add(target);
            LivingEntity previous = target;
            from = previous.getEyePosition();
            target = level.getEntitiesOfClass(LivingEntity.class, previous.getBoundingBox().inflate(jumpRange),
                            candidate -> !struck.contains(candidate) && isHostileTarget(player, candidate)
                                    && candidate.distanceToSqr(previous) <= jumpRange * jumpRange
                                    && previous.hasLineOfSight(candidate))
                    .stream().min(Comparator.comparingDouble(previous::distanceToSqr)).orElse(null);
        }
        level.playSound(null, first.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS, 0.55F, 1.7F);
        return true;
    }

    private static boolean blink(ServerLevel level, ServerPlayer player) {
        if (player.isPassenger() || player.isSleeping()) return fail(player, "blink_blocked");
        Vec3 start = player.position();
        Vec3 direction = player.getLookAngle();
        Vec3 eye = player.getEyePosition();
        double maximumRange = TechniqueScaling.range(player, 8.0);
        HitResult wall = level.clip(new ClipContext(eye, eye.add(direction.scale(maximumRange)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double range = wall.getType() == HitResult.Type.MISS ? maximumRange
                : Math.max(0.0, wall.getLocation().distanceTo(eye) - 0.4);
        Vec3 destination = start;
        // Check every step in order, including the player's full body. Never skip a
        // collision to reach an empty space behind a wall or a low opening.
        for (double distance = 0.25; distance <= range; distance += 0.25) {
            Vec3 step = start.add(direction.scale(distance));
            AABB box = player.getBoundingBox().move(step.subtract(start));
            if (!level.hasChunkAt(BlockPos.containing(step))
                    || level.isOutsideBuildHeight(BlockPos.containing(step))
                    || level.isOutsideBuildHeight(BlockPos.containing(step.add(0, player.getBbHeight(), 0)))
                    || !level.getWorldBorder().isWithinBounds(box) || !level.noCollision(player, box)) break;
            destination = step;
        }
        if (destination.distanceToSqr(start) < 0.25) return fail(player, "blink_blocked");
        level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 0.8, player.getZ(),
                24, 0.3, 0.6, 0.3, 0.12);
        player.teleportTo(destination.x, destination.y, destination.z);
        player.resetFallDistance();
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, destination.x, destination.y + 0.8, destination.z,
                24, 0.3, 0.6, 0.3, 0.06);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.65F, 1.25F);
        return true;
    }

    private static boolean healingPulse(ServerLevel level, ServerPlayer player) {
        boolean healed = false;
        double radius = TechniqueScaling.range(player, 6.0);
        for (ServerPlayer target : level.getEntitiesOfClass(ServerPlayer.class,
                player.getBoundingBox().inflate(radius), ally -> ally.isAlive() && !ally.isSpectator()
                        && (ally == player || player.isAlliedTo(ally)) && player.hasLineOfSight(ally)
                        && player.distanceToSqr(ally) <= radius * radius)) {
            if (target.getHealth() >= target.getMaxHealth()) continue;
            float before = target.getHealth();
            target.heal(TechniqueScaling.damage(player, 4.0F));
            if (target.getHealth() <= before) continue;
            healed = true;
            level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + 1.0, target.getZ(),
                    5, 0.4, 0.4, 0.4, 0.01);
        }
        if (!healed) return fail(player, "healing_full");
        ring(level, player.position().add(0, 0.2, 0), radius, ParticleTypes.HAPPY_VILLAGER, 40);
        return true;
    }

    private static boolean counterspell(ServerLevel level, ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        boolean cancelled = false;
        double range = TechniqueScaling.range(player, 12.0);
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(range),
                spell -> spell.isAlive() && spell.getOwner() != player
                        && !(spell.getOwner() instanceof LivingEntity owner && player.isAlliedTo(owner))
                        && inCone(eye, look, spell.position(), range, 0.75)
                        && clearLine(level, eye, spell.position(), player))) {
            // A counterspell follows the same PvP permission as the incoming owner's attack.
            if (projectile.getOwner() instanceof Player owner && !player.canHarmPlayer(owner)) continue;
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, projectile.getX(), projectile.getY(), projectile.getZ(),
                    8, 0.15, 0.15, 0.15, 0.03);
            projectile.discard();
            cancelled = true;
        }
        for (ServerPlayer caster : level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(range),
                target -> isHostileTarget(player, target) && player.hasLineOfSight(target)
                        && inCone(eye, look, target.getEyePosition(), range, 0.75))) {
            if (TechniqueCastingService.cancel(caster)) {
                cancelled = true;
                lightning(level, eye, caster.getEyePosition());
            }
        }
        for (Mob caster : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(range),
                target -> MobTechniqueService.isCaster(target) && isHostileTarget(player, target) && player.hasLineOfSight(target)
                        && inCone(eye, look, target.getEyePosition(), range, 0.75))) {
            if (MobTechniqueService.cancel(caster)) {
                cancelled = true;
                lightning(level, eye, caster.getEyePosition());
            }
        }
        if (!cancelled) return fail(player, "counterspell_empty");
        ring(level, player.position().add(0, 0.7, 0), 1.5, ParticleTypes.ENCHANT, 24);
        return true;
    }

    /** Safe shared targeting for area damage, control, and chain selection. */
    public static boolean isHostileTarget(ServerPlayer caster, LivingEntity target) {
        return TechniqueCombatService.canAffect(caster, target) && target != caster && target.isAlive() && !target.isSpectator()
                && target.level() == caster.level() && !caster.isAlliedTo(target)
                && (!(target instanceof Player other) || caster.canHarmPlayer(other));
    }

    static Vec3 aimPoint(ServerLevel level, ServerPlayer player, double range) {
        range = TechniqueScaling.range(player, range);
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        HitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(range)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS ? hit.getLocation()
                : hit.getLocation().subtract(look.scale(0.3));
    }

    static boolean clearLine(ServerLevel level, Vec3 from, Vec3 to, LivingEntity context) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, context)).getType() == HitResult.Type.MISS;
    }

    private static boolean inCone(Vec3 from, Vec3 direction, Vec3 to, double range, double minimumDot) {
        Vec3 delta = to.subtract(from);
        return delta.lengthSqr() <= range * range
                && (delta.lengthSqr() < 0.01 || delta.normalize().dot(direction) >= minimumDot);
    }

    static void lightning(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 difference = to.subtract(from);
        int steps = Math.max(2, (int) (difference.length() * 5.0));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = from.add(difference.scale(i / (double) steps));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                    1, 0.035, 0.035, 0.035, 0.015);
        }
    }

    static void ring(ServerLevel level, Vec3 center, double radius, ParticleOptions particle, int samples) {
        for (int i = 0; i < samples; i++) {
            double angle = Math.PI * 2.0 * i / samples;
            level.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y,
                    center.z + Math.sin(angle) * radius, 1, 0.02, 0.02, 0.02, 0.01);
        }
    }

    private static boolean fail(ServerPlayer player, String key) {
        player.sendOverlayMessage(Component.translatable("message.project_cradle." + key));
        return false;
    }
}
