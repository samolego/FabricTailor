package org.samo_lego.fabrictailor.network;

import com.mojang.authlib.properties.Property;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.util.Optional;

import static org.samo_lego.fabrictailor.FabricTailor.THREADPOOL;
import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.util.SkinFetcher.fetchSkinByName;

public class NetworkHandler {

    public static void onInit(ServerGamePacketListenerImpl listener, MinecraftServer _server) {
        var player = listener.getPlayer();
        if (ServerPlayNetworking.canSend(listener, SkinPackets.FT_HELLO)) {
            ServerPlayNetworking.send(player, SkinPackets.FT_HELLO, createHelloPacket(player.hasPermissions(2)));
        }

        THREADPOOL.submit(() -> {
            Optional<String> value = ((TailoredPlayer) player).fabrictailor_getSkinValue();
            Optional<String> signature = ((TailoredPlayer) player).fabrictailor_getSkinSignature();

            Property skinData = null;
            if (value.isEmpty() || signature.isEmpty()) {

                if (!config.defaultSkin.applyToAll)
                    skinData = fetchSkinByName(player.getGameProfile().getName());

                if (skinData == null) {
                    var defValue = config.defaultSkin.value;
                    var defSignature = config.defaultSkin.signature;

                    if (!defValue.isEmpty() && !defSignature.isEmpty()) {
                        skinData = new Property(SkinManager.PROPERTY_TEXTURES, defValue, defSignature);
                    }
                }
            } else {
                skinData = new Property(SkinManager.PROPERTY_TEXTURES, value.get(), signature.get());
            }


            // Try to set skin now
            if (skinData != null) {
                ((TailoredPlayer) player).fabrictailor_setSkin(skinData, false);
            }
            ((TailoredPlayer) player).fabrictailor_resetLastSkinChange();
        });
    }


    public static FriendlyByteBuf createHelloPacket(boolean allowDefaultSkinButton) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(allowDefaultSkinButton);

        return buf;
    }

    public static void changeVanillaSkinPacket(MinecraftServer _server, ServerPlayer player, ServerGamePacketListenerImpl _listener, FriendlyByteBuf buf, PacketSender _sender) {
        NetworkHandler.onSkinChangePacket(player, buf, () -> {
        });
    }

    public static void defaultSkinPacket(MinecraftServer _server, ServerPlayer player, ServerGamePacketListenerImpl _listener, FriendlyByteBuf buf, PacketSender _sender) {
        if (player.hasPermissions(2)) {
            String value = buf.readUtf();
            String signature = buf.readUtf();

            config.defaultSkin.value = value;
            config.defaultSkin.signature = signature;
            config.save();

            player.sendSystemMessage(
                    TextTranslations.create("command.fabrictailor.config.defaultSkin").withStyle(ChatFormatting.GREEN));
        }
    }

    public static void changeHDSkinPacket(MinecraftServer _server, ServerPlayer player, ServerGamePacketListenerImpl _listener, FriendlyByteBuf buf, PacketSender _sender) {
        NetworkHandler.onSkinChangePacket(player, buf, () ->
                player.displayClientMessage(TextTranslations.create("hint.fabrictailor.client_only")
                        .withStyle(ChatFormatting.DARK_PURPLE), false));
    }


    public static void onSkinChangePacket(ServerPlayer player, FriendlyByteBuf buf, Runnable callback) {
        long lastChange = ((TailoredPlayer) player).fabrictailor_getLastSkinChange();
        long now = System.currentTimeMillis();

        if (now - lastChange > config.skinChangeTimer * 1000 || lastChange == 0) {
            ((TailoredPlayer) player).fabrictailor_setSkin(buf.readProperty(), true);
            callback.run();
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
    }
}
