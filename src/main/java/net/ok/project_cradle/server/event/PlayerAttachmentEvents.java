package net.ok.project_cradle.server.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.QuickDropInventory;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.server.progression.ProgressionService;
import net.ok.project_cradle.server.progression.GoldsignAppearanceService;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MaxMadra;

@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class PlayerAttachmentEvents {
    private PlayerAttachmentEvents() {
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getOriginal() instanceof ServerPlayer original)
                || !(event.getEntity() instanceof ServerPlayer replacement)) {
            return;
        }

        replacement.getData(ModAttachments.SOULSPACE_INVENTORY).copyFrom(original.getData(ModAttachments.SOULSPACE_INVENTORY));
        Core core = replacement.getData(ModAttachments.CORE);
        core.copyFromCore(original.getData(ModAttachments.CORE));
        replacement.syncData(ModAttachments.CORE);
        ProgressionService.applyPermanentBenefits(replacement, core);

        QuickDropInventory quickDrop = replacement.getData(ModAttachments.QUICK_DROP_INVENTORY);
        quickDrop.copyFrom(original.getData(ModAttachments.QUICK_DROP_INVENTORY));

        Madra madra = replacement.getData(ModAttachments.MADRA);
        madra.copyFromMadra(original.getData(ModAttachments.MADRA));
        replacement.syncData(ModAttachments.MADRA);

        MaxMadra maxMadra = replacement.getData(ModAttachments.MAXMADRA);
        maxMadra.copyFromMaxMadra(original.getData(ModAttachments.MAXMADRA));
        replacement.syncData(ModAttachments.MAXMADRA);

        MadraType madraType = replacement.getData(ModAttachments.MADRA_TYPE);
        madraType.copyFrom(original.getData(ModAttachments.MADRA_TYPE));
        replacement.syncData(ModAttachments.MADRA_TYPE);
        GoldsignAppearanceService.refresh(replacement);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        player.getData(ModAttachments.MADRA);
        player.getData(ModAttachments.MAXMADRA);
        player.getData(ModAttachments.MADRA_TYPE);
        Core core = player.getData(ModAttachments.CORE);
        player.syncData(ModAttachments.CORE);
        ProgressionService.applyPermanentBenefits(player, core);
        GoldsignAppearanceService.refresh(player);
        player.syncData(ModAttachments.MADRA);
        player.syncData(ModAttachments.MAXMADRA);
        player.syncData(ModAttachments.MADRA_TYPE);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 160 == 0) {
            ProgressionService.applyTimedBenefits(player);
        }
        if (player.tickCount % 40 == 0) {
            GoldsignAppearanceService.refresh(player);
        }
    }
}
