package net.ok.project_cradle.server.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.madra.MadraPassives;
import net.ok.project_cradle.capability.madra.MadraType.Element;
import net.ok.project_cradle.server.progression.GoldPassiveService;

/** Passive maintenance and breathing belong to the logical server, including singleplayer. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class GoldPassiveEvents {
    private GoldPassiveEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 20 == 0) {
            GoldPassiveService.refresh(player);
        }
        GoldPassiveService.refreshShadow(player);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GoldPassiveService.refresh(player);
            GoldPassiveService.refreshShadow(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) GoldPassiveService.refresh(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) GoldPassiveService.refresh(player);
    }

    @SubscribeEvent
    public static void onLivingBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !MadraPassives.has(player, Element.WATER) || !player.isUnderWater()) {
            return;
        }
        event.setCanBreathe(true);
        event.setConsumeAirAmount(0);
        event.setRefillAirAmount(Math.max(event.getRefillAirAmount(), player.getMaxAirSupply()));
    }
}
