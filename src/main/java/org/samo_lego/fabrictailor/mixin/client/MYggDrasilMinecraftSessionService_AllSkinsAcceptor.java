package org.samo_lego.fabrictailor.mixin.client;

import com.google.common.net.InternetDomainName;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.net.URI;

import static org.samo_lego.fabrictailor.FabricTailor.config;

@Mixin(value = YggdrasilMinecraftSessionService.class, remap = false)
public class MYggDrasilMinecraftSessionService_AllSkinsAcceptor {

    @Inject(method = "getSecurePropertyValue", at = @At("HEAD"), cancellable = true)
    private void ft_enableInsecureValue(Property property, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(property.getValue());
    }

    @Inject(method = "isAllowedTextureDomain",
            at = @At(value = "INVOKE_ASSIGN",
                    target = "Ljava/net/URI;getHost()Ljava/lang/String;"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private static void ft_allowAllTextureDomains(String url, CallbackInfoReturnable<Boolean> cir, URI uri, String domain) {
        if (url.startsWith("file://")) {
            cir.setReturnValue(false);  // todo, allow files
            return;
        }

        String topDomain = InternetDomainName.from(domain)
                .topDomainUnderRegistrySuffix()
                .toString()
                .toLowerCase();
        cir.setReturnValue(config.allowedTextureDomains.contains(topDomain));
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
