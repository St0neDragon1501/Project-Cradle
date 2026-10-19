package net.ok.project_cradle.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.ok.project_cradle.ProjectCradle;

public record SelectTechniqueC2S(int slot) implements CustomPacketPayload {
    public static final Type<SelectTechniqueC2S> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "select_technique"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectTechniqueC2S> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SelectTechniqueC2S::slot, SelectTechniqueC2S::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
