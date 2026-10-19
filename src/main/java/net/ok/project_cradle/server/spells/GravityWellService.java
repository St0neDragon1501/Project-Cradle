package net.ok.project_cradle.server.spells;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.spells.TechniqueScaling;

/** Five-second force fields are ephemeral world effects, never saved or ticked on the client. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class GravityWellService {
    private static final Map<ServerLevel, List<Well>> ACTIVE = new WeakHashMap<>();
    private static final double RADIUS = 4.0;

    private GravityWellService() {
    }

    public static boolean create(ServerLevel level, ServerPlayer owner, Vec3 center) {
        if (!level.hasChunkAt(BlockPos.containing(center))
                || level.isOutsideBuildHeight(BlockPos.containing(center))
                || !level.getWorldBorder().isWithinBounds(BlockPos.containing(center))) return false;
        List<Well> wells = ACTIVE.computeIfAbsent(level, ignored -> new ArrayList<>());
        // Recasting replaces one's own field; a player can never accumulate permanent wells.
        wells.removeIf(well -> well.owner().equals(owner.getUUID()));
        double radius = TechniqueScaling.range(owner, RADIUS);
        wells.add(new Well(owner.getUUID(), center, radius, level.getGameTime() + TechniqueScaling.duration(owner, 100)));
        AdditionalTechniqueSpell.ring(level, center, radius, ParticleTypes.REVERSE_PORTAL, 40);
        return true;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        List<Well> wells = ACTIVE.get(level);
        if (wells == null) return;
        long now = level.getGameTime();
        Iterator<Well> iterator = wells.iterator();
        while (iterator.hasNext()) {
            Well well = iterator.next();
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(well.owner());
            if (now >= well.expiresAt() || owner == null || owner.level() != level
                    || !owner.isAlive() || owner.isSpectator()
                    || !level.hasChunkAt(BlockPos.containing(well.center()))) {
                iterator.remove();
                continue;
            }
            tick(level, owner, well, now);
        }
        if (wells.isEmpty()) ACTIVE.remove(level);
    }

    private static void tick(ServerLevel level, ServerPlayer owner, Well well, long now) {
        Vec3 center = well.center();
        double radius = well.radius();
        if (now % 5 == 0) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z,
                    9, 0.4, 0.4, 0.4, 0.08);
            AdditionalTechniqueSpell.ring(level, center, radius, ParticleTypes.ENCHANT, 20);
        }
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                AABB.ofSize(center, radius * 2, radius * 2, radius * 2),
                entity -> AdditionalTechniqueSpell.isHostileTarget(owner, entity)
                        && entity.position().distanceToSqr(center) <= radius * radius
                        && owner.hasLineOfSight(entity)
                        && AdditionalTechniqueSpell.clearLine(level, center, entity.getEyePosition(), owner))) {
            Vec3 delta = center.subtract(target.getBoundingBox().getCenter());
            if (delta.lengthSqr() > 0.16) {
                Vec3 pull = delta.normalize().scale(0.10 * TechniqueScaling.power(owner));
                target.push(pull.x, pull.y, pull.z);
                target.hurtMarked = true;
            }
            if ((well.expiresAt() - now) % 20 == 0) {
                TechniqueCombatService.hurt(owner, owner, target, 3.0F);
            }
        }
    }

    private record Well(UUID owner, Vec3 center, double radius, long expiresAt) {
    }
}
