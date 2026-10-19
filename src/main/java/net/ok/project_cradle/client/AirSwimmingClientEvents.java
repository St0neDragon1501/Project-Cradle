package net.ok.project_cradle.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.server.progression.AirSwimmingService;

/** Level flight, separate altitude controls, and a firm brake when controls are released. */
@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public final class AirSwimmingClientEvents {
    private static final double CRUISE_SPEED = 0.24;
    private static final double BOOST_SPEED = 0.42;
    private static final double VERTICAL_SPEED = 0.18;
    private static final double BOOST_VERTICAL_SPEED = 0.26;

    private AirSwimmingClientEvents() { }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        AirSwimmingService.updateSwimmingPose(player);
        if (!AirSwimmingService.isAirSwimming(player)) return;

        Input keys = event.getInput().keyPresses;
        Vec2 input = event.getInput().getMoveVector();
        int verticalInput = (keys.jump() ? 1 : 0) - (keys.shift() ? 1 : 0);
        boolean boost = keys.sprint();
        double speed = boost ? BOOST_SPEED : CRUISE_SPEED;
        double yaw = Math.toRadians(player.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        Vec3 right = new Vec3(Math.cos(yaw), 0.0, Math.sin(yaw));
        Vec3 horizontal = forward.scale(input.y).add(right.scale(input.x));
        if (horizontal.lengthSqr() > 1.0) horizontal = horizontal.normalize();
        Vec3 desired = horizontal.scale(speed).add(0.0,
                verticalInput * (boost ? BOOST_VERTICAL_SPEED : VERTICAL_SPEED), 0.0);
        Vec3 movement = player.getDeltaMovement();
        boolean braking = input.lengthSquared() == 0.0F && verticalInput == 0;
        Vec3 controlled = movement.lerp(desired, braking ? 0.7 : 0.45);

        // Retain the server's one-shot takeoff lift; don't relaunch on later landings.
        if (player.onGround() && movement.y > 0.08) {
            controlled = new Vec3(controlled.x, Math.max(0.28, movement.y), controlled.z);
        }
        controlled = new Vec3(snap(controlled.x), snap(controlled.y), snap(controlled.z));

        // Space is an altitude control during Air flight, so repeated presses cannot
        // accidentally trigger vanilla's double-tap flight-off action.
        event.getInput().keyPresses = new Input(keys.forward(), keys.backward(), keys.left(), keys.right(),
                false, keys.shift(), keys.sprint());
        // Vanilla still adds crouch descent later this tick. Cancel only that contribution.
        double vanillaVertical = keys.shift() ? -player.getAbilities().getFlyingSpeed() * 3.0 : 0.0;
        player.setDeltaMovement(controlled.add(0.0, -vanillaVertical, 0.0));
        cancelVanillaAcceleration(player, input);
    }

    private static double snap(double value) {
        return Math.abs(value) < 0.003 ? 0.0 : value;
    }

    private static void cancelVanillaAcceleration(LocalPlayer player, Vec2 input) {
        // Counter only vanilla's impending input acceleration; keep its collision and
        // travel handling. This also avoids the dry swimming pose's crawl-speed penalty.
        Vec2 vanilla = input.scale(0.98F);
        if (player.isUsingItem() && !player.isPassenger()) {
            vanilla = vanilla.scale(player.getUseItem().getOrDefault(DataComponents.USE_EFFECTS,
                    UseEffects.DEFAULT).speedMultiplier());
        }
        if (player.isMovingSlowly()) vanilla = vanilla.scale((float) player.getAttributeValue(Attributes.SNEAKING_SPEED));
        float length = vanilla.length();
        if (length > 0.0F) {
            Vec2 direction = vanilla.scale(1.0F / length);
            float min = Math.min(Math.abs(direction.x), Math.abs(direction.y));
            float max = Math.max(Math.abs(direction.x), Math.abs(direction.y));
            float squareDistance = (float) Math.sqrt(1.0F + (min / max) * (min / max));
            vanilla = direction.scale(Math.min(length * squareDistance, 1.0F));
        }
        float acceleration = player.getAbilities().getFlyingSpeed() * (player.isSprinting() ? 2.0F : 1.0F);
        player.moveRelative(-acceleration, new Vec3(vanilla.x, 0.0, vanilla.y));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof LocalPlayer player) AirSwimmingService.updateSwimmingPose(player);
    }
}
