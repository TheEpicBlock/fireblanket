package net.modfest.fireblanket.mixin.command_block_perms;

import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.modfest.fireblanket.mixinsupport.CommandBlockExecutorExt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Implements validation for regular command-blocks.
 * The executors for commandblock minecarts are currently not supported
 */
@Mixin(targets = "net/minecraft/block/entity/CommandBlockBlockEntity$1")
public abstract class MixinCommandBlockBEExecutor {
	@Shadow
	@Final
	CommandBlockBlockEntity field_11921;

	@Inject(method = "setCommand", at = @At("HEAD"))
	private void onSetCommand(String command, CallbackInfo ci) {
		var be = this.field_11921;
		var isValid = be.getWorld() != null && be.getWorld().isSkyVisible(be.getPos().up());

		((CommandBlockExecutorExt)this).fireblanket$setValid(isValid);
	}
}
