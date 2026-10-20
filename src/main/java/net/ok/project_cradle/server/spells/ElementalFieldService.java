package net.ok.project_cradle.server.spells;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.spells.ElementalTechnique;

/** Short-lived world fields shared by player, Remnant, and sacred beast casters. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class ElementalFieldService {
    private static final Map<ServerLevel, List<Field>> ACTIVE = new WeakHashMap<>();

    private ElementalFieldService() { }

    public static boolean create(ElementalCastingContext context, ElementalTechnique technique, Vec3 center) {
        if (context == null || center == null || !finite(center)
                || (technique != ElementalTechnique.BLOOM_FIELD
                        && technique != ElementalTechnique.BLADE_TEMPEST)) return false;
        ServerLevel level = context.level();
        double radius = context.range(technique.baseRange());
        if (center.distanceToSqr(context.caster().position()) > radius * radius
                || !loadedArea(level, center, radius)) return false;
        if (technique == ElementalTechnique.BLOOM_FIELD) {
            Vec3 from = center.add(0, 0.5, 0);
            Vec3 to = new Vec3(center.x, Math.max(level.getMinY() + 0.01, center.y - 6), center.z);
            if (!context.loadedLine(from, to)) return false;
            HitResult ground = level.clip(new ClipContext(from, to,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, context.caster()));
            if (ground.getType() != HitResult.Type.BLOCK) return false;
            center = ground.getLocation().add(0, 0.08, 0);
        }
        if (level.isOutsideBuildHeight(BlockPos.containing(center))
                || !level.getWorldBorder().isWithinBounds(BlockPos.containing(center))
                || !context.clearLine(context.caster().getEyePosition(), center.add(0, 0.5, 0))) return false;

        List<Field> fields = ACTIVE.computeIfAbsent(level, ignored -> new ArrayList<>());
        UUID owner = context.caster().getUUID();
        fields.removeIf(field -> field.owner.equals(owner) && field.technique == technique);
        long now = level.getGameTime();
        int interval = technique == ElementalTechnique.BLADE_TEMPEST ? context.duration(20) : 20;
        int lifetime = technique == ElementalTechnique.BLOOM_FIELD ? context.duration(120) : interval * 2 + 5;
        Field field = new Field(owner, technique, center, radius, now + lifetime, now + interval, interval);
        fields.add(field);
        pulse(context, field);
        field.pulses = 1;
        draw(context, field, now);
        return true;
    }

    public static boolean hasActive(LivingEntity caster, ElementalTechnique technique) {
        if (!(caster.level() instanceof ServerLevel level) || !caster.isAlive() || caster.isRemoved()) return false;
        List<Field> fields = ACTIVE.get(level);
        return fields != null && fields.stream().anyMatch(field -> field.owner.equals(caster.getUUID())
                && field.technique == technique && !field.invalidated && field.expiresAt > level.getGameTime());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        List<Field> fields = ACTIVE.get(level);
        if (fields == null) return;
        long now = level.getGameTime();
        Iterator<Field> iterator = fields.iterator();
        while (iterator.hasNext()) {
            Field field = iterator.next();
            LivingEntity owner = level.getEntity(field.owner) instanceof LivingEntity living ? living : null;
            ElementalCastingContext context = owner == null ? null : ElementalCastingContext.of(owner);
            if (context != null && field.technique == ElementalTechnique.BLADE_TEMPEST) {
                field.center = owner.position();
            }
            if (field.invalidated || now >= field.expiresAt || context == null || context.level() != level
                    || owner.isRemoved() || !owner.isAlive() || owner.isSpectator()
                    || !loadedArea(level, field.center, field.radius)
                    || owner.position().distanceToSqr(field.center) > square(context.range(32.0))) {
                iterator.remove();
                continue;
            }
            if (now >= field.nextPulseAt
                    && (field.technique != ElementalTechnique.BLADE_TEMPEST || field.pulses < 3)) {
                pulse(context, field);
                field.pulses++;
                field.nextPulseAt = now + field.interval;
            }
            if (now % 10 == 0) draw(context, field, now);
        }
        if (fields.isEmpty()) ACTIVE.remove(level);
    }

    private static void pulse(ElementalCastingContext context, Field field) {
        Vec3 center = field.center;
        AABB bounds = AABB.ofSize(center.add(0, 1, 0), field.radius * 2, 4.0, field.radius * 2);
        for (LivingEntity target : context.level().getEntitiesOfClass(LivingEntity.class, bounds,
                entity -> entity.isAlive() && !entity.isSpectator()
                        && horizontalDistanceSquared(entity.position(), center) <= square(field.radius)
                        && context.clearLine(context.caster().getEyePosition(), entity.getEyePosition())
                        && context.clearLine(center.add(0, 0.5, 0), entity.getEyePosition()))) {
            if (field.technique == ElementalTechnique.BLOOM_FIELD) {
                if (!context.isAlly(target) || target.getHealth() >= target.getMaxHealth()) continue;
                float before = target.getHealth();
                target.heal((float) (1.5 * context.power()));
                if (target.getHealth() > before) context.level().sendParticles(ParticleTypes.HEART,
                        target.getX(), target.getY() + target.getBbHeight() * 0.7, target.getZ(),
                        2, 0.2, 0.2, 0.2, 0.01);
            } else if (context.canHarm(target) && context.hurt(target, 3.0F)) {
                context.level().sendParticles(ParticleTypes.SWEEP_ATTACK,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private static void draw(ElementalCastingContext context, Field field, long now) {
        if (field.technique == ElementalTechnique.BLOOM_FIELD) {
            TechniqueVisualService.sigil(context.level(), field.center, field.radius,
                    field.technique.aspect(), now * 0.025, 24);
            context.level().sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    field.center.x, field.center.y + 0.25, field.center.z,
                    4, field.radius * 0.45, 0.1, field.radius * 0.45, 0.0);
            return;
        }
        // Four orbiting blades, each drawn as a thin three-mote line.
        DustParticleOptions dust = new DustParticleOptions(TechniqueVisualService.color(field.technique.aspect()), 0.8F);
        for (int blade = 0; blade < 4; blade++) {
            double angle = now * 0.16 + blade * Math.PI * 0.5;
            for (int point = 0; point < 3; point++) {
                double radius = field.radius * (0.65 + point * 0.12);
                Vec3 position = field.center.add(Math.cos(angle) * radius, 0.5 + point * 0.15,
                        Math.sin(angle) * radius);
                context.level().sendParticles(dust, position.x, position.y, position.z, 1, 0, 0, 0, 0);
            }
        }
        TechniqueVisualService.sigil(context.level(), field.center.add(0, 0.1, 0), field.radius,
                field.technique.aspect(), now * 0.07, 16);
    }

    /** Reject field edges in unloaded chunks before rays can request block data. */
    private static boolean loadedArea(ServerLevel level, Vec3 center, double radius) {
        if (!finite(center) || !Double.isFinite(radius) || radius <= 0.0 || radius > 64.0) return false;
        int minX = BlockPos.containing(center.add(-radius, 0, 0)).getX() >> 4;
        int maxX = BlockPos.containing(center.add(radius, 0, 0)).getX() >> 4;
        int minZ = BlockPos.containing(center.add(0, 0, -radius)).getZ() >> 4;
        int maxZ = BlockPos.containing(center.add(0, 0, radius)).getZ() >> 4;
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if (!level.hasChunkAt(new BlockPos(x << 4, (int) center.y, z << 4))) return false;
        }
        return true;
    }

    private static double horizontalDistanceSquared(Vec3 first, Vec3 second) {
        return square(first.x - second.x) + square(first.z - second.z);
    }

    private static double square(double value) { return value * value; }

    private static boolean finite(Vec3 position) {
        return Double.isFinite(position.x) && Double.isFinite(position.y) && Double.isFinite(position.z);
    }

    // Mark in place: a death or removal event may fire inside a damaging field's tick.
    private static void invalidate(UUID owner) {
        for (List<Field> fields : ACTIVE.values()) for (Field field : fields) {
            if (field.owner.equals(owner)) field.invalidated = true;
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level() instanceof ServerLevel) invalidate(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel) invalidate(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level() instanceof ServerLevel) invalidate(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity().level() instanceof ServerLevel) invalidate(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) ACTIVE.remove(level);
    }

    @SubscribeEvent
    public static void onStop(ServerStoppedEvent event) { ACTIVE.clear(); }

    /** UUIDs avoid retaining removed entities or unloaded levels through field values. */
    private static final class Field {
        private final UUID owner;
        private final ElementalTechnique technique;
        private Vec3 center;
        private final double radius;
        private final long expiresAt;
        private final int interval;
        private long nextPulseAt;
        private int pulses;
        private boolean invalidated;

        private Field(UUID owner, ElementalTechnique technique, Vec3 center, double radius,
                long expiresAt, long nextPulseAt, int interval) {
            this.owner = owner;
            this.technique = technique;
            this.center = center;
            this.radius = radius;
            this.expiresAt = expiresAt;
            this.nextPulseAt = nextPulseAt;
            this.interval = interval;
        }
    }
}
