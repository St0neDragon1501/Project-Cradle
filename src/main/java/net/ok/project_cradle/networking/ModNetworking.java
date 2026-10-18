package net.ok.project_cradle.networking;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.networking.packet.QuickDropKeyC2S;
import net.ok.project_cradle.networking.packet.TestPacketC2S;

@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class ModNetworking {
    private ModNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.MAIN);
        registrar.playToServer(QuickDropKeyC2S.TYPE, QuickDropKeyC2S.STREAM_CODEC, QuickDropKeyPayloadHandler::handle);
        registrar.playToServer(TestPacketC2S.TYPE, TestPacketC2S.STREAM_CODEC, MadraTestPayloadHandler::handle);
    }
}
