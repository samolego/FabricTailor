package org.samo_lego.fabrictailor.mixin;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.util.SkinFetcher.fetchSkinByName;


@Mixin(PlayerList.class)
public abstract class MixinPlayerManager {
    @Inject(
            method = "placeNewPlayer",
            at = @At(
                    target = "Lnet/minecraft/server/players/PlayerList;load(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/nbt/CompoundTag;",
                    value = "INVOKE_ASSIGN",
                    shift = At.Shift.AFTER
            )
    )
    private void onPlayerConnect(Connection clientConnection, ServerPlayer player, CallbackInfo ci) {
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
