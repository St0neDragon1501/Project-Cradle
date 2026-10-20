package net.ok.project_cradle.server.spells;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.entities.RemnantEntity;
import net.ok.project_cradle.spells.TechniqueScaling;

/** The same elemental effects run for players, Remnants, and sacred beasts on the server. */
public record ElementalCastingContext(LivingEntity caster, ServerLevel level) {
    public static ElementalCastingContext of(LivingEntity caster) {
        if (caster == null || !caster.isAlive() || caster.isSpectator()
                || !(caster.level() instanceof ServerLevel level)) return null;
        if (!(caster instanceof ServerPlayer) && !(caster instanceof Mob mob && MobTechniqueService.isCaster(mob))) return null;
        return new ElementalCastingContext(caster, level);
    }

    public double power() {
        return caster instanceof ServerPlayer player ? TechniqueScaling.power(player)
                : MobTechniqueService.power((Mob) caster);
    }

    public double range(double base) {
        return caster instanceof ServerPlayer player ? TechniqueScaling.range(player, base)
                : MobTechniqueService.range((Mob) caster, base);
    }

    public int duration(int base) {
        if (caster instanceof ServerPlayer player) return TechniqueScaling.duration(player, base);
        return Math.max(1, (int) Math.round(base * (1.0 + 0.05 * Math.max(0, rank() - 1))));
    }

    public double madraCost(double base) {
        return caster instanceof ServerPlayer player ? TechniqueScaling.madraCost(player, base)
                : Math.round(base * (1.0 + 0.18 * Math.max(0, rank() - 1)) * 100.0) / 100.0;
    }

    private int rank() {
        return caster instanceof RemnantEntity remnant ? remnant.getSourceRank().order()
                : caster.getData(ModAttachments.CORE).getAdvancement().order();
    }

    public boolean isAlly(LivingEntity target) {
        if (target == null || !target.isAlive() || target.isSpectator() || target.level() != level) return false;
        if (target == caster || caster.isAlliedTo(target) || target.isAlliedTo(caster)) return true;
        String partner = caster.getData(ModAttachments.CORE).getBondedPartner();
        if (target.getUUID().toString().equals(partner)
                || caster.getUUID().toString().equals(target.getData(ModAttachments.CORE).getBondedPartner())) return true;
        if (caster instanceof Mob mob && MobTechniqueService.isFriendly(mob, target)) return true;
        LivingEntity owner = target instanceof OwnableEntity pet ? pet.getRootOwner() : null;
        return owner != null && (owner == caster || caster.isAlliedTo(owner)
                || owner.getUUID().toString().equals(partner));
    }

    public boolean canHarm(LivingEntity target) {
        if (isAlly(target) || target instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        return caster instanceof ServerPlayer player ? AdditionalTechniqueSpell.isHostileTarget(player, target)
                : MobTechniqueService.canHarm((Mob) caster, target);
    }

    /** Accept base damage here; each caster's scaling is applied exactly once. */
    public boolean hurt(LivingEntity target, float baseDamage) {
        if (!canHarm(target)) return false;
        return caster instanceof ServerPlayer player ? TechniqueCombatService.hurt(player, caster, target, baseDamage)
                : MobTechniqueService.hurt((Mob) caster, caster, target, (float) (baseDamage * power()));
    }

    public boolean effectHit(LivingEntity target) {
        return canHarm(target) && (!(caster instanceof ServerPlayer player) || TechniqueCombatService.onEffectHit(player, target));
    }

    public Vec3 direction() {
        if (caster instanceof Mob mob) {
            LivingEntity target = mob.getTarget();
            if (target == null) {
                var memory = mob.getBrain().getMemoryInternal(MemoryModuleType.ATTACK_TARGET);
                target = memory == null ? null : memory.orElse(null);
            }
            if (target != null && canHarm(target)) {
                return target.getBoundingBox().getCenter().subtract(caster.getEyePosition()).normalize();
            }
        }
        return caster.getLookAngle().normalize();
    }

    public boolean clearLine(Vec3 from, Vec3 to) {
        return loadedLine(from, to) && AdditionalTechniqueSpell.clearLine(level, from, to, caster);
    }

    public boolean loadedLine(Vec3 from, Vec3 to) {
        if (!Double.isFinite(from.x + from.y + from.z + to.x + to.y + to.z)) return false;
        BlockPos first = BlockPos.containing(from);
        BlockPos last = BlockPos.containing(to);
        if (level.isOutsideBuildHeight(first) || level.isOutsideBuildHeight(last)
                || !level.getWorldBorder().isWithinBounds(first) || !level.getWorldBorder().isWithinBounds(last)) return false;
        int minX = Math.min(first.getX() >> 4, last.getX() >> 4);
        int maxX = Math.max(first.getX() >> 4, last.getX() >> 4);
        int minZ = Math.min(first.getZ() >> 4, last.getZ() >> 4);
        int maxZ = Math.max(first.getZ() >> 4, last.getZ() >> 4);
        // Include every crossed cell, including tiny diagonal crossings at chunk corners.
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if (!level.hasChunkAt(new BlockPos(x << 4, first.getY(), z << 4))) return false;
        }
        return true;
    }

    public Vec3 aimPoint(double baseRange) {
        Vec3 eye = caster.getEyePosition();
        Vec3 end = eye.add(direction().scale(range(baseRange)));
        // Shorten at unloaded chunks and the world border before asking the block ray tracer.
        Vec3 difference = end.subtract(eye);
        Vec3 safe = eye;
        for (double step = 0.5; step <= difference.length(); step += 0.5) {
            Vec3 point = eye.add(difference.normalize().scale(step));
            if (!loadedLine(safe, point)) break;
            safe = point;
        }
        HitResult hit = level.clip(new ClipContext(eye, safe, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return hit.getType() == HitResult.Type.MISS ? hit.getLocation() : hit.getLocation().subtract(direction().scale(0.15));
    }

    public List<LivingEntity> nearby(double baseRange) {
        double radius = range(baseRange);
        return level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius), target ->
                canHarm(target) && caster.distanceToSqr(target) <= radius * radius
                        && clearLine(caster.getEyePosition(), target.getBoundingBox().getCenter()))
                .stream().sorted(Comparator.comparingDouble(caster::distanceToSqr)).limit(32).toList();
    }

    /** A narrow body-intersecting ray chooses what the caster is actually pointing at. */
    public LivingEntity target(double baseRange) {
        Vec3 from = caster.getEyePosition();
        Vec3 end = aimPoint(baseRange);
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(from, end).inflate(0.4), target ->
                canHarm(target) && target.getBoundingBox().inflate(0.25).clip(from, end).isPresent()
                        && clearLine(from, target.getBoundingBox().getCenter()))
                .stream().min(Comparator.comparingDouble(target -> target.getBoundingBox().inflate(0.25)
                        .clip(from, end).orElse(end).distanceToSqr(from))).orElse(null);
    }
}
