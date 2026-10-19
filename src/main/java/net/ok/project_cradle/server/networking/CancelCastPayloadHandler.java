package net.ok.project_cradle.server.networking;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ok.project_cradle.networking.packet.CancelCastC2S;
import net.ok.project_cradle.server.spells.TechniqueCastingService;

public final class CancelCastPayloadHandler {
    private CancelCastPayloadHandler() { }

    public static void handle(CancelCastC2S packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) TechniqueCastingService.cancel(player);
    }
}
