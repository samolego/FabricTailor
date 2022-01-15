package org.samo_lego.fabrictailor.mixin.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket.BRAND;
import static org.samo_lego.fabrictailor.client.ClientTailor.ALLOW_DEFAULT_SKIN;
import static org.samo_lego.fabrictailor.client.ClientTailor.TAILORED_SERVER;
import static org.samo_lego.fabrictailor.client.ClientTailor.forceOpen;
import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_HELLO;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin_FTChecker {

    @Inject(
            method = "handleCustomPayload",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getIdentifier()Lnet/minecraft/resources/ResourceLocation;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onFabricTailorHello(ClientboundCustomPayloadPacket packet, CallbackInfo ci, ResourceLocation channel) {
        if (channel.equals(BRAND)) {
            TAILORED_SERVER = false;
            ALLOW_DEFAULT_SKIN = true;
            forceOpen = false;
        } else if (channel.equals(FABRICTAILOR_HELLO)) {
            // FabricTailor is installed on server
            ALLOW_DEFAULT_SKIN = packet.getData().readBoolean();
            TAILORED_SERVER = true;
        }
    }
}
