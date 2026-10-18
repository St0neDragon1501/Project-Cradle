package net.ok.project_cradle.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.ok.project_cradle.ProjectCradle;

public record QuickDropKeyC2S(int key) implements CustomPacketPayload {
    public static final Type<QuickDropKeyC2S> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "quick_drop_key"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuickDropKeyC2S> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    QuickDropKeyC2S::key,
                    QuickDropKeyC2S::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
