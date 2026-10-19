package net.ok.project_cradle.server.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.server.spells.TechniqueCastingService;

@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class TechniqueCastingEvents {
    private TechniqueCastingEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) TechniqueCastingService.tick(player);
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getHealthDamage() >= 4.0F
                && event.getSource().getEntity() != null && event.getSource().getEntity() != player) {
            TechniqueCastingService.cancel(player);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) TechniqueCastingService.cancel(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) TechniqueCastingService.cancel(player);
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) TechniqueCastingService.cancel(player);
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) TechniqueCastingService.cancel(player);
    }

    @SubscribeEvent
    public static void onStop(ServerStoppedEvent event) { TechniqueCastingService.clear(); }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer player) TechniqueCastingService.cancel(player);
    }
}
