package com.ItzChilletIgnis.horror.true_creative_mode.network;

import com.ItzChilletIgnis.horror.true_creative_mode.True_creative_mode;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SyncAshfallStatePayload(boolean isAshfall) implements CustomPayload {
    public static final CustomPayload.Id<SyncAshfallStatePayload> ID = new CustomPayload.Id<>(True_creative_mode.PACKET_SYNC_ASHFALL_STATE);
    public static final PacketCodec<RegistryByteBuf, SyncAshfallStatePayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, SyncAshfallStatePayload::isAshfall, SyncAshfallStatePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}