package net.ok.project_cradle.networking;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MaxMadra;
import net.ok.project_cradle.networking.packet.TestPacketC2S;

// Handles packets FROM THE CLIENT to the SERVER
public class ClientPayloadHandler {

    public static void handleTestPacket(
            TestPacketC2S packet,
            IPayloadContext context
    ) {
        Player player = context.player();
        Level level = player.level();

        // Make sure this is running on the server
        if (!level.isClientSide()) {

            Madra madra = player.getData(ModAttachments.MADRA);
            MaxMadra maxMadra = player.getData(ModAttachments.MAXMADRA);

            madra.addMadra(10.0, maxMadra.getLevel());

            player.sendSystemMessage(
                    Component.literal("Madra " + madra.getALevel())
                            .withStyle(ChatFormatting.BOLD)
            );
        }
    }
}