package org.samo_lego.fabrictailor.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerJoinServerCallback {
    Event<PlayerJoinServerCallback> EVENT = EventFactory.createArrayBacked(PlayerJoinServerCallback.class, listeners -> (player) -> {
        for (PlayerJoinServerCallback callback : listeners) {
            callback.onPlayerJoin(player);
        }
    });
    void onPlayerJoin(ServerPlayerEntity player);
}