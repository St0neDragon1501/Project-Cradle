package net.ok.project_cradle.client;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ok.project_cradle.networking.packet.CastingStateS2C;

/** Registered from the client-only payload registration event. */
public final class CastingStateHandler {
    private CastingStateHandler() {
    }

    public static void handle(CastingStateS2C packet, IPayloadContext context) {
        context.enqueueWork(() -> TechniqueCastingOverlay.accept(packet));
    }
}
