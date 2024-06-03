package net.modfest.fireblanket.net;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;

public record BEUpdate(BlockPos pos, BlockEntityType<?> type, NbtCompound nbt) {
    public static final PacketCodec<RegistryByteBuf, BEUpdate> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            BEUpdate::pos,
            PacketCodecs.registryValue(RegistryKeys.BLOCK_ENTITY_TYPE),
            BEUpdate::type,
            PacketCodecs.UNLIMITED_NBT_COMPOUND,
            BEUpdate::nbt,
            BEUpdate::new
    );
}
