package org.samo_lego.fabrictailor.mixin.client;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = YggdrasilMinecraftSessionService.class, remap = false)
public class MYggDrasilMinecraftSessionService_AllSkinsAcceptor {

    @Inject(method = "getSecurePropertyValue", at = @At("HEAD"), cancellable = true)
    private void ft_enableInsecureValue(Property property, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(property.value());
    }

    /**
     * Disables "requireSecure" boolean in order to allow skins from all
     * domains, specified in config.
     *
     * @param requireSecure whether to require secure connection, ignored
     * @return false
     */
    @ModifyVariable(method = "getTextures", at = @At("HEAD"), argsOnly = true)
    private boolean ft_disableSecureTextures(boolean requireSecure) {
        return false;
    }
}
