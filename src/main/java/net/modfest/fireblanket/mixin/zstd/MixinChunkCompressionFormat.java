package net.modfest.fireblanket.mixin.zstd;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import net.minecraft.world.storage.ChunkCompressionFormat;
import net.modfest.fireblanket.mixinsupport.ChunkCompressionFormatExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkCompressionFormat.class)
public class MixinChunkCompressionFormat {

	@Shadow
	private static ChunkCompressionFormat add(ChunkCompressionFormat version) {
		throw new AbstractMethodError();
	}

	@Inject(at = @At("TAIL"), method = "<clinit>")
	private static void fireblanket$addZstd(CallbackInfo ci) {
		add(ChunkCompressionFormatExt.ZSTD = new ChunkCompressionFormat(53, // chosen by fair dice roll. guaranteed to be random.
			"ZSTD",
			in -> new FastBufferedInputStream(new ZstdInputStream(in)),
			out -> {
				var z = new ZstdOutputStream(out);
				z.setLevel(7);
				z.setLong(18);
				z.setChecksum(true);
				return new FastBufferedOutputStream(z);
			}));
	}

}