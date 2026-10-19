package net.ok.project_cradle.server.progression;

import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vex;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.ProgressionRules;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;

/** Represents the clash of wills with one's own manifested spirit as a personal combat challenge. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class RemnantFusionService {
    public static final String OWN_REMNANT_TAG = "project_cradle.own_remnant";
    private static final String OWNER_KEY = "project_cradle.own_remnant_owner";
    private static final String DEADLINE_KEY = "project_cradle.own_remnant_deadline";
    private static final int CHALLENGE_LIFETIME_TICKS = 1_200;

    private RemnantFusionService() {
    }

    public static InteractionResult begin(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        if (!player.isAlive() || player.isSpectator() || core.hasHerald()
                || core.getAdvancement() != AdvancementLevel.ARCHLORD
                || player.getData(ModAttachments.MAXMADRA).getLevel() < ProgressionRules.PEAK_ARCHLORD_MADRA) {
            return fail(player, "herald_requires_peak_archlord");
        }
        if (core.getWillpower() < ProgressionRules.HERALD_WILLPOWER) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.herald_requires_willpower",
                    ProgressionRules.HERALD_WILLPOWER));
            return InteractionResult.FAIL;
        }

        ServerLevel level = player.level();
        long now = challengeTime(level);
        Entity existing = findActiveRemnant(player, core.getOwnRemnantId());
        long previousDeadline = player.getPersistentData().getLongOr(DEADLINE_KEY, 0L);
        if (existing != null) {
            long entityDeadline = existing.getPersistentData().getLongOr(DEADLINE_KEY, 0L);
            // Challenges created before deadline tracking still receive a bounded lifetime.
            if (entityDeadline == 0L) {
                entityDeadline = now + CHALLENGE_LIFETIME_TICKS;
                existing.getPersistentData().putLong(DEADLINE_KEY, entityDeadline);
            }
            if (entityDeadline > now) {
                player.getPersistentData().putLong(DEADLINE_KEY, entityDeadline);
                return fail(player, "remnant_challenge_active");
            }
            existing.discard();
        } else if (!core.getOwnRemnantId().isBlank() && previousDeadline > now) {
            // An unloaded spirit is still the active challenge; leaving its chunk cannot duplicate it.
            return fail(player, "remnant_challenge_active");
        }
        clearChallenge(player);

        Vex remnant = EntityTypes.VEX.create(level, EntitySpawnReason.EVENT);
        if (remnant == null) {
            return fail(player, "remnant_manifest_failed");
        }
        long deadline = now + CHALLENGE_LIFETIME_TICKS;
        var position = player.position().add(player.getLookAngle().scale(3.0)).add(0.0, 0.5, 0.0);
        remnant.snapTo(position.x, position.y, position.z, player.getYRot() + 180.0F, 0.0F);
        remnant.addTag(OWN_REMNANT_TAG);
        remnant.getPersistentData().putString(OWNER_KEY, player.getUUID().toString());
        remnant.getPersistentData().putLong(DEADLINE_KEY, deadline);
        remnant.setCustomName(Component.translatable("entity.project_cradle.own_remnant", player.getName()));
        remnant.setCustomNameVisible(true);
        remnant.setPersistenceRequired();
        remnant.setLimitedLife(CHALLENGE_LIFETIME_TICKS);
        remnant.setTarget(player);
        remnant.getData(ModAttachments.CORE).setAdvancement(core.getAdvancement());
        remnant.getData(ModAttachments.MADRA_TYPE).copyFrom(player.getData(ModAttachments.MADRA_TYPE));

        double health = Math.max(40.0, player.getMaxHealth() * 1.5 + (core.hasSage() ? 20.0 : 0.0));
        AttributeInstance maxHealth = remnant.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(health);
            remnant.setHealth((float) health);
        }
        AttributeInstance damage = remnant.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(6.0 + core.getAdvancement().order() * 0.4 + (core.hasSage() ? 3.0 : 0.0));
        }
        if (!level.addFreshEntity(remnant)) {
            return fail(player, "remnant_manifest_failed");
        }
        core.setOwnRemnantId(remnant.getUUID().toString());
        player.getPersistentData().putLong(DEADLINE_KEY, deadline);
        player.syncData(ModAttachments.CORE);
        level.sendParticles(ParticleTypes.SOUL, position.x, position.y, position.z,
                12, 0.35, 0.6, 0.35, 0.03);
        player.sendSystemMessage(Component.translatable("message.project_cradle.remnant_challenge_started"));
        return InteractionResult.SUCCESS;
    }

    public static boolean isOwnRemnant(Entity entity) {
        return entity.entityTags().contains(OWN_REMNANT_TAG);
    }

    /** Returns true for personal spirits, including stale or stolen kills, to suppress ordinary Remnant rewards. */
    public static boolean onDefeated(LivingEntity victim, ServerPlayer killer) {
        if (!isOwnRemnant(victim)) {
            return false;
        }
        ServerPlayer owner = findOwner(victim);
        if (owner == null || !isActiveChallenge(victim, owner)) {
            return true;
        }
        long deadline = victim.getPersistentData().getLongOr(DEADLINE_KEY, 0L);
        boolean validDefeat = killer == owner && owner.isAlive() && !owner.isSpectator()
                && owner.level() == victim.level()
                && (deadline == 0L || challengeTime(owner.level()) < deadline);
        clearChallenge(owner);
        if (!validDefeat) {
            return true;
        }

        if (!ProgressionService.completeHeraldFusion(owner)) {
            owner.sendSystemMessage(Component.translatable("message.project_cradle.remnant_fusion_failed"));
        }
        return true;
    }

    /** Remove expired or abandoned spirits when their chunks load, without loading chunks to find them. */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity remnant = event.getEntity();
        if (!(remnant.level() instanceof ServerLevel level) || !isOwnRemnant(remnant)) {
            return;
        }
        ServerPlayer owner = findOwner(remnant);
        long deadline = remnant.getPersistentData().getLongOr(DEADLINE_KEY, 0L);
        Core core = owner == null ? null : owner.getData(ModAttachments.CORE);
        boolean active = owner != null && isActiveChallenge(remnant, owner);
        if (!active || !owner.isAlive() || owner.isSpectator() || owner.level() != level
                || core.getAdvancement() != AdvancementLevel.ARCHLORD || core.hasHerald()
                || (deadline != 0L && challengeTime(level) >= deadline)) {
            if (active) {
                clearChallenge(owner);
            }
            remnant.discard();
            return;
        }
        if (remnant instanceof Mob mob && mob.getTarget() != owner) {
            mob.setTarget(owner);
        }
    }

    /** Death ends the clash rather than carrying a live challenge into the replacement body. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getEntity() instanceof ServerPlayer player) {
            clearChallenge(player);
        }
    }

    private static boolean isActiveChallenge(Entity remnant, ServerPlayer owner) {
        return remnant.getUUID().toString().equals(owner.getData(ModAttachments.CORE).getOwnRemnantId());
    }

    private static ServerPlayer findOwner(Entity remnant) {
        if (!(remnant.level() instanceof ServerLevel level)) {
            return null;
        }
        String ownerId = remnant.getPersistentData().getString(OWNER_KEY).orElse("");
        try {
            return level.getServer().getPlayerList().getPlayer(UUID.fromString(ownerId));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void clearChallenge(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        if (!core.getOwnRemnantId().isBlank()) {
            core.setOwnRemnantId("");
            player.syncData(ModAttachments.CORE);
        }
        player.getPersistentData().remove(DEADLINE_KEY);
    }

    private static long challengeTime(ServerLevel level) {
        return level.getServer().overworld().getGameTime();
    }

    private static Entity findActiveRemnant(ServerPlayer player, String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            return null;
        }
        for (ServerLevel level : player.level().getServer().getAllLevels()) {
            Entity existing = level.getEntity(uuid);
            if (existing != null && existing.isAlive() && isOwnRemnant(existing)) {
                return existing;
            }
        }
        return null;
    }

    private static InteractionResult fail(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable("message.project_cradle." + key));
        return InteractionResult.FAIL;
    }
}
