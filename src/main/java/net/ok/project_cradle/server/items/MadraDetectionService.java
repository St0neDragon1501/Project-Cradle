package net.ok.project_cradle.server.items;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.server.world.AuraService;
import net.ok.project_cradle.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import net.minecraft.server.level.ServerPlayer;

/** Gathers compatible aura into the existing core and reports object recovery times. */
public final class MadraDetectionService {
    private MadraDetectionService() {
    }

    public static InteractionResult absorbAura(ServerPlayer player) {
        if (!canGather(player)) return InteractionResult.FAIL;
        if (player.isShiftKeyDown() && player.getData(ModAttachments.CORE).getAdvancement().order() >= AdvancementLevel.JADE.order()) {
            senseNearbyArtists(player);
            return InteractionResult.SUCCESS;
        }
        return report(player, AuraService.harvestBest(player, AuraService.detect(player)));
    }

    public static InteractionResult absorbBlock(ServerPlayer player, BlockPos position) {
        if (!canGather(player)) return InteractionResult.FAIL;
        return report(player, AuraService.harvestBlock(player, position));
    }

    public static InteractionResult absorbEntity(ServerPlayer player, LivingEntity target) {
        if (!canGather(player)) return InteractionResult.FAIL;
        return report(player, AuraService.harvestEntity(player, target));
    }

    private static boolean canGather(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) return false;
        ItemStack detector = new ItemStack(ModItems.MADRA_DETETION.get());
        if (player.getCooldowns().isOnCooldown(detector)) return false;
        player.getCooldowns().addCooldown(detector, 10);
        if (player.getData(ModAttachments.CORE).getAdvancement().order() < AdvancementLevel.COPPER.order()) {
            player.sendOverlayMessage(Component.translatable("message.project_cradle.need_copper_sight"));
            return false;
        }
        return true;
    }

    private static InteractionResult report(ServerPlayer player, AuraService.Harvest harvest) {
        switch (harvest.status()) {
            case GATHERED -> player.sendOverlayMessage(Component.translatable("message.project_cradle.aura_gathered",
                    String.format(java.util.Locale.ROOT, "%.1f", harvest.amount())));
            case FULL -> player.sendOverlayMessage(Component.translatable("message.project_cradle.madra_full"));
            case COOLING -> player.sendOverlayMessage(Component.translatable("message.project_cradle.aura_source_cooldown",
                    Math.max(1L, (harvest.remainingTicks() + 19L) / 20L)));
            case INCOMPATIBLE, NO_SOURCE -> player.sendOverlayMessage(Component.translatable("message.project_cradle.no_compatible_aura"));
        }
        return InteractionResult.SUCCESS;
    }

    private static void senseNearbyArtists(ServerPlayer player) {
        List<LivingEntity> artists = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(16.0),
                entity -> entity != player && (entity instanceof Player || entity instanceof Villager)
        );
        if (artists.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.no_artists_sensed"));
            return;
        }

        for (LivingEntity artist : artists.stream().limit(5).toList()) {
            String aspects = artist.getData(ModAttachments.MADRA_TYPE).getTypes().stream()
                    .map(element -> Component.translatable(element.translationKey()).getString())
                    .collect(Collectors.joining(", "));
            Component rank = Component.translatable("advancement.project_cradle." + artist.getData(ModAttachments.CORE).getAdvancement().id());
            player.sendSystemMessage(Component.translatable("message.project_cradle.sensed_artist",
                    artist.getName(), rank, aspects));
        }
    }
}
