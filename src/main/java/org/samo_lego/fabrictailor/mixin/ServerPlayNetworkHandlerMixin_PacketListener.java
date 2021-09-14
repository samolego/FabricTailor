package org.samo_lego.fabrictailor.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.mixin.accessors.ClientSettingsC2SAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.fabrictailor.FabricTailor.config;

import static org.samo_lego.fabrictailor.client.network.SkinChangePacket.FABRICTAILOR_CHANNEL;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_PacketListener {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onCustomPayload", at = @At("TAIL"))
    private void onSkinChangePacket(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if(packet.getChannel().equals(FABRICTAILOR_CHANNEL)) {
            // This is our skin change packet
            PacketByteBuf buf = packet.getData();
            String value = buf.readString();
            String signature = buf.readString();

            ((TailoredPlayer) this.player).setSkin(value, signature, true);
        }
    }

    @Inject(
            method = "onClientSettings",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;setClientSettings(Lnet/minecraft/network/packet/c2s/play/ClientSettingsC2SPacket;)V"
            )
    )
    private void checkClientSettings(ClientSettingsC2SPacket packet, CallbackInfo ci) {
        if(!config.allowCapes) {
            byte playerModel = (byte) packet.getPlayerModelBitMask();

            // Fake cape rule to be off
            playerModel = (byte) (playerModel & ~(1));

            ((ClientSettingsC2SAccessor) packet).setPlayerModelBitMask(playerModel);
        }
    }
}
