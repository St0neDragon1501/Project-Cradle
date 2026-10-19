package net.ok.project_cradle.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.ok.project_cradle.ProjectCradle;

public record CycleC2S(int action) implements CustomPacketPayload {
    public static final Type<CycleC2S> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "cycle")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CycleC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            CycleC2S::action,
            CycleC2S::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
