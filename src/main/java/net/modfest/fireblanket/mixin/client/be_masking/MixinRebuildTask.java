package net.modfest.fireblanket.mixin.client.be_masking;

import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkBuilder.BuiltChunk.RebuildTask.class)
public class MixinRebuildTask {
	// todo
//	@Inject(method = "addBlockEntity", at = @At("TAIL"))
//	private <E extends BlockEntity> void fireblanket$addBEAnyway(ChunkBuilder.BuiltChunk.RebuildTask.RenderData renderData, E blockEntity, CallbackInfo ci) {
//		if (!renderData.blockEntities.contains(blockEntity) && ClientState.MASKED_BERS.contains(blockEntity.getType())) {
//			renderData.blockEntities.add(blockEntity);
//		}
//	}
}
