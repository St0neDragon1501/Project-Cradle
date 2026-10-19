package net.ok.project_cradle.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.ok.project_cradle.ProjectCradle;

/** A request to change flight state; eligibility and takeoff are decided by the server. */
public record ToggleAirFlightC2S() implements CustomPacketPayload {
    public static final Type<ToggleAirFlightC2S> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "toggle_air_flight"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAirFlightC2S> STREAM_CODEC =
            StreamCodec.unit(new ToggleAirFlightC2S());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
