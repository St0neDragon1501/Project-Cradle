package net.ok.project_cradle.server.items;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.server.progression.ProgressionService;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MaxMadra;
import net.ok.project_cradle.server.world.AuraService;

import net.minecraft.server.level.ServerPlayer;

/** Samples aura, restores Madra, and evaluates advancement on the server. */
public final class MadraDetectionService {
    private MadraDetectionService() {
    }

    public static InteractionResult absorbAura(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement().order() < AdvancementLevel.COPPER.order()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.need_copper_sight"));
            return InteractionResult.FAIL;
        }
        AuraService.Reading reading = AuraService.detect(player);
        if (reading.hasAura()) player.sendSystemMessage(Component.translatable("message.project_cradle.aura_detected", Component.translatable(reading.aspect().translationKey())));
        MadraType type = player.getData(ModAttachments.MADRA_TYPE);
        double amount = reading.compatibleYield(type);
        if (amount > 0.0) {
            MaxMadra maximum = player.getData(ModAttachments.MAXMADRA);
            maximum.addMaxMadra(amount);
            player.getData(ModAttachments.MADRA).addMadra(amount, maximum.getLevel());
            player.sendSystemMessage(Component.translatable("message.project_cradle.compatible_aura_absorbed", amount));
            ProgressionService.evaluateCapacity(player);
            player.syncData(ModAttachments.MADRA);
            player.syncData(ModAttachments.MAXMADRA);
        } else {
            player.sendSystemMessage(Component.translatable("message.project_cradle.no_compatible_aura"));
        }
        if (core.getAdvancement().order() >= AdvancementLevel.JADE.order()) senseNearbyArtists(player);
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
