package net.ok.project_cradle.server.event;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.madra.MadraPassives;
import net.ok.project_cradle.capability.madra.MadraType.Element;
import net.ok.project_cradle.server.progression.DreamPacificationService;
import net.ok.project_cradle.server.spells.TechniqueCombatService;
import net.ok.project_cradle.server.spells.TechniqueCombatService.TechniqueDamageSource;

/** Gold attack passives execute once in the server damage pipeline, after ownership is known. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class GoldCombatEvents {
    // Weak source keys also release snapshots when an absorbed/cancelled hit never reaches Post.
    private static final Map<DamageSource, Map<UUID, ArrayDeque<Float>>> HEALTH_BEFORE = new WeakHashMap<>();

    private GoldCombatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel)) return;
        DamageSource source = event.getSource();
        LivingEntity attacker = attackOwner(source);
        if (!isSecondaryDamage(source) && attacker != null && DreamPacificationService.isBlocked(attacker, victim)) {
            event.setCanceled(true);
            return;
        }
        ServerPlayer caster = ownedHit(source, victim);
        if (source instanceof TechniqueDamageSource && caster != null && event.getAmount() > 0.0F
                && MadraPassives.has(caster, Element.DEATH)) {
            // Modify this sequence, rather than causing a second damage event.
            event.setAmount(event.getAmount() + 1.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity().level() instanceof ServerLevel)) return;
        ServerPlayer caster = ownedHit(event.getSource(), event.getEntity());
        if (caster == null || !MadraPassives.has(caster, Element.HUNGER) || event.getNewDamage() <= 0.0F) return;
        synchronized (HEALTH_BEFORE) {
            HEALTH_BEFORE.computeIfAbsent(event.getSource(), ignored -> new HashMap<>())
                    .computeIfAbsent(event.getEntity().getUUID(), ignored -> new ArrayDeque<>())
                    .push(Math.max(0.0F, event.getEntity().getHealth()));
        }
    }

    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel)) return;
        Float previousHealth = takePreviousHealth(event.getSource(), victim.getUUID());
        ServerPlayer caster = ownedHit(event.getSource(), victim);
        if (caster == null) return;
        float damage = event.getHealthDamage();
        if (previousHealth != null) damage = Math.min(damage, previousHealth);
        boolean connected = event.shouldCauseSideEffects() && event.getInflictedDamage() > 0.0F;
        if (connected && event.getSource() instanceof TechniqueDamageSource) {
            TechniqueCombatService.onEffectHit(caster, victim);
        }
        if (connected && MadraPassives.has(caster, Element.FORCE) && victim.isAlive()) {
            victim.knockback(0.45, caster.getX() - victim.getX(), caster.getZ() - victim.getZ(),
                    event.getSource(), damage, true);
            victim.hurtMarked = true;
        }
        if (Float.isFinite(damage) && damage > 0.0F && MadraPassives.has(caster, Element.HUNGER)) {
            // Count health actually available, not overkill, shield damage or absorption hearts.
            caster.heal(Math.min(2.0F, damage * 0.2F));
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (event.getEntity().level() instanceof ServerLevel && target != null
                && DreamPacificationService.isBlocked(event.getEntity(), target)) {
            // Cancellation preserves the previous target; an explicit null drops this one instead.
            event.setNewAboutToBeSetTarget(null);
        }
    }

    private static Float takePreviousHealth(DamageSource source, UUID victim) {
        synchronized (HEALTH_BEFORE) {
            Map<UUID, ArrayDeque<Float>> victims = HEALTH_BEFORE.get(source);
            if (victims == null) return null;
            ArrayDeque<Float> values = victims.get(victim);
            if (values == null || values.isEmpty()) return null;
            Float health = values.pop();
            if (values.isEmpty()) victims.remove(victim);
            if (victims.isEmpty()) HEALTH_BEFORE.remove(source);
            return health;
        }
    }

    private static ServerPlayer ownedHit(DamageSource source, LivingEntity victim) {
        if (!(source.getEntity() instanceof ServerPlayer caster) || caster == victim
                || !caster.isAlive() || caster.isSpectator() || caster.level() != victim.level()
                || (victim instanceof Player player && (player.isSpectator() || !caster.canHarmPlayer(player)))
                || isSecondaryDamage(source)) {
            return null;
        }
        return caster;
    }

    private static LivingEntity attackOwner(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity living) return living;
        if (source.getDirectEntity() instanceof Projectile projectile
                && projectile.getOwner() instanceof LivingEntity owner) return owner;
        return source.getDirectEntity() instanceof LivingEntity living ? living : null;
    }

    private static boolean isSecondaryDamage(DamageSource source) {
        return source.is(DamageTypes.THORNS) || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.WITHER)
                || (source.is(DamageTypes.MAGIC) && !(source instanceof TechniqueDamageSource));
    }
}
