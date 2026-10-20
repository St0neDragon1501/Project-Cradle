package net.ok.project_cradle.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.networking.packet.CycleC2S;
import net.ok.project_cradle.networking.packet.CancelCastC2S;
import net.ok.project_cradle.networking.packet.ToggleAirFlightC2S;
import net.ok.project_cradle.networking.packet.QuickDropKeyC2S;

@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public final class ClientInputHandler {
    private static int cyclingDelay;
    private ClientInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        boolean noScreenOpen = Minecraft.getInstance().gui.screen() == null;
        if (cyclingDelay > 0) cyclingDelay--;
        boolean cyclePressed = false;
        while (ModKeyMappings.PRESS_CYCLING.get().consumeClick()) cyclePressed = true;
        if (noScreenOpen && Minecraft.getInstance().player != null && cyclingDelay <= 0
                && (cyclePressed || ModKeyMappings.PRESS_CYCLING.get().isDown())) {
            ClientPacketDistributor.sendToServer(new CycleC2S(0));
            cyclingDelay = 10;
        }

        while (ModKeyMappings.TOGGLE_AIR_FLIGHT.consumeClick()) {
            if (noScreenOpen && Minecraft.getInstance().player != null) {
                ClientPacketDistributor.sendToServer(new ToggleAirFlightC2S());
            }
        }

        while (ModKeyMappings.CANCEL_CAST.consumeClick()) {
            if (noScreenOpen && Minecraft.getInstance().player != null) ClientPacketDistributor.sendToServer(new CancelCastC2S());
        }

        if (noScreenOpen) {
            while (ModKeyMappings.TECHNIQUE_WHEEL.consumeClick()) {
                ClientPacketDistributor.sendToServer(new QuickDropKeyC2S(0));
            }
            while (ModKeyMappings.CAST_SELECTED_TECHNIQUE.consumeClick()) {
                ClientPacketDistributor.sendToServer(new QuickDropKeyC2S(-1));
            }
        }

        for (int index = 0; index < ModKeyMappings.QUICK_DROP_BINDINGS.size(); index++) {
            while (ModKeyMappings.QUICK_DROP_BINDINGS.get(index).consumeClick()) {
                if (noScreenOpen) {
                    ClientPacketDistributor.sendToServer(new QuickDropKeyC2S(index + 1));
                }
            }
        }
    }
}
