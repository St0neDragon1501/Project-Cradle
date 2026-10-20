package net.ok.project_cradle.server.networking;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ok.project_cradle.server.progression.ProgressionService;
import net.ok.project_cradle.networking.packet.CycleC2S;

public final class CyclingPayloadHandler {
    private CyclingPayloadHandler() {
    }

    public static void handle(CycleC2S packet, IPayloadContext context) {
        if (packet.action() != 0 || !(context.player() instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator() || player.containerMenu != player.inventoryMenu) {
            return;
        }

        ProgressionService.cycle(player);
    }
}
