package net.ok.project_cradle.server.networking;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ok.project_cradle.networking.packet.ToggleAirFlightC2S;
import net.ok.project_cradle.server.progression.AirSwimmingService;

public final class AirFlightPayloadHandler {
    private static final String LAST_TOGGLE = "project_cradle.last_flight_toggle";

    private AirFlightPayloadHandler() { }

    public static void handle(ToggleAirFlightC2S packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isAlive()
                || player.isSpectator() || player.containerMenu != player.inventoryMenu) return;
        long now = player.level().getGameTime();
        long last = player.getPersistentData().getLongOr(LAST_TOGGLE, -20L);
        if (now >= last && now - last < 4) return;
        player.getPersistentData().putLong(LAST_TOGGLE, now);
        AirSwimmingService.toggle(player);
    }
}
