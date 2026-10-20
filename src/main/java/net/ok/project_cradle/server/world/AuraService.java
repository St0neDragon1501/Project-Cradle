package net.ok.project_cradle.server.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MadraType.Element;

/** Natural sources, shared depletion, and location-dependent gathering. Pure madra remains internal. */
public final class AuraService {
    public static final int GATHER_RADIUS = 4;

    private AuraService() { }

    public static Reading detect(ServerPlayer player) {
        ServerLevel level = player.level();
        long now = level.getGameTime();
        AuraHarvestCooldowns cooldowns = cooldowns(level);
        Map<Element, Integer> environment = AuraSourceProfiles.environment(player);
        Map<Element, Integer> densities = new EnumMap<>(Element.class);
        merge(densities, environment);
        List<Source> sources = new ArrayList<>();
        BlockPos center = player.blockPosition();
        for (int x = -GATHER_RADIUS; x <= GATHER_RADIUS; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -GATHER_RADIUS; z <= GATHER_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.hasChunkAt(pos) || !cooldowns.ready(pos, now)) continue;
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    Map<Element, Integer> aspects = AuraSourceProfiles.block(level, pos, state);
                    if (aspects.isEmpty()) continue;
                    sources.add(new Source(pos.immutable(), null, aspects));
                    merge(densities, aspects);
                }
            }
        }
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(GATHER_RADIUS),
                entity -> !(entity instanceof Player) && entity.isAlive() && !entity.isSpectator())) {
            if (!cooldowns.ready(living.getUUID(), now)
                    || living.getPersistentData().getLongOr("project_cradle.aura_ready_at", 0L) > now) continue;
            Map<Element, Integer> aspects = livingAspects(living);
            sources.add(new Source(null, living.getUUID(), aspects));
            merge(densities, aspects);
        }
        return Reading.of(densities, environment, sources);
    }

    /** Automatically choose the richest compatible object that is ready to gather again. */
    public static Harvest harvestBest(ServerPlayer player, Reading reading) {
        MadraType type = player.getData(ModAttachments.MADRA_TYPE);
        Source source = reading.sources.stream().filter(candidate -> matching(candidate.aspects, type) > 0)
                .max(Comparator.comparingInt(candidate -> matching(candidate.aspects, type))).orElse(null);
        return source == null ? new Harvest(Status.NO_SOURCE, 0, 0) : harvest(player, source, reading.environment);
    }

    public static Harvest harvestBlock(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level();
        if (!level.hasChunkAt(pos) || !player.isWithinBlockInteractionRange(pos, 1.0)) return new Harvest(Status.NO_SOURCE, 0, 0);
        BlockState state = level.getBlockState(pos);
        Map<Element, Integer> aspects = state.isAir() ? Map.of() : AuraSourceProfiles.block(level, pos, state);
        if (aspects.isEmpty()) return new Harvest(Status.NO_SOURCE, 0, 0);
        return harvest(player, new Source(pos.immutable(), null, aspects), AuraSourceProfiles.environment(player));
    }

    public static Harvest harvestEntity(ServerPlayer player, LivingEntity target) {
        if (target instanceof Player || !target.isAlive() || target.level() != player.level()
                || !player.isWithinEntityInteractionRange(target, 1.0) || !player.hasLineOfSight(target)) return new Harvest(Status.NO_SOURCE, 0, 0);
        return harvest(player, new Source(null, target.getUUID(), livingAspects(target)), AuraSourceProfiles.environment(player));
    }

    private static Harvest harvest(ServerPlayer player, Source source, Map<Element, Integer> environment) {
        MadraType type = player.getData(ModAttachments.MADRA_TYPE);
        int strength = matching(source.aspects, type);
        if (strength <= 0) return new Harvest(Status.INCOMPATIBLE, 0, 0);
        long now = player.level().getGameTime();
        AuraHarvestCooldowns cooldowns = cooldowns(player.level());
        long remaining = source.position != null ? cooldowns.remaining(source.position, now) : cooldowns.remaining(source.entity, now);
        LivingEntity living = null;
        if (source.entity != null) {
            var entity = player.level().getEntity(source.entity);
            if (!(entity instanceof LivingEntity target) || !target.isAlive()) return new Harvest(Status.NO_SOURCE, 0, 0);
            living = target;
            remaining = Math.max(remaining, Math.max(0L, living.getPersistentData().getLongOr("project_cradle.aura_ready_at", 0L) - now));
        }
        if (remaining > 0) return new Harvest(Status.COOLING, 0, remaining);
        double maximum = player.getData(ModAttachments.MAXMADRA).getLevel();
        var madra = player.getData(ModAttachments.MADRA);
        double space = Math.max(0, maximum - madra.getALevel());
        if (space < 0.001) return new Harvest(Status.FULL, 0, 0);
        double location = locationMultiplier(type, environment);
        double spiral = player.getData(ModAttachments.CORE).hasJadeSpiral() ? 1.5 : 1.0;
        double amount = Math.min(space, Math.min(80.0, (8.0 + Math.min(64, strength) * 0.75) * location * spiral));
        if (source.position != null) cooldowns.consume(source.position, now);
        else {
            cooldowns.consume(source.entity, now);
            // A creature carries its recovery timer through portals as well as chunk loads.
            living.getPersistentData().putLong("project_cradle.aura_ready_at", now + AuraHarvestCooldowns.ENTITY_COOLDOWN_TICKS);
        }
        madra.addMadra(amount, maximum);
        player.syncData(ModAttachments.MADRA);
        return new Harvest(Status.GATHERED, amount, 0);
    }

    public static AuraHarvestCooldowns cooldowns(ServerLevel level) {
        return level.getData(ModAttachments.AURA_HARVEST_COOLDOWNS);
    }

    private static Map<Element, Integer> livingAspects(LivingEntity living) {
        Map<Element, Integer> aspects = new EnumMap<>(Element.class);
        String species = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()).getPath();
        boolean undead = species.contains("zombie") || species.contains("skeleton") || species.equals("drowned")
                || species.equals("husk") || species.equals("stray") || species.equals("bogged")
                || species.equals("wither") || species.equals("phantom") || species.equals("parched");
        if (species.endsWith("_golem")) {
            aspects.put(Element.EARTH, 12);
            aspects.put(Element.SWORD, 16);
            aspects.put(Element.FORCE, 12);
        } else if (undead) aspects.put(Element.DEATH, 16);
        else {
            aspects.put(Element.LIFE, 10);
            aspects.put(Element.BLOOD, 10);
        }
        if (living.isOnFire() || living.fireImmune()) aspects.put(Element.FIRE, 12);
        if (living.isInWater()) aspects.put(Element.WATER, 8);
        if (living.getDeltaMovement().lengthSqr() > 0.01) aspects.put(Element.FORCE, 8);
        if (living.getData(ModAttachments.SACRED_BEAST_AGE).isSacred()) {
            for (Element aspect : living.getData(ModAttachments.MADRA_TYPE).getTypes()) {
                if (aspect != Element.PURE) aspects.merge(aspect, 20, Integer::sum);
            }
        }
        return Map.copyOf(aspects);
    }

    private static int matching(Map<Element, Integer> aspects, MadraType type) {
        return type.getTypes().stream().filter(aspect -> aspect != Element.PURE)
                .mapToInt(aspect -> aspects.getOrDefault(aspect, 0)).sum();
    }

    private static double locationMultiplier(MadraType type, Map<Element, Integer> environment) {
        return 1.0 + Math.min(0.75, matching(environment, type) / 48.0);
    }

    private static void merge(Map<Element, Integer> into, Map<Element, Integer> from) {
        from.forEach((aspect, amount) -> { if (aspect != Element.PURE && amount > 0) into.merge(aspect, amount, Integer::sum); });
    }

    private record Source(BlockPos position, UUID entity, Map<Element, Integer> aspects) { }
    public enum Status { GATHERED, FULL, COOLING, INCOMPATIBLE, NO_SOURCE }
    public record Harvest(Status status, double amount, long remainingTicks) { }

    public record Reading(Element aspect, int strength, Map<Element, Integer> densities,
            Map<Element, Integer> environment, List<Source> sources) {
        public Reading {
            densities = Map.copyOf(densities);
            environment = Map.copyOf(environment);
            sources = List.copyOf(sources);
        }
        public static Reading of(Map<Element, Integer> samples) { return of(samples, Map.of(), List.of()); }
        private static Reading of(Map<Element, Integer> samples, Map<Element, Integer> environment, List<Source> sources) {
            Map<Element, Integer> natural = new EnumMap<>(Element.class);
            merge(natural, samples);
            Element dominant = Element.PURE;
            int strongest = 0;
            for (var entry : natural.entrySet()) {
                if (entry.getValue() > strongest) { dominant = entry.getKey(); strongest = entry.getValue(); }
            }
            return new Reading(dominant, strongest, natural, environment, sources);
        }
        public boolean hasAura() { return this.strength > 0; }
        public boolean isBalanced() { return this.densities.values().stream().filter(amount -> amount >= 8).count() >= 3; }
        public double madraYield() { return yieldFor(this.strength); }
        public double compatibleYield(MadraType type) { return yieldFor(matching(this.densities, type)); }
        public double ambientYield(MadraType type) { return Math.min(6.0, yieldFor(matching(this.environment, type))); }
        public double locationMultiplier(MadraType type) { return AuraService.locationMultiplier(type, this.environment); }
        public double meditationMultiplier() {
            // A comfortable, aura-rich location improves concentration, without creating natural Pure aura.
            return 1.0 + Math.min(0.5, this.environment.getOrDefault(Element.LIFE, 0) / 48.0
                    + this.environment.getOrDefault(Element.AIR, 0) / 96.0);
        }
        private static double yieldFor(int amount) { return amount <= 0 ? 0.0 : Math.min(20.0, amount / 4.0); }
    }
}
