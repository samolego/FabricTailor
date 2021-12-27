package org.samo_lego.fabrictailor.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.util.TranslatedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket.BRAND;
import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.client.network.SkinChangePacket.FABRICTAILOR_CHANNEL;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin_PacketListener {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleCustomPayload", at = @At("TAIL"))
    private void onSkinChangePacket(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        long lastChange = ((TailoredPlayer) this.player).getLastSkinChange();
        long now = System.currentTimeMillis();
        if(packet.getIdentifier().equals(FABRICTAILOR_SKIN_CHANGE)) {
            if(now - lastChange > config.skinChangeTimer * 1000 || lastChange == 0) {
                // This is our skin change packet
                FriendlyByteBuf buf = packet.getData();
                String value = buf.readUtf();
                String signature = buf.readUtf();

                ((TailoredPlayer) this.player).setSkin(value, signature, true);
            } else {
                // Prevent skin change spamming
                MutableComponent timeLeft = new TextComponent(String.valueOf((config.skinChangeTimer * 1000 - now + lastChange) / 1000))
                        .withStyle(ChatFormatting.LIGHT_PURPLE);
                player.displayClientMessage(
                        new TranslatedText("command.fabrictailor.skin.timer.please_wait", timeLeft)
                        .withStyle(ChatFormatting.RED),
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
    }
}
