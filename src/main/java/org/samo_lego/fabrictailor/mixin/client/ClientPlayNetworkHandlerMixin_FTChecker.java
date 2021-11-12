package org.samo_lego.fabrictailor.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.fabrictailor.client.ClientTailor.TAILORED_SERVER;
import static org.samo_lego.fabrictailor.client.ClientTailor.ALLOW_DEFAULT_SKIN;
import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_HELLO;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin_FTChecker {

    @Inject(
            method = "onCustomPayload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;getData()Lnet/minecraft/network/PacketByteBuf;"
            )
    )
    private void onFabricTailorHello(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        // FabricTailor is installed on server
        Identifier channel = packet.getChannel();
        if (channel.equals(FABRICTAILOR_HELLO)) {
            ALLOW_DEFAULT_SKIN = packet.getData().readBoolean();
            TAILORED_SERVER = true;
        }
    }
}
