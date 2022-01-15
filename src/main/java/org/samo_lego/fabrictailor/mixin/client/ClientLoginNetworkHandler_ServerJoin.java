package org.samo_lego.fabrictailor.mixin.client;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.fabrictailor.client.ClientTailor.ALLOW_DEFAULT_SKIN;
import static org.samo_lego.fabrictailor.client.ClientTailor.TAILORED_SERVER;
import static org.samo_lego.fabrictailor.client.ClientTailor.forceOpen;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class ClientLoginNetworkHandler_ServerJoin {

    @Inject(method = "handleHello", at = @At("RETURN"))
    private void onServerJoin(ClientboundHelloPacket clientboundHelloPacket, CallbackInfo ci) {
        // Reset server having fabrictailor installed status
        TAILORED_SERVER = false;
        ALLOW_DEFAULT_SKIN = true;
        forceOpen = false;
    }
}
