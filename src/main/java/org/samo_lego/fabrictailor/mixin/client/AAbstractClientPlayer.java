package org.samo_lego.fabrictailor.mixin.client;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractClientPlayer.class)
public interface AAbstractClientPlayer {
    @Invoker("getPlayerInfo")
    PlayerInfo ft_getPlayerInfo();
}
