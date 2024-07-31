package net.modfest.fireblanket.mixin.command_block_perms;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.CommandBlockExecutor;
import net.modfest.fireblanket.mixinsupport.CommandBlockExecutorExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandBlockExecutor.class)
public class MixinCommandBlockExecutor implements CommandBlockExecutorExt {
	@Unique
	private boolean isValid = false;

	@Override
	public void fireblanket$setValid(boolean v) {
		isValid = v;
	}

	@WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;areCommandBlocksEnabled()Z"))
	private boolean isEnabled(MinecraftServer instance, Operation<Boolean> original) {
		return isValid && original.call(instance);
	}
}
