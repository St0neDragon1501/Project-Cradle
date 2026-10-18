package net.ok.project_cradle.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.QuickDropInventory;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MaxMadra;

@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class PlayerAttachmentEvents {
    private PlayerAttachmentEvents() {
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        Player original = event.getOriginal();
        Player replacement = event.getEntity();

        Core core = replacement.getData(ModAttachments.CORE);
        core.copyFromCore(original.getData(ModAttachments.CORE));

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
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        player.getData(ModAttachments.MADRA);
        player.getData(ModAttachments.MAXMADRA);
        player.getData(ModAttachments.MADRA_TYPE);
        player.syncData(ModAttachments.MADRA);
        player.syncData(ModAttachments.MAXMADRA);
        player.syncData(ModAttachments.MADRA_TYPE);
    }
}
