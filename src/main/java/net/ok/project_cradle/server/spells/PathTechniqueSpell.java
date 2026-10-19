package net.ok.project_cradle.server.spells;

import java.util.List;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.spells.PathTechnique;
import net.ok.project_cradle.spells.TechniqueScaling;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.entities.BasicStrikerProjectile;
import net.ok.project_cradle.entities.ModEntities;
import net.ok.project_cradle.entities.PathTechniqueProjectile;

/** Server-side costs, cooldowns, and effects for the named Path techniques. */
public final class PathTechniqueSpell {
    private PathTechniqueSpell() {
    }

    public static boolean cast(ServerPlayer player, ItemStack stack, PathTechnique technique) {
        ServerLevel level = player.level();
        if (player.getCooldowns().isOnCooldown(stack)) {
            return false;
        }

        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement() == AdvancementLevel.UNFOUNDED) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.need_foundation"));
            return false;
        }
        if (core.getSoulfire() < technique.soulfireCost()) {
            player.sendSystemMessage(Component.translatable(
                    "message.project_cradle.technique_needs_soulfire", technique.soulfireCost()));
            return false;
        }

        double madraCost = TechniqueScaling.madraCost(player, technique.madraCost());
        Madra madra = player.getData(ModAttachments.MADRA);
        if (!madra.trySpendMadra(madraCost)) {
            player.sendSystemMessage(Component.translatable(
                    "message.project_cradle.not_enough_technique_madra", madraCost));
            return false;
        }
        if (technique.soulfireCost() > 0.0 && !core.trySpendSoulfire(technique.soulfireCost())) {
            madra.addMadra(madraCost, player.getData(ModAttachments.MAXMADRA).getLevel());
            player.syncData(ModAttachments.MADRA);
            return false;
        }

        if (!perform(player, level, stack, technique)) {
            madra.addMadra(madraCost, player.getData(ModAttachments.MAXMADRA).getLevel());
            core.addSoulfire(technique.soulfireCost());
            player.syncData(ModAttachments.MADRA);
            player.syncData(ModAttachments.CORE);
            return false;
        }

        int enforcementDuration = switch (technique) {
            case BURNING_CLOAK -> 140;
            case DRAGON_DESCENDS -> 50;
            case FLOWING_SWORD -> 200;
            case FLOWING_STARLIGHT -> 220;
            case STARS_EDGE -> 180;
            case SERPENTS_SHADOW -> 160;
            case STONE_ANCHOR -> 180;
            case GRASPING_SKY -> 220;
            case FORGED_WINGS -> 220;
            default -> 0;
        };
        if (enforcementDuration > 0) EnforcerPowerService.empower(player, enforcementDuration, technique.aspect());
        player.getCooldowns().addCooldown(stack, technique.cooldownTicks());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.75F,
                technique.aspect() == MadraType.Element.SWORD ? 1.4F : 0.95F);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        player.syncData(ModAttachments.MADRA);
        if (technique.soulfireCost() > 0.0) {
            player.syncData(ModAttachments.CORE);
        }
        return true;
    }

    private static boolean perform(ServerPlayer player, ServerLevel level, ItemStack stack, PathTechnique technique) {
        return switch (technique) {
            case DRAGONS_BREATH -> fireProjectile(player, level, stack, technique, 11.0F, 2.1F, 16.0, 0.0F, true, false);
            case BURNING_CLOAK -> burningCloak(player, level);
            case VOID_DRAGONS_DANCE -> voidDragonsDance(player, level);
            case DRAGON_DESCENDS -> dragonDescends(player, level);
            case FOX_FIRE -> foxFire(player, level);
            case INVISIBLE_FLAME -> invisibleFlame(player);
            case PHANTOM_LIGHTS -> phantomLights(player, level);
            case FOX_TAIL -> foxTail(player, level);
            case FOX_DREAM -> foxDream(player, level);
            case FOX_MIRROR -> foxMirror(player, level);
            case RIPPLING_SWORD -> fireProjectile(player, level, stack, technique, 7.0F, 2.4F, 14.0, 0.0F, false, false);
            case FLOWING_SWORD -> flowingSword(player, level);
            case ENDLESS_SWORD -> slash(player, level, 9.0, 7.5F, 65.0, 0.85);
            case HIDDEN_BLADE -> fireProjectile(player, level, stack, technique, 8.0F, 2.6F, 12.0, 0.0F, false, true);
            case FINAL_SWORD -> finalSword(player, level);
            case VENOM_NEEDLES -> volley(player, level, stack, technique, 4, 3.0F, 1.9F, 12.0, 7.0F);
            case VENOM_BLAST -> volley(player, level, stack, technique, 3, 5.0F, 1.8F, 11.0, 5.5F);
            case VENOM_MIST -> venomMist(player, level);
            case STAR_LANCE -> fireProjectile(player, level, stack, technique, 8.0F, 3.3F, 18.0, 0.0F, false, false);
            case FLOWING_STARLIGHT -> flowingStarlight(player, level);
            case STARS_EDGE -> starsEdge(player, level);
            case SERPENTS_SHADOW -> serpentsShadow(player, level);
            case FORGED_NEEDLES -> volley(player, level, stack, technique, 4, 4.0F, 2.0F, 13.0, 8.0F);
            case OATHSIGN -> oathsign(player, level);
            case STONE_ANCHOR -> stoneAnchor(player, level);
            case CUTTING_WIND -> fireProjectile(player, level, stack, technique, 6.0F, 2.6F, 13.0, 0.0F, false, false);
            case GRASPING_SKY -> graspingSky(player, level);
            case WIND_PRESSURE -> windPressure(player, level);
            case FORGED_WINGS -> forgedWings(player, level);
        };
    }

    private static boolean burningCloak(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.SPEED, 140, 1);
        addEffect(player, player, MobEffects.STRENGTH, 140, 1);
        if (!player.getAbilities().instabuild) {
            player.hurt(player.damageSources().magic(), 1.0F);
        }
        emitHalo(level, player, ParticleTypes.FLAME, 16, 0.8);
        emitHalo(level, player, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()), 8, 0.6);
        return true;
    }

    private static boolean voidDragonsDance(ServerPlayer player, ServerLevel level) {
        List<LivingEntity> targets = nearbyTargets(player, 7.0);
        for (LivingEntity target : targets) {
            if (!TechniqueCombatService.hurt(player, player, target, 8.0F)) continue;
            target.igniteForSeconds(5.0F);
            addEffect(player, target, MobEffects.WITHER, 80, 0);
            pushAway(player, target, 0.45);
            emitAt(level, ParticleTypes.FLAME, target.position().add(0, target.getBbHeight() * 0.5, 0), 12, 0.35);
        }
        emitHalo(level, player, ParticleTypes.LARGE_SMOKE, 18, 1.4);
        emitHalo(level, player, ParticleTypes.FLAME, 24, 1.7);
        return true;
    }

    private static boolean dragonDescends(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.SPEED, 50, 1);
        addEffect(player, player, MobEffects.STRENGTH, 50, 1);
        for (LivingEntity target : coneTargets(player, 5.0, 120.0)) {
            if (!TechniqueCombatService.hurt(player, player, target, 16.0F)) continue;
            target.igniteForSeconds(6.0F);
            addEffect(player, target, MobEffects.WITHER, 100, 0);
            pushAway(player, target, 1.4);
            emitAt(level, ParticleTypes.FLAME, target.position().add(0, target.getBbHeight() * 0.5, 0), 18, 0.35);
            emitAt(level, ParticleTypes.LARGE_SMOKE, target.position().add(0, target.getBbHeight() * 0.5, 0), 10, 0.25);
        }
        emitAt(level, ParticleTypes.EXPLOSION, player.position().add(player.getLookAngle().scale(2.0)), 3, 0.2);
        emitHalo(level, player, ParticleTypes.FLAME, 18, 1.2);
        return true;
    }

    private static boolean finalSword(ServerPlayer player, ServerLevel level) {
        slash(player, level, 11.0, 14.0F, 100.0, 1.2);
        addEffect(player, player, MobEffects.STRENGTH, 80, 0);
        Vec3 arc = player.getEyePosition().add(player.getLookAngle().scale(3.0));
        emitAt(level, ParticleTypes.SWEEP_ATTACK, arc, 20, 1.0);
        emitAt(level, ParticleTypes.CRIT, arc, 16, 0.8);
        emitAt(level, ParticleTypes.ENCHANTED_HIT, arc, 12, 0.6);
        return true;
    }

    private static boolean foxFire(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.position().add(0.0, player.getEyeHeight() * 0.65, 0.0);
        for (int i = 1; i <= 9; i++) {
            double distance = TechniqueScaling.range(player, i * 0.65);
            Vec3 center = origin.add(look.scale(distance));
            emitAt(level, ParticleTypes.SOUL_FIRE_FLAME, center, 4, 0.22);
            emitAt(level, ParticleTypes.WITCH, center, 2, 0.18);
        }
        return true; // Illusory flames are deliberately harmless.
    }

    private static boolean invisibleFlame(ServerPlayer player) {
        List<LivingEntity> targets = coneTargets(player, 9.0, 20.0);
        for (LivingEntity target : targets) {
            if (!TechniqueCombatService.onEffectHit(player, target)) continue;
            // The hidden status particles keep this mark from appearing as ordinary visible flames.
            addEffect(player, target, MobEffects.POISON, 80, 0);
            addEffect(player, target, MobEffects.WEAKNESS, 80, 0);
        }
        return true;
    }

    private static boolean phantomLights(ServerPlayer player, ServerLevel level) {
        for (LivingEntity target : coneTargets(player, 10.0, 28.0)) {
            if (!TechniqueCombatService.onEffectHit(player, target)) continue;
            addEffect(player, target, MobEffects.BLINDNESS, 50, 0);
            emitAt(level, ParticleTypes.END_ROD, target.position().add(0, target.getBbHeight(), 0), 10, 0.35);
        }
        return true;
    }

    private static boolean foxTail(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.SPEED, 160, 1);
        addEffect(player, player, MobEffects.INVISIBILITY, 50, 0);
        emitHalo(level, player, ParticleTypes.CLOUD, 14, 0.8);
        return true;
    }

    private static boolean foxDream(ServerPlayer player, ServerLevel level) {
        return FoxDreamCharmService.activate(player);
    }

    private static boolean foxMirror(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.INVISIBILITY, 100, 0);
        addEffect(player, player, MobEffects.SPEED, 100, 0);
        addEffect(player, player, MobEffects.RESISTANCE, 60, 0);
        for (int side : new int[] {-1, 1}) {
            Vec3 decoy = player.position().add(player.getLookAngle().z * side * 1.1, 0.3,
                    -player.getLookAngle().x * side * 1.1);
            emitAt(level, ParticleTypes.CLOUD, decoy, 12, 0.3);
            emitAt(level, ParticleTypes.WITCH, decoy.add(0, 1.0, 0), 6, 0.2);
        }
        return true;
    }

    private static boolean flowingSword(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.STRENGTH, 200, 0);
        addEffect(player, player, MobEffects.SPEED, 120, 0);
        emitHalo(level, player, ParticleTypes.CRIT, 18, 0.8);
        return true;
    }

    private static boolean venomMist(ServerPlayer player, ServerLevel level) {
        for (LivingEntity target : nearbyTargets(player, 6.0)) {
            if (!TechniqueCombatService.onEffectHit(player, target)) continue;
            addEffect(player, target, MobEffects.POISON, 120, 0);
            addEffect(player, target, MobEffects.SLOWNESS, 80, 0);
        }
        emitHalo(level, player, ParticleTypes.SPORE_BLOSSOM_AIR, 30, 2.2);
        emitHalo(level, player, ParticleTypes.WITCH, 18, 1.9);
        return true;
    }

    private static boolean flowingStarlight(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.SPEED, 220, 1);
        addEffect(player, player, MobEffects.NIGHT_VISION, 260, 0);
        addEffect(player, player, MobEffects.JUMP_BOOST, 160, 0);
        emitHalo(level, player, ParticleTypes.END_ROD, 20, 0.9);
        return true;
    }

    private static boolean starsEdge(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.STRENGTH, 180, 1);
        emitAt(level, ParticleTypes.END_ROD, player.getEyePosition().add(player.getLookAngle().scale(0.6)), 12, 0.2);
        return true;
    }

    private static boolean serpentsShadow(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.RESISTANCE, 160, 0);
        addEffect(player, player, MobEffects.SLOW_FALLING, 120, 0);
        emitHalo(level, player, ParticleTypes.END_ROD, 12, 0.9);
        emitHalo(level, player, ParticleTypes.CRIT, 8, 0.75);
        return true;
    }

    private static boolean oathsign(ServerPlayer player, ServerLevel level) {
        List<ServerPlayer> allies = level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(TechniqueScaling.range(player, 5.0)));
        for (ServerPlayer ally : allies) {
            if (ally != player && !player.isAlliedTo(ally)) continue;
            addEffect(player, ally, MobEffects.RESISTANCE, 160, 0);
            addEffect(player, ally, MobEffects.ABSORPTION, 160, 0);
            emitHalo(level, ally, ParticleTypes.END_ROD, 10, 0.65);
        }
        emitHalo(level, player, ParticleTypes.HAPPY_VILLAGER, 18, 1.3);
        return true;
    }

    private static boolean stoneAnchor(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.RESISTANCE, 180, 1);
        addEffect(player, player, MobEffects.SLOWNESS, 180, 1);
        emitHalo(level, player, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), 18, 0.9);
        return true;
    }

    private static boolean graspingSky(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.JUMP_BOOST, 220, 2);
        addEffect(player, player, MobEffects.SLOW_FALLING, 260, 0);
        emitHalo(level, player, ParticleTypes.CLOUD, 18, 0.9);
        return true;
    }

    private static boolean windPressure(ServerPlayer player, ServerLevel level) {
        for (LivingEntity target : nearbyTargets(player, 7.0)) {
            if (!TechniqueCombatService.onEffectHit(player, target)) continue;
            addEffect(player, target, MobEffects.SLOWNESS, 100, 1);
            addEffect(player, target, MobEffects.WEAKNESS, 100, 0);
            pushAway(player, target, 0.35);
        }
        emitHalo(level, player, ParticleTypes.GUST, 16, 1.5);
        return true;
    }

    private static boolean forgedWings(ServerPlayer player, ServerLevel level) {
        addEffect(player, player, MobEffects.SLOW_FALLING, 400, 0);
        addEffect(player, player, MobEffects.JUMP_BOOST, 220, 1);
        addEffect(player, player, MobEffects.RESISTANCE, 100, 0);
        Vec3 left = player.position().add(-0.65, player.getBbHeight() * 0.65, 0.0);
        Vec3 right = player.position().add(0.65, player.getBbHeight() * 0.65, 0.0);
        emitAt(level, ParticleTypes.CLOUD, left, 16, 0.3);
        emitAt(level, ParticleTypes.CLOUD, right, 16, 0.3);
        emitAt(level, ParticleTypes.END_ROD, left, 8, 0.25);
        emitAt(level, ParticleTypes.END_ROD, right, 8, 0.25);
        return true;
    }

    private static boolean slash(ServerPlayer player, ServerLevel level, double range, float damage,
            double arcDegrees, double knockback) {
        for (LivingEntity target : coneTargets(player, range, arcDegrees)) {
            if (!TechniqueCombatService.hurt(player, player, target, damage)) continue;
            applyAspectEffect(player, target, MadraType.Element.SWORD);
            pushAway(player, target, knockback);
        }
        Vec3 center = player.getEyePosition().add(player.getLookAngle().scale(Math.min(range * 0.6, 4.0)));
        emitAt(level, ParticleTypes.SWEEP_ATTACK, center, 16, 0.9);
        emitAt(level, ParticleTypes.CRIT, center, 10, 0.6);
        return true;
    }

    private static boolean fireProjectile(ServerPlayer player, ServerLevel level, ItemStack stack,
            PathTechnique technique, float damage, float speed, double range,
            float yawOffset, boolean burnOnHit, boolean invisible) {
        BasicStrikerProjectile projectile = createProjectile(player, level, stack, technique,
                damage, speed, range, yawOffset, burnOnHit, invisible);
        return level.addFreshEntity(projectile);
    }

    private static boolean volley(ServerPlayer player, ServerLevel level, ItemStack stack, PathTechnique technique,
            int count, float damage, float speed, double range, float spreadDegrees) {
        boolean createdAny = false;
        for (int i = 0; i < count; i++) {
            float offset = (i - (count - 1) / 2.0F) * spreadDegrees;
            BasicStrikerProjectile projectile = createProjectile(player, level, stack, technique,
                    damage, speed, range, offset, false, false);
            createdAny |= level.addFreshEntity(projectile);
        }
        return createdAny;
    }

    private static BasicStrikerProjectile createProjectile(ServerPlayer player, ServerLevel level, ItemStack stack,
            PathTechnique technique, float damage, float speed, double range,
            float yawOffset, boolean burnOnHit, boolean invisible) {
        var projectileType = ModEntities.PATH_PROJECTILES.get(technique);
        PathTechniqueProjectile projectile = projectileType == null
                ? new PathTechniqueProjectile(ModEntities.BASIC_STRIKER_PROJECTILE.get(), level, player, stack)
                : new PathTechniqueProjectile(projectileType.get(), level, player, stack);
        projectile.setMadraElement(technique.aspect());
        projectile.setSpellDamage(damage);
        projectile.setMaxTravelDistance(TechniqueScaling.range(player, range));
        projectile.setBurnOnHit(burnOnHit);
        projectile.setInvisible(invisible);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot() + yawOffset,
                0.0F, speed, 0.0F);
        return projectile;
    }

    private static void applyAspectEffect(ServerPlayer owner, LivingEntity target, MadraType.Element aspect) {
        switch (aspect) {
            case FIRE -> target.igniteForSeconds(3.0F);
            case WATER -> addEffect(owner, target, MobEffects.SLOWNESS, 60, 0);
            case EARTH -> addEffect(owner, target, MobEffects.WEAKNESS, 60, 0);
            case PURE -> addEffect(owner, target, MobEffects.WEAKNESS, 60, 0);
            case LIFE -> owner.heal(1.0F);
            case DEATH -> addEffect(owner, target, MobEffects.WITHER, 60, 0);
            case LIGHT -> addEffect(owner, target, MobEffects.GLOWING, 100, 0);
            case SHADOW -> addEffect(owner, target, MobEffects.DARKNESS, 50, 0);
            case BLOOD -> addEffect(owner, target, MobEffects.POISON, 80, 0);
            case DREAM -> addEffect(owner, target, MobEffects.NAUSEA, 80, 0);
            case HUNGER -> addEffect(owner, target, MobEffects.HUNGER, 100, 0);
            case AIR, DESTRUCTION, FORCE, SWORD -> { }
        }
    }

    private static List<LivingEntity> nearbyTargets(ServerPlayer player, double radius) {
        radius = TechniqueScaling.range(player, radius);
        AABB bounds = player.getBoundingBox().inflate(radius);
        return player.level().getEntitiesOfClass(LivingEntity.class, bounds,
                target -> target != player && target.isAlive() && player.hasLineOfSight(target));
    }

    private static List<LivingEntity> coneTargets(ServerPlayer player, double range, double arcDegrees) {
        final double effectiveRange = TechniqueScaling.range(player, range);
        Vec3 look = player.getLookAngle().normalize();
        double minimumDot = Math.cos(Math.toRadians(arcDegrees / 2.0));
        return player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(effectiveRange), target -> {
                    if (target == player || !target.isAlive() || !player.hasLineOfSight(target)) {
                        return false;
                    }
                    Vec3 toTarget = target.getBoundingBox().getCenter()
                            .subtract(player.getEyePosition());
                    double distance = toTarget.length();
                    return distance <= effectiveRange && distance > 0.0
                            && look.dot(toTarget.scale(1.0 / distance)) >= minimumDot;
                });
    }

    private static void pushAway(Entity source, Entity target, double strength) {
        Vec3 direction = target.position().subtract(source.position()).normalize();
        target.push(direction.x * strength, 0.15 + strength * 0.18, direction.z * strength);
        target.hurtMarked = true;
    }

    private static void addEffect(ServerPlayer caster, LivingEntity target,
            net.minecraft.core.Holder<MobEffect> effect, int duration, int amplifier) {
        duration = TechniqueScaling.duration(caster, duration);
        target.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, false));
    }

    private static void emitHalo(ServerLevel level, Entity entity, ParticleOptions particles, int count, double radius) {
        double phase = entity.tickCount * 0.12;
        double y = entity.getY() + entity.getBbHeight() * 0.5;
        for (int i = 0; i < count; i++) {
            double angle = phase + Math.PI * 2.0 * i / count;
            level.sendParticles(particles,
                    entity.getX() + Math.cos(angle) * radius,
                    y + Math.sin(angle * 2.0) * 0.25,
                    entity.getZ() + Math.sin(angle) * radius,
                    1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    private static void emitAt(ServerLevel level, ParticleOptions particles, Vec3 position, int count, double spread) {
        level.sendParticles(particles, position.x, position.y, position.z,
                count, spread, spread, spread, 0.02);
    }
}
