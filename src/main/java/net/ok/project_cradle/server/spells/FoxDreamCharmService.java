package net.ok.project_cradle.server.spells;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.server.progression.RemnantFusionService;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.spells.TechniqueScaling;

/** A temporary on-hit charm. Ordinary mob AI supplies attacks; no goals or ownership are replaced. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class FoxDreamCharmService {
    public static final int AURA_TICKS = 240;
    public static final int CHARM_TICKS = 200;
    public static final double CHARM_MADRA_COST = 4.0;
    private static final double ENEMY_RADIUS = 8.0;
    private static final String FORCED_ANGER = "project_cradle.fox_dream_anger";
    private static final Map<ServerLevel, WorldState> WORLDS = new WeakHashMap<>();

    private FoxDreamCharmService() { }

    public static boolean isCharmed(Mob mob) {
        return mob != null && mob.level() instanceof ServerLevel level && activeCharm(level, mob) != null;
    }

    public static boolean activate(ServerPlayer caster) {
        if (!caster.isAlive() || caster.isSpectator()) return false;
        WorldState state = WORLDS.computeIfAbsent(caster.level(), ignored -> new WorldState());
        state.auras.put(caster.getUUID(), caster.level().getGameTime() + TechniqueScaling.duration(caster, AURA_TICKS));
        hearts(caster.level(), caster, 5);
        return true;
    }

    @SubscribeEvent
    public static void onPlayerHit(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)
                || !(attackOwner(event.getSource().getEntity(), event.getSource().getDirectEntity()) instanceof ServerPlayer caster)
                || !event.shouldCauseSideEffects() || event.getInflictedDamage() <= 0.0F
                || !caster.isAlive() || caster.isSpectator() || caster.level() != level
                || !mob.isAlive() || mob.isNoAi() || !(mob instanceof Enemy)
                || mob instanceof EnderDragon || mob instanceof WitherBoss || mob instanceof Warden
                || RemnantFusionService.isOwnRemnant(mob)) return;
        WorldState state = WORLDS.get(level);
        long now = level.getGameTime();
        if (state == null || state.auras.getOrDefault(caster.getUUID(), 0L) <= now
                || activeCharm(level, mob) != null || RemnantTechniqueService.isCharmed(mob)
                || !isEnemy(caster, null, mob)) return;
        double radius = TechniqueScaling.range(caster, ENEMY_RADIUS);
        Mob enemy = findEnemy(caster, mob, radius);
        if (enemy == null) return;
        double cost = TechniqueScaling.madraCost(caster, CHARM_MADRA_COST);
        if (!caster.getData(ModAttachments.MADRA).trySpendMadra(cost)) return;
        caster.syncData(ModAttachments.MADRA);
        Charm charm = new Charm(caster.getUUID(), enemy.getUUID(),
                now + TechniqueScaling.duration(caster, CHARM_TICKS), radius);
        state.charms.put(mob.getUUID(), charm);
        direct(mob, enemy, charm, now);
        hearts(level, mob, 7);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        Charm charm = activeCharm(level, mob);
        if (charm == null) return;
        ServerPlayer caster = owner(level, charm);
        Mob enemy = caster == null ? null : currentEnemy(level, caster, mob, charm);
        if (enemy != null) {
            if (mob.getTarget() != enemy) resetFuse(mob);
            charm.target = enemy.getUUID();
        }
        // Setting the proposed target avoids recursively calling setTarget from its own event.
        event.setNewAboutToBeSetTarget(enemy);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity attacker = attackOwner(event.getSource().getEntity(), event.getSource().getDirectEntity());
        if (!(attacker instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        Charm charm = activeCharm(level, mob);
        if (charm == null) return;
        ServerPlayer caster = owner(level, charm);
        // Keep this check valid for a creeper that has marked itself dead before its blast lands.
        if (caster == null || !isEnemy(caster, mob, event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        WorldState state = WORLDS.get(level);
        if (state == null) return;
        long now = level.getGameTime();
        state.auras.entrySet().removeIf(entry -> entry.getValue() <= now || !present(level, entry.getKey()));
        for (var entry : new ArrayList<>(state.charms.entrySet())) {
            Charm charm = entry.getValue();
            Entity entity = level.getEntity(entry.getKey());
            ServerPlayer caster = owner(level, charm);
            if (!(entity instanceof Mob mob)) {
                state.charms.remove(entry.getKey());
                continue;
            }
            Mob enemy = caster == null || !mob.isAlive() || mob.isNoAi() || charm.until <= now
                    ? null : currentEnemy(level, caster, mob, charm);
            if (enemy == null) {
                state.charms.remove(entry.getKey());
                release(mob, charm);
                continue;
            }
            charm.target = enemy.getUUID();
            direct(mob, enemy, charm, now);
            if (now % 20 == 0) hearts(level, mob, 2);
            // Cubes already pursue arbitrary targets, but vanilla contact damage only handles players/golems.
            if (mob instanceof AbstractCubeMob cube && (cube instanceof MagmaCube || !cube.isTiny())
                    && now >= charm.nextContact && cube.getBoundingBox().inflate(0.1).intersects(enemy.getBoundingBox())
                    && cube.hasLineOfSight(enemy)) {
                charm.nextContact = now + 20;
                float damage = (float) cube.getAttributeValue(Attributes.ATTACK_DAMAGE) + (cube instanceof MagmaCube ? 2.0F : 0.0F);
                enemy.hurtServer(level, cube.damageSources().mobAttack(cube), damage);
            }
        }
        if (state.auras.isEmpty() && state.charms.isEmpty()) WORLDS.remove(level);
    }

    private static void direct(Mob mob, Mob enemy, Charm charm, long now) {
        if (mob.getTarget() != enemy) {
            resetFuse(mob);
            mob.setTarget(enemy);
        }
        var brain = mob.getBrain();
        if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)) {
            brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, enemy, Math.max(1, charm.until - now));
        }
        if (brain.checkMemory(MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED)) {
            brain.setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, enemy.getUUID(), Math.max(1, charm.until - now));
            // ANGRY_AT is saved. A reload must discard only the anger this temporary charm supplied.
            mob.getPersistentData().putString(FORCED_ANGER, enemy.getUUID().toString());
        }
    }

    private static void resetFuse(Mob mob) {
        if (mob instanceof Creeper creeper) {
            creeper.setSwellDir(-1);
            for (var goal : creeper.getGoalSelector().getAvailableGoals()) {
                if (goal.getGoal() instanceof SwellGoal) goal.stop();
            }
        }
    }

    private static void release(Mob mob, Charm charm) {
        String forced = mob.getPersistentData().getStringOr(FORCED_ANGER, "");
        if (mob.getTarget() != null && (mob.getTarget().getUUID().equals(charm.target)
                || mob.getTarget().getUUID().toString().equals(forced))) mob.setTarget(null);
        resetFuse(mob);
        var target = mob.getBrain().getMemoryInternal(MemoryModuleType.ATTACK_TARGET);
        if (target != null && target.filter(entity -> entity.getUUID().equals(charm.target) || entity.getUUID().toString().equals(forced)).isPresent()) {
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
        var angryAt = mob.getBrain().getMemoryInternal(MemoryModuleType.ANGRY_AT);
        if (angryAt != null && angryAt.filter(id -> id.equals(charm.target) || id.toString().equals(forced)).isPresent()) {
            mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        }
        mob.getPersistentData().remove(FORCED_ANGER);
        mob.getNavigation().stop();
    }

    private static Mob currentEnemy(ServerLevel level, ServerPlayer caster, Mob mob, Charm charm) {
        Entity current = level.getEntity(charm.target);
        if (current instanceof Mob enemy && canFight(caster, mob, enemy, charm.radius)) return enemy;
        return findEnemy(caster, mob, charm.radius);
    }

    private static Mob findEnemy(ServerPlayer caster, Mob mob, double radius) {
        return caster.level().getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(radius),
                        enemy -> canFight(caster, mob, enemy, radius) && mob.hasLineOfSight(enemy))
                .stream().min(Comparator.comparingDouble(mob::distanceToSqr)).orElse(null);
    }

    private static boolean canFight(ServerPlayer caster, Mob mob, Mob enemy, double radius) {
        return isEnemy(caster, mob, enemy) && mob.canAttack(enemy) && !mob.isAlliedTo(enemy)
                && mob.distanceToSqr(enemy) <= radius * radius;
    }

    private static boolean isEnemy(ServerPlayer caster, Mob controlled, LivingEntity target) {
        if (!(target instanceof Mob mob) || !(target instanceof Enemy) || target == controlled
                || !target.isAlive() || target.level() != caster.level() || target.isSpectator()
                || caster.isAlliedTo(target)
                || target instanceof OwnableEntity ownable && ownable.getRootOwner() instanceof Player) return false;
        // Charmed creatures are temporary allies, even though their scoreboard teams stay unchanged.
        return activeCharm(caster.level(), mob) == null;
    }

    private static Charm activeCharm(ServerLevel level, Mob mob) {
        WorldState state = WORLDS.get(level);
        Charm charm = state == null ? null : state.charms.get(mob.getUUID());
        return charm != null && charm.until > level.getGameTime() ? charm : null;
    }

    private static ServerPlayer owner(ServerLevel level, Charm charm) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(charm.owner);
        return player != null && player.isAlive() && !player.isSpectator() && player.level() == level ? player : null;
    }

    private static boolean present(ServerLevel level, UUID id) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
        return player != null && player.isAlive() && !player.isSpectator() && player.level() == level;
    }

    private static LivingEntity attackOwner(Entity owner, Entity direct) {
        if (owner instanceof LivingEntity living) return living;
        if (direct instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity living) return living;
        return direct instanceof LivingEntity living ? living : null;
    }

    private static void hearts(ServerLevel level, LivingEntity entity, int count) {
        level.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY() + entity.getBbHeight() + 0.15,
                entity.getZ(), count, 0.3, 0.2, 0.3, 0.0);
    }

    private static void clearOwner(ServerPlayer player) {
        for (var world : new ArrayList<>(WORLDS.entrySet())) {
            WorldState state = world.getValue();
            state.auras.remove(player.getUUID());
            for (var entry : new ArrayList<>(state.charms.entrySet())) {
                if (!entry.getValue().owner.equals(player.getUUID())) continue;
                state.charms.remove(entry.getKey());
                if (world.getKey().getEntity(entry.getKey()) instanceof Mob mob) release(mob, entry.getValue());
            }
            if (state.auras.isEmpty() && state.charms.isEmpty()) WORLDS.remove(world.getKey());
        }
    }

    @SubscribeEvent
    public static void onEntityLoad(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel) || !(event.getEntity() instanceof Mob mob)) return;
        String forced = mob.getPersistentData().getStringOr(FORCED_ANGER, "");
        if (forced.isEmpty()) return;
        var angryAt = mob.getBrain().getMemoryInternal(MemoryModuleType.ANGRY_AT);
        if (angryAt != null && angryAt.filter(id -> id.toString().equals(forced)).isPresent()) {
            mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        }
        mob.getPersistentData().remove(FORCED_ANGER);
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)) return;
        WorldState state = WORLDS.get(level);
        if (state == null) return;
        Charm charm = state.charms.remove(mob.getUUID());
        if (charm != null) release(mob, charm);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clearOwner(player);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clearOwner(player);
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clearOwner(player);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer player) clearOwner(player);
    }

    @SubscribeEvent
    public static void onStop(ServerStoppedEvent event) { WORLDS.clear(); }

    private static final class WorldState {
        final Map<UUID, Long> auras = new HashMap<>();
        final Map<UUID, Charm> charms = new HashMap<>();
    }

    private static final class Charm {
        final UUID owner;
        UUID target;
        final long until;
        final double radius;
        long nextContact;

        Charm(UUID owner, UUID target, long until, double radius) {
            this.owner = owner;
            this.target = target;
            this.until = until;
            this.radius = radius;
        }
    }
}
