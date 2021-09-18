package org.samo_lego.fabrictailor.mixin;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.fabrictailor.util.SkinFetcher.fetchSkinByName;
import static org.samo_lego.fabrictailor.FabricTailor.config;


@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager {
    @Inject(
            method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At(
                    target = "Lnet/minecraft/server/PlayerManager;loadPlayerData(Lnet/minecraft/server/network/ServerPlayerEntity;)Lnet/minecraft/nbt/NbtCompound;",
                    value = "INVOKE_ASSIGN",
                    shift = At.Shift.AFTER
            )
    )
    private void onPlayerConnect(ClientConnection clientConnection, ServerPlayerEntity player, CallbackInfo ci) {
        String value = ((TailoredPlayer) player).getSkinValue();
        String signature = ((TailoredPlayer) player).getSkinSignature();

        Property skinData = null;
        if(value == null || signature == null) {

            if(!config.defaultSkin.applyToAll)
                skinData = fetchSkinByName(player.getGameProfile().getName());

            if(skinData == null) {
                value = config.defaultSkin.value;
                signature = config.defaultSkin.signature;

                if(!value.isEmpty() && !signature.isEmpty())
                    skinData = new Property("textures", value, signature);
            }


        } else {
            skinData = new Property("textures", value, signature);
        }
        // Try to set skin now
        if(skinData != null)
            ((TailoredPlayer) player).setSkin(skinData, false);
            ((TailoredPlayer) player).resetLastSkinChange();
    }
}
