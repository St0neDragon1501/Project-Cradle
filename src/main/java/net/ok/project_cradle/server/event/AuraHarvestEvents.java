package net.ok.project_cradle.server.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.items.MadraDetectionItem;
import net.ok.project_cradle.server.items.MadraDetectionService;

/** The detector gathers before a chest opens, a villager trades, or an Allay takes the item. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class AuraHarvestEvents {
    private AuraHarvestEvents() { }

    @SubscribeEvent
    public static void onBlockUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getItemStack().getItem() instanceof MadraDetectionItem)) return;
        event.setCanceled(true);
        event.setCancellationResult(event.getEntity() instanceof ServerPlayer player
                ? MadraDetectionService.absorbBlock(player, event.getPos()) : InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onEntityUse(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getItemStack().getItem() instanceof MadraDetectionItem)
                || !(event.getTarget() instanceof LivingEntity target)) return;
        event.setCanceled(true);
        event.setCancellationResult(event.getEntity() instanceof ServerPlayer player
                ? MadraDetectionService.absorbEntity(player, target) : InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onConversion(LivingConversionEvent.Post event) {
        if (!(event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel)) return;
        long ready = event.getEntity().getPersistentData().getLongOr("project_cradle.aura_ready_at", 0L);
        if (ready > 0L) event.getOutcome().getPersistentData().putLong("project_cradle.aura_ready_at", ready);
    }
}
