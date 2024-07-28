package net.modfest.fireblanket.world.render_regions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.AddRegion;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.AttachBlock;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.AttachEntity;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.DestroyRegion;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.DetachAll;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.DetachBlock;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.DetachEntity;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.FullState;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.FullStateLegacy;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.InvalidCommand;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.RedefineRegion;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.RegistryRegionSyncRequest;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.Reset;
import net.modfest.fireblanket.world.render_regions.RenderRegion.Mode;

import java.util.UUID;
import java.util.function.Function;

public sealed interface RegionSyncRequest extends CustomPayload permits InvalidCommand, FullState, Reset, AddRegion,
	DestroyRegion, DetachAll, AttachEntity, AttachBlock, DetachEntity, DetachBlock, RedefineRegion, FullStateLegacy,
	RegistryRegionSyncRequest {

	CustomPayload.Id<RegionSyncRequest> ID = new CustomPayload.Id<>(Fireblanket.REGIONS_UPDATE);

	PacketCodec<RegistryByteBuf, RegionSyncRequest> CODEC = PacketCodec.of(
		RegionSyncRequest::toPacket, RegionSyncRequest::read
	);

	enum RequestType {
		INVALID_COMMAND(InvalidCommand::read, "invalid_command"),
		FULL_STATE_LEGACY(FullStateLegacy::read, "full_state_legacy"),
		RESET(Reset::read, "reset"),
		ADD_REGION(AddRegion::read, "add_region"),
		DESTROY_REGION(DestroyRegion::read, "destroy_region"),
		DETACH_ALL(DetachAll::read, "detach_all"),
		ATTACH_ENTITY(AttachEntity::read, "attach_entity"),
		ATTACH_BLOCK(AttachBlock::read, "attach_block"),
		DETACH_ENTITY(DetachEntity::read, "detach_entity"),
		DETACH_BLOCK(DetachBlock::read, "detach_block"),
		REDEFINE_REGION(RedefineRegion::read, "redefine_region"),
		ATTACH_ENTITY_TYPE(AttachEntityType::read, "attach_entity_type"),
		DETACH_ENTITY_TYPE(DetachEntityType::read, "detach_entity_type"),
		ATTACH_BLOCK_ENTITY_TYPE(AttachBlockEntityType::read, "attach_block_entity_type"),
		DETACH_BLOCK_ENTITY_TYPE(DetachBlockEntityType::read, "detach_block_entity_type"),
		FULL_STATE(FullState::read, "full_state"),
		;
		public static final ImmutableList<RequestType> VALUES = ImmutableList.copyOf(values());
		public final Function<PacketByteBuf, ? extends RegionSyncRequest> reader;
		public final Identifier id;

		RequestType(Function<PacketByteBuf, ? extends RegionSyncRequest> reader, String name) {
			this.reader = reader;
			this.id = Identifier.of("fireblanket", name);
		}

	}

	RequestType type();

	//  static self read(PacketByteBuf buf);
	void write(PacketByteBuf buf);

	void apply(RenderRegions tgt);

	boolean valid();

	sealed interface RegistryRegionSyncRequest<T> extends RegionSyncRequest permits AttachEntityType, DetachEntityType,
		AttachBlockEntityType, DetachBlockEntityType {

		Registry<T> registry();

		String name();

		Identifier id();

		@Override
		default boolean valid() {
			return name() != null && registry().get(id()) != null;
		}

		@Override
		default void write(PacketByteBuf buf) {
			buf.writeString(name());
			writeId(buf, registry(), id());
		}
	}

	default void toPacket(RegistryByteBuf buf) {
		buf.writeByte(type().ordinal());
		write(buf);
	}

	@Override
	default Id<? extends CustomPayload> getId() {
		return ID;
	}

	private static void writeRegion(PacketByteBuf buf, RenderRegion r) {
		buf.writeByte(r.mode().ordinal());
		buf.writeVarInt(r.minX()).writeVarInt(r.minY()).writeVarInt(r.minZ());
		buf.writeVarInt(r.maxX()).writeVarInt(r.maxY()).writeVarInt(r.maxZ());
	}

	private static RenderRegion readRegion(PacketByteBuf buf) {
		int modeId = buf.readUnsignedByte();
		if (modeId >= Mode.VALUES.size()) {
			Fireblanket.LOGGER.warn("Unknown region mode id " + modeId);
			modeId = 0;
		}
		Mode mode = Mode.VALUES.get(modeId);
		return new RenderRegion(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
			buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
			mode);
	}

	private static <T> Identifier readId(PacketByteBuf buf, Registry<T> registry) {
		return registry.getId(registry.get(buf.readVarInt()));
	}

	private static <T> void writeId(PacketByteBuf buf, Registry<T> registry, Identifier id) {
		buf.writeVarInt(registry.getRawId(registry.get(id)));
	}

	static RegionSyncRequest read(RegistryByteBuf buf) {
		int tid = buf.readUnsignedByte();
		if (tid >= RequestType.VALUES.size()) {
			Fireblanket.LOGGER.warn("Unknown region sync command id " + tid);
			return new InvalidCommand();
		}
		RequestType t = RequestType.VALUES.get(tid);
		return t.reader.apply(buf);
	}

	record InvalidCommand() implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.INVALID_COMMAND;
		}

		@Override
		public void write(PacketByteBuf buf) {
			Fireblanket.LOGGER.warn("Writing an invalid command");
		}

		public static InvalidCommand read(PacketByteBuf buf) {
			return new InvalidCommand();
		}

		@Override
		public void apply(RenderRegions tgt) {
			Fireblanket.LOGGER.warn("Attempted to apply invalid command");
		}

		@Override
		public boolean valid() {
			return false;
		}

	}

	record FullStateLegacy(ImmutableMap<String, RenderRegion> regions,
	                              ImmutableMultimap<RenderRegion, UUID> entityAttachments,
	                              ImmutableMultimap<RenderRegion, Long> blockAttachments) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.FULL_STATE_LEGACY;
		}

		@Override
		public void write(PacketByteBuf buf) {
			throw new UnsupportedOperationException();
		}

		public static FullStateLegacy read(PacketByteBuf buf) {
			ImmutableMap.Builder<String, RenderRegion> regionsBldr = ImmutableMap.builder();
			ImmutableMultimap.Builder<RenderRegion, UUID> entityAttachmentsBldr = ImmutableMultimap.builder();
			ImmutableMultimap.Builder<RenderRegion, Long> blockAttachmentsBldr = ImmutableMultimap.builder();
			int regionCount = buf.readVarInt();
			for (int i = 0; i < regionCount; i++) {
				String name = buf.readString();
				RenderRegion r = readRegion(buf);
				regionsBldr.put(name, r);
				int entityCount = buf.readVarInt();
				for (int j = 0; j < entityCount; j++) {
					entityAttachmentsBldr.put(r, buf.readUuid());
				}
				int blockCount = buf.readVarInt();
				for (int j = 0; j < blockCount; j++) {
					blockAttachmentsBldr.put(r, buf.readLong());
				}
			}
			return new FullStateLegacy(regionsBldr.build(), entityAttachmentsBldr.build(), blockAttachmentsBldr.build());
		}

		@Override
		public boolean valid() {
			return regions != null && entityAttachments != null && blockAttachments != null
				&& !regions.isEmpty();
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.clear();
			regions.forEach(tgt::add);
			entityAttachments.forEach(tgt::attachEntity);
			blockAttachments.forEach(tgt::attachBlock);
		}
	}

	record Reset(boolean valid) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.RESET;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeInt(0xDEADDEAD);
		}

		public static Reset read(PacketByteBuf buf) {
			return new Reset(buf.readInt() == 0xDEADDEAD);
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.clear();
		}

	}

	record AddRegion(String name, RenderRegion region) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.ADD_REGION;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			writeRegion(buf, region);
		}

		public static AddRegion read(PacketByteBuf buf) {
			return new AddRegion(buf.readString(), readRegion(buf));
		}

		@Override
		public boolean valid() {
			return name != null && region != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.add(name, region);
		}

	}

	record DestroyRegion(String name) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.DESTROY_REGION;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
		}

		public static DestroyRegion read(PacketByteBuf buf) {
			return new DestroyRegion(buf.readString());
		}

		@Override
		public boolean valid() {
			return name != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.remove(tgt.getByName(name));
		}

	}

	record DetachAll(String name) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.DETACH_ALL;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
		}

		public static DetachAll read(PacketByteBuf buf) {
			return new DetachAll(buf.readString());
		}

		@Override
		public boolean valid() {
			return name != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachAll(tgt.getByName(name));
		}
	}

	record AttachEntity(String name, UUID entity) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.ATTACH_ENTITY;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			buf.writeUuid(entity);
		}

		public static AttachEntity read(PacketByteBuf buf) {
			return new AttachEntity(buf.readString(), buf.readUuid());
		}

		@Override
		public boolean valid() {
			return name != null && entity != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.attachEntity(tgt.getByName(name), entity);
		}

	}

	record AttachBlock(String name, long pos) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.ATTACH_BLOCK;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			buf.writeLong(pos);
		}

		public static AttachBlock read(PacketByteBuf buf) {
			return new AttachBlock(buf.readString(), buf.readLong());
		}

		@Override
		public boolean valid() {
			return name != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.attachBlock(tgt.getByName(name), pos);
		}

	}

	record DetachEntity(String name, UUID entity) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.DETACH_ENTITY;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			buf.writeUuid(entity);
		}

		public static DetachEntity read(PacketByteBuf buf) {
			return new DetachEntity(buf.readString(), buf.readUuid());
		}

		@Override
		public boolean valid() {
			return name != null && entity != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachEntity(tgt.getByName(name), entity);
		}

	}

	record DetachBlock(String name, long pos) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.DETACH_BLOCK;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			buf.writeLong(pos);
		}

		public static DetachBlock read(PacketByteBuf buf) {
			return new DetachBlock(buf.readString(), buf.readLong());
		}

		@Override
		public boolean valid() {
			return name != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachBlock(tgt.getByName(name), pos);
		}

	}

	record RedefineRegion(String name, RenderRegion region) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.REDEFINE_REGION;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			writeRegion(buf, region);
		}

		public static RedefineRegion read(PacketByteBuf buf) {
			return new RedefineRegion(buf.readString(), readRegion(buf));
		}

		@Override
		public boolean valid() {
			return name != null && region != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.redefine(name, region);
		}

	}

	record AttachEntityType(String name, Identifier id) implements RegistryRegionSyncRequest<EntityType<?>> {

		@Override
		public RequestType type() {
			return RequestType.ATTACH_ENTITY_TYPE;
		}

		@Override
		public Registry<EntityType<?>> registry() {
			return Registries.ENTITY_TYPE;
		}

		public static AttachEntityType read(PacketByteBuf buf) {
			return new AttachEntityType(buf.readString(), readId(buf, Registries.ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.attachEntityType(tgt.getByName(name), id);
		}

	}

	record DetachEntityType(String name, Identifier id) implements RegistryRegionSyncRequest<EntityType<?>> {

		@Override
		public RequestType type() {
			return RequestType.DETACH_ENTITY_TYPE;
		}

		@Override
		public Registry<EntityType<?>> registry() {
			return Registries.ENTITY_TYPE;
		}

		public static DetachEntityType read(PacketByteBuf buf) {
			return new DetachEntityType(buf.readString(), readId(buf, Registries.ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachEntityType(tgt.getByName(name), id);
		}

	}

	record AttachBlockEntityType(String name, Identifier id) implements RegistryRegionSyncRequest<BlockEntityType<?>> {

		@Override
		public RequestType type() {
			return RequestType.ATTACH_BLOCK_ENTITY_TYPE;
		}

		@Override
		public Registry<BlockEntityType<?>> registry() {
			return Registries.BLOCK_ENTITY_TYPE;
		}

		public static AttachBlockEntityType read(PacketByteBuf buf) {
			return new AttachBlockEntityType(buf.readString(), readId(buf, Registries.BLOCK_ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.attachBlockEntityType(tgt.getByName(name), id);
		}

	}

	record DetachBlockEntityType(String name, Identifier id) implements RegistryRegionSyncRequest<BlockEntityType<?>> {

		@Override
		public RequestType type() {
			return RequestType.DETACH_BLOCK_ENTITY_TYPE;
		}

		@Override
		public Registry<BlockEntityType<?>> registry() {
			return Registries.BLOCK_ENTITY_TYPE;
		}

		public static DetachBlockEntityType read(PacketByteBuf buf) {
			return new DetachBlockEntityType(buf.readString(), readId(buf, Registries.BLOCK_ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachBlockEntityType(tgt.getByName(name), id);
		}

	}

	record FullState(ImmutableList<ExplainedRenderRegion> regions) implements RegionSyncRequest {

		@Override
		public RequestType type() {
			return RequestType.FULL_STATE;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeVarInt(regions.size());
			for (var ex : regions) {
				buf.writeString(ex.name);
				int sizePos = buf.writerIndex();
				buf.writeMedium(0);
				int start = buf.writerIndex();
				RenderRegion r = ex.reg;
				writeRegion(buf, r);
				var ea = ex.entityAttachments;
				buf.writeVarInt(ea.size());
				for (UUID id : ea) {
					buf.writeUuid(id);
				}
				var ba = ex.blockAttachments;
				buf.writeVarInt(ba.size());
				LongIterator iter = ba.longIterator();
				while (iter.hasNext()) {
					buf.writeLong(iter.nextLong());
				}
				var et = ex.entityTypeAttachments;
				buf.writeVarInt(et.size());
				for (Identifier id : et) {
					writeId(buf, Registries.ENTITY_TYPE, id);
				}
				var bet = ex.beTypeAttachments;
				buf.writeVarInt(bet.size());
				for (Identifier id : bet) {
					writeId(buf, Registries.BLOCK_ENTITY_TYPE, id);
				}
				int len = buf.writerIndex() - start;
				buf.markWriterIndex();
				buf.writerIndex(sizePos);
				buf.writeMedium(len);
				buf.resetWriterIndex();
			}
		}

		public static FullState read(PacketByteBuf buf) {
			ImmutableList.Builder<ExplainedRenderRegion> bldr = ImmutableList.builder();
			int regionCount = buf.readVarInt();
			for (int i = 0; i < regionCount; i++) {
				String name = buf.readString();
				int len = buf.readUnsignedMedium();
				int start = buf.readerIndex();
				RenderRegion r = readRegion(buf);
				ExplainedRenderRegion ex = new ExplainedRenderRegion(name, r);
				int entityCount = buf.readVarInt();
				for (int j = 0; j < entityCount; j++) {
					ex.entityAttachments.add(buf.readUuid());
				}
				int blockCount = buf.readVarInt();
				for (int j = 0; j < blockCount; j++) {
					ex.blockAttachments.add(buf.readLong());
				}
				int entityTypeCount = buf.readVarInt();
				for (int j = 0; j < entityTypeCount; j++) {
					ex.entityTypeAttachments.add(readId(buf, Registries.ENTITY_TYPE));
				}
				int beTypeCount = buf.readVarInt();
				for (int j = 0; j < beTypeCount; j++) {
					ex.beTypeAttachments.add(readId(buf, Registries.BLOCK_ENTITY_TYPE));
				}
				buf.readerIndex(start + len);
				bldr.add(ex);
			}
			return new FullState(bldr.build());
		}

		@Override
		public boolean valid() {
			return regions != null && !regions.isEmpty();
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.clear();
			for (var ex : regions) {
				tgt.add(ex.name, ex.reg);
				ex.entityAttachments.forEach(id -> tgt.attachEntity(ex.reg, id));
				ex.blockAttachments.forEach(pos -> tgt.attachBlock(ex.reg, pos));
				ex.entityTypeAttachments.forEach(id -> tgt.attachEntityType(ex.reg, id));
				ex.beTypeAttachments.forEach(id -> tgt.attachBlockEntityType(ex.reg, id));
			}
		}
	}

}
