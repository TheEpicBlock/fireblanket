package net.modfest.fireblanket.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public class DumpCommand {
	public static void init(LiteralArgumentBuilder<ServerCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("dump")
			.requires(source -> source.hasPermissionLevel(4))
			.then(literal("command-blocks")
				.executes(server -> {
					server.getSource().getServer().submit(() -> {
						ServerWorld world = server.getSource().getWorld();
						for (ChunkHolder holder : world.getChunkManager().chunkLoadingManager.entryIterator()) {
							WorldChunk chunk = holder.getWorldChunk();
							if (chunk == null) {
								continue;
							}

							for (Map.Entry<BlockPos, BlockEntity> e : chunk.getBlockEntities().entrySet()) {
								if (e.getValue() instanceof CommandBlockBlockEntity cbe) {
									BlockState state = cbe.getCachedState();

									String type;
									if (state.isOf(Blocks.COMMAND_BLOCK)) {
										type = "B";
									} else if (state.isOf(Blocks.CHAIN_COMMAND_BLOCK)) {
										type = "C";
									} else if (state.isOf(Blocks.REPEATING_COMMAND_BLOCK)) {
										type = "R";
									} else {
										type = "???";
									}

									if (cbe.isPowered()) {
										type += "*";
									}

									if (cbe.isAuto()) {
										type += "!";
									}

									String ft = type;
									server.getSource().sendFeedback(() -> Text.literal("[" + e.getKey().toShortString() + "] [" + ft + "] : " + cbe.getCommandExecutor().getCommand()), false);
								}
							}
						}
					});

					return 0;
				})
			)
			.then(literal("entity-types")
				.executes(server -> {
					for (EntityType<?> type : Registries.ENTITY_TYPE) {
						server.getSource().sendFeedback(() -> Text.literal(Registries.ENTITY_TYPE.getId(type) + " alwaysUpdateVelocity=" + type.alwaysUpdateVelocity() + " updateDistance(blocks)=" + (type.getMaxTrackDistance() * 16) + " tickInterval=" + type.getTrackTickInterval()), false);
					}

					return 0;
				})
			)
			.then(literal("entities")
				.executes(cmd -> {
					MinecraftServer server = cmd.getSource().getServer();
					server.submit(() -> {
						for (ServerWorld world : server.getWorlds()) {
							cmd.getSource().sendFeedback(() -> Text.literal("----- Dumping types for dimension " + world.getDimensionEntry().getKey().get().getValue() + " --------"), false);

							Object2IntOpenHashMap<EntityType<?>> map = new Object2IntOpenHashMap<>();

							for (Entity entity : world.iterateEntities()) {
								int r = map.getOrDefault(entity.getType(), 0);
								map.put(entity.getType(), r + 1);
							}

							ArrayList<Object2IntMap.Entry<EntityType<?>>> entries = new ArrayList<>(map.object2IntEntrySet());
							entries.sort(Map.Entry.comparingByValue());
							Collections.reverse(entries);
							for (Object2IntMap.Entry<EntityType<?>> e : entries) {
								EntityType<?> key = e.getKey();
								String string = Registries.ENTITY_TYPE.getId(key).toString();

								cmd.getSource().sendFeedback(() -> Text.literal(string + " -> " + e.getIntValue()), false);
							}
						}
					});

					return 0;
				})
			)
		);
	}
}
