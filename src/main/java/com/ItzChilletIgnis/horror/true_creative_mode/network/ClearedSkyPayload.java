package com.ItzChilletIgnis.horror.true_creative_mode.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClearedSkyPayload(boolean active) implements CustomPayload {
    public static final CustomPayload.Id<ClearedSkyPayload> ID = new CustomPayload.Id<>(Identifier.of("true_creative_mode", "cleared_sky"));
    public static final PacketCodec<RegistryByteBuf, ClearedSkyPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, ClearedSkyPayload::active, ClearedSkyPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}