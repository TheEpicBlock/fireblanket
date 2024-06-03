package net.modfest.fireblanket.mixin.zstd;

import net.minecraft.world.PlayerSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.datafixers.DataFixer;

import net.minecraft.world.level.storage.LevelStorage;
import net.modfest.fireblanket.mixinsupport.ZestyPlayerSaveHandler;

@Mixin(LevelStorage.Session.class)
public class MixinLevelStorageSession {

	@Redirect(at=@At(value="NEW", target="(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lcom/mojang/datafixers/DataFixer;)Lnet/minecraft/world/PlayerSaveHandler;"),
			method="createSaveHandler")
	public PlayerSaveHandler fireblanket$useZstd(LevelStorage.Session session, DataFixer dataFixer) {
		return new ZestyPlayerSaveHandler(session, dataFixer);
	}
}
