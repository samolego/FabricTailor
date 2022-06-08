package org.samo_lego.fabrictailor.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.util.TextTranslations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket.BRAND;
import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_DEFAULT_SKIN;
import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_SKIN_CHANGE;
import static org.samo_lego.fabrictailor.network.SkinPackets.createHelloPacket;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin_PacketListener {
    @Shadow public ServerPlayer player;

    @Shadow public abstract void send(Packet<?> packet);

    @Inject(method = "handleCustomPayload", at = @At("TAIL"))
    private void onSkinChangePacket(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        long lastChange = ((TailoredPlayer) this.player).getLastSkinChange();
        long now = System.currentTimeMillis();
        ResourceLocation channel = packet.getIdentifier();
        if(channel.equals(FABRICTAILOR_SKIN_CHANGE)) {
            if(now - lastChange > config.skinChangeTimer * 1000 || lastChange == 0) {
                // This is our skin change packet
                FriendlyByteBuf buf = packet.getData();
                String value = buf.readUtf();
                String signature = buf.readUtf();

                ((TailoredPlayer) this.player).setSkin(value, signature, true);
            } else {
                // Prevent skin change spamming
                MutableComponent timeLeft = Component.literal(String.valueOf((config.skinChangeTimer * 1000 - now + lastChange) / 1000))
                        .withStyle(ChatFormatting.LIGHT_PURPLE);
                player.displayClientMessage(
                        TextTranslations.create("command.fabrictailor.skin.timer.please_wait", timeLeft)
                        .withStyle(ChatFormatting.RED),
                        false
                );
            }
        } else if (channel.equals(FABRICTAILOR_DEFAULT_SKIN)) {
            if(this.player.hasPermissions(2)) {
                FriendlyByteBuf buf = packet.getData();
                String value = buf.readUtf();
                String signature = buf.readUtf();

                config.defaultSkin.value = value;
                config.defaultSkin.signature = signature;
                config.save();


                player.sendSystemMessage(
                        TextTranslations.create("command.fabrictailor.config.defaultSkin").withStyle(ChatFormatting.GREEN));
            }
        } else if (channel.equals(BRAND)) {
            // Brand packet - let's send info that server is using FabricTailor
            ClientboundCustomPayloadPacket helloPacket = createHelloPacket(this.player.hasPermissions(2));
            this.send(helloPacket);
        }
    }
}
