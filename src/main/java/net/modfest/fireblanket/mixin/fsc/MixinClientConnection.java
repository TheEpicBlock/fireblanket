package net.modfest.fireblanket.mixin.fsc;

import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.Fireblanket.QueuedPacket;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.LinkedBlockingQueue;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection implements FSCConnection {

	@Shadow
	private Channel channel;

	@Shadow
	private void sendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush) {
		throw new AbstractMethodError();
	}

	@Shadow
	private volatile @Nullable PacketListener packetListener;

	@Shadow
	public abstract void flush();

	private final LinkedBlockingQueue<QueuedPacket> fireblanket$queue = Fireblanket.getNextQueue();
	private final boolean fireblanket$fsc = false;
	private final boolean fireblanket$fscStarted = false;

	/**
	 * With a lot of connections, simply the act of writing packets becomes slow.
	 * Doing this on the server thread reduces TPS for no good reason.
	 *
	 * The client already does networking roughly like this, so the protocol stack is already
	 * designed to expect this behavior.
	 */
//	@Redirect(at=@At(value="INVOKE", target="net/minecraft/network/ClientConnection.sendImmediately(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V"),
//			method="send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V")
//	public void fireblanket$asyncPacketSending(ClientConnection subject, Packet<?> pkt, PacketCallbacks listener, boolean flush) {
//		if (pkt instanceof GameJoinS2CPacket && fireblanket$fsc && !fireblanket$fscStarted) {
//			fireblanket$enableFSCNow();
//		}
//		if (this.packetListener != null && this.packetListener.getState() == NetworkState.PLAY) {
//			fireblanket$queue.add(new QueuedPacket(subject, pkt, listener));
//		} else {
//			sendImmediately(pkt, listener, flush);
//		}
//	}
//
//	@Inject(at=@At("HEAD"), method="setCompressionThreshold", cancellable=true)
//	public void fireblanket$handleCompression(int threshold, boolean check, CallbackInfo ci) {
//		if (fireblanket$fscStarted) {
//			ci.cancel();
//		}
//	}
//
//	@Inject(at=@At("HEAD"), method="setPacketListener")
//	public void fireblanket$handleFSC(PacketListener listener, CallbackInfo ci) {
//		if (listener.getState() == NetworkState.PLAY && fireblanket$fsc && !fireblanket$fscStarted) {
//			fireblanket$enableFSCNow();
//		}
//	}
//
//	private void fireblanket$enableFSCNow() {
//		fireblanket$fscStarted = true;
//		ChannelPipeline pipeline = channel.pipeline();
//		ClientConnection self = (ClientConnection)(Object)this;
//		try {
//			boolean client = self.getSide() == NetworkSide.CLIENTBOUND;
//			ReassignableOutputStream ros = new ReassignableOutputStream();
//			ZstdOutputStream zos = new ZstdOutputStream(ros);
//			zos.setLevel(client ? 6 : 4);
//			zos.setLong(client ? 27 : 22);
//			zos.setCloseFrameOnFlush(false);
//			ZstdEncoder enc = new ZstdEncoder(ros, zos, TimeUnit.MILLISECONDS.toNanos(client ? 0 : 40));
//			ZstdDecoder dec = new ZstdDecoder();
//			pipeline.remove("compress");
//			pipeline.remove("decompress");
//			pipeline.addBefore("prepender", "fireblanket:fsc_enc", enc);
//			pipeline.addBefore("splitter", "fireblanket:fsc_dec", dec);
//		} catch (IOException e) {
//			throw new UncheckedIOException(e);
//		}
//	}
//
//	@Override
//	public void fireblanket$enableFullStreamCompression() {
//		fireblanket$fsc = true;
//	}

}
