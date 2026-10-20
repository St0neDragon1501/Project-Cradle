package net.ok.project_cradle.server.event;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.ok.project_cradle.Config;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.server.spells.MobTechniqueService;
import net.ok.project_cradle.server.spells.TechniqueVisualService;
import net.ok.project_cradle.server.world.SacredBeastService;

/** Server-only rare spawning, migration and defensive technique use for vanilla bodies. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class SacredBeastEvents {
    private static final Set<Mob> NEW_SPAWNS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<Mob> RESTORE = Collections.newSetFromMap(new WeakHashMap<>());

    private SacredBeastEvents() { }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel) || !(event.getEntity() instanceof Mob mob)
                || !SacredBeastService.canBecomeSacred(mob)) return;
        var state = mob.getData(ModAttachments.SACRED_BEAST_AGE);
        if (event.loadedFromDisk()) state.markSpawnChecked();
        else if (!state.wasSpawnChecked()) NEW_SPAWNS.add(mob);
        if (state.isSacred()) RESTORE.add(mob);
        // Actual initialization waits for ticking: joins can occur before a chunk is fully loaded.
    }

    @SubscribeEvent
    public static void onConversion(LivingConversionEvent.Post event) {
        if (event.getEntity() instanceof Mob source && event.getOutcome() instanceof Mob target
                && source.level() instanceof ServerLevel && SacredBeastService.canBecomeSacred(source)
                && SacredBeastService.canBecomeSacred(target)) {
            SacredBeastService.copySpirit(source, target);
            if (target.getData(ModAttachments.SACRED_BEAST_AGE).isSacred()) RESTORE.add(target);
        }
    }

    @SubscribeEvent
    public static void onSplit(MobSplitEvent event) {
        if (!(event.getParent().level() instanceof ServerLevel)) return;
        for (Mob child : event.getChildren()) SacredBeastService.ordinarySplitChild(event.getParent(), child);
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        var state = mob.getData(ModAttachments.SACRED_BEAST_AGE);
        if (NEW_SPAWNS.remove(mob) && !state.wasSpawnChecked()) {
            state.markSpawnChecked();
            if (mob.isAlive() && SacredBeastService.canBecomeSacred(mob)
                    && mob.getRandom().nextDouble() < Config.sacredBeastSpawnChance()) SacredBeastService.awaken(mob);
        }
        if (RESTORE.remove(mob)) SacredBeastService.restore(mob);
        if (!state.isSacred() || !mob.isAlive() || !SacredBeastService.canBecomeSacred(mob)) return;
        if (mob.getTarget() != null && !MobTechniqueService.canHarm(mob, mob.getTarget())) mob.setTarget(null);
        // Dragon phase AI does not populate Mob's target; use the players it already fights.
        if (mob instanceof EnderDragon && mob.tickCount % 20 == 0) {
            Player target = level.players().stream().filter(player -> player.isAlive() && !player.isCreative()
                            && !player.isSpectator() && mob.distanceToSqr(player) <= 24.0 * 24.0
                            && !MobTechniqueService.isFriendly(mob, player))
                    .min(java.util.Comparator.comparingDouble(mob::distanceToSqr)).orElse(null);
            mob.setTarget(target);
        }
        MobTechniqueService.tick(mob);
        if (!mob.isInvisible() && mob.tickCount % 40 == 0) {
            var aspects = mob.getData(ModAttachments.MADRA_TYPE).getTypes();
            var aspect = aspects.get(Math.floorMod(mob.tickCount / 40, aspects.size()));
            level.sendParticles(new DustParticleOptions(TechniqueVisualService.color(aspect), 0.7F),
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 2,
                    mob.getBbWidth() * 0.3, mob.getBbHeight() * 0.2, mob.getBbWidth() * 0.3, 0.01);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel)
                || !mob.getData(ModAttachments.SACRED_BEAST_AGE).isSacred() || !mob.isAlive()
                || !event.shouldCauseSideEffects() || event.getInflictedDamage() <= 0.0F) return;
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != mob
                && attacker.isAlive() && !attacker.isSpectator() && !mob.isAlliedTo(attacker)) {
            // Setting the target first allows the common combat policy to validate retaliation.
            LivingEntity previous = mob.getTarget();
            mob.setTarget(attacker);
            if (!MobTechniqueService.canHarm(mob, attacker)) mob.setTarget(previous);
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        NEW_SPAWNS.remove(mob);
        RESTORE.remove(mob);
        if (mob.getData(ModAttachments.SACRED_BEAST_AGE).isSacred() && mob.level() instanceof ServerLevel
                && (mob.isDeadOrDying() || mob.getRemovalReason() == Entity.RemovalReason.KILLED)) {
            // Creeper explosions and /kill on a dragon bypass the ordinary LivingDeathEvent.
            SacredWorldEvents.trySpawnRemnant(mob, mob.getKillCredit());
        }
    }

    @SubscribeEvent
    public static void onStop(ServerStoppedEvent event) {
        NEW_SPAWNS.clear();
        RESTORE.clear();
    }
}
