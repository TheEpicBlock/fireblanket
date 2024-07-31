package net.modfest.fireblanket.mixin.gamemode_selection;

import net.minecraft.client.Keyboard;
import net.minecraft.client.network.ClientPlayerEntity;
import net.modfest.fireblanket.util.CanSwitchGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @see CanSwitchGameMode#canSwitchGameMode(ClientPlayerEntity)
 */
@Mixin(Keyboard.class)
public class MixinKeyboard {
	/**
	 * Replaces the {@link net.minecraft.entity.Entity#hasPermissionLevel(int)} check for
	 * using {@code f3+n} with the one in {@link CanSwitchGameMode}.
	 */
	@Redirect(method = "processF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasPermissionLevel(I)Z", ordinal = 1))
	private boolean redirectPermissionCheck(ClientPlayerEntity instance, int i) {
		return CanSwitchGameMode.canSwitchGameMode(instance);
	}

	/**
	 * Replaces the {@link net.minecraft.entity.Entity#hasPermissionLevel(int)} check for
	 * using {@code f3+f4} with the one in {@link CanSwitchGameMode}.
	 */
	@Redirect(method = "processF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasPermissionLevel(I)Z", ordinal = 2))
	private boolean redirectPermissionCheck2(ClientPlayerEntity instance, int i) {
		return CanSwitchGameMode.canSwitchGameMode(instance);
	}
}
