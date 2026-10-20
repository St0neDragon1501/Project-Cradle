package net.ok.project_cradle.networking;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.server.networking.CyclingPayloadHandler;
import net.ok.project_cradle.server.networking.QuickDropKeyPayloadHandler;
import net.ok.project_cradle.server.networking.TechniqueWheelPayloadHandler;
import net.ok.project_cradle.networking.packet.CycleC2S;
import net.ok.project_cradle.networking.packet.CancelCastC2S;
import net.ok.project_cradle.networking.packet.CastingStateS2C;
import net.ok.project_cradle.server.networking.CancelCastPayloadHandler;
import net.ok.project_cradle.networking.packet.ToggleAirFlightC2S;
import net.ok.project_cradle.server.networking.AirFlightPayloadHandler;
import net.ok.project_cradle.networking.packet.QuickDropKeyC2S;
import net.ok.project_cradle.networking.packet.SelectTechniqueC2S;

@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class ModNetworking {
    private ModNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("6").executesOn(HandlerThread.MAIN);
        registrar.playToServer(ToggleAirFlightC2S.TYPE, ToggleAirFlightC2S.STREAM_CODEC, AirFlightPayloadHandler::handle);
        registrar.playToClient(CastingStateS2C.TYPE, CastingStateS2C.STREAM_CODEC);
        registrar.playToServer(CancelCastC2S.TYPE, CancelCastC2S.STREAM_CODEC, CancelCastPayloadHandler::handle);
        registrar.playToServer(CycleC2S.TYPE, CycleC2S.STREAM_CODEC, CyclingPayloadHandler::handle);
        registrar.playToServer(QuickDropKeyC2S.TYPE, QuickDropKeyC2S.STREAM_CODEC, QuickDropKeyPayloadHandler::handle);
        registrar.playToServer(SelectTechniqueC2S.TYPE, SelectTechniqueC2S.STREAM_CODEC,
                TechniqueWheelPayloadHandler::handle);
    }
}
