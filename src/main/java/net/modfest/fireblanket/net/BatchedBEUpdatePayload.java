package net.modfest.fireblanket.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.modfest.fireblanket.Fireblanket;

import java.util.ArrayList;
import java.util.List;

public record BatchedBEUpdatePayload(List<BEUpdate> updates) implements CustomPayload {
    public static final CustomPayload.Id<BatchedBEUpdatePayload> ID = new CustomPayload.Id<>(Fireblanket.BATCHED_BE_UPDATE);
    public static final PacketCodec<RegistryByteBuf, BatchedBEUpdatePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, BEUpdate.CODEC),
            BatchedBEUpdatePayload::updates,
            BatchedBEUpdatePayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
