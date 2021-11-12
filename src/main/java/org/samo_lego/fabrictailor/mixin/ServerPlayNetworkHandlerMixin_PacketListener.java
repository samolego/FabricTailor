package org.samo_lego.fabrictailor.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.util.TranslatedText;
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
        long lastChange = ((TailoredPlayer) this.player).getLastSkinChange();
        long now = System.currentTimeMillis();
        if(packet.getChannel().equals(FABRICTAILOR_CHANNEL)) {
            if(now - lastChange > config.skinChangeTimer * 1000 || lastChange == 0) {
                // This is our skin change packet
                PacketByteBuf buf = packet.getData();
                String value = buf.readString();
                String signature = buf.readString();

                ((TailoredPlayer) this.player).setSkin(value, signature, true);
            } else {
                // Prevent skin change spamming
                MutableText timeLeft = new LiteralText(String.valueOf((config.skinChangeTimer * 1000 - now + lastChange) / 1000))
                        .formatted(Formatting.LIGHT_PURPLE);
                player.sendMessage(
                        new TranslatedText("command.fabrictailor.skin.timer.please_wait", timeLeft)
                        .formatted(Formatting.RED),
                        false
                );
            }
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
            byte playerModel = (byte) packet.playerModelBitMask();

            // Fake cape rule to be off
            playerModel = (byte) (playerModel & ~(1));

            //fixme mixins with records?
            //((ClientSettingsC2SAccessor) packet).setPlayerModelBitMask(playerModel);
        }
    }
}
