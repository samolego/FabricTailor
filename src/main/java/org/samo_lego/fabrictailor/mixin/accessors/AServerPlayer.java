package org.samo_lego.fabrictailor.mixin.accessors;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface AServerPlayer {
    @Accessor
    MinecraftServer getServer();
}
