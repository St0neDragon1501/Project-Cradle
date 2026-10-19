package net.ok.project_cradle.server.spells;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.spells.TechniqueScaling;

/** Extra Enforcer potency is continuous, temporary, and never stacks through repeated casting. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class EnforcerPowerService {
    private static final Identifier BONUS = Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "enforcer_refinement");
    private static final Map<UUID, Empowerment> ACTIVE = new HashMap<>();

    private EnforcerPowerService() { }

    public static void empower(ServerPlayer player, int baseDuration, MadraType.Element aspect) {
        long expires = player.level().getGameTime() + TechniqueScaling.duration(player, baseDuration);
        double power = TechniqueScaling.power(player);
        Empowerment old = ACTIVE.get(player.getUUID());
        if (old != null && old.owner() == player && old.expires() > player.level().getGameTime()) {
            expires = Math.max(expires, old.expires());
            power = Math.max(power, old.power());
        }
        ACTIVE.put(player.getUUID(), new Empowerment(player, expires, power, aspect));
        apply(player, power);
    }

    private static void apply(ServerPlayer player, double power) {
        modifier(player, Attributes.ATTACK_DAMAGE, (power - 1.0) * 3.0, AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.MOVEMENT_SPEED, (power - 1.0) * 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(player, Attributes.ARMOR, (power - 1.0) * 2.0, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void modifier(ServerPlayer player, Holder<Attribute> type, double value, AttributeModifier.Operation operation) {
        var attribute = player.getAttribute(type);
        if (attribute == null) return;
        attribute.removeModifier(BONUS);
        if (value > 0.0) attribute.addTransientModifier(new AttributeModifier(BONUS, value, operation));
    }

    private static void clear(ServerPlayer player) {
        Empowerment active = ACTIVE.get(player.getUUID());
        if (active != null && active.owner() == player) ACTIVE.remove(player.getUUID());
        for (var type : java.util.List.of(Attributes.ATTACK_DAMAGE, Attributes.MOVEMENT_SPEED, Attributes.ARMOR)) {
            var attribute = player.getAttribute(type);
            if (attribute != null) attribute.removeModifier(BONUS);
        }
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Empowerment active = ACTIVE.get(player.getUUID());
        if (active == null) return;
        if (active.owner() != player || !player.isAlive() || player.isSpectator()
                || player.level().getGameTime() >= active.expires()) {
            clear(player);
            return;
        }
        if (player.tickCount % 10 == 0) TechniqueVisualService.enforcer(player, active.aspect());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer player) clear(player);
    }

    @SubscribeEvent
    public static void onStop(ServerStoppedEvent event) { ACTIVE.clear(); }

    private record Empowerment(ServerPlayer owner, long expires, double power, MadraType.Element aspect) { }
}
