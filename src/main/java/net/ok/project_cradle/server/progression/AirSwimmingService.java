package net.ok.project_cradle.server.progression;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.madra.MadraPassives;
import net.ok.project_cradle.capability.madra.MadraType;

/** Authorizes Air movement without changing game-mode abilities or other flight grants. */
public final class AirSwimmingService {
    private static final String GENTLE_LANDING = "project_cradle.air_gentle_landing";
    private static final Identifier AIR_FLIGHT = Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "air_swimming");
    private static final AttributeModifier FLIGHT_PERMISSION = new AttributeModifier(
            AIR_FLIGHT, 1.0, AttributeModifier.Operation.ADD_VALUE);
    private static final Set<Player> OWNED_SWIMMING_POSES = Collections.newSetFromMap(new WeakHashMap<>());

    private AirSwimmingService() {
    }

    private static boolean canUseAir(Player player) {
        return MadraPassives.has(player, MadraType.Element.AIR)
                && !player.isCreative() && !player.isSpectator()
                && !player.isPassenger() && !player.isFallFlying()
                && !player.isInWater() && !player.isInLava() && !player.isSleeping();
    }

    public static void refresh(ServerPlayer player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        boolean wasAirborne = flight != null && flight.hasModifier(AIR_FLIGHT) && player.getAbilities().flying;
        if (flight != null) {
            if (canUseAir(player)) {
                if (!flight.hasModifier(AIR_FLIGHT)) flight.addTransientModifier(FLIGHT_PERMISSION);
            } else {
                flight.removeModifier(AIR_FLIGHT);
            }
        }
        stopUnsupportedFlight(player);
        if (wasAirborne && !player.getAbilities().flying) beginLanding(player);
        updateSwimmingPose(player);
        if (isAirSwimming(player)) player.resetFallDistance();
        updateLanding(player);
    }

    public static void toggle(ServerPlayer player) {
        refresh(player);
        if (!canUseAir(player)) {
            player.sendOverlayMessage(Component.translatable("message.project_cradle.flight_requires_air"));
            return;
        }
        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            releaseSwimmingPose(player);
            beginLanding(player);
            player.sendOverlayMessage(Component.translatable("message.project_cradle.air_flight_landing"));
            return;
        }
        if (!player.mayFly()) return;
        player.getPersistentData().remove(GENTLE_LANDING);
        // Send the lift immediately: vanilla otherwise switches grounded flight off again.
        if (player.onGround()) {
            player.setDeltaMovement(player.getDeltaMovement().x, 0.35, player.getDeltaMovement().z);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.resetFallDistance();
        updateSwimmingPose(player);
        player.sendOverlayMessage(Component.translatable("message.project_cradle.air_flight_started"));
    }

    private static void beginLanding(ServerPlayer player) {
        if (!player.isAlive() || player.isCreative() || player.isSpectator() || player.onGround()
                || player.isInWater() || player.isInLava() || player.isPassenger()) return;
        player.getPersistentData().putBoolean(GENTLE_LANDING, true);
        updateLanding(player);
    }

    private static void updateLanding(ServerPlayer player) {
        if (!player.getPersistentData().getBooleanOr(GENTLE_LANDING, false)) return;
        if (!player.isAlive() || player.isCreative() || player.isSpectator() || player.onGround()
                || player.isInWater() || player.isInLava() || player.isPassenger()
                || player.onClimbable() || player.isFallFlying() || player.getAbilities().flying) {
            player.getPersistentData().remove(GENTLE_LANDING);
            return;
        }
        var effect = player.getEffect(MobEffects.SLOW_FALLING);
        if (effect == null || (!effect.isInfiniteDuration() && effect.getDuration() < 40)) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, true, false, false));
        }
        player.resetFallDistance();
    }

    public static void clear(ServerPlayer player) {
        if (isAirSwimming(player) && !player.onGround()) {
            player.getPersistentData().putBoolean(GENTLE_LANDING, true);
        }
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight != null) flight.removeModifier(AIR_FLIGHT);
        stopUnsupportedFlight(player);
        releaseSwimmingPose(player);
    }

    private static void stopUnsupportedFlight(ServerPlayer player) {
        if (player.getAbilities().flying && !player.mayFly()) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    public static boolean isAirSwimming(Player player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        return canUseAir(player) && player.getAbilities().flying && player.mayFly()
                && flight != null && flight.hasModifier(AIR_FLIGHT);
    }

    /** Forced pose is separate from the water-only swimming flag in vanilla Player. */
    public static synchronized void updateSwimmingPose(Player player) {
        if (!isAirSwimming(player)) {
            releaseSwimmingPose(player);
            return;
        }
        if (player.getForcedPose() == null) {
            player.setForcedPose(Pose.SWIMMING);
            OWNED_SWIMMING_POSES.add(player);
        } else if (player.getForcedPose() != Pose.SWIMMING) {
            OWNED_SWIMMING_POSES.remove(player);
        }
    }

    private static synchronized void releaseSwimmingPose(Player player) {
        if (OWNED_SWIMMING_POSES.remove(player) && player.getForcedPose() == Pose.SWIMMING) {
            player.setForcedPose(null);
        }
    }
}
