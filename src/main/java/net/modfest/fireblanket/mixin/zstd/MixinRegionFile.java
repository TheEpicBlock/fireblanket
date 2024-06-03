package net.modfest.fireblanket.mixin.zstd;

import net.minecraft.world.storage.ChunkCompressionFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.storage.RegionFile;
import net.modfest.fireblanket.mixinsupport.ChunkCompressionFormatExt;

@Mixin(RegionFile.class)
public class MixinRegionFile {

	@Redirect(at=@At(value="FIELD", target="Lnet/minecraft/world/storage/RegionFile;compressionFormat:Lnet/minecraft/world/storage/ChunkCompressionFormat;"),
			method="<init>(Lnet/minecraft/world/storage/StorageKey;Ljava/nio/file/Path;Ljava/nio/file/Path;Lnet/minecraft/world/storage/ChunkCompressionFormat;Z)V")
	private void fireblanket$useZstd(RegionFile instance, ChunkCompressionFormat value) {
		ChunkCompressionFormat.exists(0); // initialize class
		value = ChunkCompressionFormatExt.ZSTD;
	}
}
