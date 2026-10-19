package net.ok.project_cradle.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.ok.project_cradle.ProjectCradle;

/** Cancels the sender's pending technique without targeting another player's cast. */
public record CancelCastC2S() implements CustomPacketPayload {
    public static final Type<CancelCastC2S> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "cancel_cast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CancelCastC2S> STREAM_CODEC = StreamCodec.unit(new CancelCastC2S());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
