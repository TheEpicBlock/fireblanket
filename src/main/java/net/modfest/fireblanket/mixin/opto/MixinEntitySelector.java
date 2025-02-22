package net.modfest.fireblanket.mixin.opto;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public abstract class MixinEntitySelector {
	@Shadow
	protected abstract void checkSourcePermission(ServerCommandSource source) throws CommandSyntaxException;

	@Shadow
	@Final
	private @Nullable String playerName;

	@Shadow
	@Final
	private @Nullable UUID uuid;

	@Shadow
	@Final
	private Function<Vec3d, Vec3d> positionOffset;

	@Shadow
	protected abstract Predicate<Entity> getPositionPredicate(Vec3d pos, @Nullable Box box, @Nullable FeatureSet enabledFeatures);

	@Shadow
	@Final
	private boolean senderOnly;

	@Shadow
	protected abstract int getAppendLimit();

	@Shadow
	public abstract boolean isLocalWorldOnly();

	@Shadow
	protected abstract <T extends Entity> List<T> getEntities(Vec3d pos, List<T> entities);

	@Shadow
	@Final
	private NumberRange.DoubleRange distance;

	@Shadow
	@Final
	private @Nullable Box box;

	@Shadow
	@Nullable
	protected abstract Box getOffsetBox(Vec3d offset);

	@Shadow
	@Final
	private List<Predicate<Entity>> predicates;

	/**
	 * @author Jasmine
	 * @reason Always predicate on distance and d(x|y|z) *before* checking the NBT, or any other predicate.
	 */
	@Overwrite
	public List<ServerPlayerEntity> getPlayers(ServerCommandSource source) throws CommandSyntaxException {
		this.checkSourcePermission(source);
		if (this.playerName != null) {
			ServerPlayerEntity serverPlayerEntity = source.getServer().getPlayerManager().getPlayer(this.playerName);
			return serverPlayerEntity == null ? Collections.emptyList() : Lists.newArrayList(serverPlayerEntity);
		} else if (this.uuid != null) {
			ServerPlayerEntity serverPlayerEntity = source.getServer().getPlayerManager().getPlayer(this.uuid);
			return serverPlayerEntity == null ? Collections.emptyList() : Lists.newArrayList(serverPlayerEntity);
		} else {
			Vec3d pos = this.positionOffset.apply(source.getPosition());
			Box box = this.getOffsetBox(pos);
			Predicate<Entity> predicate = this.getPositionPredicate(pos, box, null);
			if (this.senderOnly) {
				if (source.getEntity() instanceof ServerPlayerEntity serverPlayerEntity2 && predicate.test(serverPlayerEntity2)) {
					return Lists.newArrayList(serverPlayerEntity2);
				}

				return Collections.emptyList();
			} else {
				int i = this.getAppendLimit();
				List<ServerPlayerEntity> list;
				if (this.isLocalWorldOnly()) {
					// The change is here: Get players with distance predicate first, move onto base predicate later.
					predicate = getPositionOnlyPredicate(pos, box, null);

					Predicate<Entity> basePredicate = Util.allOf(this.predicates);

					list = source.getWorld().getPlayers(predicate, i);
					list.removeIf(basePredicate.negate());
				} else {
					list = Lists.newArrayList();

					for (ServerPlayerEntity serverPlayerEntity3 : source.getServer().getPlayerManager().getPlayerList()) {
						if (predicate.test(serverPlayerEntity3)) {
							list.add(serverPlayerEntity3);
							if (list.size() >= i) {
								return list;
							}
						}
					}
				}

				return this.getEntities(pos, list);
			}
		}
	}

	private Predicate<Entity> getPositionOnlyPredicate(Vec3d pos) {
		Predicate<Entity> predicate = e -> true;
		if (this.box != null) {
			Box box = this.box.offset(pos);
			predicate = predicate.and(entity -> box.intersects(entity.getBoundingBox()));
		}

		if (!this.distance.isDummy()) {
			predicate = predicate.and(entity -> this.distance.testSqrt(entity.squaredDistanceTo(pos)));
		}

		return predicate;
	}

	private Predicate<Entity> getPositionOnlyPredicate(Vec3d pos, @Nullable Box box, @Nullable FeatureSet enabledFeatures) {
		boolean bl = enabledFeatures != null;
		boolean bl2 = box != null;
		boolean bl3 = !this.distance.isDummy();
		int i = (bl ? 1 : 0) + (bl2 ? 1 : 0) + (bl3 ? 1 : 0);
		List<Predicate<Entity>> list;
		if (i == 0) {
			list = new ArrayList<>();
		} else {
			List<Predicate<Entity>> list2 = new ObjectArrayList<>(3);
			if (bl) {
				list2.add(entity -> entity.getType().isEnabled(enabledFeatures));
			}

			if (bl2) {
				list2.add(entity -> box.intersects(entity.getBoundingBox()));
			}

			if (bl3) {
				list2.add(entity -> this.distance.testSqrt(entity.squaredDistanceTo(pos)));
			}

			list = list2;
		}

		return Util.allOf(list);
	}
}
