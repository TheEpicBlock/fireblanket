package net.modfest.fireblanket.mixin.be_sync;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.WorldChunk;
import net.modfest.fireblanket.net.BEUpdate;
import net.modfest.fireblanket.net.BatchedBEUpdatePayload;
import net.modfest.fireblanket.world.CachedCompoundBE;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder extends AbstractChunkHolder {
	private static final List<BEUpdate> BATCHED_UPDATES = new ArrayList<>();

	public MixinChunkHolder(ChunkPos pos) {
		super(pos);
	}

	@Shadow protected abstract void sendBlockEntityUpdatePacket(List<ServerPlayerEntity> players, World world, BlockPos pos);

	@Shadow protected abstract void sendPacketToPlayers(List<ServerPlayerEntity> players, Packet<?> packet);

	@Shadow @Final private ChunkHolder.PlayersWatchingChunkProvider playersWatchingChunkProvider;

	/**
	 * @author Jasmine
	 *
	 * @reason Don't send BE update packets until the observable state of the BE has *actually* changed.
	 */
	@Overwrite
	private void tryUpdateBlockEntityAt(List<ServerPlayerEntity> players, World world, BlockPos pos, BlockState state) {
		if (state.hasBlockEntity()) {
			// TODO: this code here is duplicated because mods mixin to sendBlockEntityUpdatePacket, which makes it less ideal for mixing into
			BlockEntity blockEntity = world.getBlockEntity(pos);

			if (blockEntity != null) {
				CachedCompoundBE cbe = (CachedCompoundBE) blockEntity;
				Packet<?> packet = blockEntity.toUpdatePacket();
				if (packet instanceof BlockEntityUpdateS2CPacket bes2c) {
					NbtCompound cached = cbe.fireblanket$getCachedCompound();
					NbtCompound nbt = bes2c.getNbt();

					// Don't update if we're the same as before.

					// TODO: do this over the network thread, so it doesn't block the main thread
					if (Objects.equals(cached, nbt)) {
						return;
					}

					cbe.fireblanket$setCachedCompound(nbt);
				}

				this.sendBlockEntityUpdatePacket(players, world, pos);
			}
		}
	}

	@Redirect(method = "sendBlockEntityUpdatePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;sendPacketToPlayers(Ljava/util/List;Lnet/minecraft/network/packet/Packet;)V"))
	private void fireblanket$dontSendUpdate(ChunkHolder instance, List<ServerPlayerEntity> players, Packet<?> packet) {
		if (packet instanceof BlockEntityUpdateS2CPacket bes2c) {
			// We're a chunk update- let's batch per chunk
			BATCHED_UPDATES.add(new BEUpdate(bes2c.getPos(), bes2c.getBlockEntityType(), bes2c.getNbt()));
		} else {
			// No idea what we are- need to fall back to worst case
			this.sendPacketToPlayers(players, packet);
		}
	}

	@Inject(method = "flushUpdates", at = @At("HEAD"))
	private void fireblanket$flushUpdates$head(WorldChunk chunk, CallbackInfo ci) {
		// Should probably assert here for the list being clear

		BATCHED_UPDATES.clear();
	}

	@Inject(method = "flushUpdates", at = @At("TAIL"))
	private void fireblanket$flushUpdates$tail(WorldChunk chunk, CallbackInfo ci) {
		if (!BATCHED_UPDATES.isEmpty()) {
			List<ServerPlayerEntity> list = this.playersWatchingChunkProvider.getPlayersWatchingChunk(this.pos, false);

			int size = BATCHED_UPDATES.size();
			if (list.isEmpty()) {
				BATCHED_UPDATES.clear();
				return;
			}

//			RegistryByteBuf buf = RegistryByteBuf.makeFactory(chunk.getWorld().getRegistryManager()).apply(Unpooled.buffer());
//			BatchedBEUpdatePayload.CODEC.encode(buf, new BatchedBEUpdatePayload(BATCHED_UPDATES));

			for (ServerPlayerEntity p : list) {
				ServerPlayNetworking.send(p, new BatchedBEUpdatePayload(BATCHED_UPDATES));
			}

			BATCHED_UPDATES.clear();
		}
	}
}
