package net.ok.project_cradle.server.spells;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.MadraType.Element;
import net.ok.project_cradle.entities.BasicStrikerProjectile;
import net.ok.project_cradle.entities.MadraShieldEntity;
import net.ok.project_cradle.entities.ModEntities;
import net.ok.project_cradle.entities.PathTechniqueProjectile;
import net.ok.project_cradle.entities.RemnantEntity;
import net.ok.project_cradle.entities.SpellBoltProjectile;
import net.ok.project_cradle.items.AdditionalTechniqueItem;
import net.ok.project_cradle.items.BasicStrikerItem;
import net.ok.project_cradle.items.EnforcerTechniqueItem;
import net.ok.project_cradle.items.ElementalTechniqueItem;
import net.ok.project_cradle.items.ForgerTechniqueItem;
import net.ok.project_cradle.items.PathTechniqueItem;
import net.ok.project_cradle.items.RulerTechniqueItem;
import net.ok.project_cradle.server.progression.DreamPacificationService;
import net.ok.project_cradle.server.progression.RemnantFusionService;
import net.ok.project_cradle.spells.AdditionalTechnique;
import net.ok.project_cradle.spells.PathTechnique;
import net.ok.project_cradle.spells.TechniqueCastProfile;
import net.ok.project_cradle.spells.TechniqueCatalog;

/** Server-owned preparation and combat shared by sacred beasts and Remnants. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class MobTechniqueService {
    private static final Map<Mob, CastState> CASTS = new WeakHashMap<>();
    private static final Map<Mob, Charm> CHARMS = new WeakHashMap<>();
    private static final Identifier ENFORCEMENT = Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "remnant_enforcement");

    private MobTechniqueService() { }

    /** State values contain only identifiers, numbers and positions; no owner or world references. */
    public static void tick(Mob caster) {
        if (!(caster.level() instanceof ServerLevel level)) return;
        if (!isCaster(caster) || !caster.isAlive() || caster.isNoAi()) {
            CASTS.remove(caster);
            clearEnforcement(caster);
            return;
        }
        CastState state = CASTS.computeIfAbsent(caster, ignored -> new CastState());
        long now = level.getGameTime();
        if (caster.tickCount % 4 == 0) caster.getData(ModAttachments.MADRA)
                .addMadra(1.0, caster.getData(ModAttachments.MAXMADRA).getLevel());
        if (state.enforcedUntil > 0 && state.enforcedUntil <= now) {
            clearEnforcement(caster);
            state.enforcedUntil = 0;
        }
        tickWell(caster, level, state, now);
        if (state.pending != null) {
            Entity entity = level.getEntity(state.target);
            LivingEntity target = entity instanceof LivingEntity living ? living : null;
            ItemStack stack = TechniqueCatalog.stack(state.pending);
            TechniqueCastProfile profile = TechniqueCastProfile.fromItemStack(stack);
            if (profile == null || !readyTarget(caster, target, stack) || caster.hurtTime > 8) {
                cancel(caster);
                return;
            }
            caster.getLookControl().setLookAt(target, 180.0F, 180.0F);
            if (now < state.finish) {
                if (caster.tickCount % 4 == 0) preparation(level, caster, profile);
                return;
            }
            String id = state.pending;
            state.pending = null;
            state.target = null;
            finish(caster, level, state, stack, id, profile, target, now);
            return;
        }
        if (now < state.ready || caster.hurtTime > 0) return;
        LivingEntity target = combatTarget(caster);
        if (!canHarm(caster, target) || !caster.hasLineOfSight(target)) return;
        List<String> known = knownTechniques(caster);
        for (int attempt = 0; attempt < known.size(); attempt++) {
            String id = known.get(Math.floorMod(state.next++, known.size()));
            if (state.cooldowns.getOrDefault(id, 0L) > now) continue;
            ItemStack stack = TechniqueCatalog.stack(id);
            TechniqueCastProfile profile = TechniqueCastProfile.fromItemStack(stack);
            if (profile == null || !readyTarget(caster, target, stack) || !worthCasting(caster, level, stack, target, state)
                    || caster.getData(ModAttachments.MADRA).getALevel() < cost(caster, profile.madraCost())
                    || caster.getData(ModAttachments.CORE).getSoulfire() < profile.soulfireCost()) continue;
            caster.getLookControl().setLookAt(target, 180.0F, 180.0F);
            if (profile.castTimeTicks() > 0) {
                state.pending = id;
                state.target = target.getUUID();
                state.finish = now + profile.castTimeTicks();
                preparation(level, caster, profile);
            } else finish(caster, level, state, stack, id, profile, target, now);
            return;
        }
        state.ready = now + 8;
    }

    /** Counterspell can interrupt the same preparation that damage or lost sight interrupts. */
    public static boolean cancel(Mob caster) {
        CastState state = CASTS.get(caster);
        if (state == null || state.pending == null) return false;
        state.pending = null;
        state.target = null;
        state.ready = caster.level().getGameTime() + 20;
        return true;
    }

    public static double power(Mob caster) {
        return (caster instanceof RemnantEntity ? 0.65 : 1.0) * (1.0 + 0.18 * Math.max(0, rank(caster).order() - 1));
    }

    public static double range(Mob caster, double base) {
        return base * (1.0 + 0.08 * Math.max(0, rank(caster).order() - 1));
    }

    private static double cost(Mob caster, double base) {
        return Math.round(base * (1.0 + 0.18 * Math.max(0, rank(caster).order() - 1)) * 100.0) / 100.0;
    }

    private static int duration(Mob caster, int ticks) {
        return (int) Math.round(ticks * (1.0 + 0.05 * Math.max(0, rank(caster).order() - 1)));
    }

    public static boolean isCaster(Mob mob) {
        return mob instanceof RemnantEntity || mob != null && mob.getData(ModAttachments.SACRED_BEAST_AGE).isSacred();
    }

    private static AdvancementLevel rank(Mob caster) {
        return caster instanceof RemnantEntity remnant ? remnant.getSourceRank()
                : caster.getData(ModAttachments.CORE).getAdvancement();
    }

    private static Element aspect(Mob caster) {
        return caster.getData(ModAttachments.MADRA_TYPE).getTypes().get(0);
    }

    private static List<String> knownTechniques(Mob caster) {
        return caster instanceof RemnantEntity remnant ? remnant.getKnownTechniques()
                : caster.getData(ModAttachments.SACRED_BEAST_AGE).getTechniques();
    }

    /** Brain-driven mobs keep their target in memory instead of Mob's target field. */
    private static LivingEntity combatTarget(Mob caster) {
        LivingEntity target = caster.getTarget();
        if (target != null) return target;
        var remembered = caster.getBrain().getMemoryInternal(MemoryModuleType.ATTACK_TARGET);
        return remembered == null ? null : remembered.orElse(null);
    }

    public static boolean isFriendly(Mob caster, LivingEntity target) {
        if (caster.isAlliedTo(target) || target.isAlliedTo(caster)) return true;
        String partner = caster.getData(ModAttachments.CORE).getBondedPartner();
        if (target.getUUID().toString().equals(partner)
                || caster.getUUID().toString().equals(target.getData(ModAttachments.CORE).getBondedPartner())) return true;
        LivingEntity owner = caster instanceof OwnableEntity ownable ? ownable.getRootOwner() : null;
        LivingEntity targetOwner = target instanceof OwnableEntity ownable ? ownable.getRootOwner() : null;
        if (owner != null && (target == owner || owner.isAlliedTo(target) || targetOwner == owner)) return true;
        return targetOwner != null && targetOwner.getUUID().toString().equals(partner);
    }

    /** Area techniques and projectiles use the same exclusions as direct targeting. */
    public static boolean canHarm(Mob caster, LivingEntity target) {
        if (caster == null || !isCaster(caster) || target == null || target == caster || !caster.isAlive() || !target.isAlive()
                || caster.level() != target.level() || target.isSpectator() || isFriendly(caster, target)
                || DreamPacificationService.isBlocked(caster, target)) return false;
        if (target instanceof Player player && player.isCreative()) return false;
        Charm charm = target instanceof Mob mob ? activeCharm(mob) : null;
        if (charm != null && charm.owner.equals(caster.getUUID())) return false;
        if (target instanceof OwnableEntity pet && pet.getRootOwner() instanceof Player) return false;
        // Remnants retain their hostile nature; sacred creatures keep vanilla aggro and retaliation.
        if (caster instanceof RemnantEntity && target instanceof Player) return true;
        return target == combatTarget(caster) || target instanceof Mob mob && combatTarget(mob) == caster;
    }

    /** Damage is already scaled at creation; piercing and growing bolts must not scale it twice. */
    public static boolean hurt(Mob caster, Entity direct, LivingEntity target, float damage) {
        if (!(caster.level() instanceof ServerLevel level) || !canHarm(caster, target)
                || !Float.isFinite(damage) || damage <= 0.0F || direct != null && direct.level() != level) return false;
        return target.hurtServer(level, caster.damageSources().indirectMagic(direct == null ? caster : direct, caster), damage);
    }

    private static boolean readyTarget(Mob caster, LivingEntity target, ItemStack stack) {
        double distance = range(caster, baseRange(stack));
        return canHarm(caster, target) && caster.hasLineOfSight(target)
                && caster.distanceToSqr(target) <= distance * distance;
    }

    private static double baseRange(ItemStack stack) {
        if (stack.getItem() instanceof ElementalTechniqueItem item)
            return item.getTechnique().baseRange() > 0.0 ? item.getTechnique().baseRange() : 8.0;
        if (stack.getItem() instanceof BasicStrikerItem) return 5.0;
        if (stack.getItem() instanceof RulerTechniqueItem) return 7.0;
        if (stack.getItem() instanceof PathTechniqueItem item) return switch (item.getTechnique()) {
            case DRAGONS_BREATH -> 16.0;
            case VOID_DRAGONS_DANCE, WIND_PRESSURE -> 7.0;
            case DRAGON_DESCENDS -> 5.0;
            case FOX_FIRE -> 6.0;
            case INVISIBLE_FLAME, ENDLESS_SWORD -> 9.0;
            case PHANTOM_LIGHTS -> 10.0;
            case RIPPLING_SWORD -> 14.0;
            case HIDDEN_BLADE, VENOM_NEEDLES -> 12.0;
            case FINAL_SWORD, VENOM_BLAST -> 11.0;
            case VENOM_MIST -> 6.0;
            case STAR_LANCE -> 18.0;
            case FORGED_NEEDLES, CUTTING_WIND -> 13.0;
            case FOX_DREAM -> 10.0;
            default -> 24.0;
        };
        if (stack.getItem() instanceof AdditionalTechniqueItem item) return switch (item.getTechnique()) {
            case FIREBALL -> 24.0;
            case ICE_LANCE -> 30.0;
            case CHAIN_LIGHTNING -> 18.0;
            case COUNTERSPELL, GRAVITY_WELL -> 12.0;
            default -> 24.0;
        };
        return 24.0;
    }

    private static boolean worthCasting(Mob caster, ServerLevel level, ItemStack stack,
            LivingEntity target, CastState state) {
        if (stack.getItem() instanceof ElementalTechniqueItem item)
            return ElementalTechniqueSpell.canCast(caster, target, item.getTechnique());
        if (stack.getItem() instanceof ForgerTechniqueItem) return expiring(caster, MobEffects.ABSORPTION);
        if (stack.getItem() instanceof EnforcerTechniqueItem) return expiring(caster, MobEffects.STRENGTH);
        if (stack.getItem() instanceof RulerTechniqueItem) return rank(caster).order() >= AdvancementLevel.JADE.order();
        if (stack.getItem() instanceof PathTechniqueItem item) return switch (item.getTechnique()) {
            case BURNING_CLOAK, FLOWING_SWORD, STARS_EDGE -> expiring(caster, MobEffects.STRENGTH);
            case FOX_TAIL, FLOWING_STARLIGHT -> expiring(caster, MobEffects.SPEED);
            case FOX_MIRROR -> expiring(caster, MobEffects.INVISIBILITY);
            case SERPENTS_SHADOW, OATHSIGN, STONE_ANCHOR -> expiring(caster, MobEffects.RESISTANCE);
            case GRASPING_SKY, FORGED_WINGS -> expiring(caster, MobEffects.SLOW_FALLING);
            case FOX_DREAM -> target instanceof Mob mob && charmable(caster, mob) && findCharmEnemy(caster, mob, range(caster, 8.0)) != null;
            default -> true;
        };
        if (stack.getItem() instanceof AdditionalTechniqueItem item) return switch (item.getTechnique()) {
            case HEALING_PULSE -> caster.getHealth() < caster.getMaxHealth();
            case BLINK -> caster.distanceToSqr(target) > 16.0;
            case GRAVITY_WELL -> state.well == null;
            case SHIELD -> level.getEntitiesOfClass(MadraShieldEntity.class, caster.getBoundingBox().inflate(40.0),
                    shield -> shield.isOwnedBy(caster) && shield.isAlive()).isEmpty();
            case COUNTERSPELL -> canCounter(caster, target);
            default -> true;
        };
        return true;
    }

    private static boolean expiring(LivingEntity entity, Holder<MobEffect> effect) {
        MobEffectInstance active = entity.getEffect(effect);
        return active == null || active.getDuration() <= 30;
    }

    private static void finish(Mob caster, ServerLevel level, CastState state, ItemStack stack,
            String id, TechniqueCastProfile profile, LivingEntity target, long now) {
        double price = cost(caster, profile.madraCost());
        var madra = caster.getData(ModAttachments.MADRA);
        var core = caster.getData(ModAttachments.CORE);
        if (!madra.trySpendMadra(price)) {
            state.ready = now + 10;
            return;
        }
        if (profile.soulfireCost() > 0.0 && !core.trySpendSoulfire(profile.soulfireCost())) {
            madra.addMadra(price, caster.getData(ModAttachments.MAXMADRA).getLevel());
            state.ready = now + 10;
            return;
        }
        if (!perform(caster, level, state, stack, target)) {
            madra.addMadra(price, caster.getData(ModAttachments.MAXMADRA).getLevel());
            if (profile.soulfireCost() > 0.0) core.addSoulfire(profile.soulfireCost());
            state.ready = now + 10;
            return;
        }
        state.cooldowns.put(id, now + Math.max(30, profile.cooldownTicks()));
        state.ready = now + 30;
        level.playSound(null, caster.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.HOSTILE, 0.55F, profile.aspect() == Element.SWORD ? 1.4F : 1.05F);
    }

    private static boolean perform(Mob caster, ServerLevel level, CastState state,
            ItemStack stack, LivingEntity target) {
        if (stack.getItem() instanceof ElementalTechniqueItem item)
            return ElementalTechniqueSpell.perform(ElementalCastingContext.of(caster), item.getTechnique());
        if (stack.getItem() instanceof BasicStrikerItem) {
            BasicStrikerProjectile bolt = new BasicStrikerProjectile(level, caster, stack);
            bolt.setMadraElement(aspect(caster));
            bolt.setSpellDamage((float) (6.0 * power(caster)));
            bolt.setMaxTravelDistance(range(caster, 5.0));
            aim(bolt, caster, target, 1.8F, 0.0F);
            return level.addFreshEntity(bolt);
        }
        if (stack.getItem() instanceof ForgerTechniqueItem) {
            effect(caster, caster, MobEffects.ABSORPTION, 200, 1);
            halo(level, caster, ParticleTypes.ENCHANT, 0.8, 12);
            return true;
        }
        if (stack.getItem() instanceof EnforcerTechniqueItem) {
            effect(caster, caster, MobEffects.STRENGTH, 160, 0);
            effect(caster, caster, MobEffects.SPEED, 160, 0);
            effect(caster, caster, MobEffects.RESISTANCE, 100, 0);
            enforce(caster, state, 160);
            halo(level, caster, ParticleTypes.ENCHANTED_HIT, 0.8, 12);
            return true;
        }
        if (stack.getItem() instanceof RulerTechniqueItem) {
            for (LivingEntity enemy : nearby(caster, 7.0)) {
                if (!hurt(caster, caster, enemy, (float) (5.0 * power(caster)))) continue;
                effect(caster, enemy, MobEffects.SLOWNESS, 60, 1);
                push(caster, enemy, 0.55);
            }
            sigil(level, caster, 3.0, aspect(caster));
            return true;
        }
        if (stack.getItem() instanceof PathTechniqueItem item) return path(caster, level, state, stack, target, item.getTechnique());
        if (stack.getItem() instanceof AdditionalTechniqueItem item) return additional(caster, level, state, stack, target, item.getTechnique());
        return false;
    }

    private static boolean path(Mob caster, ServerLevel level, CastState state, ItemStack stack,
            LivingEntity target, PathTechnique technique) {
        switch (technique) {
            case DRAGONS_BREATH: return projectile(caster, level, stack, target, technique, 11.0F, 2.1F, 16.0, 0, true, false);
            case RIPPLING_SWORD: return projectile(caster, level, stack, target, technique, 7.0F, 2.4F, 14.0, 0, false, false);
            case HIDDEN_BLADE: return projectile(caster, level, stack, target, technique, 8.0F, 2.6F, 12.0, 0, false, true);
            case STAR_LANCE: return projectile(caster, level, stack, target, technique, 8.0F, 3.3F, 18.0, 0, false, false);
            case CUTTING_WIND: return projectile(caster, level, stack, target, technique, 6.0F, 2.6F, 13.0, 0, false, false);
            case VENOM_NEEDLES: return volley(caster, level, stack, target, technique, 4, 3.0F, 1.9F, 12.0, 7.0F);
            case VENOM_BLAST: return volley(caster, level, stack, target, technique, 3, 5.0F, 1.8F, 11.0, 5.5F);
            case FORGED_NEEDLES: return volley(caster, level, stack, target, technique, 4, 4.0F, 2.0F, 13.0, 8.0F);
            case BURNING_CLOAK:
                effect(caster, caster, MobEffects.SPEED, 140, 1);
                effect(caster, caster, MobEffects.STRENGTH, 140, 1);
                enforce(caster, state, 140);
                halo(level, caster, ParticleTypes.FLAME, 0.8, 12);
                break;
            case VOID_DRAGONS_DANCE:
                for (LivingEntity enemy : nearby(caster, 7.0)) {
                    if (!hurt(caster, caster, enemy, (float) (8.0 * power(caster)))) continue;
                    enemy.igniteForSeconds(5.0F);
                    effect(caster, enemy, MobEffects.WITHER, 80, 0);
                    push(caster, enemy, 0.45);
                }
                halo(level, caster, ParticleTypes.FLAME, 1.7, 20);
                halo(level, caster, ParticleTypes.LARGE_SMOKE, 1.4, 10);
                break;
            case DRAGON_DESCENDS:
                effect(caster, caster, MobEffects.SPEED, 50, 1);
                effect(caster, caster, MobEffects.STRENGTH, 50, 1);
                enforce(caster, state, 50);
                for (LivingEntity enemy : cone(caster, target, 5.0, 120.0)) {
                    if (!hurt(caster, caster, enemy, (float) (16.0 * power(caster)))) continue;
                    enemy.igniteForSeconds(6.0F);
                    effect(caster, enemy, MobEffects.WITHER, 100, 0);
                    push(caster, enemy, 1.4);
                }
                emit(level, ParticleTypes.EXPLOSION, caster.getEyePosition().add(direction(caster, target).scale(2.0)), 2, 0.2);
                break;
            case FOX_FIRE:
                for (int i = 1; i <= 9; i++) {
                    Vec3 center = caster.getEyePosition().add(direction(caster, target).scale(range(caster, i * 0.65)));
                    emit(level, ParticleTypes.SOUL_FIRE_FLAME, center, 3, 0.2);
                    emit(level, ParticleTypes.WITCH, center, 1, 0.2);
                }
                break; // These flames are an illusion, just as with the living artist's technique.
            case INVISIBLE_FLAME:
                for (LivingEntity enemy : cone(caster, target, 9.0, 20.0)) {
                    effect(caster, enemy, MobEffects.POISON, 80, 0);
                    effect(caster, enemy, MobEffects.WEAKNESS, 80, 0);
                }
                break;
            case PHANTOM_LIGHTS:
                for (LivingEntity enemy : cone(caster, target, 10.0, 28.0)) {
                    effect(caster, enemy, MobEffects.BLINDNESS, 50, 0);
                    emit(level, ParticleTypes.END_ROD, enemy.getEyePosition(), 6, 0.35);
                }
                break;
            case FOX_TAIL:
                effect(caster, caster, MobEffects.SPEED, 160, 1);
                effect(caster, caster, MobEffects.INVISIBILITY, 50, 0);
                halo(level, caster, ParticleTypes.CLOUD, 0.8, 10);
                break;
            case FOX_DREAM: return target instanceof Mob mob && charm(caster, mob);
            case FOX_MIRROR:
                effect(caster, caster, MobEffects.INVISIBILITY, 100, 0);
                effect(caster, caster, MobEffects.SPEED, 100, 0);
                effect(caster, caster, MobEffects.RESISTANCE, 60, 0);
                Vec3 across = direction(caster, target).cross(new Vec3(0, 1, 0)).normalize();
                for (int side : new int[] {-1, 1}) {
                    Vec3 decoy = caster.position().add(across.scale(side * 1.1)).add(0, 0.4, 0);
                    emit(level, ParticleTypes.CLOUD, decoy, 8, 0.3);
                    emit(level, ParticleTypes.WITCH, decoy.add(0, 0.8, 0), 4, 0.2);
                }
                break;
            case FLOWING_SWORD:
                effect(caster, caster, MobEffects.STRENGTH, 200, 0);
                effect(caster, caster, MobEffects.SPEED, 120, 0);
                enforce(caster, state, 200);
                halo(level, caster, ParticleTypes.CRIT, 0.8, 10);
                break;
            case ENDLESS_SWORD: return slash(caster, level, target, 9.0, 7.5F, 65.0, 0.85);
            case FINAL_SWORD:
                slash(caster, level, target, 11.0, 14.0F, 100.0, 1.2);
                effect(caster, caster, MobEffects.STRENGTH, 80, 0);
                break;
            case VENOM_MIST:
                for (LivingEntity enemy : nearby(caster, 6.0)) {
                    effect(caster, enemy, MobEffects.POISON, 120, 0);
                    effect(caster, enemy, MobEffects.SLOWNESS, 80, 0);
                }
                halo(level, caster, ParticleTypes.SPORE_BLOSSOM_AIR, 2.2, 18);
                halo(level, caster, ParticleTypes.WITCH, 1.9, 12);
                break;
            case FLOWING_STARLIGHT:
                effect(caster, caster, MobEffects.SPEED, 220, 1);
                effect(caster, caster, MobEffects.NIGHT_VISION, 260, 0);
                effect(caster, caster, MobEffects.JUMP_BOOST, 160, 0);
                enforce(caster, state, 220);
                halo(level, caster, ParticleTypes.END_ROD, 0.9, 12);
                break;
            case STARS_EDGE:
                effect(caster, caster, MobEffects.STRENGTH, 180, 1);
                enforce(caster, state, 180);
                emit(level, ParticleTypes.END_ROD, caster.getEyePosition(), 8, 0.3);
                break;
            case SERPENTS_SHADOW:
                effect(caster, caster, MobEffects.RESISTANCE, 160, 0);
                effect(caster, caster, MobEffects.SLOW_FALLING, 120, 0);
                enforce(caster, state, 160);
                halo(level, caster, ParticleTypes.END_ROD, 0.9, 10);
                break;
            case OATHSIGN:
                double oathRange = range(caster, 5.0);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(oathRange),
                        entity -> entity.isAlive() && (entity == caster || caster.isAlliedTo(entity))
                                && caster.distanceToSqr(entity) <= oathRange * oathRange && caster.hasLineOfSight(entity))) {
                    effect(caster, ally, MobEffects.RESISTANCE, 160, 0);
                    effect(caster, ally, MobEffects.ABSORPTION, 160, 0);
                }
                halo(level, caster, ParticleTypes.HAPPY_VILLAGER, 1.3, 12);
                break;
            case STONE_ANCHOR:
                effect(caster, caster, MobEffects.RESISTANCE, 180, 1);
                effect(caster, caster, MobEffects.SLOWNESS, 180, 1);
                enforce(caster, state, 180);
                sigil(level, caster, 1.2, Element.EARTH);
                break;
            case GRASPING_SKY:
                effect(caster, caster, MobEffects.JUMP_BOOST, 220, 2);
                effect(caster, caster, MobEffects.SLOW_FALLING, 260, 0);
                enforce(caster, state, 220);
                halo(level, caster, ParticleTypes.CLOUD, 0.9, 12);
                break;
            case WIND_PRESSURE:
                for (LivingEntity enemy : nearby(caster, 7.0)) {
                    effect(caster, enemy, MobEffects.SLOWNESS, 100, 1);
                    effect(caster, enemy, MobEffects.WEAKNESS, 100, 0);
                    push(caster, enemy, 0.35);
                }
                halo(level, caster, ParticleTypes.GUST, 1.5, 12);
                break;
            case FORGED_WINGS:
                effect(caster, caster, MobEffects.SLOW_FALLING, 400, 0);
                effect(caster, caster, MobEffects.JUMP_BOOST, 220, 1);
                effect(caster, caster, MobEffects.RESISTANCE, 100, 0);
                enforce(caster, state, 220);
                for (int side : new int[] {-1, 1}) {
                    Vec3 wing = caster.position().add(side * 0.65, caster.getBbHeight() * 0.65, 0);
                    emit(level, ParticleTypes.CLOUD, wing, 10, 0.3);
                    emit(level, ParticleTypes.END_ROD, wing, 5, 0.25);
                }
                break;
        }
        return true;
    }

    private static boolean additional(Mob caster, ServerLevel level, CastState state, ItemStack stack,
            LivingEntity target, AdditionalTechnique technique) {
        return switch (technique) {
            case SHIELD -> {
                // The cardinal barrier faces the same horizontal direction as its incoming enemy.
                Vec3 delta = target.position().subtract(caster.position());
                caster.setYRot((float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0));
                yield MadraShieldEntity.spawn(caster);
            }
            case FIREBALL, ICE_LANCE -> {
                SpellBoltProjectile bolt = new SpellBoltProjectile(level, caster, stack.copyWithCount(1));
                bolt.setTechnique(technique);
                bolt.setSpellDamage((float) ((technique == AdditionalTechnique.FIREBALL ? 10.0 : 7.0) * power(caster)));
                bolt.setMaxTravelDistance(range(caster, technique == AdditionalTechnique.FIREBALL ? 24.0 : 30.0));
                aim(bolt, caster, target, technique == AdditionalTechnique.FIREBALL ? 0.8F : 1.65F, 0);
                yield level.addFreshEntity(bolt);
            }
            case CHAIN_LIGHTNING -> chainLightning(caster, level, target);
            case BLINK -> blink(caster, level, target);
            case HEALING_PULSE -> {
                if (caster.getHealth() >= caster.getMaxHealth()) yield false;
                caster.heal((float) (4.0 * power(caster)));
                emit(level, ParticleTypes.HEART, caster.getEyePosition(), 5, 0.4);
                halo(level, caster, ParticleTypes.HAPPY_VILLAGER, 1.6, 14);
                yield true;
            }
            case COUNTERSPELL -> counterspell(caster, level, target);
            case GRAVITY_WELL -> {
                if (state.well != null) yield false;
                state.well = target.position().add(0, 0.6, 0);
                state.wellUntil = level.getGameTime() + duration(caster, 100);
                state.wellPulse = level.getGameTime() + 20;
                sigil(level, caster, 1.0, Element.FORCE);
                yield true;
            }
        };
    }

    private static boolean projectile(Mob caster, ServerLevel level, ItemStack stack, LivingEntity target,
            PathTechnique technique, float damage, float speed, double distance, float yaw, boolean burn, boolean hidden) {
        var registered = ModEntities.PATH_PROJECTILES.get(technique);
        PathTechniqueProjectile bolt = registered == null
                ? new PathTechniqueProjectile(ModEntities.BASIC_STRIKER_PROJECTILE.get(), level, caster, stack)
                : new PathTechniqueProjectile(registered.get(), level, caster, stack);
        bolt.setMadraElement(technique.aspect());
        bolt.setSpellDamage((float) (damage * power(caster)));
        bolt.setMaxTravelDistance(range(caster, distance));
        bolt.setBurnOnHit(burn);
        bolt.setInvisible(hidden);
        aim(bolt, caster, target, speed, yaw);
        return level.addFreshEntity(bolt);
    }

    private static boolean volley(Mob caster, ServerLevel level, ItemStack stack, LivingEntity target,
            PathTechnique technique, int count, float damage, float speed, double distance, float spread) {
        boolean spawned = false;
        for (int i = 0; i < count; i++) spawned |= projectile(caster, level, stack, target, technique,
                damage, speed, distance, (i - (count - 1) / 2.0F) * spread, false, false);
        return spawned;
    }

    private static void aim(Projectile bolt, Mob caster, LivingEntity target, float speed, float yaw) {
        Vec3 direction = direction(caster, target).yRot((float) Math.toRadians(yaw));
        bolt.shoot(direction.x, direction.y, direction.z, speed, 0.0F);
    }

    private static Vec3 direction(Mob caster, LivingEntity target) {
        Vec3 direction = target.getBoundingBox().getCenter().subtract(caster.getEyePosition());
        return direction.lengthSqr() < 0.001 ? caster.getLookAngle() : direction.normalize();
    }

    private static List<LivingEntity> nearby(Mob caster, double radius) {
        double effective = range(caster, radius);
        return caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(effective),
                enemy -> canHarm(caster, enemy) && caster.distanceToSqr(enemy) <= effective * effective && caster.hasLineOfSight(enemy));
    }

    private static List<LivingEntity> cone(Mob caster, LivingEntity aimAt, double radius, double arc) {
        Vec3 look = direction(caster, aimAt);
        double dot = Math.cos(Math.toRadians(arc * 0.5));
        return nearby(caster, radius).stream().filter(enemy -> {
            Vec3 delta = enemy.getBoundingBox().getCenter().subtract(caster.getEyePosition());
            return delta.lengthSqr() < 0.01 || delta.normalize().dot(look) >= dot;
        }).toList();
    }

    private static boolean slash(Mob caster, ServerLevel level, LivingEntity target,
            double radius, float damage, double arc, double knockback) {
        for (LivingEntity enemy : cone(caster, target, radius, arc)) {
            if (hurt(caster, caster, enemy, (float) (damage * power(caster)))) push(caster, enemy, knockback);
        }
        Vec3 center = caster.getEyePosition().add(direction(caster, target).scale(Math.min(range(caster, radius) * 0.6, 4.0)));
        emit(level, ParticleTypes.SWEEP_ATTACK, center, 6, 0.9);
        emit(level, ParticleTypes.CRIT, center, 8, 0.6);
        return true;
    }

    private static boolean chainLightning(Mob caster, ServerLevel level, LivingEntity first) {
        Set<UUID> struck = new HashSet<>();
        LivingEntity target = first;
        Vec3 from = caster.getEyePosition();
        double jumpRange = range(caster, 5.0);
        for (int jump = 0; jump < 5 && target != null; jump++) {
            AdditionalTechniqueSpell.lightning(level, from, target.getEyePosition());
            hurt(caster, caster, target, (float) (Math.max(3.0, 8.0 - jump) * power(caster)));
            struck.add(target.getUUID());
            LivingEntity previous = target;
            from = previous.getEyePosition();
            target = level.getEntitiesOfClass(LivingEntity.class, previous.getBoundingBox().inflate(jumpRange),
                            enemy -> canHarm(caster, enemy) && !struck.contains(enemy.getUUID()) && previous.hasLineOfSight(enemy)
                                    && previous.distanceToSqr(enemy) <= jumpRange * jumpRange)
                    .stream().min(Comparator.comparingDouble(previous::distanceToSqr)).orElse(null);
        }
        return !struck.isEmpty();
    }

    private static boolean blink(Mob caster, ServerLevel level, LivingEntity target) {
        if (caster.isPassenger()) return false;
        Vec3 start = caster.position();
        Vec3 direction = direction(caster, target);
        double maximum = Math.min(range(caster, 8.0), Math.max(0.0, caster.distanceTo(target) - caster.getBbWidth() - 1.0));
        Vec3 destination = start;
        for (double distance = 0.25; distance <= maximum; distance += 0.25) {
            Vec3 step = start.add(direction.scale(distance));
            AABB bounds = caster.getBoundingBox().move(step.subtract(start));
            if (!safeBlinkSpace(level, caster, bounds)) break;
            destination = step;
        }
        if (destination.distanceToSqr(start) < 0.25) return false;
        emit(level, ParticleTypes.PORTAL, caster.getEyePosition(), 14, 0.4);
        caster.teleportTo(destination.x, destination.y, destination.z);
        caster.resetFallDistance();
        emit(level, ParticleTypes.REVERSE_PORTAL, caster.getEyePosition(), 14, 0.4);
        level.playSound(null, caster.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.6F, 1.3F);
        return true;
    }

    private static boolean safeBlinkSpace(ServerLevel level, Mob caster, AABB bounds) {
        if (!level.getWorldBorder().isWithinBounds(bounds)) return false;
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
            if (level.isOutsideBuildHeight(pos) || !level.hasChunkAt(pos)) return false;
        }
        return level.noCollision(caster, bounds);
    }

    private static boolean canCounter(Mob caster, LivingEntity target) {
        if (target instanceof Mob mob) {
            CastState targetState = CASTS.get(mob);
            if (targetState != null && targetState.pending != null) return true;
        }
        // Player cast state is intentionally private; attempting cancellation is cheap and refundable.
        return target instanceof ServerPlayer || !incoming(caster, target).isEmpty();
    }

    private static List<Projectile> incoming(Mob caster, LivingEntity target) {
        double maximum = range(caster, 12.0);
        Vec3 eye = caster.getEyePosition();
        Vec3 look = direction(caster, target);
        return caster.level().getEntitiesOfClass(Projectile.class, caster.getBoundingBox().inflate(maximum), spell -> {
            if (!spell.isAlive() || spell.getOwner() == caster || spell.getOwner() instanceof LivingEntity owner
                    && (!canHarm(caster, owner) || caster.isAlliedTo(owner))) return false;
            Vec3 delta = spell.position().subtract(eye);
            return delta.lengthSqr() <= maximum * maximum && (delta.lengthSqr() < 0.01 || delta.normalize().dot(look) >= 0.75)
                    && AdditionalTechniqueSpell.clearLine((ServerLevel) caster.level(), eye, spell.position(), caster);
        });
    }

    private static boolean counterspell(Mob caster, ServerLevel level, LivingEntity target) {
        boolean changed = false;
        for (Projectile spell : incoming(caster, target)) {
            emit(level, ParticleTypes.ENCHANTED_HIT, spell.position(), 5, 0.15);
            spell.discard();
            changed = true;
        }
        for (LivingEntity enemy : cone(caster, target, 12.0, 80.0)) {
            boolean interrupted = enemy instanceof ServerPlayer player ? TechniqueCastingService.cancel(player)
                    : enemy instanceof Mob mob && cancel(mob);
            if (!interrupted) continue;
            AdditionalTechniqueSpell.lightning(level, caster.getEyePosition(), enemy.getEyePosition());
            changed = true;
        }
        return changed;
    }

    private static void tickWell(Mob caster, ServerLevel level, CastState state, long now) {
        if (state.well == null) return;
        if (now >= state.wellUntil || !level.hasChunkAt(BlockPos.containing(state.well))) {
            state.well = null;
            return;
        }
        Vec3 center = state.well;
        double radius = range(caster, 4.0);
        if (now % 5 == 0) {
            TechniqueVisualService.sigil(level, center, radius, Element.FORCE, now * 0.09, 16);
            emit(level, ParticleTypes.REVERSE_PORTAL, center, 3, 0.3);
        }
        boolean pulse = now >= state.wellPulse;
        if (pulse) state.wellPulse = now + 20;
        for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(center, radius * 2, radius * 2, radius * 2),
                entity -> canHarm(caster, entity) && entity.distanceToSqr(center) <= radius * radius && caster.hasLineOfSight(entity)
                        && AdditionalTechniqueSpell.clearLine(level, center, entity.getEyePosition(), caster))) {
            Vec3 pull = center.subtract(enemy.getBoundingBox().getCenter());
            if (pull.lengthSqr() > 0.09) {
                Vec3 force = pull.normalize().scale(0.085);
                enemy.push(force.x, force.y * 0.55, force.z);
                enemy.hurtMarked = true;
            }
            if (pulse) hurt(caster, caster, enemy, (float) (3.0 * power(caster)));
        }
    }

    private static void enforce(Mob caster, CastState state, int ticks) {
        state.enforcedUntil = Math.max(state.enforcedUntil, caster.level().getGameTime() + duration(caster, ticks));
        double bonus = Math.max(0.0, power(caster) - 1.0);
        modifier(caster, Attributes.ATTACK_DAMAGE, bonus * 3.0, AttributeModifier.Operation.ADD_VALUE);
        modifier(caster, Attributes.MOVEMENT_SPEED, bonus * 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(caster, Attributes.ARMOR, bonus * 2.0, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void modifier(Mob caster, Holder<net.minecraft.world.entity.ai.attributes.Attribute> type,
            double amount, AttributeModifier.Operation operation) {
        var attribute = caster.getAttribute(type);
        if (attribute == null) return;
        attribute.removeModifier(ENFORCEMENT);
        if (amount > 0.0) attribute.addTransientModifier(new AttributeModifier(ENFORCEMENT, amount, operation));
    }

    private static void clearEnforcement(Mob caster) {
        for (var type : List.of(Attributes.ATTACK_DAMAGE, Attributes.MOVEMENT_SPEED, Attributes.ARMOR)) {
            var attribute = caster.getAttribute(type);
            if (attribute != null) attribute.removeModifier(ENFORCEMENT);
        }
    }

    private static void effect(Mob caster, LivingEntity target, Holder<MobEffect> effect, int ticks, int amplifier) {
        if (target != caster && !caster.isAlliedTo(target) && !canHarm(caster, target)) return;
        target.addEffect(new MobEffectInstance(effect, duration(caster, ticks), amplifier, true, false, false));
    }

    private static void push(Entity source, LivingEntity target, double amount) {
        Vec3 delta = target.position().subtract(source.position()).normalize();
        target.push(delta.x * amount, 0.15 + amount * 0.18, delta.z * amount);
        target.hurtMarked = true;
    }

    private static void preparation(ServerLevel level, Mob caster, TechniqueCastProfile profile) {
        Element aspect = profile.aspect() == null ? aspect(caster) : profile.aspect();
        Vec3 hand = caster.getEyePosition().add(caster.getLookAngle().scale(0.5));
        emit(level, new DustParticleOptions(TechniqueVisualService.color(aspect), 0.75F), hand, 2, 0.1);
        if (caster.tickCount % 12 == 0) sigil(level, caster, 0.75, aspect);
    }

    private static void sigil(ServerLevel level, Mob caster, double radius, Element aspect) {
        TechniqueVisualService.sigil(level, caster.position().add(0, 0.15, 0), radius, aspect, caster.tickCount * 0.1, 16);
    }

    private static void halo(ServerLevel level, Entity entity, ParticleOptions particles, double radius, int count) {
        AdditionalTechniqueSpell.ring(level, entity.position().add(0, entity.getBbHeight() * 0.5, 0), radius, particles, count);
    }

    private static void emit(ServerLevel level, ParticleOptions particles, Vec3 position, int count, double spread) {
        level.sendParticles(particles, position.x, position.y, position.z, count, spread, spread, spread, 0.02);
    }

    // Fox Dream only turns an existing hostile creature against another hostile creature.
    // UUID-only values and transient brain memories keep the influence temporary across unloads.
    private static boolean charmable(Mob caster, Mob target) {
        return canHarm(caster, target) && target instanceof Enemy && !target.isNoAi() && activeCharm(target) == null
                && !FoxDreamCharmService.isCharmed(target)
                && !(target instanceof OwnableEntity) && !(target instanceof EnderDragon) && !(target instanceof WitherBoss)
                && !(target instanceof Warden) && !RemnantFusionService.isOwnRemnant(target);
    }

    private static boolean charmEnemy(Mob caster, Mob controlled, Mob enemy, double radius) {
        return enemy != caster && enemy != controlled && enemy instanceof Enemy && enemy.isAlive() && !enemy.isNoAi()
                && !(enemy instanceof OwnableEntity) && !isFriendly(caster, enemy) && !controlled.isAlliedTo(enemy)
                && controlled.canAttack(enemy) && activeCharm(enemy) == null && !RemnantFusionService.isOwnRemnant(enemy)
                && !FoxDreamCharmService.isCharmed(enemy)
                && controlled.distanceToSqr(enemy) <= radius * radius && controlled.hasLineOfSight(enemy);
    }

    private static Mob findCharmEnemy(Mob caster, Mob controlled, double radius) {
        return caster.level().getEntitiesOfClass(Mob.class, controlled.getBoundingBox().inflate(radius),
                        enemy -> charmEnemy(caster, controlled, enemy, radius))
                .stream().min(Comparator.comparingDouble(controlled::distanceToSqr)).orElse(null);
    }

    private static boolean charm(Mob caster, Mob controlled) {
        if (!charmable(caster, controlled)) return false;
        double radius = range(caster, 8.0);
        Mob enemy = findCharmEnemy(caster, controlled, radius);
        if (enemy == null) return false;
        Charm charm = new Charm(caster.getUUID(), enemy.getUUID(), caster.level().getGameTime() + duration(caster, 200), radius);
        CHARMS.put(controlled, charm);
        directCharm(controlled, enemy, charm);
        if (caster.getTarget() == controlled) caster.setTarget(null);
        emit((ServerLevel) caster.level(), ParticleTypes.HEART, controlled.getEyePosition(), 5, 0.3);
        return true;
    }

    private static Charm activeCharm(Mob mob) {
        Charm charm = CHARMS.get(mob);
        return charm != null && charm.until > mob.level().getGameTime() ? charm : null;
    }

    public static boolean isCharmed(Mob mob) {
        return mob != null && activeCharm(mob) != null;
    }

    private static Mob charmOwner(Mob mob, Charm charm) {
        if (!(mob.level() instanceof ServerLevel level)) return null;
        Entity owner = level.getEntity(charm.owner);
        return owner instanceof Mob caster && isCaster(caster) && caster.isAlive() && !caster.isNoAi() ? caster : null;
    }

    private static Mob currentCharmEnemy(Mob caster, Mob controlled, Charm charm) {
        Entity current = ((ServerLevel) caster.level()).getEntity(charm.enemy);
        return current instanceof Mob mob && charmEnemy(caster, controlled, mob, charm.radius)
                ? mob : findCharmEnemy(caster, controlled, charm.radius);
    }

    private static void directCharm(Mob controlled, Mob enemy, Charm charm) {
        if (controlled.getTarget() != enemy) {
            resetFuse(controlled);
            controlled.setTarget(enemy);
        }
        var brain = controlled.getBrain();
        if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)) {
            brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, enemy, Math.max(1, charm.until - controlled.level().getGameTime()));
        }
        // ANGRY_AT is deliberately untouched: it is saved and must not outlive a temporary charm.
    }

    private static void releaseCharm(Mob controlled, Charm charm) {
        if (controlled.getTarget() != null && controlled.getTarget().getUUID().equals(charm.enemy)) controlled.setTarget(null);
        var remembered = controlled.getBrain().getMemoryInternal(MemoryModuleType.ATTACK_TARGET);
        if (remembered != null && remembered.filter(target -> target.getUUID().equals(charm.enemy)).isPresent()) {
            controlled.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
        resetFuse(controlled);
        controlled.getNavigation().stop();
    }

    private static void resetFuse(Mob controlled) {
        if (controlled instanceof Creeper creeper) {
            creeper.setSwellDir(-1);
            for (var goal : creeper.getGoalSelector().getAvailableGoals()) if (goal.getGoal() instanceof SwellGoal) goal.stop();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCharmTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        Charm charm = activeCharm(mob);
        if (charm == null) return;
        Mob owner = charmOwner(mob, charm);
        Mob enemy = owner == null ? null : currentCharmEnemy(owner, mob, charm);
        if (enemy != null) {
            if (mob.getTarget() != enemy) resetFuse(mob);
            charm.enemy = enemy.getUUID();
        }
        event.setNewAboutToBeSetTarget(enemy);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCharmedDamage(LivingIncomingDamageEvent event) {
        Entity entity = event.getSource().getEntity();
        if (!(entity instanceof Mob) && event.getSource().getDirectEntity() instanceof Projectile projectile) entity = projectile.getOwner();
        if (!(entity instanceof Mob controlled)) return;
        Charm charm = activeCharm(controlled);
        if (charm == null) return;
        Mob owner = charmOwner(controlled, charm);
        LivingEntity target = event.getEntity();
        if (owner == null || !(target instanceof Mob enemy) || !charmEnemy(owner, controlled, enemy, charm.radius)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onCharmTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (var entry : new ArrayList<>(CHARMS.entrySet())) {
            Mob controlled = entry.getKey();
            Charm charm = entry.getValue();
            if (controlled.level() != level) continue;
            Mob owner = charmOwner(controlled, charm);
            Mob enemy = owner == null || !controlled.isAlive() || controlled.isNoAi() || charm.until <= level.getGameTime()
                    ? null : currentCharmEnemy(owner, controlled, charm);
            if (enemy == null) {
                CHARMS.remove(controlled);
                releaseCharm(controlled, charm);
                continue;
            }
            charm.enemy = enemy.getUUID();
            directCharm(controlled, enemy, charm);
            if (level.getGameTime() % 20 == 0) emit(level, ParticleTypes.HEART, controlled.getEyePosition(), 2, 0.2);
            // Their vanilla contact attack only accepts players and golems, although their AI can pursue other mobs.
            if (controlled instanceof AbstractCubeMob cube && (cube instanceof MagmaCube || !cube.isTiny())
                    && level.getGameTime() >= charm.nextContact
                    && cube.getBoundingBox().inflate(0.1).intersects(enemy.getBoundingBox()) && cube.hasLineOfSight(enemy)) {
                charm.nextContact = level.getGameTime() + 20;
                float damage = (float) cube.getAttributeValue(Attributes.ATTACK_DAMAGE) + (cube instanceof MagmaCube ? 2.0F : 0.0F);
                enemy.hurtServer(level, cube.damageSources().mobAttack(cube), damage);
            }
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Mob caster) {
            CASTS.remove(caster);
            clearEnforcement(caster);
        }
        if (event.getEntity() instanceof Mob mob) {
            Charm charm = CHARMS.remove(mob);
            if (charm != null) releaseCharm(mob, charm);
        }
    }

    @SubscribeEvent
    public static void onStop(ServerStoppedEvent event) {
        CASTS.clear();
        CHARMS.clear();
    }

    private static final class CastState {
        int next;
        long ready;
        String pending;
        UUID target;
        long finish;
        long enforcedUntil;
        final Map<String, Long> cooldowns = new HashMap<>();
        Vec3 well;
        long wellUntil;
        long wellPulse;
    }

    private static final class Charm {
        final UUID owner;
        UUID enemy;
        final long until;
        final double radius;
        long nextContact;

        Charm(UUID owner, UUID enemy, long until, double radius) {
            this.owner = owner;
            this.enemy = enemy;
            this.until = until;
            this.radius = radius;
        }
    }
}
