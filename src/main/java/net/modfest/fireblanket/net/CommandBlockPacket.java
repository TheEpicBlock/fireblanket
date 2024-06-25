package net.modfest.fireblanket.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CommandBlockPacket() implements CustomPayload {
	public static final CustomPayload.Id<CommandBlockPacket> ID =
			new CustomPayload.Id<>(Identifier.of("fireblanket", "place_command_block"));
	public static final CommandBlockPacket INST = new CommandBlockPacket();
	public static final PacketCodec<RegistryByteBuf, CommandBlockPacket> CODEC = PacketCodec.unit(INST);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
