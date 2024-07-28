package net.modfest.fireblanket.mixin.zstd;

import net.minecraft.world.storage.ChunkCompressionFormat;
import net.minecraft.world.storage.RegionFile;
import net.minecraft.world.storage.StorageKey;
import net.modfest.fireblanket.mixinsupport.ChunkCompressionFormatExt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(RegionFile.class)
public class MixinRegionFile {

	@Mutable
	@Shadow
	@Final
	private ChunkCompressionFormat compressionFormat;

	@Inject(at = @At("TAIL"),
		method = "<init>(Lnet/minecraft/world/storage/StorageKey;Ljava/nio/file/Path;Ljava/nio/file/Path;Lnet/minecraft/world/storage/ChunkCompressionFormat;Z)V")
	private void fireblanket$useZstd(StorageKey storageKey, Path path, Path directory, ChunkCompressionFormat compressionFormat, boolean dsync, CallbackInfo ci) {
		ChunkCompressionFormat.exists(0); // initialize class
		this.compressionFormat = ChunkCompressionFormatExt.ZSTD;
	}
}
