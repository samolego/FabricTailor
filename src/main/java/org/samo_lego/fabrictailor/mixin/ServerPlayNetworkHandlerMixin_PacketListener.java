package org.samo_lego.fabrictailor.mixin;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.mixin.accessors.ClientSettingsC2SAccessor;
import org.samo_lego.fabrictailor.util.TranslatedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket.BRAND;
import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.network.SkinPackets.*;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_PacketListener {
    @Shadow public ServerPlayerEntity player;

    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Inject(method = "onCustomPayload", at = @At("TAIL"))
    private void onSkinChangePacket(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        Identifier packetChannel = packet.getChannel();
        if(packetChannel.equals(FABRICTAILOR_SKIN_CHANGE)) {
            long lastChange = ((TailoredPlayer) this.player).getLastSkinChange();
            long now = System.currentTimeMillis();
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
        } else if (packetChannel.equals(FABRICTAILOR_DEFAULT_SKIN)) {
            System.out.println("DEFAULT SKIN PACKET");
            if(this.player.hasPermissionLevel(2)) {
                PacketByteBuf buf = packet.getData();
                String value = buf.readString();
                String signature = buf.readString();

                config.defaultSkin.value = value;
                config.defaultSkin.signature = signature;
                config.save();


                player.sendMessage(
                        new TranslatedText("command.fabrictailor.config.defaultSkin").formatted(Formatting.GREEN),
                        false
                );
            }
        } else if (packetChannel.equals(BRAND)) {
            // Brand packet - let's send info that server is using FabricTailor
            CustomPayloadS2CPacket helloPacket = createHelloPacket(this.player.hasPermissionLevel(2));
            this.sendPacket(helloPacket);
        }

        System.out.println(packetChannel);
    }

    /**
     * Toggles capes server-side.
     * @param packet client settings packet
     * @param ci - mixin stuff.
     */
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
