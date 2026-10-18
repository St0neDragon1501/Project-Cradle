package net.ok.project_cradle.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.keymapping.ModKeyMappings;
import net.ok.project_cradle.networking.packet.QuickDropKeyC2S;
import net.ok.project_cradle.networking.packet.TestPacketC2S;

@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public final class ClientInputHandler {
    private ClientInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ModKeyMappings.PRESS_CYCLING.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new TestPacketC2S("Testing Pack", 67));
        }

        boolean noScreenOpen = Minecraft.getInstance().gui.screen() == null;
        for (int index = 0; index < ModKeyMappings.QUICK_DROP_BINDINGS.size(); index++) {
            while (ModKeyMappings.QUICK_DROP_BINDINGS.get(index).consumeClick()) {
                if (noScreenOpen) {
                    ClientPacketDistributor.sendToServer(new QuickDropKeyC2S(index + 1));
                }
            }
        }
    }
}
