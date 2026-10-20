package net.ok.project_cradle.server.spells;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.spells.ElementalTechnique;

/** Elemental techniques share effect execution across player and creature casters. */
public final class ElementalTechniqueSpell {
    private ElementalTechniqueSpell() {
    }

    public static boolean cast(ServerPlayer player, ItemStack stack, ElementalTechnique technique) {
        return MadraTechniqueSpell.cast(player, stack, technique.madraCost(), technique.cooldownTicks(),
                (level, caster) -> perform(ElementalCastingContext.of(caster), technique));
    }

    /** Creature casting already owns payment, windup, cooldown, and knowledge. */
    public static boolean perform(ElementalCastingContext context, ElementalTechnique technique) {
        if (context == null) return false;
        return switch (technique) {
            case FLAME_RING -> flameRing(context);
            case FROST_NOVA -> frostNova(context);
            case GALE_STEP -> galeStep(context);
            case STONE_AEGIS -> stoneAegis(context);
            case SPIRIT_SEVER -> spiritSever(context);
            case BLOOM_FIELD -> ElementalFieldService.create(context, technique, context.caster().position());
            case WITHERING_TOUCH -> witheringTouch(context);
            case SOLAR_BEAM -> solarBeam(context);
            case NIGHT_VEIL -> nightVeil(context);
            case CRIMSON_SIPHON -> crimsonSiphon(context);
            case DREAM_SNARE -> dreamSnare(context);
            case RUIN_BURST -> ruinBurst(context);
            case REPULSION_WAVE -> repulsionWave(context);
            case BLADE_TEMPEST -> ElementalFieldService.create(context, technique, context.caster().position());
            case MADRA_LEECH -> madraLeech(context);
        };
    }

    /** Avoid wasting creature casts on active buffs, full health, or an unreachable target. */
    public static boolean canCast(Mob caster, LivingEntity target, ElementalTechnique technique) {
        ElementalCastingContext context = ElementalCastingContext.of(caster);
        if (context == null || !context.canHarm(target)) return false;
        return switch (technique) {
            case STONE_AEGIS -> !caster.hasEffect(MobEffects.ABSORPTION) && !caster.hasEffect(MobEffects.RESISTANCE);
            case NIGHT_VEIL -> !caster.hasEffect(MobEffects.INVISIBILITY);
            case BLOOM_FIELD -> caster.getHealth() < caster.getMaxHealth() * 0.85F
                    && !ElementalFieldService.hasActive(caster, technique);
            case BLADE_TEMPEST -> !ElementalFieldService.hasActive(caster, technique);
            case CRIMSON_SIPHON -> caster.getHealth() < caster.getMaxHealth() * 0.95F && context.target(technique.baseRange()) != null;
            case SPIRIT_SEVER, DREAM_SNARE, MADRA_LEECH -> context.target(technique.baseRange()) != null;
            case GALE_STEP -> !caster.isPassenger() && !caster.isSleeping() && caster.distanceToSqr(target) > 9.0;
            default -> true;
        };
    }

    private static boolean flameRing(ElementalCastingContext context) {
        for (LivingEntity target : context.nearby(ElementalTechnique.FLAME_RING.baseRange())) {
            if (!context.hurt(target, 5.0F)) continue;
            target.igniteForSeconds(4.0F);
            burst(context, ParticleTypes.FLAME, target.getBoundingBox().getCenter(), 5, 0.3);
        }
        ring(context, ElementalTechnique.FLAME_RING, ParticleTypes.FLAME);
        sound(context, SoundEvents.BLAZE_SHOOT, 0.8F);
        return true;
    }

    private static boolean frostNova(ElementalCastingContext context) {
        for (LivingEntity target : context.nearby(ElementalTechnique.FROST_NOVA.baseRange())) {
            if (!context.hurt(target, 4.0F)) continue;
            effect(context, target, MobEffects.SLOWNESS, 80, 2);
            if (target.canFreeze()) target.setTicksFrozen(Math.max(target.getTicksFrozen(), 160));
            burst(context, ParticleTypes.SNOWFLAKE, target.getBoundingBox().getCenter(), 6, 0.35);
        }
        ring(context, ElementalTechnique.FROST_NOVA, ParticleTypes.SNOWFLAKE);
        sound(context, SoundEvents.GLASS_BREAK, 1.3F);
        return true;
    }

    private static boolean galeStep(ElementalCastingContext context) {
        LivingEntity caster = context.caster();
        if (caster.isPassenger() || caster.isSleeping()) return false;
        Vec3 look = context.direction();
        Vec3 direction = new Vec3(look.x, 0.0, look.z).normalize();
        if (direction.lengthSqr() < 0.5) return false;
        Vec3 start = caster.position();
        Vec3 destination = start;
        double range = context.range(ElementalTechnique.GALE_STEP.baseRange());
        for (double distance = 0.25; distance <= range; distance += 0.25) {
            Vec3 step = start.add(direction.scale(distance));
            AABB bounds = caster.getBoundingBox().move(step.subtract(start));
            if (!safeSpace(context, bounds)) break;
            destination = step;
        }
        if (destination.distanceToSqr(start) < 0.25) return false;
        beam(context, start.add(0, 0.5, 0), destination.add(0, 0.5, 0), ParticleTypes.CLOUD, 1.5);
        caster.teleportTo(destination.x, destination.y, destination.z);
        caster.setDeltaMovement(Vec3.ZERO);
        caster.resetFallDistance();
        effect(context, caster, MobEffects.SLOW_FALLING, 40, 0);
        for (LivingEntity target : context.nearby(2.0)) {
            if (context.effectHit(target)) push(context, target, 0.45);
        }
        sound(context, SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), 1.4F);
        return true;
    }

    private static boolean safeSpace(ElementalCastingContext context, AABB bounds) {
        if (!context.level().getWorldBorder().isWithinBounds(bounds)) return false;
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
            if (!context.level().hasChunkAt(pos) || context.level().isOutsideBuildHeight(pos)) return false;
        }
        return context.level().noCollision(context.caster(), bounds);
    }

    private static boolean stoneAegis(ElementalCastingContext context) {
        int potency = Math.min(2, (int) Math.floor(Math.max(0.0, context.power() - 1.0)));
        effect(context, context.caster(), MobEffects.RESISTANCE, 160, potency);
        effect(context, context.caster(), MobEffects.ABSORPTION, 160, Math.min(3, potency + 1));
        ParticleOptions stone = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());
        Vec3 center = context.caster().position().add(0, 0.8, 0);
        for (int i = 0; i < 24; i++) {
            double angle = i * Math.PI / 6.0;
            burst(context, stone, center.add(Math.cos(angle) * 0.8, (i % 6) * 0.2 - 0.5, Math.sin(angle) * 0.8), 1, 0.025);
        }
        sound(context, SoundEvents.IRON_GOLEM_REPAIR, 0.8F);
        return true;
    }

    private static boolean spiritSever(ElementalCastingContext context) {
        LivingEntity target = context.target(ElementalTechnique.SPIRIT_SEVER.baseRange());
        if (target == null) return false;
        boolean interrupted = interrupt(target);
        boolean hit = context.hurt(target, 4.0F);
        if (!hit && !interrupted) return false;
        if (target.isAlive()) effect(context, target, MobEffects.WEAKNESS, 100, 1);
        beam(context, context.caster().getEyePosition(), target.getEyePosition(), ParticleTypes.ENCHANTED_HIT, 3.0);
        burst(context, ParticleTypes.ENCHANT, target.getBoundingBox().getCenter(), 12, 0.35);
        return true;
    }

    private static boolean witheringTouch(ElementalCastingContext context) {
        Vec3 direction = context.direction();
        for (LivingEntity target : context.nearby(ElementalTechnique.WITHERING_TOUCH.baseRange())) {
            Vec3 delta = target.getBoundingBox().getCenter().subtract(context.caster().getEyePosition());
            if (delta.lengthSqr() > 0.1 && delta.normalize().dot(direction) < 0.75) continue;
            if (!context.hurt(target, 5.0F)) continue;
            effect(context, target, MobEffects.WITHER, 100, 0);
            burst(context, ParticleTypes.SOUL, target.getBoundingBox().getCenter(), 5, 0.3);
        }
        beam(context, context.caster().getEyePosition(), context.aimPoint(4.0), ParticleTypes.SOUL, 2.0);
        return true;
    }

    private static boolean solarBeam(ElementalCastingContext context) {
        Vec3 start = context.caster().getEyePosition();
        Vec3 end = context.aimPoint(ElementalTechnique.SOLAR_BEAM.baseRange());
        List<LivingEntity> targets = context.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(start, end).inflate(0.35), target -> context.canHarm(target)
                        && target.getBoundingBox().inflate(0.25).clip(start, end).isPresent()
                        && context.clearLine(start, target.getBoundingBox().getCenter()))
                .stream().sorted(Comparator.comparingDouble(context.caster()::distanceToSqr)).limit(8).toList();
        for (LivingEntity target : targets) {
            if (context.hurt(target, 8.0F)) effect(context, target, MobEffects.GLOWING, 120, 0);
        }
        beam(context, start, end, ParticleTypes.END_ROD, 3.0);
        beam(context, start, end, new DustParticleOptions(0xFFF1A8, 0.8F), 2.0);
        burst(context, ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFE3A0), end, 1, 0.0);
        sound(context, SoundEvents.BEACON_ACTIVATE, 1.8F);
        return true;
    }

    private static boolean nightVeil(ElementalCastingContext context) {
        effect(context, context.caster(), MobEffects.INVISIBILITY, 120, 0);
        int potency = Math.min(2, (int) Math.floor(Math.max(0.0, context.power() - 1.0)));
        effect(context, context.caster(), MobEffects.SPEED, 120, potency);
        burst(context, ParticleTypes.LARGE_SMOKE, context.caster().position().add(0, 0.8, 0), 12, 0.45);
        AdditionalTechniqueSpell.ring(context.level(), context.caster().position().add(0, 0.15, 0),
                1.0, new DustParticleOptions(0x53366D, 0.8F), 20);
        return true;
    }

    private static boolean crimsonSiphon(ElementalCastingContext context) {
        LivingEntity target = context.target(ElementalTechnique.CRIMSON_SIPHON.baseRange());
        if (target == null) return false;
        float before = target.getHealth();
        if (!context.hurt(target, 6.0F)) return false;
        float removed = Math.max(0.0F, before - target.getHealth());
        context.caster().heal(Math.min(removed * 0.4F, (float) (3.0 * context.power())));
        beam(context, target.getEyePosition(), context.caster().getEyePosition(), new DustParticleOptions(0xB82D4B, 0.9F), 3.0);
        burst(context, ParticleTypes.HEART, context.caster().getEyePosition(), 3, 0.2);
        return true;
    }

    private static boolean dreamSnare(ElementalCastingContext context) {
        LivingEntity target = context.target(ElementalTechnique.DREAM_SNARE.baseRange());
        if (target == null || !context.effectHit(target)) return false;
        interrupt(target);
        effect(context, target, MobEffects.SLOWNESS, 100, 2);
        effect(context, target, MobEffects.WEAKNESS, 100, 1);
        effect(context, target, MobEffects.NAUSEA, 60, 0);
        AdditionalTechniqueSpell.ring(context.level(), target.position().add(0, 0.15, 0),
                Math.max(0.6, target.getBbWidth()), new DustParticleOptions(0xD68AF5, 0.8F), 24);
        burst(context, ParticleTypes.WITCH, target.getEyePosition(), 5, 0.3);
        return true;
    }

    private static boolean ruinBurst(ElementalCastingContext context) {
        LivingEntity aim = context.target(ElementalTechnique.RUIN_BURST.baseRange());
        Vec3 center = aim == null ? context.aimPoint(ElementalTechnique.RUIN_BURST.baseRange()) : aim.getBoundingBox().getCenter();
        double radius = context.range(3.0);
        for (LivingEntity target : context.level().getEntitiesOfClass(LivingEntity.class,
                AABB.ofSize(center, radius * 2, radius * 2, radius * 2), target -> context.canHarm(target)
                        && target.getBoundingBox().getCenter().distanceToSqr(center) <= radius * radius
                        && context.clearLine(center, target.getBoundingBox().getCenter())
                        && context.clearLine(context.caster().getEyePosition(), target.getBoundingBox().getCenter()))
                .stream().sorted(Comparator.comparingDouble(target -> target.distanceToSqr(center))).limit(32).toList()) {
            if (context.hurt(target, 9.0F)) push(context, target, 0.55);
        }
        beam(context, context.caster().getEyePosition(), center, new DustParticleOptions(0xAA45BD, 0.8F), 2.0);
        AdditionalTechniqueSpell.ring(context.level(), center, radius, ParticleTypes.REVERSE_PORTAL, 32);
        burst(context, ParticleTypes.EXPLOSION, center, 2, 0.3);
        sound(context, SoundEvents.GENERIC_EXPLODE.value(), 1.5F);
        return true;
    }

    private static boolean repulsionWave(ElementalCastingContext context) {
        for (LivingEntity target : context.nearby(ElementalTechnique.REPULSION_WAVE.baseRange())) {
            if (!context.hurt(target, 3.0F)) continue;
            push(context, target, Math.min(2.2, 0.9 * Math.sqrt(context.power())));
            burst(context, ParticleTypes.GUST, target.getBoundingBox().getCenter(), 1, 0.0);
        }
        ring(context, ElementalTechnique.REPULSION_WAVE, ParticleTypes.CLOUD);
        sound(context, SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), 0.8F);
        return true;
    }

    private static boolean madraLeech(ElementalCastingContext context) {
        LivingEntity target = context.target(ElementalTechnique.MADRA_LEECH.baseRange());
        if (target == null || !context.hurt(target, 3.0F)) return false;
        // Ordinary vanilla creatures have no developed core and therefore yield no madra.
        if (target instanceof ServerPlayer || target instanceof Mob mob && MobTechniqueService.isCaster(mob)) {
            double available = target.getData(ModAttachments.MADRA).getALevel();
            double cap = context.caster().getData(ModAttachments.MAXMADRA).getLevel();
            double current = context.caster().getData(ModAttachments.MADRA).getALevel();
            double castCost = context.madraCost(ElementalTechnique.MADRA_LEECH.madraCost());
            // A transfer can recover at most half the price paid, and never creates madra.
            double amount = Math.min(Math.min(available, Math.max(0.0, cap - current)), Math.min(10.0 * context.power(), castCost * 0.5));
            if (Double.isFinite(amount) && amount > 0.0 && target.getData(ModAttachments.MADRA).trySpendMadra(amount)) {
                context.caster().getData(ModAttachments.MADRA).addMadra(amount, cap);
                if (target instanceof ServerPlayer player) player.syncData(ModAttachments.MADRA);
                if (context.caster() instanceof ServerPlayer player) player.syncData(ModAttachments.MADRA);
            }
        }
        effect(context, target, MobEffects.WEAKNESS, 60, 0);
        beam(context, target.getEyePosition(), context.caster().getEyePosition(), new DustParticleOptions(0xD5C9A7, 0.8F), 3.0);
        burst(context, ParticleTypes.SOUL, target.getBoundingBox().getCenter(), 4, 0.25);
        return true;
    }

    private static boolean interrupt(LivingEntity target) {
        return target instanceof ServerPlayer player ? TechniqueCastingService.cancel(player)
                : target instanceof Mob mob && MobTechniqueService.cancel(mob);
    }

    private static void effect(ElementalCastingContext context, LivingEntity target, Holder<MobEffect> effect,
            int ticks, int amplifier) {
        if (target.isAlive()) target.addEffect(new MobEffectInstance(effect, context.duration(ticks), amplifier, true, false, false));
    }

    private static void push(ElementalCastingContext context, LivingEntity target, double amount) {
        Vec3 delta = target.position().subtract(context.caster().position()).multiply(1, 0, 1).normalize();
        if (delta.lengthSqr() < 0.01) delta = context.direction().multiply(1, 0, 1).normalize();
        target.push(delta.x * amount, Math.min(0.5, 0.12 + amount * 0.18), delta.z * amount);
        target.hurtMarked = true;
    }

    private static void ring(ElementalCastingContext context, ElementalTechnique technique, ParticleOptions particle) {
        Vec3 center = context.caster().position().add(0, 0.2, 0);
        double radius = context.range(technique.baseRange());
        AdditionalTechniqueSpell.ring(context.level(), center, radius * 0.6, particle, 24);
        AdditionalTechniqueSpell.ring(context.level(), center, radius, particle, 32);
    }

    private static void burst(ElementalCastingContext context, ParticleOptions particle, Vec3 center, int count, double spread) {
        context.level().sendParticles(particle, center.x, center.y, center.z, count, spread, spread, spread, 0.02);
    }

    private static void beam(ElementalCastingContext context, Vec3 from, Vec3 to, ParticleOptions particle, double density) {
        Vec3 delta = to.subtract(from);
        int count = Math.max(2, Math.min(120, (int) Math.ceil(delta.length() * density)));
        for (int i = 0; i <= count; i++) burst(context, particle, from.add(delta.scale(i / (double) count)), 1, 0.015);
    }

    private static void sound(ElementalCastingContext context, net.minecraft.sounds.SoundEvent sound, float pitch) {
        context.level().playSound(null, context.caster().blockPosition(), sound, SoundSource.PLAYERS, 0.65F, pitch);
    }
}
