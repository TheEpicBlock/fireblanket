package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerChunkManager.class)
public interface ServerChunkManagerAccessor {

	@Accessor("ticketManager")
	ChunkTicketManager fireblanket$getTicketManager();

}
