package net.modfest.fireblanket.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CmdFindReplaceCommand {
	public static void init(LiteralArgumentBuilder<ServerCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("cmd-find-replace")
			.requires(source -> source.hasPermissionLevel(4))
			.then(argument("regex", StringArgumentType.string())
				.then(argument("replacement", StringArgumentType.string())
					.executes(ctx -> {
						Pattern p = Pattern.compile(StringArgumentType.getString(ctx, "regex"));
						String replacement = StringArgumentType.getString(ctx, "replacement");

						StringBuilder sb = new StringBuilder();
						int blocks = 0;
						int matches = 0;

						ServerWorld world = ctx.getSource().getWorld();
						for (ChunkHolder holder : world.getChunkManager().chunkLoadingManager.entryIterator()) {
							WorldChunk chunk = holder.getWorldChunk();
							if (chunk == null) {
								continue;
							}
							for (Map.Entry<BlockPos, BlockEntity> e : chunk.getBlockEntities().entrySet()) {
								if (e.getValue() instanceof CommandBlockBlockEntity cbe) {
									sb.setLength(0);
									String cmd = cbe.getCommandExecutor().getCommand();
									Matcher m = p.matcher(cmd);
									boolean found = false;
									while (m.find()) {
										if (!found) {
											found = true;
											blocks++;
										}
										matches++;
										m.appendReplacement(sb, replacement);
									}
									m.appendTail(sb);
									if (found) {
										cbe.getCommandExecutor().setCommand(sb.toString());
									}
								}
							}
						}
						if (matches == 0) {
							throw CommandUtils.GENERIC_EXCEPTION.create(Text.literal("No command blocks matched the regex"));
						} else {
							final int fmatches = matches;
							final int fblocks = blocks;
							ctx.getSource().sendFeedback(() -> Text.literal("Replaced " + fmatches + " occurence" + (fmatches == 1 ? "" : "s") + " across " + fblocks + " block" + (fblocks == 1 ? "" : "s")), true);
						}

						return 0;
					})
				)
			)
		);
	}
}
