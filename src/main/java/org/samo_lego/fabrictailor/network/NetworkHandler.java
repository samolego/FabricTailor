package org.samo_lego.fabrictailor.network;

import com.mojang.authlib.properties.Property;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.network.payload.DefaultSkinPayload;
import org.samo_lego.fabrictailor.network.payload.FabricTailorHelloPayload;
import org.samo_lego.fabrictailor.network.payload.HDSkinPayload;
import org.samo_lego.fabrictailor.network.payload.VanillaSkinPayload;
import org.samo_lego.fabrictailor.util.Logging;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.samo_lego.fabrictailor.FabricTailor.THREADPOOL;
import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.network.SkinPackets.FT_HELLO;
import static org.samo_lego.fabrictailor.util.SkinFetcher.fetchSkinByName;

public class NetworkHandler {

    public static void onInit(ServerGamePacketListenerImpl listener, MinecraftServer _server) {
        var player = listener.getPlayer();

        THREADPOOL.submit(() -> {
            Optional<String> value = ((TailoredPlayer) player).fabrictailor_getSkinValue();
            Optional<String> signature = ((TailoredPlayer) player).fabrictailor_getSkinSignature();

            Property skinData = null;
            if (value.isEmpty() || signature.isEmpty()) {

                if (!config.defaultSkin.applyToAll) {
                    skinData = fetchSkinByName(player.getGameProfile().getName());
                }

                if (skinData == null) {
                    var defValue = config.defaultSkin.value;
                    var defSignature = config.defaultSkin.signature;

                    if (!defValue.isEmpty() && !defSignature.isEmpty()) {
                        skinData = new Property(TailoredPlayer.PROPERTY_TEXTURES, defValue, defSignature);
                    }
                }
                
                // Try to set skin now
                if (skinData != null) {
                    ((TailoredPlayer) player).fabrictailor_setSkin(skinData, false);
                }
                ((TailoredPlayer) player).fabrictailor_resetLastSkinChange();
            }
        });
    }


    public static void changeVanillaSkinPacket(VanillaSkinPayload payload, Context context) {
        NetworkHandler.onSkinChangePacket(context.player(), payload.skinProperty(), () -> { });
    }

    public static void defaultSkinPacket(DefaultSkinPayload payload, Context context) {
        if (context.player().hasPermissions(2)) {

            config.defaultSkin.value = payload.skinProperty().value();
            config.defaultSkin.signature = payload.skinProperty().signature();
            config.save();

            if (config.logging.skinChangeFeedback) {
                context.player().displayClientMessage(TextTranslations.create("command.fabrictailor.config.defaultSkin")
                        .withStyle(ChatFormatting.GREEN), false);
            }
        }
    }

    public static void changeHDSkinPacket(HDSkinPayload payload, Context context) {
        NetworkHandler.onSkinChangePacket(context.player(), payload.skinProperty(), () -> {
                if (config.logging.skinChangeFeedback) {
                    context.player().displayClientMessage(TextTranslations.create("hint.fabrictailor.client_only")
                            .withStyle(ChatFormatting.DARK_PURPLE), false);
                }
            });
    }


    public static void onSkinChangePacket(ServerPlayer player, Property skin, Runnable callback) {
        long lastChange = ((TailoredPlayer) player).fabrictailor_getLastSkinChange();
        long now = System.currentTimeMillis();

        if (now - lastChange > config.skinChangeTimer * 1000 || lastChange == 0) {
            ((TailoredPlayer) player).fabrictailor_setSkin(skin, true);
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

    public static void onConfigured(ServerConfigurationPacketListenerImpl listener, MinecraftServer server) {
        if (ServerConfigurationNetworking.canSend(listener, FT_HELLO)) {
            boolean allowDefaultSkinButton = false;
            try {
                allowDefaultSkinButton = Permissions.check(listener.getOwner(), "fabrictailor.set_default_skin", 2, server).get();
            } catch (InterruptedException | ExecutionException e) {
                Logging.error(e.getMessage());
            }
            var payload = new FabricTailorHelloPayload(allowDefaultSkinButton);
            ServerConfigurationNetworking.send(listener, payload);
        }
    }
}
