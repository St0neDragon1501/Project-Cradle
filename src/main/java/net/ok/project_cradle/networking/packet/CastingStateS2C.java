package net.ok.project_cradle.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.ok.project_cradle.ProjectCradle;

/** Sent only to the caster; no inventory or private progression data is included. */
public record CastingStateS2C(String translationKey, int durationTicks, boolean active) implements CustomPacketPayload {
    public static final Type<CastingStateS2C> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "casting_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastingStateS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256), CastingStateS2C::translationKey,
            ByteBufCodecs.VAR_INT, CastingStateS2C::durationTicks,
            ByteBufCodecs.BOOL, CastingStateS2C::active, CastingStateS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
