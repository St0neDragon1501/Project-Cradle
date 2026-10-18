package net.ok.project_cradle.networking;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MaxMadra;
import net.ok.project_cradle.networking.packet.TestPacketC2S;

public final class MadraTestPayloadHandler {
    private MadraTestPayloadHandler() {
    }

    public static void handle(TestPacketC2S packet, IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();
        if (level.isClientSide()) {
            return;
        }

        Madra madra = player.getData(ModAttachments.MADRA);
        MaxMadra maxMadra = player.getData(ModAttachments.MAXMADRA);
        madra.addMadra(10.0, maxMadra.getLevel());
        player.syncData(ModAttachments.MADRA);
    }
}
